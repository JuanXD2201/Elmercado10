package com.example.domain.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val tipoUsuario: String = "comprador", // "comprador" o "vendedor"
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fotoPerfil: String = "",
    val telefono: String = ""
)
