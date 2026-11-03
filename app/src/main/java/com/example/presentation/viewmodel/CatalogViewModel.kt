package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Producto
import com.example.domain.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class SortOption(val label: String) {
    RECENT("Más recientes"),
    PRICE_ASC("Menor precio"),
    PRICE_DESC("Mayor precio"),
    RATING_DESC("Mejor calificados")
}

data class CatalogFilterState(
    val searchQuery: String = "",
    val selectedCategory: String = "Todos",
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val condition: String = "todos", // "todos", "nuevo", "usado"
    val minRating: Int = 0,
    val sortOption: SortOption = SortOption.RECENT
)

class CatalogViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    val categories = listOf("Todos", "Tecnología", "Hogar", "Moda", "Deportes", "Vehículos", "Juguetes")

    private val _filterState = MutableStateFlow(CatalogFilterState())
    val filterState: StateFlow<CatalogFilterState> = _filterState.asStateFlow()

    val products: StateFlow<List<Producto>> = combine(
        productRepository.getProducts(),
        _filterState
    ) { allProducts, filters ->
        allProducts.filter { product ->
            // Búsqueda por texto (nombre, descripción, categoría)
            val matchesQuery = filters.searchQuery.isBlank() ||
                product.nombre.contains(filters.searchQuery, ignoreCase = true) ||
                product.descripcion.contains(filters.searchQuery, ignoreCase = true) ||
                product.categoria.contains(filters.searchQuery, ignoreCase = true)

            // Filtro por categoría
            val matchesCategory = filters.selectedCategory == "Todos" ||
                product.categoria.equals(filters.selectedCategory, ignoreCase = true)

            // Filtro por precio
            val matchesMinPrice = filters.minPrice == null || product.precio >= filters.minPrice
            val matchesMaxPrice = filters.maxPrice == null || product.precio <= filters.maxPrice

            // Filtro por estado
            val matchesCondition = filters.condition == "todos" ||
                product.estado.equals(filters.condition, ignoreCase = true)

            // Filtro por calificación
            val matchesRating = product.calificacionPromedio >= filters.minRating

            matchesQuery && matchesCategory && matchesMinPrice && matchesMaxPrice && matchesCondition && matchesRating
        }.let { list ->
            when (filters.sortOption) {
                SortOption.RECENT -> list.sortedByDescending { it.fechaPublicacion }
                SortOption.PRICE_ASC -> list.sortedBy { it.precio }
                SortOption.PRICE_DESC -> list.sortedByDescending { it.precio }
                SortOption.RATING_DESC -> list.sortedByDescending { it.calificacionPromedio }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun onCategorySelected(category: String) {
        _filterState.value = _filterState.value.copy(selectedCategory = category)
    }

    fun onConditionSelected(condition: String) {
        _filterState.value = _filterState.value.copy(condition = condition)
    }

    fun onPriceRangeChanged(min: Double?, max: Double?) {
        _filterState.value = _filterState.value.copy(minPrice = min, maxPrice = max)
    }

    fun onMinRatingChanged(rating: Int) {
        _filterState.value = _filterState.value.copy(minRating = rating)
    }

    fun onSortOptionChanged(sort: SortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sort)
    }

    fun resetFilters() {
        _filterState.value = CatalogFilterState()
    }
}
