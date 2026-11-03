package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Calificacion
import com.example.domain.repository.RatingRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RatingViewModel(
    private val productoId: String,
    private val userId: String,
    private val userName: String,
    private val ratingRepository: RatingRepository
) : ViewModel() {

    private val _existingRating = MutableStateFlow<Calificacion?>(null)
    val existingRating: StateFlow<Calificacion?> = _existingRating.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    init {
        loadExisting()
    }

    private fun loadExisting() {
        viewModelScope.launch {
            ratingRepository.getUserRating(productoId, userId).onSuccess { rating ->
                _existingRating.value = rating
            }
        }
    }

    fun submitRating(stars: Int, comment: String) {
        if (stars !in 1..5) {
            viewModelScope.launch { _eventFlow.emit("Selecciona entre 1 y 5 estrellas.") }
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            val calificacion = Calificacion(
                usuarioId = userId,
                estrellas = stars,
                comentario = comment.trim(),
                fecha = System.currentTimeMillis(),
                nombreUsuario = userName
            )

            val result = ratingRepository.addOrUpdateRating(productoId, calificacion)
            _isSubmitting.value = false
            result.fold(
                onSuccess = {
                    _eventFlow.emit("Calificación guardada exitosamente.")
                },
                onFailure = { err ->
                    _eventFlow.emit(err.localizedMessage ?: "Error al guardar calificación.")
                }
            )
        }
    }
}
