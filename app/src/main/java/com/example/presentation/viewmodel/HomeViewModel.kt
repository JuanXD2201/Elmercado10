package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Producto
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val featuredProducts: List<Producto> = emptyList(),
    val recentProducts: List<Producto> = emptyList(),
    val categories: List<String> = listOf("Tecnología", "Hogar", "Moda", "Deportes", "Vehículos", "Juguetes"),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                productRepository.getProducts().collect { products ->
                    val featured = products.filter { it.calificacionPromedio >= 4.0 || it.numCalificaciones > 0 }
                        .ifEmpty { products.take(6) }
                    val recent = products.sortedByDescending { it.fechaPublicacion }.take(10)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        featuredProducts = featured,
                        recentProducts = recent,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "Error al cargar productos del catálogo"
                )
            }
        }
    }
}
