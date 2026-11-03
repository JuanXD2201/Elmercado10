package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.domain.model.CarritoItem
import com.example.domain.model.Pedido
import com.example.domain.model.PedidoItem
import com.example.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseOrderRepository(
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : OrderRepository {

    override suspend fun createOrder(userId: String, cartItems: List<CarritoItem>): Result<String> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        if (cartItems.isEmpty()) {
            return Result.failure(IllegalArgumentException("El carrito está vacío."))
        }

        return try {
            val orderRef = db.collection("pedidos").document()
            val orderId = orderRef.id

            db.runTransaction { transaction ->
                var total = 0.0
                val sellerSet = mutableSetOf<String>()
                val pedidoItems = mutableListOf<PedidoItem>()

                // 1. TODAS LAS LECTURAS PRIMERO (Requisito estricto de Firestore)
                val itemsWithSnapshots = cartItems.map { item ->
                    val productRef = db.collection("productos").document(item.productoId)
                    val productSnap = transaction.get(productRef)
                    Triple(item, productRef, productSnap)
                }

                // 2. VALIDAR Y CALCULAR
                val updatesToApply = mutableListOf<Pair<com.google.firebase.firestore.DocumentReference, Int>>()
                for ((item, productRef, productSnap) in itemsWithSnapshots) {
                    if (!productSnap.exists()) {
                        throw IllegalStateException("El producto '${item.nombreProducto}' ya no está disponible.")
                    }

                    val currentStock = productSnap.getLong("cantidad")?.toInt() ?: 0
                    if (currentStock < item.cantidad) {
                        throw IllegalStateException("Stock insuficiente para '${item.nombreProducto}'. Disponible: $currentStock.")
                    }

                    val newStock = currentStock - item.cantidad
                    updatesToApply.add(productRef to newStock)

                    val subtotal = item.precioUnitario * item.cantidad
                    total += subtotal
                    if (item.vendedorId.isNotBlank()) {
                        sellerSet.add(item.vendedorId)
                    }

                    pedidoItems.add(
                        PedidoItem(
                            productoId = item.productoId,
                            nombreProducto = item.nombreProducto,
                            cantidad = item.cantidad,
                            precioUnitario = item.precioUnitario,
                            subtotal = subtotal,
                            vendedorId = item.vendedorId,
                            imagenProducto = item.imagenProducto
                        )
                    )
                }

                // 3. TODAS LAS ESCRITURAS DESPUÉS
                // Actualizar inventario de productos
                for ((productRef, newStock) in updatesToApply) {
                    transaction.update(productRef, "cantidad", newStock)
                }

                // Crear pedido histórico inmutable
                val nuevoPedido = Pedido(
                    id = orderId,
                    usuarioId = userId,
                    items = pedidoItems,
                    totalAmount = total,
                    status = "pendiente",
                    createdAt = System.currentTimeMillis(),
                    vendedorIds = sellerSet.toList()
                )
                transaction.set(orderRef, nuevoPedido)

                // Vaciar items del carrito del usuario
                for (item in cartItems) {
                    val cartItemRef = db.collection("carritos").document(userId)
                        .collection("items").document(item.productoId)
                    transaction.delete(cartItemRef)
                }
            }.await()

            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserOrders(userId: String): Flow<List<Pedido>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("pedidos")
            .whereEqualTo("usuarioId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Pedido::class.java)
                    trySend(orders.sortedByDescending { it.createdAt })
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override fun getSellerOrders(sellerId: String): Flow<List<Pedido>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("pedidos")
            .whereArrayContains("vendedorIds", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.toObjects(Pedido::class.java)
                    trySend(orders.sortedByDescending { it.createdAt })
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            db.collection("pedidos").document(orderId).update("status", newStatus).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
