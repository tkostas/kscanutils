package io.github.tkostas.kscanutils

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationTest {

    @Test
    fun backFromPdfUtilitiesReturnsHome() {
        assertEquals(AppDestination.Home, previousDestination(AppDestination.PdfUtilities))
    }

    @Test
    fun backFromJoinPdfsReturnsPdfUtilities() {
        assertEquals(AppDestination.PdfUtilities, previousDestination(AppDestination.JoinPdfs))
    }

    @Test
    fun homeIsTheNavigationRoot() {
        assertEquals(AppDestination.Home, previousDestination(AppDestination.Home))
    }

    @Test
    fun backFromScanDocumentReturnsHome() {
        assertEquals(AppDestination.Home, previousDestination(AppDestination.ScanDocument))
    }

    @Test
    fun backFromEditPdfPagesReturnsPdfUtilities() {
        assertEquals(AppDestination.PdfUtilities, previousDestination(AppDestination.EditPdfPages))
    }
}
