package com.example.presentation.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Producto
import com.example.presentation.components.ConfirmationDialog
import com.example.presentation.components.ElmercadoTopBar
import com.example.presentation.components.EmptyState
import com.example.presentation.components.ImagePickerBottomSheet
import com.example.presentation.components.PrimaryButton
import com.example.presentation.components.RatingStars
import com.example.presentation.viewmodel.SellerProductsViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ErrorRose
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerPanelScreen(
    sellerViewModel: SellerProductsViewModel,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToEditProduct: (Producto) -> Unit
) {
    val products by sellerViewModel.myProducts.collectAsState()
    var productToDelete by remember { mutableStateOf<Producto?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sellerViewModel.eventFlow) {
        sellerViewModel.eventFlow.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (productToDelete != null) {
        ConfirmationDialog(
            title = "Eliminar Producto",
            message = "¿Estás seguro de que deseas eliminar '${productToDelete?.nombre}'? Esta acción eliminará el registro y la fotografía asociada.",
            confirmText = "Eliminar",
            onConfirm = {
                productToDelete?.let { prod ->
                    sellerViewModel.deleteProduct(prod.id, prod.imagen.takeIf { it.isNotBlank() })
                }
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }

    Scaffold(
        topBar = {
            ElmercadoTopBar(title = "Mis Productos a la Venta")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Publicar Producto")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (products.isEmpty()) {
            EmptyState(
                title = "No has publicado productos",
                description = "Comienza a vender en Elmercado10 publicando tu primer artículo.",
                icon = Icons.Default.Inventory,
                actionText = "Publicar Primer Producto",
                onAction = onNavigateToAddProduct,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                ) {
                    item {
                        // Dashboard Summary Card
                        val totalInventoryValue = products.sumOf { it.precio * it.cantidad }
                        val inStockCount = products.count { it.cantidad > 0 }

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Resumen de Inventario",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Publicaciones", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${products.size}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Column {
                                        Text("En Stock", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$inStockCount",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = SuccessEmerald
                                        )
                                    }
                                    Column {
                                        Text("Valor Inventario", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "$${String.format(java.util.Locale.getDefault(), "%,.2f", totalInventoryValue)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    items(products, key = { it.id }) { producto ->
                        SellerProductRow(
                            producto = producto,
                            onEdit = { onNavigateToEditProduct(producto) },
                            onDelete = { productToDelete = producto }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SellerProductRow(
    producto: Producto,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("seller_product_card_${producto.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (producto.imagen.isNotBlank()) {
                    AsyncImage(
                        model = producto.imagen,
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(imageVector = Icons.Default.Inbox, contentDescription = null, tint = Slate400)
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = producto.categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$${String.format(java.util.Locale.getDefault(), "%,.2f", producto.precio)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Stock: ${producto.cantidad}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (producto.cantidad > 0) MaterialTheme.colorScheme.onSurfaceVariant else ErrorRose,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (producto.estado.equals("nuevo", ignoreCase = true)) SuccessEmerald.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = producto.estado.replaceFirstChar { it.uppercase() },
                            color = if (producto.estado.equals("nuevo", ignoreCase = true)) SuccessEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_product_${producto.id}")) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_product_${producto.id}")) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar", tint = ErrorRose)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductScreen(
    sellerViewModel: SellerProductsViewModel,
    productToEdit: Producto? = null,
    onNavigateBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(productToEdit?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(productToEdit?.descripcion ?: "") }
    var precioStr by remember { mutableStateOf(productToEdit?.precio?.toString() ?: "") }
    var categoria by remember { mutableStateOf(productToEdit?.categoria ?: "Tecnología") }
    var estado by remember { mutableStateOf(productToEdit?.estado ?: "nuevo") }
    var cantidadStr by remember { mutableStateOf(productToEdit?.cantidad?.toString() ?: "1") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrlInput by remember { mutableStateOf(productToEdit?.imagen ?: "") }
    var showImagePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val categories = listOf("Tecnología", "Hogar", "Moda", "Deportes", "Vehículos", "Juguetes", "Otros")
    val isSaving by sellerViewModel.isSaving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sellerViewModel.eventFlow) {
        sellerViewModel.eventFlow.collect { msg ->
            snackbarHostState.showSnackbar(msg)
            if (msg.contains("guardado", ignoreCase = true) || msg.contains("exitosamente", ignoreCase = true)) {
                onNavigateBack()
            }
        }
    }

    if (showImagePicker) {
        ImagePickerBottomSheet(
            onDismiss = { showImagePicker = false },
            onImageSelected = { uri ->
                selectedImageUri = uri
            }
        )
    }

    Scaffold(
        topBar = {
            ElmercadoTopBar(
                title = if (productToEdit == null) "Publicar Producto" else "Editar Producto",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = imageUrlInput,
                    onValueChange = { imageUrlInput = it },
                    label = { Text("URL de la imagen externa (Opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Selector de Fotografía con CameraX y Galería
                Text(
                    text = "Fotografía del Producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    onClick = { showImagePicker = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .testTag("product_image_picker_card")
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            selectedImageUri != null -> {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Foto seleccionada",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            !imageUrlInput.isBlank() -> {
                                AsyncImage(
                                    model = imageUrlInput,
                                    contentDescription = "Foto actual o externa",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Cámara",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Toca para tomar foto con CameraX o elegir de galería", fontWeight = FontWeight.SemiBold)
                                    Text("Soporta cámara nativa y almacenamiento local", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = imageUrlInput,
                    onValueChange = { imageUrlInput = it },
                    label = { Text("URL de la imagen externa (Opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Nombre
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Producto") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_product_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción detallada") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_product_description")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Precio y Stock
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = precioStr,
                        onValueChange = { precioStr = it },
                        label = { Text("Precio ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("input_product_price")
                    )
                    OutlinedTextField(
                        value = cantidadStr,
                        onValueChange = { cantidadStr = it },
                        label = { Text("Stock disponible") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).testTag("input_product_stock")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Selector de Categoría
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .testTag("input_product_category")
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    categoria = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = imageUrlInput,
                    onValueChange = { imageUrlInput = it },
                    label = { Text("URL de la imagen externa (Opcional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Estado: Nuevo vs Usado
                Text(text = "Condición:", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FilterChip(
                        selected = estado == "nuevo",
                        onClick = { estado = "nuevo" },
                        label = { Text("Nuevo") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = estado == "usado",
                        onClick = { estado = "usado" },
                        label = { Text("Usado") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                PrimaryButton(
                    text = if (productToEdit == null) "Publicar Producto" else "Guardar Cambios",
                    isLoading = isSaving,
                    onClick = {
                        val precio = precioStr.toDoubleOrNull() ?: 0.0
                        val cantidad = cantidadStr.toIntOrNull() ?: 0

                        sellerViewModel.saveProduct(
                            id = productToEdit?.id,
                            nombre = nombre,
                            descripcion = descripcion,
                            precio = precio,
                            categoria = categoria,
                            estado = estado,
                            cantidad = cantidad,
                            imageUri = selectedImageUri,
                            existingImageUrl = imageUrlInput
                        )
                    },
                    testTag = "submit_product_button"
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
