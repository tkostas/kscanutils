package io.github.tkostas.kscanutils

import kotlin.test.Test
import kotlin.test.assertEquals

class ScanModelsTest {
    @Test
    fun onePageCanBeSavedAsImageOrPdf() {
        assertEquals(ScanFormat.entries, availableScanFormats(1))
    }

    @Test
    fun multiplePagesCanOnlyBeSavedAsPdf() {
        assertEquals(listOf(ScanFormat.PDF), availableScanFormats(2))
    }

    @Test
    fun rotationWrapsAround() {
        val page = ScannedPage(byteArrayOf(1), rotationDegrees = 270)
        assertEquals(0, page.rotateClockwise().rotationDegrees)
        assertEquals(180, page.rotateCounterClockwise().rotationDegrees)
    }
}
