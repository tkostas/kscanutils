package io.github.tkostas.kscanutils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private enum class TopMenu { File, Tools, Help }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppTopBar(appState: KScanAppState, onExit: () -> Unit) {
    var expandedMenu by remember { mutableStateOf<TopMenu?>(null) }
    var toolsSubmenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(expandedMenu) {
        if (expandedMenu != TopMenu.Tools) toolsSubmenuExpanded = false
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clickable { appState.navigateTo(AppDestination.Home) }
                    .semantics {
                        contentDescription = "Go to Home"
                        role = Role.Button
                    }
                    .padding(horizontal = 4.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("K", fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text("KScanUtils", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.width(18.dp))

            MenuAnchor(
                label = "File",
                expanded = expandedMenu == TopMenu.File,
                onClick = { expandedMenu = if (expandedMenu == TopMenu.File) null else TopMenu.File },
                onHover = { expandedMenu = TopMenu.File },
            ) {
                DropdownMenuItem(
                    text = { Text("Exit") },
                    onClick = {
                        expandedMenu = null
                        onExit()
                    },
                )
            }

            MenuAnchor(
                label = "Tools",
                expanded = expandedMenu == TopMenu.Tools,
                onClick = { expandedMenu = if (expandedMenu == TopMenu.Tools) null else TopMenu.Tools },
                onHover = { expandedMenu = TopMenu.Tools },
            ) {
                DropdownMenuItem(
                    text = { Text("Home") },
                    modifier = Modifier.onPointerEvent(PointerEventType.Enter) {
                        toolsSubmenuExpanded = false
                    },
                    onClick = {
                        expandedMenu = null
                        appState.navigateTo(AppDestination.Home)
                    },
                )
                HorizontalDivider(Modifier.padding(vertical = 5.dp))
                DropdownMenuItem(
                    text = { Text("Scan Documents") },
                    modifier = Modifier.onPointerEvent(PointerEventType.Enter) {
                        toolsSubmenuExpanded = false
                    },
                    onClick = {
                        expandedMenu = null
                        appState.navigateTo(AppDestination.ScanDocument)
                    },
                )
                Box {
                    DropdownMenuItem(
                        text = { Text("PDF Utilities") },
                        trailingIcon = { Text("›", style = MaterialTheme.typography.titleMedium) },
                        onClick = { toolsSubmenuExpanded = !toolsSubmenuExpanded },
                        modifier = Modifier.onPointerEvent(PointerEventType.Enter) {
                            toolsSubmenuExpanded = true
                        },
                    )
                    DropdownMenu(
                        expanded = toolsSubmenuExpanded,
                        onDismissRequest = { toolsSubmenuExpanded = false },
                        offset = DpOffset(150.dp, (-48).dp),
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 8.dp,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Overview") },
                            onClick = {
                                toolsSubmenuExpanded = false
                                expandedMenu = null
                                appState.navigateTo(AppDestination.PdfUtilities)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Join PDFs") },
                            onClick = {
                                toolsSubmenuExpanded = false
                                expandedMenu = null
                                appState.navigateTo(AppDestination.JoinPdfs)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Edit PDF Pages") },
                            onClick = {
                                toolsSubmenuExpanded = false
                                expandedMenu = null
                                appState.navigateTo(AppDestination.EditPdfPages)
                            },
                        )
                    }
                }
            }

            MenuAnchor(
                label = "Help",
                expanded = expandedMenu == TopMenu.Help,
                onClick = { expandedMenu = if (expandedMenu == TopMenu.Help) null else TopMenu.Help },
                onHover = { expandedMenu = TopMenu.Help },
            ) {
                DropdownMenuItem(
                    text = { Text("About KScanUtils") },
                    onClick = {
                        expandedMenu = null
                        appState.showDialog(AppDialog.About)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MenuAnchor(
    label: String,
    expanded: Boolean,
    onClick: () -> Unit,
    onHover: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = Modifier.onPointerEvent(PointerEventType.Enter) { onHover() },
    ) {
        TextButton(
            onClick = onClick,
            modifier = Modifier.height(34.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = if (expanded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                contentColor = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text(label, fontWeight = FontWeight.Medium)
            Spacer(Modifier.width(5.dp))
            Text("⌄")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onClick,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 8.dp,
            content = content,
        )
    }
}
