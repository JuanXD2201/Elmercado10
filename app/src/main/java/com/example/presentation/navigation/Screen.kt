package com.example.presentation.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object Catalog : Screen("catalog?category={category}") {
        fun createRoute(category: String? = null) = if (category.isNullOrBlank()) "catalog" else "catalog?category=$category"
    }
    data object Cart : Screen("cart")
    data object Orders : Screen("orders")
    data object Chats : Screen("chats")
    data object ChatDetail : Screen("chat_detail/{chatId}/{productName}") {
        fun createRoute(chatId: String, productName: String) = "chat_detail/$chatId/$productName"
    }
    data object SellerProducts : Screen("seller_products")
    data object AddEditProduct : Screen("add_edit_product")
    data object Profile : Screen("profile")
    data object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}
