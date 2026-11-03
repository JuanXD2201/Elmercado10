package com.example.data.repository

import android.net.Uri
import com.example.data.firebase.FirebaseManager
import com.example.domain.repository.StorageRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class FirebaseStorageRepository(
    private val storage: FirebaseStorage? = FirebaseManager.storage
) : StorageRepository {

    override suspend fun uploadProductImage(
        sellerId: String,
        productId: String,
        imageUri: Uri
    ): Result<String> {
        val storageInstance = storage
            ?: return Result.failure(Exception("Firebase Storage no está inicializado."))

        return try {
            val ref = storageInstance.reference
                .child("productos")
                .child(sellerId)
                .child(productId)
                .child("imagen_${System.currentTimeMillis()}.jpg")

            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String> {
        val storageInstance = storage
            ?: return Result.failure(Exception("Firebase Storage no está inicializado."))

        return try {
            val ref = storageInstance.reference
                .child("usuarios")
                .child(userId)
                .child("perfil.jpg")

            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteImage(imageUrl: String): Result<Unit> {
        val storageInstance = storage
            ?: return Result.failure(Exception("Firebase Storage no está inicializado."))

        return try {
            val ref = storageInstance.getReferenceFromUrl(imageUrl)
            ref.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
