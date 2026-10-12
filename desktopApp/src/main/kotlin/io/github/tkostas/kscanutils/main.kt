package io.github.tkostas.kscanutils

import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    val appState = remember { KScanAppState() }
    Window(
        onCloseRequest = ::exitApplication,
        title = "KScanUtils",
        icon = painterResource("icons/app-icon.png"),
        state = rememberWindowState(width = 1100.dp, height = 800.dp),
    ) {
        LaunchedEffect(window) {
            window.minimumSize = Dimension(820, 650)
        }
        App(appState = appState, onExit = ::exitApplication)
    }
}
