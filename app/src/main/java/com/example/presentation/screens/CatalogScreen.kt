package com.example.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.CategoryChip
import com.example.presentation.components.ElmercadoTopBar
import com.example.presentation.components.EmptyState
import com.example.presentation.components.InteractiveRatingBar
import com.example.presentation.components.ProductCard
import com.example.presentation.components.SearchBar
import com.example.presentation.viewmodel.CatalogViewModel
import com.example.presentation.viewmodel.SortOption
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    catalogViewModel: CatalogViewModel,
    cartCount: Int,
    initialCategory: String? = null,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val filterState by catalogViewModel.filterState.collectAsState()
    val products by catalogViewModel.products.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    remember(initialCategory) {
        if (!initialCategory.isNullOrBlank()) {
            catalogViewModel.onCategorySelected(initialCategory)
        }
        true
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            currentMinPrice = filterState.minPrice?.toString() ?: "",
            currentMaxPrice = filterState.maxPrice?.toString() ?: "",
            currentCondition = filterState.condition,
            currentMinRating = filterState.minRating,
            onApply = { min, max, cond, rating ->
                catalogViewModel.onPriceRangeChanged(min.toDoubleOrNull(), max.toDoubleOrNull())
                catalogViewModel.onConditionSelected(cond)
                catalogViewModel.onMinRatingChanged(rating)
                showFilterSheet = false
            },
            onReset = {
                catalogViewModel.resetFilters()
                showFilterSheet = false
            },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        topBar = {
            ElmercadoTopBar(
                title = "Catálogo de Productos",
                cartItemCount = cartCount,
                onCartClick = onNavigateToCart,
                actions = {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (filterState.sortOption == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    catalogViewModel.onSortOptionChanged(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Buscador y botón de filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = filterState.searchQuery,
                    onQueryChange = { catalogViewModel.onSearchQueryChanged(it) },
                    placeholder = "Buscar por nombre o descripción...",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                val isFilterActive = filterState.condition != "todos" || filterState.minPrice != null || filterState.minRating > 0
                Surface(
                    onClick = { showFilterSheet = true },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("open_filter_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtros",
                            tint = if (isFilterActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Chips de Categoría con scroll suave
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                catalogViewModel.categories.forEach { cat ->
                    CategoryChip(
                        category = cat,
                        isSelected = filterState.selectedCategory.equals(cat, ignoreCase = true),
                        onSelected = { catalogViewModel.onCategorySelected(cat) }
                    )
                }
            }

            // Indicador de filtros aplicados
            val activeFiltersCount = (if (filterState.condition != "todos") 1 else 0) +
                (if (filterState.minPrice != null || filterState.maxPrice != null) 1 else 0) +
                (if (filterState.minRating > 0) 1 else 0)

            if (activeFiltersCount > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$activeFiltersCount filtro(s) activo(s) • ${filterState.sortOption.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    TextButton(onClick = { catalogViewModel.resetFilters() }) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Grid Adaptativo de Productos (responsive)
            if (products.isEmpty()) {
                EmptyState(
                    title = "No se encontraron productos",
                    description = "Intenta modificar tu búsqueda o elimina los filtros para ver más resultados.",
                    actionText = "Restablecer filtros",
                    onAction = { catalogViewModel.resetFilters() }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 165.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(products, key = { it.id }) { producto ->
                        ProductCard(
                            producto = producto,
                            onClick = { onNavigateToProduct(producto.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentMinPrice: String,
    currentMaxPrice: String,
    currentCondition: String,
    currentMinRating: Int,
    onApply: (minPrice: String, maxPrice: String, condition: String, minRating: Int) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var minPrice by remember { mutableStateOf(currentMinPrice) }
    var maxPrice by remember { mutableStateOf(currentMaxPrice) }
    var condition by remember { mutableStateOf(currentCondition) }
    var minRating by remember { mutableStateOf(currentMinRating) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Filtros de Búsqueda",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Estado: Nuevo / Usado / Todos
            Text(
                text = "Condición del producto",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("todos" to "Todos", "nuevo" to "Nuevo", "usado" to "Usado").forEach { (key, label) ->
                    FilterChip(
                        selected = condition == key,
                        onClick = { condition = key },
                        label = { Text(label, fontWeight = if (condition == key) FontWeight.Bold else FontWeight.Normal) },
                        shape = RoundedCornerShape(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Rango de Precios
            Text(
                text = "Rango de Precios ($)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minPrice,
                    onValueChange = { minPrice = it },
                    label = { Text("Mínimo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = maxPrice,
                    onValueChange = { maxPrice = it },
                    label = { Text("Máximo") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Calificación Mínima
            Text(
                text = "Calificación mínima",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            InteractiveRatingBar(
                rating = minRating,
                onRatingChange = { minRating = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReset,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Limpiar", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        onApply(minPrice, maxPrice, condition, minRating)
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("Aplicar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
