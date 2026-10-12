package io.github.tkostas.kscanutils

import org.apache.pdfbox.Loader
import org.apache.pdfbox.io.IOUtils
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

actual fun choosePdfOutput(): String? {
    val dialog = FileDialog(null as Frame?, "Save merged PDF", FileDialog.SAVE).apply {
        file = "merged.pdf"
        isMultipleMode = false
    }
    dialog.isVisible = true
    val selectedFile = dialog.file ?: return null
    val selectedDirectory = dialog.directory ?: return null
    val selectedPath = Paths.get(selectedDirectory, selectedFile)
    return if (selectedPath.fileName.toString().endsWith(".pdf", ignoreCase = true)) {
        selectedPath.toAbsolutePath().normalize().toString()
    } else {
        selectedPath.resolveSibling("${selectedPath.fileName}.pdf").toAbsolutePath().normalize().toString()
    }
}

actual fun choosePdfInputs(): List<String> {
    val dialog = FileDialog(null as Frame?, "Add PDF files", FileDialog.LOAD).apply {
        isMultipleMode = true
        setFilenameFilter { _, name -> name.endsWith(".pdf", ignoreCase = true) }
    }
    dialog.isVisible = true
    return dialog.files.map { it.absolutePath }
}

actual fun outputFileExists(path: String): Boolean = Files.exists(Paths.get(path))

actual fun mergePdfs(inputPaths: List<String>, outputPath: String): PdfMergeResult {
    if (inputPaths.size < 2) return failed("Add at least two PDF files before merging.")

    val inputs = inputPaths.map { Paths.get(it).toAbsolutePath().normalize() }
    val destination = Paths.get(outputPath).toAbsolutePath().normalize()
    if (destination in inputs) return failed("The output file must be different from every source file.")

    val parent = destination.parent ?: return failed("The output location is invalid.")
    var temporary: Path? = null

    return try {
        Files.createDirectories(parent)
        inputs.forEach { input ->
            if (!Files.isRegularFile(input) || !Files.isReadable(input)) {
                error("${input.fileName} is no longer available or readable.")
            }
        }

        temporary = Files.createTempFile(parent, ".kscan-merge-", ".pdf.tmp")
        val merger = PDFMergerUtility().apply {
            inputs.forEach { addSource(it.toFile()) }
            destinationFileName = temporary.toString()
        }
        merger.mergeDocuments(IOUtils.createTempFileOnlyStreamCache())

        Loader.loadPDF(temporary.toFile()).use { merged ->
            if (merged.numberOfPages <= 0) error("The merged PDF contains no pages.")
        }

        try {
            Files.move(
                temporary,
                destination,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING,
            )
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING)
        }
        temporary = null
        PdfMergeResult(successful = true, outputPath = destination.toString())
    } catch (error: Exception) {
        failed(error.message?.takeIf { it.isNotBlank() } ?: "The PDF merge failed.")
    } finally {
        temporary?.let { runCatching { Files.deleteIfExists(it) } }
    }
}

private fun failed(message: String) = PdfMergeResult(successful = false, errorMessage = message)
