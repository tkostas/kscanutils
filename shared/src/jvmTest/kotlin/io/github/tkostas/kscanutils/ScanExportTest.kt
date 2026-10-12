package io.github.tkostas.kscanutils

import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.Loader
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScanExportTest {
    @Test
    fun rotatedSinglePageExportsAsPng() {
        runBlocking {
            val output = Files.createTempFile("scan-export-", ".png")
            try {
                val result = exportScan(listOf(testPage().rotateClockwise()), ScanFormat.PNG, output.toString())
                assertTrue(result.isSuccess)
                ImageIO.read(output.toFile()).also {
                    assertEquals(3, it.width)
                    assertEquals(2, it.height)
                }
            } finally {
                Files.deleteIfExists(output)
            }
        }
    }

    @Test
    fun multiplePagesExportAsReadablePdf() {
        runBlocking {
            val output = Files.createTempFile("scan-export-", ".pdf")
            try {
                assertTrue(exportScan(listOf(testPage(), testPage()), ScanFormat.PDF, output.toString()).isSuccess)
                Loader.loadPDF(output.toFile()).use { assertEquals(2, it.numberOfPages) }
            } finally {
                Files.deleteIfExists(output)
            }
        }
    }

    @Test
    fun multiplePagesCannotExportAsImage() {
        runBlocking {
            val output = Files.createTempFile("scan-export-", ".png")
            try {
                assertTrue(exportScan(listOf(testPage(), testPage()), ScanFormat.PNG, output.toString()).isFailure)
            } finally {
                Files.deleteIfExists(output)
            }
        }
    }

    private fun testPage(): ScannedPage {
        val image = BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().also { graphics ->
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, image.width, image.height)
            graphics.dispose()
        }
        return ScannedPage(ByteArrayOutputStream().also { ImageIO.write(image, "png", it) }.toByteArray())
    }
}
