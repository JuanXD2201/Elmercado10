package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.domain.model.Usuario
import com.example.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseUserRepository(
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : UserRepository {

    override suspend fun getUser(uid: String): Result<Usuario?> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val doc = db.collection("usuarios").document(uid).get().await()
            Result.success(doc.toObject(Usuario::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserFlow(uid: String): Flow<Usuario?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Usuario::class.java))
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun updateUser(
        uid: String,
        nombre: String,
        telefono: String,
        fotoPerfil: String?
    ): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val updates = mutableMapOf<String, Any>(
                "nombre" to nombre.trim(),
                "telefono" to telefono.trim()
            )
            if (!fotoPerfil.isNullOrEmpty()) {
                updates["fotoPerfil"] = fotoPerfil
            }
            db.collection("usuarios").document(uid).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
