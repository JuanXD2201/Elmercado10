package com.example.domain.repository

import com.example.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserFlow: Flow<Usuario?>
    fun isUserLoggedIn(): Boolean
    fun getCurrentUserId(): String?
    suspend fun signIn(email: String, password: String): Result<Usuario>
    suspend fun signUp(
        nombre: String,
        email: String,
        telefono: String,
        password: String,
        tipoUsuario: String
    ): Result<Usuario>
    suspend fun signOut()
}
