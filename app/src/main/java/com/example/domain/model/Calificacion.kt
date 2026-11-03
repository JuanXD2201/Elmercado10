package com.example.domain.model

data class Calificacion(
    val usuarioId: String = "",
    val estrellas: Int = 5, // 1 to 5
    val comentario: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val nombreUsuario: String = ""
)
