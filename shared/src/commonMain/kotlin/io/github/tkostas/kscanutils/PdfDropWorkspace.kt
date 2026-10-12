package io.github.tkostas.kscanutils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PdfDropWorkspace(
    modifier: Modifier = Modifier,
) {
    var entries by remember { mutableStateOf(emptyList<PdfInputEntry>()) }
    var rejections by remember { mutableStateOf(emptyList<PdfValidationResult>()) }
    var dragActive by remember { mutableStateOf(false) }
    var nextId by remember { mutableStateOf(1L) }
    var merging by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<PdfMergeResult?>(null) }
    var overwritePath by remember { mutableStateOf<String?>(null) }
    var draggedEntryId by remember { mutableStateOf<Long?>(null) }
    var reorderOffsetPx by remember { mutableStateOf(0f) }
    val reorderThresholdPx = with(LocalDensity.current) { 56.dp.toPx() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var previousEntryCount by remember { mutableStateOf(0) }

    LaunchedEffect(entries.size) {
        if (entries.size > previousEntryCount) listState.animateScrollToItem(entries.lastIndex)
        previousEntryCount = entries.size
    }

    fun acceptPaths(paths: List<String>) {
        val results = paths.map(::validatePdf)
        val accepted = results.filter { it.accepted }.map { validated ->
            PdfInputEntry(nextId++, validated.fileName, validated.path)
        }
        entries = entries + accepted
        rejections = results.filterNot { it.accepted }
        result = null
    }

    fun beginMerge(outputPath: String) {
        merging = true
        result = null
        scope.launch {
            result = withContext(Dispatchers.Default) {
                mergePdfs(entries.map { it.path }, outputPath)
            }
            merging = false
        }
    }

    fun selectOutput() {
        val output = choosePdfOutput() ?: return
        if (outputFileExists(output)) overwritePath = output else beginMerge(output)
    }

    fun addPdfsFromChooser() {
        val paths = choosePdfInputs()
        if (paths.isNotEmpty()) acceptPaths(paths)
    }

    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                dragActive = true
            }

            override fun onExited(event: DragAndDropEvent) {
                dragActive = false
            }

            override fun onEnded(event: DragAndDropEvent) {
                dragActive = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                dragActive = false
                val files = (event.dragData() as? DragData.FilesList)?.readFiles() ?: return false
                acceptPaths(files)
                return true
            }
        }
    }

    overwritePath?.let { output ->
        AlertDialog(
            onDismissRequest = { overwritePath = null },
            title = { Text("Replace existing file?") },
            text = { Text("A file already exists at:\n$output\n\nReplace it with the merged PDF?") },
            confirmButton = {
                Button(onClick = {
                    overwritePath = null
                    beginMerge(output)
                }) { Text("Replace") }
            },
            dismissButton = {
                TextButton(onClick = { overwritePath = null }) { Text("Cancel") }
            },
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PdfDropTarget(
            dragActive = dragActive,
            dropTarget = dropTarget,
            onAddPdfs = ::addPdfsFromChooser,
        )

        RejectionMessage(rejections)

        if (entries.isNotEmpty()) {
            Text(
                text = "Files (${entries.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Drag a file to change its order.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
                        PdfEntryRow(
                            entry = entry,
                            position = index + 1,
                            canMoveUp = index > 0 && !merging,
                            canMoveDown = index < entries.lastIndex && !merging,
                            actionsEnabled = !merging,
                            dragging = draggedEntryId == entry.id,
                            dragOffsetPx = if (draggedEntryId == entry.id) reorderOffsetPx else 0f,
                            onDragStart = {
                                draggedEntryId = entry.id
                                reorderOffsetPx = 0f
                            },
                            onDrag = { deltaY ->
                                reorderOffsetPx += deltaY
                                val direction = when {
                                    reorderOffsetPx > reorderThresholdPx -> 1
                                    reorderOffsetPx < -reorderThresholdPx -> -1
                                    else -> 0
                                }
                                if (direction != 0) {
                                    val reordered = movePdfEntry(entries, entry.id, direction)
                                    if (reordered != entries) {
                                        entries = reordered
                                        reorderOffsetPx -= reorderThresholdPx * direction
                                        result = null
                                    } else {
                                        reorderOffsetPx = 0f
                                    }
                                }
                            },
                            onDragEnd = {
                                draggedEntryId = null
                                reorderOffsetPx = 0f
                            },
                            onMoveUp = { entries = movePdfEntry(entries, entry.id, -1); result = null },
                            onMoveDown = { entries = movePdfEntry(entries, entry.id, 1); result = null },
                            onRemove = { entries = removePdfEntry(entries, entry.id); result = null },
                        )
                    }
                }
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                )
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        if (merging) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text("Merging and validating the output…", style = MaterialTheme.typography.bodyMedium)
        }

        MergeResultMessage(result)

        Button(
            onClick = ::selectOutput,
            enabled = entries.size >= 2 && !merging,
            modifier = Modifier.align(Alignment.End),
        ) {
            Text(if (merging) "Merging…" else "Merge and export")
        }
        if (entries.size < 2) {
            Text(
                "Add at least two valid PDFs to enable merging.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun PdfDropTarget(
    dragActive: Boolean,
    dropTarget: DragAndDropTarget,
    onAddPdfs: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .dragAndDropTarget(
                shouldStartDragAndDrop = { it.dragData() is DragData.FilesList },
                target = dropTarget,
            )
            .semantics { contentDescription = "PDF file drop area" },
        shape = RoundedCornerShape(22.dp),
        color = if (dragActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(
            if (dragActive) 2.dp else 1.dp,
            if (dragActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    "PDF",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (dragActive) "Release to add PDFs" else "Drop PDF files here",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Drop several files at once or choose them from your computer.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            OutlinedButton(onClick = onAddPdfs) { Text("Add PDFs…") }
        }
    }
}

@Composable
private fun RejectionMessage(rejections: List<PdfValidationResult>) {
    if (rejections.isEmpty()) return
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Some files were not added", fontWeight = FontWeight.SemiBold)
            rejections.forEach { rejected ->
                Text("${rejected.fileName}: ${rejected.rejectionReason}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun PdfEntryRow(
    entry: PdfInputEntry,
    position: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    actionsEnabled: Boolean,
    dragging: Boolean,
    dragOffsetPx: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
) {
    val rowModifier = Modifier
        .fillMaxWidth()
        .zIndex(if (dragging) 1f else 0f)
        .graphicsLayer { translationY = dragOffsetPx }
        .then(if (dragging) Modifier.shadow(8.dp, RoundedCornerShape(12.dp)) else Modifier)
        .semantics {
            contentDescription = "Position $position, ${entry.fileName}. Drag to reorder."
            customActions = buildList {
                if (canMoveUp) add(CustomAccessibilityAction("Move up") { onMoveUp(); true })
                if (canMoveDown) add(CustomAccessibilityAction("Move down") { onMoveDown(); true })
            }
        }
        .focusable(enabled = actionsEnabled)
        .pointerInput(entry.id, actionsEnabled) {
            if (actionsEnabled) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.y)
                    },
                )
            }
        }

    Surface(
        modifier = rowModifier,
        shape = RoundedCornerShape(12.dp),
        color = if (dragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        border = if (dragging) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("$position", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f)) {
                Text(entry.fileName, fontWeight = FontWeight.Medium)
                Text(
                    entry.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text("⋮⋮", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(
                onClick = onRemove,
                enabled = actionsEnabled,
                modifier = Modifier.semantics { contentDescription = "Remove ${entry.fileName}" },
            ) { Text("Remove") }
        }
    }
}

@Composable
private fun MergeResultMessage(result: PdfMergeResult?) {
    result ?: return
    val background = if (result.successful) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
    val foreground = if (result.successful) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
    Surface(color = background, contentColor = foreground, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(if (result.successful) "PDF merged successfully" else "Merge failed", fontWeight = FontWeight.SemiBold)
            Text(result.outputPath ?: result.errorMessage.orEmpty(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
