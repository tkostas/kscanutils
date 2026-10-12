package io.github.tkostas.kscanutils

data class EditablePdfPage(
    val originalIndex: Int,
    val previewBytes: ByteArray,
    val rotationDegrees: Int = 0,
) {
    fun rotateClockwise() = copy(rotationDegrees = (rotationDegrees + 90) % 360)
    fun rotateCounterClockwise() = copy(rotationDegrees = (rotationDegrees + 270) % 360)

    override fun equals(other: Any?): Boolean = other is EditablePdfPage &&
        originalIndex == other.originalIndex &&
        previewBytes.contentEquals(other.previewBytes) &&
        rotationDegrees == other.rotationDegrees

    override fun hashCode(): Int = 31 * (31 * originalIndex + previewBytes.contentHashCode()) + rotationDegrees
}

data class EditablePdfDocument(
    val sourcePath: String,
    val fileName: String,
    val pages: List<EditablePdfPage>,
)

data class PdfEditResult(
    val successful: Boolean,
    val outputPath: String? = null,
    val errorMessage: String? = null,
)

fun rotatePdfPages(
    pages: List<EditablePdfPage>,
    selectedOriginalIndexes: Set<Int>,
    clockwise: Boolean,
): List<EditablePdfPage> = pages.map { page ->
    if (page.originalIndex !in selectedOriginalIndexes) page
    else if (clockwise) page.rotateClockwise() else page.rotateCounterClockwise()
}

fun removePdfPages(
    pages: List<EditablePdfPage>,
    selectedOriginalIndexes: Set<Int>,
): List<EditablePdfPage> = if (pages.none { it.originalIndex !in selectedOriginalIndexes }) {
    pages
} else {
    pages.filterNot { it.originalIndex in selectedOriginalIndexes }
}

expect fun choosePdfToEdit(): String?
expect fun chooseEditedPdfOutput(sourcePath: String): String?
expect suspend fun loadPdfForEditing(path: String): Result<EditablePdfDocument>
expect suspend fun exportEditedPdf(
    sourcePath: String,
    pages: List<EditablePdfPage>,
    outputPath: String,
): PdfEditResult
