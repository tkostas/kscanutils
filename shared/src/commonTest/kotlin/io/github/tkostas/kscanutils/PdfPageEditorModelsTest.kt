package io.github.tkostas.kscanutils

import kotlin.test.Test
import kotlin.test.assertEquals

class PdfPageEditorModelsTest {
    private val pages = (0..2).map { EditablePdfPage(it, byteArrayOf(it.toByte())) }

    @Test
    fun rotatesOnlySelectedPages() {
        val result = rotatePdfPages(pages, setOf(0, 2), clockwise = true)

        assertEquals(listOf(90, 0, 90), result.map { it.rotationDegrees })
        assertEquals(listOf(0, 1, 2), result.map { it.originalIndex })
    }

    @Test
    fun counterClockwiseRotationWrapsTo270Degrees() {
        val result = rotatePdfPages(pages, setOf(1), clockwise = false)

        assertEquals(270, result[1].rotationDegrees)
    }

    @Test
    fun removesSelectedPagesAndPreservesOrder() {
        val result = removePdfPages(pages, setOf(1))

        assertEquals(listOf(0, 2), result.map { it.originalIndex })
    }

    @Test
    fun refusesToRemoveEveryPage() {
        assertEquals(pages, removePdfPages(pages, setOf(0, 1, 2)))
    }
}
