package com.example.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Producto
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.StorageRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SellerProductsViewModel(
    private val sellerId: String,
    private val productRepository: ProductRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    val myProducts: StateFlow<List<Producto>> = productRepository.getProductsBySeller(sellerId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _eventFlow = MutableSharedFlow<String>()
    val eventFlow: SharedFlow<String> = _eventFlow.asSharedFlow()

    fun saveProduct(
        id: String?,
        nombre: String,
        descripcion: String,
        precio: Double,
        categoria: String,
        estado: String,
        cantidad: Int,
        imageUri: Uri?,
        existingImageUrl: String
    ) {
        if (nombre.isBlank() || descripcion.isBlank() || precio <= 0 || cantidad < 0) {
            viewModelScope.launch {
                _eventFlow.emit("Por favor completa los campos con valores válidos.")
            }
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            val productId = id ?: UUID.randomUUID().toString()
            var finalImageUrl = existingImageUrl

            if (imageUri != null) {
                val uploadResult = storageRepository.uploadProductImage(sellerId, productId, imageUri)
                if (uploadResult.isSuccess) {
                    finalImageUrl = uploadResult.getOrNull() ?: existingImageUrl
                } else {
                    _eventFlow.emit("Advertencia: No se pudo subir imagen a Storage: ${uploadResult.exceptionOrNull()?.message}")
                }
            }

            val product = Producto(
                id = productId,
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                precio = precio,
                categoria = categoria,
                estado = estado,
                cantidad = cantidad,
                imagen = finalImageUrl,
                vendedorId = sellerId
            )

            val result = if (id.isNullOrBlank()) {
                productRepository.createProduct(product)
            } else {
                productRepository.updateProduct(product)
            }

            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _eventFlow.emit("Producto guardado exitosamente.")
                },
                onFailure = { err ->
                    _eventFlow.emit(err.localizedMessage ?: "Error al guardar el producto.")
                }
            )
        }
    }

    fun deleteProduct(productId: String, imageUrl: String?) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = productRepository.deleteProduct(productId, sellerId, imageUrl)
            _isSaving.value = false
            result.fold(
                onSuccess = {
                    _eventFlow.emit("Producto eliminado.")
                },
                onFailure = { err ->
                    _eventFlow.emit(err.localizedMessage ?: "Error al eliminar producto.")
                }
            )
        }
    }
}
