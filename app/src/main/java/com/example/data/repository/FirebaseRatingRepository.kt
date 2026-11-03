package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.domain.model.Calificacion
import com.example.domain.repository.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseRatingRepository(
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : RatingRepository {

    override fun getRatings(productoId: String): Flow<List<Calificacion>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("productos").document(productoId)
            .collection("calificaciones")
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ratings = snapshot.toObjects(Calificacion::class.java)
                    trySend(ratings)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getUserRating(productoId: String, userId: String): Result<Calificacion?> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val doc = db.collection("productos").document(productoId)
                .collection("calificaciones").document(userId).get().await()
            Result.success(doc.toObject(Calificacion::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addOrUpdateRating(productoId: String, calificacion: Calificacion): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        if (calificacion.estrellas !in 1..5) {
            return Result.failure(IllegalArgumentException("La calificación debe estar entre 1 y 5 estrellas."))
        }

        return try {
            val productRef = db.collection("productos").document(productoId)
            val ratingRef = productRef.collection("calificaciones").document(calificacion.usuarioId)

            db.runTransaction { transaction ->
                // 1. Todas las lecturas primero
                val currentRatingDoc = transaction.get(ratingRef)
                val productDoc = transaction.get(productRef)

                // 2. Cálculos en memoria
                val prevStars = if (currentRatingDoc.exists()) {
                    currentRatingDoc.getLong("estrellas")?.toInt() ?: 0
                } else null

                val currentCount = productDoc.getLong("numCalificaciones")?.toInt() ?: 0
                val currentAvg = productDoc.getDouble("calificacionPromedio") ?: 0.0

                val newCount: Int
                val newAvg: Double

                if (prevStars == null) {
                    // Nueva calificación
                    newCount = currentCount + 1
                    val currentTotalSum = currentAvg * currentCount
                    newAvg = (currentTotalSum + calificacion.estrellas) / newCount
                } else {
                    // Modificación de calificación existente
                    newCount = currentCount
                    val currentTotalSum = currentAvg * currentCount
                    newAvg = if (newCount > 0) {
                        (currentTotalSum - prevStars + calificacion.estrellas) / newCount
                    } else {
                        calificacion.estrellas.toDouble()
                    }
                }

                // 3. Todas las escrituras después
                transaction.set(ratingRef, calificacion)
                transaction.update(
                    productRef,
                    mapOf(
                        "numCalificaciones" to newCount,
                        // Sin redondeo: evita acumular error en cada recálculo. La UI formatea a 1 decimal.
                        "calificacionPromedio" to newAvg
                    )
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
