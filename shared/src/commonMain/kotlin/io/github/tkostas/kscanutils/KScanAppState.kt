package io.github.tkostas.kscanutils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppDialog {
    About,
}

class KScanAppState {
    var destination by mutableStateOf(AppDestination.Home)
        private set
    var dialog by mutableStateOf<AppDialog?>(null)
        private set

    fun navigateTo(destination: AppDestination) {
        this.destination = destination
    }

    fun goBack() {
        destination = previousDestination(destination)
    }

    fun showDialog(dialog: AppDialog) {
        this.dialog = dialog
    }

    fun dismissDialog() {
        dialog = null
    }
}
