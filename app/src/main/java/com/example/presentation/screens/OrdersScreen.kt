package com.example.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Pedido
import com.example.domain.model.Usuario
import com.example.presentation.components.ElmercadoTopBar
import com.example.presentation.components.EmptyState
import com.example.presentation.components.OrderProgressTracker
import com.example.presentation.components.OrderStatusBadge
import com.example.presentation.viewmodel.OrderViewModel
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    currentUser: Usuario?,
    orderViewModel: OrderViewModel,
    cartCount: Int,
    onNavigateBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onExploreCatalog: () -> Unit
) {
    val orders by orderViewModel.orders.collectAsState()
    val isSeller = currentUser?.tipoUsuario == "vendedor"

    Scaffold(
        topBar = {
            ElmercadoTopBar(
                title = if (isSeller) "Pedidos de Clientes" else "Mis Pedidos",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                cartItemCount = cartCount,
                onCartClick = onNavigateToCart
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (orders.isEmpty()) {
            EmptyState(
                title = "No hay pedidos registrados",
                description = if (isSeller) {
                    "Aún no has recibido pedidos para tus productos publicados."
                } else {
                    "Aquí podrás dar seguimiento al estado y avance de todas tus compras realizadas."
                },
                icon = Icons.Default.LocalShipping,
                actionText = if (!isSeller) "Explorar Catálogo" else null,
                onAction = if (!isSeller) onExploreCatalog else null,
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
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxSize()
                ) {
                    items(orders, key = { it.id }) { pedido ->
                        OrderCard(
                            pedido = pedido,
                            isSeller = isSeller,
                            onUpdateStatus = { newStatus ->
                                orderViewModel.updateStatus(pedido.id, newStatus)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    pedido: Pedido,
    isSeller: Boolean,
    onUpdateStatus: (String) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    val statuses = com.example.domain.model.OrderStatus.nextOptions(pedido.status).toList()

    val dateFormatted = SimpleDateFormat("dd/MM/yyyy • hh:mm a", Locale.getDefault())
        .format(Date(pedido.createdAt))

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${pedido.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header del Pedido
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Orden #${pedido.id.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OrderStatusBadge(status = pedido.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual tracking timeline
            OrderProgressTracker(status = pedido.status)

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Items del Pedido
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pedido.items.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.imagenProducto.isNotBlank()) {
                                AsyncImage(
                                    model = item.imagenProducto,
                                    contentDescription = item.nombreProducto,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(imageVector = Icons.Default.Inbox, contentDescription = null, tint = Slate400, modifier = Modifier.size(22.dp))
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.nombreProducto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.cantidad} x $${String.format(java.util.Locale.getDefault(), "%,.2f", item.precioUnitario)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = "$${String.format(java.util.Locale.getDefault(), "%,.2f", item.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Total y acciones
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total Pagado:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format(java.util.Locale.getDefault(), "%,.2f", pedido.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Si es vendedor, permitir cambiar estado
            if (isSeller && statuses.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { showStatusMenu = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cambiar Estado: ${pedido.status.replaceFirstChar { it.uppercase() }}", fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false }
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st.replaceFirstChar { it.uppercase() }, fontWeight = if (pedido.status == st) FontWeight.Bold else FontWeight.Normal) },
                                    onClick = {
                                        onUpdateStatus(st)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
