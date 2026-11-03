package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Pedido
import com.example.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderViewModel(
    private val userId: String,
    private val isSeller: Boolean,
    private val orderRepository: OrderRepository
) : ViewModel() {

    val orders: StateFlow<List<Pedido>> = (if (isSeller) {
        orderRepository.getSellerOrders(userId)
    } else {
        orderRepository.getUserOrders(userId)
    }).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    fun updateStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _isUpdating.value = true
            orderRepository.updateOrderStatus(orderId, newStatus)
            _isUpdating.value = false
        }
    }
}
