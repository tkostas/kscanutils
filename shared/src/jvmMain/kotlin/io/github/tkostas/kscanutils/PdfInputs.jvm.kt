package io.github.tkostas.kscanutils

import java.io.File
import java.io.RandomAccessFile
import java.net.URI

actual fun validatePdf(path: String): PdfValidationResult {
    val file = runCatching {
        if (path.startsWith("file:", ignoreCase = true)) File(URI(path)) else File(path)
    }.getOrElse {
        return PdfValidationResult(path, path, false, "The file path is invalid")
    }
    val resolvedPath = file.absolutePath
    val name = file.name.ifBlank { path }

    fun rejected(reason: String) = PdfValidationResult(resolvedPath, name, false, reason)

    if (!file.isFile) return rejected("Not a file")
    if (!file.extension.equals("pdf", ignoreCase = true)) return rejected("Unsupported file type")
    if (!file.canRead()) return rejected("File is not readable")

    return try {
        RandomAccessFile(file, "r").use { input ->
            if (input.length() < 8) return rejected("The PDF is empty or incomplete")

            val headerSize = minOf(1024L, input.length()).toInt()
            val header = ByteArray(headerSize)
            input.readFully(header)
            if (!header.toAscii().contains("%PDF-")) {
                return rejected("The file does not contain a valid PDF header")
            }

            val tailSize = minOf(4096L, input.length()).toInt()
            val tail = ByteArray(tailSize)
            input.seek(input.length() - tailSize)
            input.readFully(tail)
            if (!tail.toAscii().contains("%%EOF")) {
                return rejected("The PDF appears to be incomplete or corrupted")
            }

            if (containsEncryptionMarker(input)) {
                return rejected("Password-protected PDFs are not supported")
            }

            PdfValidationResult(resolvedPath, name, true)
        }
    } catch (error: Exception) {
        rejected(error.message?.takeIf { it.isNotBlank() } ?: "The PDF could not be read")
    }
}

private fun ByteArray.toAscii(): String = toString(Charsets.ISO_8859_1)

private fun containsEncryptionMarker(input: RandomAccessFile): Boolean {
    input.seek(0)
    val marker = "/Encrypt".toByteArray(Charsets.ISO_8859_1)
    val buffer = ByteArray(32 * 1024)
    var overlap = ByteArray(0)

    while (true) {
        val count = input.read(buffer)
        if (count < 0) return false
        val block = overlap + buffer.copyOf(count)
        if (block.indexOf(marker) >= 0) return true
        overlap = block.takeLast(marker.size - 1).toByteArray()
    }
}

private fun ByteArray.indexOf(needle: ByteArray): Int {
    if (needle.isEmpty()) return 0
    for (start in 0..size - needle.size) {
        if (needle.indices.all { this[start + it] == needle[it] }) return start
    }
    return -1
}
