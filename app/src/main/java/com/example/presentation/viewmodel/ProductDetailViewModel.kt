package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Calificacion
import com.example.domain.model.Producto
import com.example.domain.model.Usuario
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.RatingRepository
import com.example.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ProductDetailEvent {
    data class ShowToast(val message: String) : ProductDetailEvent
    data class NavigateToChat(val chatId: String) : ProductDetailEvent
    data object NavigateToCart : ProductDetailEvent
}

class ProductDetailViewModel(
    private val productId: String,
    private val productRepository: ProductRepository,
    private val ratingRepository: RatingRepository,
    private val cartRepository: CartRepository,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    val product: StateFlow<Producto?> = productRepository.getProductFlow(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val ratings: StateFlow<List<Calificacion>> = ratingRepository.getRatings(productId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _seller = MutableStateFlow<Usuario?>(null)
    val seller: StateFlow<Usuario?> = _seller.asStateFlow()

    private val _eventFlow = MutableSharedFlow<ProductDetailEvent>()
    val eventFlow: SharedFlow<ProductDetailEvent> = _eventFlow.asSharedFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    init {
        loadSellerInfo()
    }

    private fun loadSellerInfo() {
        viewModelScope.launch {
            product.collect { prod ->
                if (prod != null && prod.vendedorId.isNotBlank()) {
                    if (_seller.value?.uid != prod.vendedorId) {
                        userRepository.getUser(prod.vendedorId).onSuccess { user ->
                            _seller.value = user
                        }
                    }
                }
            }
        }
    }

    fun addToCart(userId: String, quantity: Int = 1) {
        val currentProduct = product.value ?: return
        if (currentProduct.cantidad <= 0) {
            viewModelScope.launch {
                _eventFlow.emit(ProductDetailEvent.ShowToast("Este producto está agotado."))
            }
            return
        }

        viewModelScope.launch {
            _isActionLoading.value = true
            val result = cartRepository.addToCart(userId, currentProduct, quantity)
            _isActionLoading.value = false
            result.fold(
                onSuccess = {
                    _eventFlow.emit(ProductDetailEvent.ShowToast("¡Producto agregado al carrito!"))
                },
                onFailure = { err ->
                    _eventFlow.emit(ProductDetailEvent.ShowToast(err.localizedMessage ?: "Error al agregar al carrito"))
                }
            )
        }
    }

    fun buyNow(userId: String) {
        val currentProduct = product.value ?: return
        if (currentProduct.cantidad <= 0) {
            viewModelScope.launch {
                _eventFlow.emit(ProductDetailEvent.ShowToast("Este producto está agotado."))
            }
            return
        }

        viewModelScope.launch {
            _isActionLoading.value = true
            val result = cartRepository.addToCart(userId, currentProduct, 1)
            _isActionLoading.value = false
            result.fold(
                onSuccess = {
                    _eventFlow.emit(ProductDetailEvent.NavigateToCart)
                },
                onFailure = { err ->
                    _eventFlow.emit(ProductDetailEvent.ShowToast(err.localizedMessage ?: "Error al procesar compra"))
                }
            )
        }
    }

    fun contactSeller(buyerId: String) {
        val currentProduct = product.value ?: return
        if (buyerId == currentProduct.vendedorId) {
            viewModelScope.launch {
                _eventFlow.emit(ProductDetailEvent.ShowToast("No puedes chatear contigo mismo en tu propio producto."))
            }
            return
        }

        viewModelScope.launch {
            _isActionLoading.value = true
            val result = chatRepository.getOrCreateChat(
                buyerId = buyerId,
                sellerId = currentProduct.vendedorId,
                productoId = currentProduct.id,
                productName = currentProduct.nombre
            )
            _isActionLoading.value = false
            result.fold(
                onSuccess = { chatId ->
                    _eventFlow.emit(ProductDetailEvent.NavigateToChat(chatId))
                },
                onFailure = { err ->
                    _eventFlow.emit(ProductDetailEvent.ShowToast(err.localizedMessage ?: "Error al abrir chat con el vendedor"))
                }
            )
        }
    }
}
