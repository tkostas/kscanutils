package io.github.tkostas.kscanutils

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.awt.Graphics2D
import java.awt.FileDialog
import java.awt.Frame
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual fun decodeScanPreview(bytes: ByteArray): ImageBitmap? =
    runCatching { ImageIO.read(ByteArrayInputStream(bytes))?.toComposeImageBitmap() }.getOrNull()

actual suspend fun discoverScanners(): Result<List<ScannerDevice>> = withContext(Dispatchers.IO) { runCatching {
    when {
        isWindows() -> runPowerShell(
            "\$m=New-Object -ComObject WIA.DeviceManager; " +
                "\$m.DeviceInfos|?{\$_.Type -eq 1}|%{\$n=(\$_.Properties|?{\$_.PropertyID -eq 7}).Value; Write-Output (\$_.DeviceID+[char]9+\$n)}",
        ).lineSequence().filter { it.contains('\t') }.map {
            val (id, name) = it.split('\t', limit = 2); ScannerDevice(id, name)
        }.toList()
        isLinux() -> runCommand("scanimage", "-L").lineSequence().mapNotNull { line ->
            Regex("device `([^']+)' is a (.+)").find(line)?.destructured?.let { (id, name) -> ScannerDevice(id, name) }
        }.toList()
        else -> throw UnsupportedOperationException("Scanning is not yet available on this operating system.")
    }
} }

actual suspend fun acquireScan(scanner: ScannerDevice, dpi: Int): Result<ByteArray> = withContext(Dispatchers.IO) { runCatching {
    val temporary = Files.createTempFile("kscanutils-scan-", ".png")
    try {
        // WIA ImageFile.SaveFile only accepts a path that does not already exist.
        // The temporary file is created first to obtain a collision-safe name, then
        // removed so the scanner backend can create the output itself.
        Files.deleteIfExists(temporary)
        if (isWindows()) {
            val safeId = scanner.id.replace("'", "''")
            val safePath = temporary.toAbsolutePath().toString().replace("'", "''")
            runPowerShell(
                "\$m=New-Object -ComObject WIA.DeviceManager; \$d=(\$m.DeviceInfos|?{\$_.DeviceID -eq '$safeId'}).Connect(); " +
                    "\$i=\$d.Items.Item(1); foreach(\$p in \$i.Properties){if(\$p.PropertyID -eq 6147 -or \$p.PropertyID -eq 6148){\$p.Value=$dpi}}; " +
                    "\$x=\$i.Transfer('{B96B3CAF-0728-11D3-9D7B-0000F81EF32E}'); \$x.SaveFile('$safePath')",
            )
        } else if (isLinux()) {
            runBinaryCommand(temporary, "scanimage", "--device-name=${scanner.id}", "--resolution=$dpi", "--format=png")
        } else throw UnsupportedOperationException("Scanning is not yet available on this operating system.")
        Files.readAllBytes(temporary).also { require(it.isNotEmpty()) { "The scanner returned an empty image." } }
    } finally {
        Files.deleteIfExists(temporary)
    }
} }

actual fun chooseScanOutput(format: ScanFormat): String? {
    val dialog = FileDialog(null as Frame?, "Save scanned document", FileDialog.SAVE).apply {
        file = "scan.${format.extension}"
        isMultipleMode = false
        setFilenameFilter { _, name -> name.endsWith(".${format.extension}", ignoreCase = true) }
    }
    dialog.isVisible = true
    val selectedFile = dialog.file ?: return null
    val selectedDirectory = dialog.directory ?: return null
    val selectedPath = Path.of(selectedDirectory, selectedFile)
    return if (selectedPath.fileName.toString().endsWith(".${format.extension}", ignoreCase = true)) {
        selectedPath.toAbsolutePath().normalize().toString()
    } else {
        selectedPath.resolveSibling("${selectedPath.fileName}.${format.extension}").toAbsolutePath().normalize().toString()
    }
}

