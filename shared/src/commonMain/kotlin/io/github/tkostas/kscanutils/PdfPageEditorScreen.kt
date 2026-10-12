package io.github.tkostas.kscanutils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PdfPageEditorScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    var document by remember { mutableStateOf<EditablePdfDocument?>(null) }
    var pages by remember { mutableStateOf(emptyList<EditablePdfPage>()) }
    var originalPages by remember { mutableStateOf(emptyList<EditablePdfPage>()) }
    var selectedIndexes by remember { mutableStateOf(emptySet<Int>()) }
    var undoHistory by remember { mutableStateOf(emptyList<List<EditablePdfPage>>()) }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Choose one PDF to begin editing.") }
    var result by remember { mutableStateOf<PdfEditResult?>(null) }
    var overwritePath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun openPdf() {
        val path = choosePdfToEdit() ?: return
        busy = true
        message = "Loading page previews…"
        result = null
        scope.launch {
            loadPdfForEditing(path).fold(
                onSuccess = { loaded ->
                    document = loaded
                    pages = loaded.pages
                    originalPages = loaded.pages
                    selectedIndexes = emptySet()
                    undoHistory = emptyList()
                    message = "${loaded.pages.size} page${if (loaded.pages.size == 1) "" else "s"} loaded. Select pages to edit."
                },
                onFailure = { error -> message = error.message ?: "The PDF could not be opened." },
            )
            busy = false
        }
    }

    fun applyEdit(transform: (List<EditablePdfPage>) -> List<EditablePdfPage>) {
        val edited = transform(pages)
        if (edited == pages) return
        undoHistory = undoHistory + listOf(pages)
        pages = edited
        selectedIndexes = selectedIndexes.intersect(edited.mapTo(mutableSetOf()) { it.originalIndex })
        result = null
    }

    fun beginSave(outputPath: String) {
        val source = document ?: return
        busy = true
        message = "Saving and validating the edited PDF…"
        result = null
        scope.launch {
            result = withContext(Dispatchers.Default) {
                exportEditedPdf(source.sourcePath, pages, outputPath)
            }
            message = if (result?.successful == true) "Edited PDF saved successfully." else "The edited PDF could not be saved."
            busy = false
        }
    }

    fun save() {
        val source = document ?: return
        val output = chooseEditedPdfOutput(source.sourcePath) ?: return
        if (outputFileExists(output)) overwritePath = output else beginSave(output)
    }

    overwritePath?.let { output ->
        AlertDialog(
            onDismissRequest = { overwritePath = null },
            title = { Text("Replace existing file?") },
            text = { Text("A file already exists at:\n$output\n\nReplace it with the edited PDF?") },
            confirmButton = {
                Button(onClick = { overwritePath = null; beginSave(output) }) { Text("Replace") }
            },
            dismissButton = { TextButton(onClick = { overwritePath = null }) { Text("Cancel") } },
        )
    }

    PageContainer(modifier) {
        BackAction("Back to PDF Utilities", onBack)
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Edit PDF Pages", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Select pages to rotate or remove, then save a new PDF.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = ::openPdf, enabled = !busy) {
                Text(if (document == null) "Open PDF…" else "Open another PDF…")
            }
        }
        Spacer(Modifier.height(16.dp))

        if (document == null) {
            EmptyPdfEditor(onOpenPdf = ::openPdf, enabled = !busy, modifier = Modifier.weight(1f))
        } else {
            PdfEditorToolbar(
                fileName = document!!.fileName,
                pageCount = pages.size,
                selectionCount = selectedIndexes.size,
                canUndo = undoHistory.isNotEmpty() && !busy,
                enabled = !busy,
                onSelectAll = { selectedIndexes = pages.mapTo(mutableSetOf()) { it.originalIndex } },
                onClearSelection = { selectedIndexes = emptySet() },
                onRotateLeft = { applyEdit { rotatePdfPages(it, selectedIndexes, clockwise = false) } },
                onRotateRight = { applyEdit { rotatePdfPages(it, selectedIndexes, clockwise = true) } },
                onRemove = {
                    if (selectedIndexes.size == pages.size) {
                        message = "A PDF must contain at least one page. Keep one page before removing the selection."
                    } else {
                        applyEdit { removePdfPages(it, selectedIndexes) }
                        selectedIndexes = emptySet()
                    }
                },
                onUndo = {
                    pages = undoHistory.last()
                    undoHistory = undoHistory.dropLast(1)
                    selectedIndexes = emptySet()
                    result = null
                },
            )
            Spacer(Modifier.height(12.dp))
            PdfPageGrid(
                pages = pages,
                selectedIndexes = selectedIndexes,
                onToggle = { originalIndex ->
                    selectedIndexes = if (originalIndex in selectedIndexes) {
                        selectedIndexes - originalIndex
                    } else {
                        selectedIndexes + originalIndex
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }

        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        result?.let { PdfEditResultMessage(it) }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = ::save,
                enabled = document != null && pages != originalPages && !busy,
            ) { Text(if (busy) "Working…" else "Save as PDF") }
        }
    }
}

