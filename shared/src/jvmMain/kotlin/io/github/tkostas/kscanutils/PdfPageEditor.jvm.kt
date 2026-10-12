package io.github.tkostas.kscanutils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.FileDialog
import java.awt.Frame
import java.io.ByteArrayOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

actual fun choosePdfToEdit(): String? {
    val dialog = FileDialog(null as Frame?, "Open PDF to edit", FileDialog.LOAD).apply {
        isMultipleMode = false
        setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
    }
    dialog.isVisible = true
    return dialog.files.singleOrNull()?.toPath()?.toAbsolutePath()?.normalize()?.toString()
}

actual fun chooseEditedPdfOutput(sourcePath: String): String? {
    val source = Path.of(sourcePath)
    val baseName = source.fileName.toString().removeSuffix(".pdf").removeSuffix(".PDF")
    val dialog = FileDialog(null as Frame?, "Save edited PDF", FileDialog.SAVE).apply {
        directory = source.parent?.toString()
        file = "$baseName-edited.pdf"
        isMultipleMode = false
        setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
    }
    dialog.isVisible = true
    val selectedFile = dialog.file ?: return null
    val selectedDirectory = dialog.directory ?: return null
    val selectedPath = Path.of(selectedDirectory, selectedFile)
    return if (selectedPath.fileName.toString().endsWith(".pdf", ignoreCase = true)) {
        selectedPath.toAbsolutePath().normalize().toString()
    } else {
        selectedPath.resolveSibling("${selectedPath.fileName}.pdf").toAbsolutePath().normalize().toString()
    }
}

actual suspend fun loadPdfForEditing(path: String): Result<EditablePdfDocument> = withContext(Dispatchers.IO) {
    runCatching {
        val source = Path.of(path).toAbsolutePath().normalize()
        require(Files.isRegularFile(source) && Files.isReadable(source)) { "The PDF is not available or readable." }
        Loader.loadPDF(source.toFile()).use { document ->
            require(document.numberOfPages > 0) { "The PDF contains no pages." }
            val renderer = PDFRenderer(document)
            val pages = (0 until document.numberOfPages).map { index ->
                val image = renderer.renderImageWithDPI(index, 96f, ImageType.RGB)
                val bytes = ByteArrayOutputStream().use { output ->
                    check(ImageIO.write(image, "png", output)) { "A page preview could not be created." }
                    output.toByteArray()
                }
                EditablePdfPage(originalIndex = index, previewBytes = bytes)
            }
            EditablePdfDocument(source.toString(), source.fileName.toString(), pages)
        }
    }
}

actual suspend fun exportEditedPdf(
    sourcePath: String,
    pages: List<EditablePdfPage>,
    outputPath: String,
): PdfEditResult = withContext(Dispatchers.IO) {
    if (pages.isEmpty()) return@withContext failedEdit("The edited PDF must contain at least one page.")
    val source = Path.of(sourcePath).toAbsolutePath().normalize()
    val destination = Path.of(outputPath).toAbsolutePath().normalize()
    val parent = destination.parent ?: return@withContext failedEdit("The output location is invalid.")
    var temporary: Path? = null

    try {
        require(Files.isRegularFile(source) && Files.isReadable(source)) { "The source PDF is no longer available or readable." }
        Files.createDirectories(parent)
        temporary = Files.createTempFile(parent, ".kscan-edit-", ".pdf.tmp")

        Loader.loadPDF(source.toFile()).use { input ->
            org.apache.pdfbox.pdmodel.PDDocument().use { output ->
                output.documentInformation = input.documentInformation
                output.version = input.version
                pages.forEach { edit ->
                    require(edit.originalIndex in 0 until input.numberOfPages) { "The source PDF changed while it was being edited." }
                    val imported = output.importPage(input.getPage(edit.originalIndex))
                    imported.rotation = normalizeRotation(imported.rotation + edit.rotationDegrees)
                }
                output.save(temporary.toFile())
            }
        }

        Loader.loadPDF(temporary.toFile()).use { written ->
            check(written.numberOfPages == pages.size) { "The saved PDF did not contain every retained page." }
        }
        try {
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING)
        }
        temporary = null
        PdfEditResult(successful = true, outputPath = destination.toString())
    } catch (error: Exception) {
        failedEdit(error.message?.takeIf { it.isNotBlank() } ?: "The edited PDF could not be saved.")
    } finally {
        temporary?.let { runCatching { Files.deleteIfExists(it) } }
    }
}

private fun normalizeRotation(rotation: Int): Int = ((rotation % 360) + 360) % 360
private fun failedEdit(message: String) = PdfEditResult(successful = false, errorMessage = message)
