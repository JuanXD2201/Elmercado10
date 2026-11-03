package com.example.domain.repository

import com.example.domain.model.CarritoItem
import com.example.domain.model.Producto
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(userId: String): Flow<List<CarritoItem>>
    suspend fun addToCart(userId: String, producto: Producto, cantidad: Int): Result<Unit>
    suspend fun updateQuantity(userId: String, productoId: String, newQuantity: Int, availableStock: Int): Result<Unit>
    suspend fun removeFromCart(userId: String, productoId: String): Result<Unit>
    suspend fun clearCart(userId: String): Result<Unit>
}
