package com.example.domain.repository

import com.example.domain.model.CarritoItem
import com.example.domain.model.Pedido
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun createOrder(userId: String, cartItems: List<CarritoItem>): Result<String>
    fun getUserOrders(userId: String): Flow<List<Pedido>>
    fun getSellerOrders(sellerId: String): Flow<List<Pedido>>
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit>
}
