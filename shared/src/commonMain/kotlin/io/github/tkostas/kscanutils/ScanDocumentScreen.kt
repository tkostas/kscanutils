package io.github.tkostas.kscanutils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

expect fun decodeScanPreview(bytes: ByteArray): ImageBitmap?

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanDocumentScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var scanners by remember { mutableStateOf(emptyList<ScannerDevice>()) }
    var selectedScanner by remember { mutableStateOf<ScannerDevice?>(null) }
    var dpi by remember { mutableStateOf(300) }
    var pages by remember { mutableStateOf(emptyList<ScannedPage>()) }
    var selectedPage by remember { mutableStateOf(0) }
    var format by remember { mutableStateOf(ScanFormat.PDF) }
    var imageQuality by remember { mutableStateOf(ImageQuality.HIGH) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Looking for scanners…") }
    var scannerMenu by remember { mutableStateOf(false) }
    var dpiMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        discoverScanners().fold(
            onSuccess = {
                scanners = it
                selectedScanner = it.firstOrNull()
                message = if (it.isEmpty()) "No scanners found. Check the connection and scanner software."
                else "Select Preview to scan the first page."
            },
            onFailure = { message = it.message ?: "Scanner discovery failed." },
        )
    }

    fun scanPage() {
        val scanner = selectedScanner ?: return
        busy = true
        message = "Scanning at $dpi DPI…"
        scope.launch {
            acquireScan(scanner, dpi).fold(
                onSuccess = {
                    pages = pages + ScannedPage(it, dpi = dpi)
                    selectedPage = pages.lastIndex
                    if (pages.size > 1) format = ScanFormat.PDF
                    message = "Page ${pages.size} scanned."
                },
                onFailure = { message = it.message ?: "Scanning failed." },
            )
            busy = false
        }
    }

    fun save() {
        val output = chooseScanOutput(format) ?: return
        busy = true
        message = "Saving…"
        scope.launch {
            withContext(Dispatchers.Default) { exportScan(pages, format, output, imageQuality) }.fold(
                onSuccess = { message = "Saved to $it" },
                onFailure = { message = it.message ?: "The scan could not be saved." },
            )
            busy = false
        }
    }

    PageContainer(modifier) {
        BackAction("Back to Home", onBack)
        Spacer(Modifier.height(12.dp))
        Text("Scan a document", style = MaterialTheme.typography.headlineLarge)
        Text("Choose a scanner and resolution, preview each page, then export.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ExposedDropdownMenuBox(scannerMenu, { scannerMenu = !scannerMenu }, Modifier.weight(1f)) {
                OutlinedTextField(
                    selectedScanner?.name ?: "No scanner available", {}, readOnly = true,
                    label = { Text("Scanner") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(scannerMenu) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(scannerMenu, { scannerMenu = false }) {
                    scanners.forEach { scanner ->
                        DropdownMenuItem({ Text(scanner.name) }, { selectedScanner = scanner; scannerMenu = false })
                    }
                }
            }
            ExposedDropdownMenuBox(dpiMenu, { dpiMenu = !dpiMenu }, Modifier.width(150.dp)) {
                OutlinedTextField(
                    "$dpi DPI", {}, readOnly = true, label = { Text("Resolution") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dpiMenu) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(dpiMenu, { dpiMenu = false }) {
                    supportedScanDpis.forEach { value ->
                        DropdownMenuItem({ Text("$value DPI") }, { dpi = value; dpiMenu = false })
                    }
                }
            }
            Button(::scanPage, enabled = selectedScanner != null && !busy) {
                Text(if (pages.isEmpty()) "Preview" else "Add page")
            }
        }
        Spacer(Modifier.height(14.dp))
        ScanPreview(pages.getOrNull(selectedPage), Modifier.fillMaxWidth().weight(1f))
        if (pages.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton({ pages = pages.replaceAt(selectedPage, pages[selectedPage].rotateCounterClockwise()) }) { Text("↶ Rotate left") }
                OutlinedButton({ pages = pages.replaceAt(selectedPage, pages[selectedPage].rotateClockwise()) }) { Text("↷ Rotate right") }
                Text("Page ${selectedPage + 1} of ${pages.size}", modifier = Modifier.weight(1f))
                TextButton({
                    pages = pages.filterIndexed { index, _ -> index != selectedPage }
                    selectedPage = selectedPage.coerceAtMost((pages.size - 1).coerceAtLeast(0))
                    if (pages.size > 1) format = ScanFormat.PDF
                }) { Text("Remove page") }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(pages) { index, page ->
                    OutlinedButton(
                        onClick = { selectedPage = index },
                        border = BorderStroke(if (index == selectedPage) 2.dp else 1.dp, if (index == selectedPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                    ) { Text("Page ${index + 1}${if (page.rotationDegrees != 0) " · ${page.rotationDegrees}°" else ""}") }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (pages.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text("Save as", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(72.dp))
                availableScanFormats(pages.size).forEach { value ->
                    FilterChip(
                        selected = format == value,
                        onClick = { format = value },
                        enabled = !busy,
                        label = { Text(value.label, maxLines = 1) },
                    )
                }
                if (pages.size > 1) {
                    Text("Multi-page scans use PDF.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (format == ScanFormat.JPEG) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Quality", style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(72.dp))
                    ImageQuality.entries.forEach { value ->
                        FilterChip(
                            selected = imageQuality == value,
                            onClick = { imageQuality = value },
                            enabled = !busy,
                            label = { Text(value.label, maxLines = 1) },
                        )
                    }
                }
            } else if (format == ScanFormat.PNG) {
                Text(
                    "PNG is lossless; DPI controls the captured resolution.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 82.dp),
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (busy) CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 3.dp)
            Button(::save, enabled = pages.isNotEmpty() && !busy) { Text("Save") }
        }
    }
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> = toMutableList().also { it[index] = value }

@Composable
private fun ScanPreview(page: ScannedPage?, modifier: Modifier = Modifier) {
    Surface(
        modifier, shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            val bitmap = remember(page?.imageBytes) { page?.let { decodeScanPreview(it.imageBytes) } }
            if (bitmap == null) Text("Your scanned page preview will appear here", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else Image(bitmap, "Scanned page preview", Modifier.fillMaxSize().rotate(page?.rotationDegrees?.toFloat() ?: 0f))
        }
    }
}
