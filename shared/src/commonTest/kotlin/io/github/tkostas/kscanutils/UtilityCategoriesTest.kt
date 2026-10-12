package io.github.tkostas.kscanutils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilityCategoriesTest {

    @Test
    fun pdfUtilitiesIsAvailableOnTheHomeScreen() {
        val pdfUtilities = utilityCategories.single { it.id == "pdf" }

        assertEquals("PDF Utilities", pdfUtilities.title)
        assertTrue(pdfUtilities.description.isNotBlank())
    }

    @Test
    fun categoryIdsAreUnique() {
        assertEquals(utilityCategories.size, utilityCategories.map { it.id }.distinct().size)
    }

    @Test
    fun scanningIsAvailableOnTheHomeScreen() {
        assertEquals("Scan Documents", utilityCategories.single { it.id == "scan" }.title)
    }
}
