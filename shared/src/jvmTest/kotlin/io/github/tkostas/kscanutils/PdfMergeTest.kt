package io.github.tkostas.kscanutils

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.nio.file.Files
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PdfMergeTest {

    @Test
    fun mergesEveryPageInInputOrderWithoutChangingSources() {
        val directory = Files.createTempDirectory("kscan-merge-test-")
        val first = directory.resolve("first.pdf")
        val second = directory.resolve("second.pdf")
        val output = directory.resolve("merged.pdf")
        try {
            createPdf(first.toString(), 200f, 300f)
            createPdf(second.toString(), 400f, 500f)
            val firstHash = hash(first.toString())
            val secondHash = hash(second.toString())

            val result = mergePdfs(listOf(first.toString(), second.toString()), output.toString())

            assertTrue(result.successful, result.errorMessage)
            Loader.loadPDF(output.toFile()).use { merged ->
                assertEquals(2, merged.numberOfPages)
                assertEquals(200f, merged.getPage(0).mediaBox.width)
                assertEquals(400f, merged.getPage(1).mediaBox.width)
            }
            assertEquals(firstHash, hash(first.toString()))
            assertEquals(secondHash, hash(second.toString()))
        } finally {
            listOf(output, first, second).forEach { Files.deleteIfExists(it) }
            Files.deleteIfExists(directory)
        }
    }

    @Test
    fun refusesToOverwriteASourceFile() {
        val directory = Files.createTempDirectory("kscan-source-test-")
        val first = directory.resolve("first.pdf")
        val second = directory.resolve("second.pdf")
        try {
            createPdf(first.toString(), 200f, 300f)
            createPdf(second.toString(), 400f, 500f)

            val result = mergePdfs(listOf(first.toString(), second.toString()), first.toString())

            assertFalse(result.successful)
        } finally {
            listOf(first, second).forEach { Files.deleteIfExists(it) }
            Files.deleteIfExists(directory)
        }
    }

    private fun createPdf(path: String, width: Float, height: Float) {
        PDDocument().use { document ->
            document.addPage(PDPage(PDRectangle(width, height)))
            document.save(path)
        }
    }

    private fun hash(path: String): String =
        MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(java.nio.file.Paths.get(path)))
            .joinToString("") { "%02x".format(it) }
}
