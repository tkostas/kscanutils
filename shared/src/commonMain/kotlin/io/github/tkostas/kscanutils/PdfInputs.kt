package io.github.tkostas.kscanutils

data class PdfInputEntry(
    val id: Long,
    val fileName: String,
    val path: String,
)

data class PdfValidationResult(
    val path: String,
    val fileName: String,
    val accepted: Boolean,
    val rejectionReason: String? = null,
)

expect fun validatePdf(path: String): PdfValidationResult

data class PdfMergeResult(
    val successful: Boolean,
    val outputPath: String? = null,
    val errorMessage: String? = null,
)

fun movePdfEntry(entries: List<PdfInputEntry>, entryId: Long, offset: Int): List<PdfInputEntry> {
    val from = entries.indexOfFirst { it.id == entryId }
    if (from < 0) return entries
    val to = (from + offset).coerceIn(entries.indices)
    if (from == to) return entries
    return entries.toMutableList().apply { add(to, removeAt(from)) }
}

fun removePdfEntry(entries: List<PdfInputEntry>, entryId: Long): List<PdfInputEntry> =
    entries.filterNot { it.id == entryId }

expect fun choosePdfOutput(): String?

expect fun choosePdfInputs(): List<String>

expect fun outputFileExists(path: String): Boolean

expect fun mergePdfs(inputPaths: List<String>, outputPath: String): PdfMergeResult
