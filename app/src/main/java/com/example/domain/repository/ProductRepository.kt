package com.example.domain.repository

import com.example.domain.model.Producto
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getProducts(): Flow<List<Producto>>
    fun getProductsBySeller(sellerId: String): Flow<List<Producto>>
    suspend fun getProductById(id: String): Result<Producto?>
    fun getProductFlow(id: String): Flow<Producto?>
    suspend fun createProduct(producto: Producto): Result<String>
    suspend fun updateProduct(producto: Producto): Result<Unit>
    suspend fun deleteProduct(productId: String, sellerId: String, imageUrl: String? = null): Result<Unit>
}
