package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.domain.model.CarritoItem
import com.example.domain.model.Producto
import com.example.domain.repository.CartRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseCartRepository(
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : CartRepository {

    override fun getCartItems(userId: String): Flow<List<CarritoItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("carritos").document(userId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.toObjects(CarritoItem::class.java)
                    trySend(items)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun addToCart(userId: String, producto: Producto, cantidad: Int): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            val cartRef = db.collection("carritos").document(userId)
            val itemRef = cartRef.collection("items").document(producto.id)

            db.runTransaction { transaction ->
                val itemSnapshot = transaction.get(itemRef)
                val currentQty = if (itemSnapshot.exists()) {
                    itemSnapshot.getLong("cantidad")?.toInt() ?: 0
                } else 0

                val targetQty = currentQty + cantidad
                if (targetQty > producto.cantidad) {
                    throw IllegalStateException("No puedes agregar más del stock disponible (${producto.cantidad}).")
                }

                val item = CarritoItem(
                    productoId = producto.id,
                    nombreProducto = producto.nombre,
                    cantidad = targetQty,
                    precioUnitario = producto.precio,
                    agregadoEn = System.currentTimeMillis(),
                    imagenProducto = producto.imagen,
                    vendedorId = producto.vendedorId
                )

                transaction.set(itemRef, item)
                transaction.set(
                    cartRef,
                    mapOf(
                        "uid" to userId,
                        "fechaActualizacion" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateQuantity(
        userId: String,
        productoId: String,
        newQuantity: Int,
        availableStock: Int
    ): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        if (newQuantity <= 0) {
            return removeFromCart(userId, productoId)
        }

        if (newQuantity > availableStock) {
            return Result.failure(IllegalArgumentException("Supera el stock disponible ($availableStock)."))
        }

        return try {
            val itemRef = db.collection("carritos").document(userId)
                .collection("items").document(productoId)
            itemRef.update("cantidad", newQuantity).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeFromCart(userId: String, productoId: String): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            db.collection("carritos").document(userId)
                .collection("items").document(productoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCart(userId: String): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            val itemsSnapshot = db.collection("carritos").document(userId)
                .collection("items").get().await()

            val batch = db.batch()
            for (doc in itemsSnapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
