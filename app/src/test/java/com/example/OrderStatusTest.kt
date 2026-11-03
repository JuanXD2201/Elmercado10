package com.example

import com.example.domain.model.OrderStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderStatusTest {

    @Test
    fun pendiente_puedeConfirmarseOCancelarse() {
        assertEquals(setOf("confirmado", "cancelado"), OrderStatus.nextOptions("pendiente"))
    }

    @Test
    fun flujoNormal_hastaEntregado() {
        assertTrue(OrderStatus.canTransition("pendiente", "confirmado"))
        assertTrue(OrderStatus.canTransition("confirmado", "enviado"))
        assertTrue(OrderStatus.canTransition("enviado", "entregado"))
    }

    @Test
    fun noSePermiteSaltarEstados() {
        assertFalse(OrderStatus.canTransition("pendiente", "entregado"))
        assertFalse(OrderStatus.canTransition("pendiente", "enviado"))
    }

    @Test
    fun enviado_noPuedeCancelarse() {
        assertFalse(OrderStatus.canTransition("enviado", "cancelado"))
    }

    @Test
    fun estadosFinales_noTienenSalida() {
        assertTrue(OrderStatus.nextOptions("entregado").isEmpty())
        assertTrue(OrderStatus.nextOptions("cancelado").isEmpty())
    }

    @Test
    fun estadoDesconocido_noTieneTransiciones() {
        assertTrue(OrderStatus.nextOptions("inventado").isEmpty())
        assertFalse(OrderStatus.canTransition("inventado", "entregado"))
    }

    @Test
    fun noSePermiteRegresar() {
        assertFalse(OrderStatus.canTransition("confirmado", "pendiente"))
        assertFalse(OrderStatus.canTransition("entregado", "enviado"))
    }
}
