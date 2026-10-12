package io.github.tkostas.kscanutils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UtilityCategory(
    val id: String,
    val title: String,
    val description: String,
)

const val APP_VERSION = "1.0.1"
private const val PROJECT_URL = "https://github.com/tkostas/kscanutils"

val utilityCategories = listOf(
    UtilityCategory(
        id = "scan",
        title = "Scan Documents",
        description = "Preview, rotate, and save one or more scanned pages.",
    ),
    UtilityCategory(
        id = "pdf",
        title = "PDF Utilities",
        description = "Join, organize, and export PDF documents.",
    ),
)

enum class AppDestination {
    Home,
    ScanDocument,
    PdfUtilities,
    JoinPdfs,
    EditPdfPages,
}

fun previousDestination(destination: AppDestination): AppDestination = when (destination) {
    AppDestination.Home -> AppDestination.Home
    AppDestination.ScanDocument -> AppDestination.Home
    AppDestination.PdfUtilities -> AppDestination.Home
    AppDestination.JoinPdfs -> AppDestination.PdfUtilities
    AppDestination.EditPdfPages -> AppDestination.PdfUtilities
}

@Composable
@Preview
fun App(
    appState: KScanAppState = remember { KScanAppState() },
    onExit: () -> Unit = {},
) {

    KScanTheme {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { AppTopBar(appState = appState, onExit = onExit) },
            bottomBar = { AppVersionFooter() },
        ) { innerPadding ->
            when (appState.destination) {
                AppDestination.Home -> HomeScreen(
                    categories = utilityCategories,
                    selectedCategoryId = null,
                    onOpenCategory = { category ->
                        appState.navigateTo(
                            if (category.id == "scan") AppDestination.ScanDocument else AppDestination.PdfUtilities,
                        )
                    },
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestination.ScanDocument -> ScanDocumentScreen(
                    onBack = appState::goBack,
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestination.PdfUtilities -> PdfUtilitiesScreen(
                    onBack = appState::goBack,
                    onOpenJoinPdfs = { appState.navigateTo(AppDestination.JoinPdfs) },
                    onOpenEditPdfPages = { appState.navigateTo(AppDestination.EditPdfPages) },
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestination.JoinPdfs -> JoinPdfsScreen(
                    onBack = appState::goBack,
                    appState = appState,
                    modifier = Modifier.padding(innerPadding),
                )

                AppDestination.EditPdfPages -> PdfPageEditorScreen(
                    onBack = appState::goBack,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
        AppInformationDialog(appState)
    }
}

@Composable
private fun AppInformationDialog(appState: KScanAppState) {
    appState.dialog ?: return
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = appState::dismissDialog,
        title = { Text("About KScanUtils") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("KScanUtils $APP_VERSION")
                Text(
                    "A cross-platform desktop application for scanning and PDF utilities.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Created by Kostas Tsolis")
                Text(
                    text = "github.com/tkostas/kscanutils",
                    modifier = Modifier.clickable { uriHandler.openUri(PROJECT_URL) },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider()
                Text("Application license", fontWeight = FontWeight.SemiBold)
                Text(
                    "KScanUtils — Apache License 2.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text("Third-party software", fontWeight = FontWeight.SemiBold)
                Text(
                    "Apache PDFBox 3.0.8 — Apache License 2.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = { TextButton(onClick = appState::dismissDialog) { Text("Close") } },
    )
}

@Composable
private fun AppVersionFooter() {
    Row(
        modifier = Modifier.fillMaxWidth().height(24.dp).padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "v$APP_VERSION",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}

@Composable
fun PdfUtilitiesScreen(
    onBack: () -> Unit,
    onOpenJoinPdfs: () -> Unit,
    onOpenEditPdfPages: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(modifier = modifier) {
        BackAction(label = "Back to Home", onClick = onBack)
        Spacer(Modifier.height(18.dp))
        SectionLabel("DOCUMENT TOOLS")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "PDF Utilities",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Choose a tool for your PDF documents.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))
        ToolCard(
            title = "Join PDFs",
            description = "Combine multiple PDF files in the order you choose.",
            onClick = onOpenJoinPdfs,
        )
        Spacer(Modifier.height(14.dp))
        ToolCard(
            title = "Edit PDF Pages",
            description = "Rotate or remove selected pages and save an edited PDF.",
            onClick = onOpenEditPdfPages,
        )
    }
}

@Composable
fun JoinPdfsScreen(
    onBack: () -> Unit,
    appState: KScanAppState,
    modifier: Modifier = Modifier,
) {
    PageContainer(modifier = modifier) {
        BackAction(label = "Back to PDF Utilities", onClick = onBack)
        Spacer(Modifier.height(18.dp))
        SectionLabel("PDF UTILITIES")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Join PDFs",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Add PDF files, arrange their order, and merge them into one document.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(22.dp))
        PdfDropWorkspace(
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun PageContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 22.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1080.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            shadowElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
            fontSize = 11.sp,
            letterSpacing = 0.8.sp,
        )
    }
}

@Composable
internal fun BackAction(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        modifier = Modifier.semantics { contentDescription = label },
    ) {
        Text("←  $label")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolCard(
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 520.dp)
            .height(154.dp)
            .semantics {
                contentDescription = "Open $title"
                role = Role.Button
            },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            if (focused) 3.dp else 1.dp,
            if (focused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = if (focused) 5.dp else 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PdfBadge()
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    categories: List<UtilityCategory>,
    selectedCategoryId: String?,
    onOpenCategory: (UtilityCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    PageContainer(modifier = modifier) {
            SectionLabel("DESKTOP DOCUMENT WORKSPACE")
            Spacer(Modifier.height(14.dp))
            Text(
                text = "KScanUtils",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Fast, private document tools that run on your computer.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(30.dp))
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 260.dp),
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(categories, key = { it.id }) { category ->
                    UtilityCategoryCard(
                        category = category,
                        selected = category.id == selectedCategoryId,
                        onClick = { onOpenCategory(category) },
                    )
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UtilityCategoryCard(
    category: UtilityCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val emphasized = selected || focused

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(196.dp)
            .semantics {
                contentDescription = "Open ${category.title}"
                role = Role.Button
                this.selected = selected
            },
        interactionSource = interactionSource,
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            width = if (emphasized) 3.dp else 1.dp,
            color = if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLowest,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (emphasized) 5.dp else 1.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            if (category.id == "scan") ScanBadge() else PdfBadge()
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (selected) {
                        Text(
                            text = "  Selected",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PdfBadge() {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFE5484D),
        contentColor = Color.White,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "PDF",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ScanBadge() {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
    ) {
        Canvas(Modifier.padding(11.dp)) {
            val strokeWidth = 2.dp.toPx()
            val cornerRadius = 2.dp.toPx()

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(size.width * 0.24f, 0f),
                size = Size(size.width * 0.52f, size.height * 0.55f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                style = Stroke(strokeWidth),
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.34f, size.height * 0.18f),
                end = Offset(size.width * 0.66f, size.height * 0.18f),
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.34f, size.height * 0.32f),
                end = Offset(size.width * 0.60f, size.height * 0.32f),
                strokeWidth = strokeWidth,
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(0f, size.height * 0.48f),
                size = Size(size.width, size.height * 0.42f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
                style = Stroke(strokeWidth),
            )
            drawLine(
                color = Color.White,
                start = Offset(size.width * 0.18f, size.height * 0.72f),
                end = Offset(size.width * 0.82f, size.height * 0.72f),
                strokeWidth = strokeWidth,
            )
        }
    }
}
