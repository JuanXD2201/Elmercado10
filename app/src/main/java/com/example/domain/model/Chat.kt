package com.example.domain.model

data class Chat(
    val id: String = "",
    val participantes: List<String> = emptyList(),
    val productoId: String = "",
    val nombreProducto: String = "",
    val ultimoMensaje: String = "",
    val ultimaActividad: Long = System.currentTimeMillis(),
    val compradorId: String = "",
    val vendedorId: String = ""
)

data class Mensaje(
    val id: String = "",
    val emisorId: String = "",
    val texto: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val leido: Boolean = false
)
