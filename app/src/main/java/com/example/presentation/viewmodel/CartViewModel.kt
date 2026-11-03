package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CarritoItem
import com.example.domain.repository.CartRepository
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface CartEvent {
    data class ShowToast(val message: String) : CartEvent
    data class OrderCompleted(val orderId: String) : CartEvent
}

class CartViewModel(
    private val userId: String,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    val cartItems: StateFlow<List<CarritoItem>> = cartRepository.getCartItems(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isCheckingOut = MutableStateFlow(false)
    val isCheckingOut: StateFlow<Boolean> = _isCheckingOut.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CartEvent>()
    val eventFlow: SharedFlow<CartEvent> = _eventFlow.asSharedFlow()

    fun updateQuantity(item: CarritoItem, delta: Int) {
        viewModelScope.launch {
            val newQty = item.cantidad + delta
            if (newQty <= 0) {
                cartRepository.removeFromCart(userId, item.productoId)
                return@launch
            }

            // Obtener stock actual del producto
            val prod = productRepository.getProductById(item.productoId).getOrNull()
            val available = prod?.cantidad ?: item.cantidad

            if (newQty > available) {
                _eventFlow.emit(CartEvent.ShowToast("No puedes superar el stock disponible ($available)"))
                return@launch
            }

            cartRepository.updateQuantity(userId, item.productoId, newQty, available)
        }
    }

    fun removeItem(productoId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(userId, productoId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart(userId)
        }
    }

    fun checkout() {
        val items = cartItems.value
        if (items.isEmpty()) {
            viewModelScope.launch {
                _eventFlow.emit(CartEvent.ShowToast("El carrito está vacío"))
            }
            return
        }

        viewModelScope.launch {
            _isCheckingOut.value = true
            val result = orderRepository.createOrder(userId, items)
            _isCheckingOut.value = false
            result.fold(
                onSuccess = { orderId ->
                    _eventFlow.emit(CartEvent.OrderCompleted(orderId))
                },
                onFailure = { err ->
                    _eventFlow.emit(CartEvent.ShowToast(err.localizedMessage ?: "Error al procesar pedido."))
                }
            )
        }
    }
}
