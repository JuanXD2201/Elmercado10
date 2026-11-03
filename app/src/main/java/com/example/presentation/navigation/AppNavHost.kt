package com.example.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Build
import android.content.Context
import android.content.Intent
import com.example.presentation.notification.AppNotificationHelper
import com.example.presentation.notification.AppNotificationService
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.model.Producto
import com.example.presentation.screens.AddEditProductScreen
import com.example.presentation.screens.CartScreen
import com.example.presentation.screens.CatalogScreen
import com.example.presentation.screens.ChatDetailScreen
import com.example.presentation.screens.ChatListScreen
import com.example.presentation.screens.HomeScreen
import com.example.presentation.screens.LoginScreen
import com.example.presentation.screens.OrdersScreen
import com.example.presentation.screens.ProductDetailScreen
import com.example.presentation.screens.ProfileScreen
import com.example.presentation.screens.RatingBottomSheet
import com.example.presentation.screens.RegisterScreen
import com.example.presentation.screens.SellerPanelScreen
import com.example.presentation.screens.SplashScreen
import com.example.presentation.viewmodel.AppContainer
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.CartViewModel
import com.example.presentation.viewmodel.CatalogViewModel
import com.example.presentation.viewmodel.ChatViewModel
import com.example.presentation.viewmodel.HomeViewModel
import com.example.presentation.viewmodel.OrderViewModel
import com.example.presentation.viewmodel.ProductDetailViewModel
import com.example.presentation.viewmodel.ProfileViewModel
import com.example.presentation.viewmodel.RatingViewModel
import com.example.presentation.viewmodel.SellerProductsViewModel
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.PrimaryBlue

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val authViewModel = navViewModel { AuthViewModel(AppContainer.authRepository) }
    val currentUser by authViewModel.currentUser.collectAsState()

    // Carrito del usuario actual
    val cartFlow = remember(currentUser?.uid) {
        currentUser?.uid?.let { AppContainer.cartRepository.getCartItems(it) }
            ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }
    val cartItems by cartFlow.collectAsState(initial = emptyList())
    val cartCount = cartItems.sumOf { it.cantidad }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // Estado para editar producto en vendedor
    var editingProduct by remember { mutableStateOf<Producto?>(null) }

    // Estado para calificar producto
    var ratingProductTarget by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(productId, productName)

    val isSeller = currentUser?.tipoUsuario == "vendedor"

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && currentUser != null) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("user_id", currentUser!!.uid)
                .putBoolean("is_seller", isSeller)
                .apply()
            context.startService(Intent(context, AppNotificationService::class.java))
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("user_id", currentUser!!.uid)
                .putBoolean("is_seller", isSeller)
                .apply()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.POST_NOTIFICATIONS"
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
                } else {
                    context.startService(Intent(context, AppNotificationService::class.java))
                }
            } else {
                context.startService(Intent(context, AppNotificationService::class.java))
            }
        } else {
            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()
            context.stopService(Intent(context, AppNotificationService::class.java))
        }
    }

    LaunchedEffect(currentUser, currentDestination) {
        if (currentUser == null && currentDestination != null) {
            val isAuthScreen = currentDestination == Screen.Login.route || 
                               currentDestination == Screen.Register.route || 
                               currentDestination == Screen.Splash.route
            if (!isAuthScreen) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    val bottomNavItems = if (isSeller) {
        listOf(
            BottomNavItem("Inicio", Screen.Home.route, Icons.Default.Home),
            BottomNavItem("Mis Productos", Screen.SellerProducts.route, Icons.Default.Inventory),
            BottomNavItem("Pedidos", Screen.Orders.route, Icons.Default.LocalShipping),
            BottomNavItem("Chats", Screen.Chats.route, Icons.AutoMirrored.Filled.Chat),
            BottomNavItem("Perfil", Screen.Profile.route, Icons.Default.Person)
        )
    } else {
        listOf(
            BottomNavItem("Inicio", Screen.Home.route, Icons.Default.Home),
            BottomNavItem("Catálogo", "catalog", Icons.Default.Storefront),
            BottomNavItem("Carrito", Screen.Cart.route, Icons.Default.ShoppingCart, badgeCount = cartCount),
            BottomNavItem("Pedidos", Screen.Orders.route, Icons.Default.LocalShipping),
            BottomNavItem("Chats", Screen.Chats.route, Icons.AutoMirrored.Filled.Chat),
            BottomNavItem("Perfil", Screen.Profile.route, Icons.Default.Person)
        )
    }

    val showBottomBar = currentDestination in listOf(
        Screen.Home.route,
        "catalog",
        Screen.Catalog.route,
        Screen.Cart.route,
        Screen.Orders.route,
        Screen.Chats.route,
        Screen.SellerProducts.route,
        Screen.Profile.route
    )

    // Rating Bottom Sheet
    if (ratingProductTarget != null && currentUser != null) {
        val (pId, pName) = ratingProductTarget!!
        val ratingVm = navViewModel(key = "rating_$pId") {
            RatingViewModel(
                productoId = pId,
                userId = currentUser!!.uid,
                userName = currentUser!!.nombre,
                ratingRepository = AppContainer.ratingRepository
            )
        }
        RatingBottomSheet(
            ratingViewModel = ratingVm,
            productName = pName,
            onDismiss = { ratingProductTarget = null }
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar && currentUser != null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.startsWith(item.route) == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                if (item.badgeCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = AccentOrange,
                                                contentColor = Color.White
                                            ) {
                                                Text(if (item.badgeCount > 99) "99+" else item.badgeCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = item.icon, contentDescription = item.title)
                                    }
                                } else {
                                    Icon(imageVector = item.icon, contentDescription = item.title)
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${item.title}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Splash
            composable(Screen.Splash.route) {
                SplashScreen(
                    currentUser = currentUser,
                    isLoggedIn = authViewModel.isUserLoggedIn(),
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Register
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home
            composable(Screen.Home.route) {
                val homeVm = navViewModel { HomeViewModel(AppContainer.productRepository) }
                HomeScreen(
                    currentUser = currentUser,
                    cartCount = cartCount,
                    homeViewModel = homeVm,
                    onNavigateToProduct = { pId ->
                        navController.navigate(Screen.ProductDetail.createRoute(pId))
                    },
                    onNavigateToCatalog = { category ->
                        navController.navigate(Screen.Catalog.createRoute(category))
                    },
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }

            // Catalog
            composable(
                route = Screen.Catalog.route,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val categoryParam = backStackEntry.arguments?.getString("category")
                val catalogVm = navViewModel { CatalogViewModel(AppContainer.productRepository) }

                CatalogScreen(
                    catalogViewModel = catalogVm,
                    cartCount = cartCount,
                    initialCategory = categoryParam,
                    onNavigateToProduct = { pId ->
                        navController.navigate(Screen.ProductDetail.createRoute(pId))
                    },
                    onNavigateToCart = {
                        navController.navigate(Screen.Cart.route)
                    }
                )
            }

            // Product Detail
            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val productDetailVm = navViewModel(key = "product_$productId") {
                    ProductDetailViewModel(
                        productId = productId,
                        productRepository = AppContainer.productRepository,
                        ratingRepository = AppContainer.ratingRepository,
                        cartRepository = AppContainer.cartRepository,
                        chatRepository = AppContainer.chatRepository,
                        userRepository = AppContainer.userRepository
                    )
                }

                val currentProd by productDetailVm.product.collectAsState()

                ProductDetailScreen(
                    currentUser = currentUser,
                    cartCount = cartCount,
                    viewModel = productDetailVm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                    onNavigateToChat = { chatId ->
                        navController.navigate(
                            Screen.ChatDetail.createRoute(
                                chatId = chatId,
                                productName = currentProd?.nombre ?: "Chat"
                            )
                        )
                    },
                    onOpenRating = {
                        currentProd?.let { p ->
                            ratingProductTarget = Pair(p.id, p.nombre)
                        }
                    }
                )
            }

            // Cart
            composable(Screen.Cart.route) {
                if (currentUser != null) {
                    val cartVm = navViewModel(key = "cart_${currentUser!!.uid}") {
                        CartViewModel(
                            userId = currentUser!!.uid,
                            cartRepository = AppContainer.cartRepository,
                            orderRepository = AppContainer.orderRepository,
                            productRepository = AppContainer.productRepository
                        )
                    }

                    CartScreen(
                        cartViewModel = cartVm,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCatalog = { navController.navigate("catalog") },
                        onNavigateToOrders = { navController.navigate(Screen.Orders.route) }
                    )
                }
            }

            // Orders
            composable(Screen.Orders.route) {
                if (currentUser != null) {
                    val orderVm = navViewModel(key = "orders_${currentUser!!.uid}_$isSeller") {
                        OrderViewModel(
                            userId = currentUser!!.uid,
                            isSeller = isSeller,
                            orderRepository = AppContainer.orderRepository
                        )
                    }

                    OrdersScreen(
                        currentUser = currentUser,
                        orderViewModel = orderVm,
                        cartCount = cartCount,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                        onExploreCatalog = { navController.navigate("catalog") }
                    )
                }
            }

            // Seller Products
            composable(Screen.SellerProducts.route) {
                if (currentUser != null) {
                    val sellerVm = navViewModel(key = "seller_${currentUser!!.uid}") {
                        SellerProductsViewModel(
                            sellerId = currentUser!!.uid,
                            productRepository = AppContainer.productRepository,
                            storageRepository = AppContainer.storageRepository
                        )
                    }

                    SellerPanelScreen(
                        sellerViewModel = sellerVm,
                        onNavigateToAddProduct = {
                            editingProduct = null
                            navController.navigate(Screen.AddEditProduct.route)
                        },
                        onNavigateToEditProduct = { prod ->
                            editingProduct = prod
                            navController.navigate(Screen.AddEditProduct.route)
                        }
                    )
                }
            }

            // Add / Edit Product
            composable(Screen.AddEditProduct.route) {
                if (currentUser != null) {
                    val sellerVm = navViewModel(key = "seller_edit_${currentUser!!.uid}") {
                        SellerProductsViewModel(
                            sellerId = currentUser!!.uid,
                            productRepository = AppContainer.productRepository,
                            storageRepository = AppContainer.storageRepository
                        )
                    }

                    AddEditProductScreen(
                        sellerViewModel = sellerVm,
                        productToEdit = editingProduct,
                        onNavigateBack = {
                            editingProduct = null
                            navController.popBackStack()
                        }
                    )
                }
            }

            // Chats List
            composable(Screen.Chats.route) {
                if (currentUser != null) {
                    val chatVm = navViewModel(key = "chats_${currentUser!!.uid}") {
                        ChatViewModel(
                            userId = currentUser!!.uid,
                            chatRepository = AppContainer.chatRepository
                        )
                    }

                    ChatListScreen(
                        chatViewModel = chatVm,
                        cartCount = cartCount,
                        onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                        onSelectChat = { chatId, pName ->
                            navController.navigate(Screen.ChatDetail.createRoute(chatId, pName))
                        }
                    )
                }
            }

            // Chat Detail
            composable(
                route = Screen.ChatDetail.route,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType },
                    navArgument("productName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                val productName = backStackEntry.arguments?.getString("productName") ?: ""

                if (currentUser != null) {
                    val chatVm = navViewModel(key = "chat_detail_${currentUser!!.uid}") {
                        ChatViewModel(
                            userId = currentUser!!.uid,
                            chatRepository = AppContainer.chatRepository
                        )
                    }

                    ChatDetailScreen(
                        chatId = chatId,
                        productName = productName,
                        currentUserId = currentUser!!.uid,
                        chatViewModel = chatVm,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            // Profile
            composable(Screen.Profile.route) {
                if (currentUser != null) {
                    val profileVm = navViewModel(key = "profile_${currentUser!!.uid}") {
                        ProfileViewModel(
                            userId = currentUser!!.uid,
                            userRepository = AppContainer.userRepository,
                            storageRepository = AppContainer.storageRepository,
                            authRepository = AppContainer.authRepository
                        )
                    }

                    ProfileScreen(
                        profileViewModel = profileVm,
                        onSignOutSuccess = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
