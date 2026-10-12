package io.github.tkostas.kscanutils

import java.nio.file.Files
import kotlin.io.path.writeBytes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PdfValidationTest {

    @Test
    fun acceptsAReadablePdfWithHeaderAndEofMarker() {
        val file = Files.createTempFile("kscan-valid-", ".pdf")
        try {
            file.writeBytes("%PDF-1.7\n1 0 obj\n<<>>\nendobj\n%%EOF".toByteArray())
            assertTrue(validatePdf(file.toString()).accepted)
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun rejectsNonPdfAndIncompletePdf() {
        val text = Files.createTempFile("kscan-invalid-", ".txt")
        val incomplete = Files.createTempFile("kscan-incomplete-", ".pdf")
        try {
            text.writeBytes("not a pdf".toByteArray())
            incomplete.writeBytes("%PDF-1.7\nmissing eof".toByteArray())
            assertFalse(validatePdf(text.toString()).accepted)
            assertFalse(validatePdf(incomplete.toString()).accepted)
        } finally {
            Files.deleteIfExists(text)
            Files.deleteIfExists(incomplete)
        }
    }

    @Test
    fun rejectsEncryptedPdf() {
        val file = Files.createTempFile("kscan-encrypted-", ".pdf")
        try {
            file.writeBytes("%PDF-1.7\n<< /Encrypt 3 0 R >>\n%%EOF".toByteArray())
            assertFalse(validatePdf(file.toString()).accepted)
        } finally {
            Files.deleteIfExists(file)
        }
    }
}
