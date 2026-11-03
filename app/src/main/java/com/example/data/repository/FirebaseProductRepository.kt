package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.domain.model.Producto
import com.example.domain.repository.ProductRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseProductRepository(
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : ProductRepository {

    override fun getProducts(): Flow<List<Producto>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("productos")
            .orderBy("fechaPublicacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Producto::class.java)
                    trySend(products)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override fun getProductsBySeller(sellerId: String): Flow<List<Producto>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("productos")
            .whereEqualTo("vendedorId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val products = snapshot.toObjects(Producto::class.java)
                    trySend(products.sortedByDescending { it.fechaPublicacion })
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getProductById(id: String): Result<Producto?> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val doc = db.collection("productos").document(id).get().await()
            Result.success(doc.toObject(Producto::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getProductFlow(id: String): Flow<Producto?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val registration = db.collection("productos").document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject(Producto::class.java))
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun createProduct(producto: Producto): Result<String> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val docRef = if (producto.id.isNotBlank()) {
                db.collection("productos").document(producto.id)
            } else {
                db.collection("productos").document()
            }
            val finalProduct = producto.copy(
                id = docRef.id,
                calificacionPromedio = 0.0,
                numCalificaciones = 0,
                fechaPublicacion = System.currentTimeMillis()
            )
            docRef.set(finalProduct).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProduct(producto: Producto): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val updates = mapOf(
                "nombre" to producto.nombre,
                "descripcion" to producto.descripcion,
                "precio" to producto.precio,
                "categoria" to producto.categoria,
                "estado" to producto.estado,
                "cantidad" to producto.cantidad,
                "imagen" to producto.imagen
            )
            db.collection("productos").document(producto.id).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteProduct(productId: String, sellerId: String, imageUrl: String?): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))
        return try {
            val doc = db.collection("productos").document(productId).get().await()
            val existing = doc.toObject(Producto::class.java)
            if (existing != null && existing.vendedorId != sellerId) {
                return Result.failure(Exception("No tienes permisos para eliminar este producto"))
            }

            db.collection("productos").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
