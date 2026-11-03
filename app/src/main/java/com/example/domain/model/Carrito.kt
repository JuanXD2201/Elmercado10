package com.example.domain.model

data class Carrito(
    val uid: String = "",
    val fechaActualizacion: Long = System.currentTimeMillis()
)

data class CarritoItem(
    val productoId: String = "",
    val nombreProducto: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val agregadoEn: Long = System.currentTimeMillis(),
    val imagenProducto: String = "",
    val vendedorId: String = ""
) {
    val subtotal: Double
        get() = precioUnitario * cantidad
}
