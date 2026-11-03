package com.example.domain.repository

import com.example.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUser(uid: String): Result<Usuario?>
    fun getUserFlow(uid: String): Flow<Usuario?>
    suspend fun updateUser(uid: String, nombre: String, telefono: String, fotoPerfil: String? = null): Result<Unit>
}
