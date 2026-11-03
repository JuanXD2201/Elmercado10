package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Usuario
import com.example.presentation.components.CategoryCard
import com.example.presentation.components.ElmercadoTopBar
import com.example.presentation.components.EmptyState
import com.example.presentation.components.FeatureTrustBanner
import com.example.presentation.components.LoadingState
import com.example.presentation.components.ProductCard
import com.example.presentation.components.SearchBar
import com.example.presentation.components.SectionHeader
import com.example.presentation.components.UserAvatar
import com.example.presentation.viewmodel.HomeViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.BrandGradient
import com.example.ui.theme.HeroGradient
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.PrimaryIndigoDark
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.SuccessEmerald

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    currentUser: Usuario?,
    cartCount: Int,
    homeViewModel: HomeViewModel,
    onNavigateToProduct: (String) -> Unit,
    onNavigateToCatalog: (category: String?) -> Unit,
    onNavigateToCart: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ElmercadoTopBar(
                title = "Elmercado10",
                cartItemCount = cartCount,
                onCartClick = onNavigateToCart
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (uiState.isLoading && uiState.featuredProducts.isEmpty() && uiState.recentProducts.isEmpty()) {
            LoadingState(message = "Cargando catálogo y ofertas...")
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 800.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header de Saludo & Perfil Rápido
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                photoUrl = currentUser?.fotoPerfil,
                                name = currentUser?.nombre ?: "Usuario",
                                size = 48
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "¡Hola, ${currentUser?.nombre?.ifBlank { "Comprador" } ?: "Bienvenido"}!",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (currentUser?.tipoUsuario == "vendedor") MaterialTheme.colorScheme.primaryContainer else SecondaryTeal.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (currentUser?.tipoUsuario == "vendedor") "Modo Vendedor" else "Comprador",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentUser?.tipoUsuario == "vendedor") MaterialTheme.colorScheme.primary else SecondaryTeal,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "¿Qué buscas hoy?",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Buscador interactivo
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        SearchBar(
                            query = "",
                            onQueryChange = { query ->
                                if (query.isNotBlank()) onNavigateToCatalog(null)
                            },
                            placeholder = "Buscar productos, marcas, categorías...",
                            modifier = Modifier.clickable { onNavigateToCatalog(null) }
                        )
                    }

                    // Banner Promocional Curado de Lujo
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(24.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "AUTENTICIDAD GARANTIZADA",
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = AccentCoral.copy(alpha = 0.85f)
                                    ) {
                                        Text(
                                            text = "ENVÍO EXPRESS",
                                            color = Color.White,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "La nueva era de compras de confianza",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Normal,
                                        letterSpacing = (-0.2).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Una experiencia de marketplace blindada con verificación estricta, chat en tiempo real y envíos prioritarios.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    onClick = { onNavigateToCatalog(null) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Explorar Catálogo",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.2.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }


                    // Categorías
                    SectionHeader(
                        title = "Categorías",
                        subtitle = "Explora por departamento",
                        actionText = "Ver todas",
                        onActionClick = { onNavigateToCatalog(null) }
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.categories) { category ->
                            val icon: ImageVector = when (category) {
                                "Tecnología" -> Icons.Default.Devices
                                "Hogar" -> Icons.Default.Home
                                "Moda" -> Icons.Default.ShoppingBag
                                "Deportes" -> Icons.Default.FitnessCenter
                                "Vehículos" -> Icons.Default.DirectionsCar
                                "Juguetes" -> Icons.Default.Toys
                                else -> Icons.Default.Category
                            }

                            CategoryCard(
                                title = category,
                                icon = icon,
                                onClick = { onNavigateToCatalog(category) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Productos Destacados
                    if (uiState.featuredProducts.isNotEmpty()) {
                        SectionHeader(
                            title = "Productos Destacados",
                            subtitle = "Los mejor calificados por la comunidad",
                            actionText = "Ver todos",
                            onActionClick = { onNavigateToCatalog(null) }
                        )

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.featuredProducts) { producto ->
                                ProductCard(
                                    producto = producto,
                                    onClick = { onNavigateToProduct(producto.id) },
                                    modifier = Modifier.width(210.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Productos Recientes
                    SectionHeader(
                        title = "Recién Publicados",
                        subtitle = "Novedades agregadas recientemente",
                        actionText = "Ver catálogo",
                        onActionClick = { onNavigateToCatalog(null) }
                    )

                    if (uiState.recentProducts.isEmpty()) {
                        EmptyState(
                            title = "No hay productos aún",
                            description = "Sé el primer vendedor en publicar un producto en Elmercado10.",
                            actionText = "Explorar catálogo",
                            onAction = { onNavigateToCatalog(null) }
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.recentProducts) { producto ->
                                ProductCard(
                                    producto = producto,
                                    onClick = { onNavigateToProduct(producto.id) },
                                    modifier = Modifier.width(210.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Beneficios / Trust Badges
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        FeatureTrustBanner()
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