@Composable
private fun EmptyPdfEditor(onOpenPdf: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Open a PDF to see its pages", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("Your document stays on this computer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(18.dp))
            Button(onClick = onOpenPdf, enabled = enabled) { Text("Choose PDF…") }
        }
    }
}

@Composable
private fun PdfEditorToolbar(
    fileName: String,
    pageCount: Int,
    selectionCount: Int,
    canUndo: Boolean,
    enabled: Boolean,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onRemove: () -> Unit,
    onUndo: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(fileName, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$pageCount page${if (pageCount == 1) "" else "s"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("$selectionCount selected", modifier = Modifier.width(88.dp))
            TextButton(onClick = onSelectAll, enabled = enabled && selectionCount < pageCount) { Text("Select all") }
            TextButton(onClick = onClearSelection, enabled = enabled && selectionCount > 0) { Text("Clear") }
            VerticalDivider(Modifier.height(28.dp))
            OutlinedButton(onClick = onRotateLeft, enabled = enabled && selectionCount > 0) { Text("↶ Rotate left") }
            OutlinedButton(onClick = onRotateRight, enabled = enabled && selectionCount > 0) { Text("↷ Rotate right") }
            OutlinedButton(onClick = onRemove, enabled = enabled && selectionCount > 0) { Text("Remove") }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onUndo, enabled = canUndo) { Text("Undo") }
        }
    }
}

@Composable
private fun PdfPageGrid(
    pages: List<EditablePdfPage>,
    selectedIndexes: Set<Int>,
    onToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    Box(modifier) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize().padding(end = 12.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(pages, key = { _, page -> page.originalIndex }) { displayedIndex, page ->
                val selected = page.originalIndex in selectedIndexes
                Card(
                    onClick = { onToggle(page.originalIndex) },
                    modifier = Modifier.semantics {
                        contentDescription = "Page ${displayedIndex + 1}${if (selected) ", selected" else ""}"
                        role = Role.Checkbox
                        this.selected = selected
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(if (selected) 3.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                    colors = CardDefaults.cardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        val bitmap = remember(page.previewBytes) { decodeScanPreview(page.previewBytes) }
                        Box(Modifier.fillMaxWidth().height(170.dp), contentAlignment = Alignment.Center) {
                            if (bitmap == null) Text("Preview unavailable")
                            else Image(
                                bitmap = bitmap,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(4.dp).rotate(page.rotationDegrees.toFloat()),
                            )
                        }
                        Text(
                            "Page ${displayedIndex + 1}${if (page.rotationDegrees != 0) " · ${page.rotationDegrees}°" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(gridState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
    }
}

@Composable
private fun PdfEditResultMessage(result: PdfEditResult) {
    val background = if (result.successful) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val foreground = if (result.successful) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Surface(color = background, contentColor = foreground, shape = RoundedCornerShape(12.dp)) {
        Text(
            result.outputPath ?: result.errorMessage.orEmpty(),
            modifier = Modifier.fillMaxWidth().padding(12.dp),
        )
    }
}
