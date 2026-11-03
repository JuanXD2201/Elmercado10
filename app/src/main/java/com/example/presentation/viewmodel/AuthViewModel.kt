package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Usuario
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val usuario: Usuario) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<Usuario?> = authRepository.currentUserFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun isUserLoggedIn(): Boolean = authRepository.isUserLoggedIn()

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Por favor completa todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signIn(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = { err ->
                    val friendlyMsg = when {
                        err.message?.contains("permission-denied", ignoreCase = true) == true ||
                        err.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                            "Permiso denegado por las reglas de Firestore. Revisa las reglas de seguridad publicadas."
                        err.message?.contains("password", ignoreCase = true) == true -> "Contraseña incorrecta."
                        err.message?.contains("user-not-found", ignoreCase = true) == true -> "Usuario no encontrado con este correo."
                        err.message?.contains("invalid-email", ignoreCase = true) == true -> "El formato del correo electrónico no es válido."
                        err.message?.contains("network", ignoreCase = true) == true -> "Error de conexión de red. Verifica tu Internet."
                        err.message?.contains("too-many-requests", ignoreCase = true) == true -> "Demasiados intentos fallidos. Intenta más tarde."
                        else -> err.localizedMessage ?: "Error al iniciar sesión."
                    }
                    _uiState.value = AuthUiState.Error(friendlyMsg)
                }
            )
        }
    }

    fun signUp(
        nombre: String,
        email: String,
        telefono: String,
        pass: String,
        confirmPass: String,
        tipoUsuario: String
    ) {
        if (nombre.isBlank() || email.isBlank() || telefono.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Todos los campos son obligatorios.")
            return
        }

        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("La contraseña debe tener al menos 6 caracteres.")
            return
        }

        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Las contraseñas no coinciden.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = authRepository.signUp(nombre, email, telefono, pass, tipoUsuario)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Success(user)
                },
                onFailure = { err ->
                    val friendlyMsg = when {
                        err.message?.contains("permission-denied", ignoreCase = true) == true ||
                        err.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                            "Permiso denegado en Firestore al guardar el perfil. Aplica las reglas en Firebase Console."
                        err.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                            "Este correo ya se encuentra registrado."
                        err.message?.contains("invalid-email", ignoreCase = true) == true ->
                            "Formato de correo inválido."
                        err.message?.contains("weak-password", ignoreCase = true) == true ->
                            "La contraseña es demasiado débil (mínimo 6 caracteres)."
                        err.message?.contains("network", ignoreCase = true) == true ->
                            "Error de conexión con los servidores de Firebase."
                        else -> err.localizedMessage ?: "Error al registrar usuario."
                    }
                    _uiState.value = AuthUiState.Error(friendlyMsg)
                }
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _uiState.value = AuthUiState.Idle
        }
    }
}
