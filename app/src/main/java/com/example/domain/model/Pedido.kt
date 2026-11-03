package com.example.domain.model

data class PedidoItem(
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0,
    val vendedorId: String = "",
    val imagenProducto: String = ""
)

data class Pedido(
    val id: String = "",
    val usuarioId: String = "",
    val items: List<PedidoItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "pendiente", // "pendiente", "confirmado", "enviado", "entregado", "cancelado"
    val createdAt: Long = System.currentTimeMillis(),
    val vendedorIds: List<String> = emptyList()
)
