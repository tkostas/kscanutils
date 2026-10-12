package io.github.tkostas.kscanutils

import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PdfPageEditorTest {
    @Test
    fun loadsEveryPageAndCreatesPreviews() = runBlocking {
        val directory = Files.createTempDirectory("kscan-edit-load-test-")
        val source = directory.resolve("source.pdf")
        try {
            createPdf(source, 200f, 300f, 400f)

            val loaded = loadPdfForEditing(source.toString()).getOrThrow()

            assertEquals(source.toAbsolutePath().normalize().toString(), loaded.sourcePath)
            assertEquals(listOf(0, 1, 2), loaded.pages.map { it.originalIndex })
            assertTrue(loaded.pages.all { it.previewBytes.isNotEmpty() })
        } finally {
            Files.deleteIfExists(source)
            Files.deleteIfExists(directory)
        }
    }

    @Test
    fun exportsRetainedPagesWithRequestedRotationsWithoutChangingSource() = runBlocking {
        val directory = Files.createTempDirectory("kscan-edit-test-")
        val source = directory.resolve("source.pdf")
        val output = directory.resolve("edited.pdf")
        try {
            createPdf(source, 200f, 300f, 400f)
            val sourceHash = hash(source)
            val pages = listOf(
                EditablePdfPage(0, byteArrayOf(), rotationDegrees = 90),
                EditablePdfPage(2, byteArrayOf(), rotationDegrees = 270),
            )

            val result = exportEditedPdf(source.toString(), pages, output.toString())

            assertTrue(result.successful, result.errorMessage)
            Loader.loadPDF(output.toFile()).use { edited ->
                assertEquals(2, edited.numberOfPages)
                assertEquals(200f, edited.getPage(0).mediaBox.width)
                assertEquals(400f, edited.getPage(1).mediaBox.width)
                assertEquals(90, edited.getPage(0).rotation)
                assertEquals(270, edited.getPage(1).rotation)
            }
            assertEquals(sourceHash, hash(source))
        } finally {
            Files.deleteIfExists(output)
            Files.deleteIfExists(source)
            Files.deleteIfExists(directory)
        }
    }

    @Test
    fun safelyReplacesSourceAfterEditing() = runBlocking {
        val directory = Files.createTempDirectory("kscan-edit-replace-test-")
        val source = directory.resolve("source.pdf")
        try {
            createPdf(source, 200f, 300f)

            val result = exportEditedPdf(
                source.toString(),
                listOf(EditablePdfPage(1, byteArrayOf(), rotationDegrees = 90)),
                source.toString(),
            )

            assertTrue(result.successful, result.errorMessage)
            Loader.loadPDF(source.toFile()).use { edited ->
                assertEquals(1, edited.numberOfPages)
                assertEquals(300f, edited.getPage(0).mediaBox.width)
                assertEquals(90, edited.getPage(0).rotation)
            }
        } finally {
            Files.deleteIfExists(source)
            Files.deleteIfExists(directory)
        }
    }

    private fun createPdf(path: Path, vararg widths: Float) {
        PDDocument().use { document ->
            widths.forEach { width -> document.addPage(PDPage(PDRectangle(width, 500f))) }
            document.save(path.toFile())
        }
    }

    private fun hash(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }
}