actual suspend fun exportScan(
    pages: List<ScannedPage>,
    format: ScanFormat,
    outputPath: String,
    imageQuality: ImageQuality,
): Result<String> = runCatching {
    require(pages.isNotEmpty()) { "Scan at least one page before saving." }
    require(format == ScanFormat.PDF || pages.size == 1) { "Multi-page scans can only be saved as PDF." }
    val destination = Path.of(outputPath).toAbsolutePath()
    destination.parent?.let(Files::createDirectories)
    val temporary = Files.createTempFile(destination.parent, ".kscanutils-", ".tmp")
    try {
        if (format == ScanFormat.PDF) writePdf(pages, temporary)
        else if (format == ScanFormat.JPEG) {
            writeJpeg(rotatedImage(pages.single()), temporary, imageQuality.compressionQuality)
        } else {
            val image = rotatedImage(pages.single())
            check(ImageIO.write(image, format.extension, temporary.toFile())) { "No image writer is available for ${format.label}." }
        }
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
    } catch (_: java.nio.file.AtomicMoveNotSupportedException) {
        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING)
    } finally {
        Files.deleteIfExists(temporary)
    }
    destination.toString()
}

private fun writeJpeg(source: BufferedImage, output: Path, quality: Float) {
    val image = if (source.type == BufferedImage.TYPE_INT_RGB) source else {
        BufferedImage(source.width, source.height, BufferedImage.TYPE_INT_RGB).also { converted ->
            converted.createGraphics().also { graphics ->
                graphics.color = java.awt.Color.WHITE
                graphics.fillRect(0, 0, converted.width, converted.height)
                graphics.drawImage(source, 0, 0, null)
                graphics.dispose()
            }
        }
    }
    val writer = ImageIO.getImageWritersByFormatName("jpeg").asSequence().firstOrNull()
        ?: error("No JPEG image writer is available.")
    ImageIO.createImageOutputStream(output.toFile()).use { stream ->
        writer.output = stream
        val parameters = writer.defaultWriteParam.apply {
            compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality
        }
        writer.write(null, javax.imageio.IIOImage(image, null, null), parameters)
    }
    writer.dispose()
}

private fun writePdf(pages: List<ScannedPage>, output: Path) {
    PDDocument().use { document ->
        pages.forEach { scanned ->
            val image = rotatedImage(scanned)
            val encoded = ByteArrayOutputStream().also { ImageIO.write(image, "jpg", it) }.toByteArray()
            val width = image.width * 72f / scanned.dpi
            val height = image.height * 72f / scanned.dpi
            val page = PDPage(PDRectangle(width, height))
            document.addPage(page)
            val pdfImage = PDImageXObject.createFromByteArray(document, encoded, "scan")
            PDPageContentStream(document, page).use { it.drawImage(pdfImage, 0f, 0f, width, height) }
        }
        document.save(output.toFile())
    }
    Loader.loadPDF(output.toFile()).use { check(it.numberOfPages == pages.size) }
}

private fun rotatedImage(page: ScannedPage): BufferedImage {
    val source = ImageIO.read(ByteArrayInputStream(page.imageBytes)) ?: error("The scanner returned an unsupported image.")
    if (page.rotationDegrees == 0) return source
    val swap = page.rotationDegrees == 90 || page.rotationDegrees == 270
    val result = BufferedImage(if (swap) source.height else source.width, if (swap) source.width else source.height, BufferedImage.TYPE_INT_RGB)
    val graphics: Graphics2D = result.createGraphics()
    graphics.translate(result.width / 2.0, result.height / 2.0)
    graphics.rotate(Math.toRadians(page.rotationDegrees.toDouble()))
    graphics.drawImage(source, -source.width / 2, -source.height / 2, null)
    graphics.dispose()
    return result
}

private fun isWindows() = System.getProperty("os.name").startsWith("Windows", ignoreCase = true)
private fun isLinux() = System.getProperty("os.name").startsWith("Linux", ignoreCase = true)
private fun runPowerShell(script: String) = runCommand("powershell.exe", "-NoProfile", "-NonInteractive", "-Command", script)
private fun runCommand(vararg command: String): String {
    val process = ProcessBuilder(*command).redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader().readText()
    check(process.waitFor() == 0) { output.ifBlank { "Scanner command failed." } }
    return output
}
private fun runBinaryCommand(output: Path, vararg command: String) {
    val process = ProcessBuilder(*command).redirectOutput(output.toFile()).redirectError(ProcessBuilder.Redirect.PIPE).start()
    val error = process.errorStream.bufferedReader().readText()
    check(process.waitFor() == 0) { error.ifBlank { "Scanner command failed." } }
}
