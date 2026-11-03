package com.example.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Crea un ViewModel con ámbito de ciclo de vida: se conserva en rotaciones y se limpia
 * (onCleared) cuando su dueño (la entrada del back stack o la actividad) se destruye,
 * cancelando sus listeners de Firestore.
 *
 * Sustituye a `remember { XViewModel(...) }`, que nunca llamaba a onCleared.
 */
@Composable
internal inline fun <reified T : ViewModel> navViewModel(
    key: String? = null,
    crossinline build: () -> T
): T = viewModel(
    key = key,
    factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V = build() as V
    }
)
