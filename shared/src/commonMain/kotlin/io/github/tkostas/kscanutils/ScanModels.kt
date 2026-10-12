package io.github.tkostas.kscanutils

data class ScannerDevice(val id: String, val name: String)

data class ScannedPage(val imageBytes: ByteArray, val rotationDegrees: Int = 0, val dpi: Int = 300) {
    fun rotateClockwise() = copy(rotationDegrees = (rotationDegrees + 90) % 360)
    fun rotateCounterClockwise() = copy(rotationDegrees = (rotationDegrees + 270) % 360)

    override fun equals(other: Any?): Boolean = other is ScannedPage &&
        imageBytes.contentEquals(other.imageBytes) && rotationDegrees == other.rotationDegrees && dpi == other.dpi
    override fun hashCode(): Int = 31 * (31 * imageBytes.contentHashCode() + rotationDegrees) + dpi
}

enum class ScanFormat(val label: String, val extension: String) {
    PNG("PNG", "png"), JPEG("JPG", "jpg"), PDF("PDF", "pdf"),
}

enum class ImageQuality(val label: String, val compressionQuality: Float) {
    STANDARD("Standard", 0.75f), HIGH("High", 0.9f), MAXIMUM("Maximum", 1f),
}

val supportedScanDpis = listOf(150, 200, 300, 600)
fun availableScanFormats(pageCount: Int): List<ScanFormat> =
    if (pageCount == 1) ScanFormat.entries else listOf(ScanFormat.PDF)

expect suspend fun discoverScanners(): Result<List<ScannerDevice>>
expect suspend fun acquireScan(scanner: ScannerDevice, dpi: Int): Result<ByteArray>
expect fun chooseScanOutput(format: ScanFormat): String?
expect suspend fun exportScan(
    pages: List<ScannedPage>,
    format: ScanFormat,
    outputPath: String,
    imageQuality: ImageQuality = ImageQuality.HIGH,
): Result<String>
