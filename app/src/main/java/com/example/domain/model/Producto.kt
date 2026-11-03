package com.example.domain.model

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "Tecnología",
    val estado: String = "nuevo", // "nuevo" o "usado"
    val cantidad: Int = 1,
    val imagen: String = "",
    val vendedorId: String = "",
    val calificacionPromedio: Double = 0.0,
    val numCalificaciones: Int = 0,
    val fechaPublicacion: Long = System.currentTimeMillis()
)
