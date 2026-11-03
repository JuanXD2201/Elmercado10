package com.example.domain.model

/**
 * Estados válidos de un [Pedido] y sus transiciones permitidas.
 * Las mismas reglas se refuerzan en firestore.rules.
 */
object OrderStatus {
    const val PENDIENTE = "pendiente"
    const val CONFIRMADO = "confirmado"
    const val ENVIADO = "enviado"
    const val ENTREGADO = "entregado"
    const val CANCELADO = "cancelado"

    val all = listOf(PENDIENTE, CONFIRMADO, ENVIADO, ENTREGADO, CANCELADO)

    private val transitions = mapOf(
        PENDIENTE to setOf(CONFIRMADO, CANCELADO),
        CONFIRMADO to setOf(ENVIADO, CANCELADO),
        ENVIADO to setOf(ENTREGADO),
        ENTREGADO to emptySet(),
        CANCELADO to emptySet()
    )

    /** Estados a los que puede pasar un pedido desde [current]. */
    fun nextOptions(current: String): Set<String> = transitions[current].orEmpty()

    fun canTransition(from: String, to: String): Boolean = to in nextOptions(from)
}
