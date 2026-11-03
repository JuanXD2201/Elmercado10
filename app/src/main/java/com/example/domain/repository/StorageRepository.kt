package com.example.domain.repository

import android.net.Uri

interface StorageRepository {
    suspend fun uploadProductImage(sellerId: String, productId: String, imageUri: Uri): Result<String>
    suspend fun uploadProfileImage(userId: String, imageUri: Uri): Result<String>
    suspend fun deleteImage(imageUrl: String): Result<Unit>
}
