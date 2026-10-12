package io.github.tkostas.kscanutils

import kotlin.test.Test
import kotlin.test.assertEquals

class PdfOrderingTest {
    private val entries = listOf(
        PdfInputEntry(1, "one.pdf", "/one.pdf"),
        PdfInputEntry(2, "two.pdf", "/two.pdf"),
        PdfInputEntry(3, "three.pdf", "/three.pdf"),
    )

    @Test
    fun movesEntriesWithinBounds() {
        assertEquals(listOf(2L, 1L, 3L), movePdfEntry(entries, 1, 1).map { it.id })
        assertEquals(listOf(1L, 3L, 2L), movePdfEntry(entries, 3, -1).map { it.id })
        assertEquals(entries, movePdfEntry(entries, 1, -1))
    }

    @Test
    fun removesOnlyTheEntryWithTheMatchingStableId() {
        val duplicates = listOf(
            PdfInputEntry(1, "same.pdf", "/same.pdf"),
            PdfInputEntry(2, "same.pdf", "/same.pdf"),
        )
        assertEquals(listOf(2L), removePdfEntry(duplicates, 1).map { it.id })
    }
}
