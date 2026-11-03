package com.example.domain.repository

import com.example.domain.model.Calificacion
import kotlinx.coroutines.flow.Flow

interface RatingRepository {
    fun getRatings(productoId: String): Flow<List<Calificacion>>
    suspend fun getUserRating(productoId: String, userId: String): Result<Calificacion?>
    suspend fun addOrUpdateRating(productoId: String, calificacion: Calificacion): Result<Unit>
}
