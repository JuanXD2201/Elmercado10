package com.example.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Usuario
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.StorageRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userId: String,
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val user: StateFlow<Usuario?> = userRepository.getUserFlow(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    fun updateProfile(nombre: String, telefono: String, newPhotoUri: Uri? = null, externalPhotoUrl: String? = null) {
        if (nombre.isBlank()) {
            viewModelScope.launch { _toastEvent.emit("El nombre no puede estar vacío.") }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            var photoUrl: String? = externalPhotoUrl

            if (newPhotoUri != null) {
                val uploadResult = storageRepository.uploadProfileImage(userId, newPhotoUri)
                if (uploadResult.isSuccess) {
                    photoUrl = uploadResult.getOrNull() ?: externalPhotoUrl
                } else {
                    _toastEvent.emit("Advertencia: Falló Storage, usando URL previa: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            val result = userRepository.updateUser(userId, nombre, telefono, photoUrl)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _toastEvent.emit("Perfil actualizado correctamente")
                },
                onFailure = { err ->
                    _toastEvent.emit(err.localizedMessage ?: "Error al actualizar perfil")
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
