package com.example

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.domain.model.Calificacion
import com.example.domain.model.CarritoItem
import com.example.domain.model.Chat
import com.example.domain.model.Mensaje
import com.example.domain.model.Pedido
import com.example.domain.model.PedidoItem
import com.example.domain.model.Producto
import com.example.domain.model.Usuario
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.RatingRepository
import com.example.domain.repository.StorageRepository
import com.example.domain.repository.UserRepository
import com.example.presentation.screens.AddEditProductScreen
import com.example.presentation.screens.CartScreen
import com.example.presentation.screens.CatalogScreen
import com.example.presentation.screens.ChatDetailScreen
import com.example.presentation.screens.ChatListScreen
import com.example.presentation.screens.FilterBottomSheet
import com.example.presentation.screens.HomeScreen
import com.example.presentation.screens.LoginScreen
import com.example.presentation.screens.OrdersScreen
import com.example.presentation.screens.ProductDetailScreen
import com.example.presentation.screens.ProfileScreen
import com.example.presentation.screens.RatingBottomSheet
import com.example.presentation.screens.RegisterScreen
import com.example.presentation.screens.SellerPanelScreen
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
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Genera las capturas de documentación renderizando las pantallas REALES de la aplicación con
 * repositorios de demostración (datos ficticios, sin red ni Firebase real).
 * No son capturas de un dispositivo: son renders en JVM (Robolectric + Roborazzi).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ScreenshotsDocTest {

    @get:Rule val rule = createComposeRule()

    // ---------- Datos de demostración (ficticios) ----------
    private val buyerId = "u-comprador"
    private val sellerId = "u-vendedor"
    private val buyer = Usuario(uid = buyerId, nombre = "Camila Rojas", correo = "camila@ejemplo.com", tipoUsuario = "comprador", telefono = "3001112233")
    private val seller = Usuario(uid = sellerId, nombre = "Tienda Los Andes", correo = "andes@ejemplo.com", tipoUsuario = "vendedor", telefono = "3004445566")

    private val products = listOf(
        Producto("p1", "Auriculares inalámbricos", "Auriculares Bluetooth con estuche de carga y 20 horas de batería.", 189000.0, "Tecnología", "nuevo", 8, "", sellerId, 4.6, 12),
        Producto("p2", "Lámpara de escritorio LED", "Lámpara con tres niveles de brillo y brazo flexible.", 64000.0, "Hogar", "nuevo", 15, "", sellerId, 4.2, 5),
        Producto("p3", "Balón de fútbol profesional", "Balón número 5, cosido a máquina, poco uso.", 55000.0, "Deportes", "usado", 2, "", sellerId, 4.8, 9),
        Producto("p4", "Chaqueta impermeable", "Chaqueta talla M con capucha y costuras selladas.", 120000.0, "Moda", "nuevo", 6, "", sellerId, 3.9, 4),
        Producto("p5", "Bicicleta de montaña", "Rodada 26, 21 velocidades, frenos de disco.", 780000.0, "Vehículos", "usado", 1, "", sellerId, 4.5, 7),
        Producto("p6", "Set de bloques de construcción", "480 piezas compatibles, para mayores de 6 años.", 72000.0, "Juguetes", "nuevo", 20, "", sellerId, 4.7, 11)
    )
    private val cartItems = listOf(
        CarritoItem("p1", "Auriculares inalámbricos", 1, 189000.0, 0L, "", sellerId),
        CarritoItem("p3", "Balón de fútbol profesional", 2, 55000.0, 0L, "", sellerId)
    )
    private val orders = listOf(
        Pedido("ped-1001", buyerId, listOf(PedidoItem("p1", "Auriculares inalámbricos", 1, 189000.0, 189000.0, sellerId, "")), 189000.0, "enviado", 1_762_000_000_000, listOf(sellerId)),
        Pedido("ped-1000", buyerId, listOf(PedidoItem("p6", "Set de bloques de construcción", 2, 72000.0, 144000.0, sellerId, "")), 144000.0, "pendiente", 1_761_900_000_000, listOf(sellerId))
    )
    private val chat = Chat("c1", listOf(buyerId, sellerId), "p1", "Auriculares inalámbricos", "Sí, tengo disponibilidad hoy.", 1_762_000_000_000, buyerId, sellerId)
    private val messages = listOf(
        Mensaje("m1", buyerId, "Hola, ¿siguen disponibles los auriculares?", 1_761_999_000_000, true),
        Mensaje("m2", sellerId, "Sí, tengo disponibilidad hoy.", 1_762_000_000_000, false)
    )
    private val ratings = listOf(
        Calificacion("u2", 5, "Muy buen sonido y llegaron rápido.", 1_761_000_000_000, "Andrés M."),
        Calificacion("u3", 4, "Cumplen lo prometido.", 1_760_000_000_000, "Laura P.")
    )

    // ---------- Repositorios de demostración ----------
    private inner class FakeAuth : AuthRepository {
        override val currentUserFlow: Flow<Usuario?> = flowOf(buyer)
        override fun isUserLoggedIn() = true
        override fun getCurrentUserId(): String? = buyerId
        override suspend fun signIn(email: String, password: String) = Result.success(buyer)
        override suspend fun signUp(nombre: String, email: String, telefono: String, password: String, tipoUsuario: String) = Result.success(buyer)
        override suspend fun signOut() {}
    }

    private inner class FakeProducts : ProductRepository {
        override fun getProducts(): Flow<List<Producto>> = flowOf(products)
        override fun getProductsBySeller(sellerId: String): Flow<List<Producto>> = flowOf(products.filter { it.vendedorId == sellerId })
        override suspend fun getProductById(id: String) = Result.success(products.firstOrNull { it.id == id })
        override fun getProductFlow(id: String): Flow<Producto?> = flowOf(products.firstOrNull { it.id == id })
        override suspend fun createProduct(producto: Producto) = Result.success(producto.id)
        override suspend fun updateProduct(producto: Producto) = Result.success(Unit)
        override suspend fun deleteProduct(productId: String, sellerId: String, imageUrl: String?) = Result.success(Unit)
    }

    private inner class FakeCart : CartRepository {
        override fun getCartItems(userId: String): Flow<List<CarritoItem>> = flowOf(cartItems)
        override suspend fun addToCart(userId: String, producto: Producto, cantidad: Int) = Result.success(Unit)
        override suspend fun updateQuantity(userId: String, productoId: String, newQuantity: Int, availableStock: Int) = Result.success(Unit)
        override suspend fun removeFromCart(userId: String, productoId: String) = Result.success(Unit)
        override suspend fun clearCart(userId: String) = Result.success(Unit)
    }

    private inner class FakeOrders : OrderRepository {
        override suspend fun createOrder(userId: String, cartItems: List<CarritoItem>) = Result.success("ped-demo")
        override fun getUserOrders(userId: String): Flow<List<Pedido>> = flowOf(orders)
        override fun getSellerOrders(sellerId: String): Flow<List<Pedido>> = flowOf(orders)
        override suspend fun updateOrderStatus(orderId: String, newStatus: String) = Result.success(Unit)
    }

    private inner class FakeChats : ChatRepository {
        override fun getUserChats(userId: String): Flow<List<Chat>> = flowOf(listOf(chat))
        override suspend fun getOrCreateChat(buyerId: String, sellerId: String, productoId: String, productName: String) = Result.success("c1")
        override fun getMessages(chatId: String): Flow<List<Mensaje>> = flowOf(messages)
        override suspend fun sendMessage(chatId: String, emisorId: String, texto: String) = Result.success(Unit)
        override suspend fun markMessagesAsRead(chatId: String, userId: String) = Result.success(Unit)
    }

    private inner class FakeRatings : RatingRepository {
        override fun getRatings(productoId: String): Flow<List<Calificacion>> = flowOf(ratings)
        override suspend fun getUserRating(productoId: String, userId: String) = Result.success<Calificacion?>(null)
        override suspend fun addOrUpdateRating(productoId: String, calificacion: Calificacion) = Result.success(Unit)
    }

    private inner class FakeUsers : UserRepository {
        override suspend fun getUser(uid: String) = Result.success<Usuario?>(if (uid == sellerId) seller else buyer)
        override fun getUserFlow(uid: String): Flow<Usuario?> = flowOf(if (uid == sellerId) seller else buyer)
        override suspend fun updateUser(uid: String, nombre: String, telefono: String, fotoPerfil: String?) = Result.success(Unit)
    }

    private inner class FakeStorage : StorageRepository {
        override suspend fun uploadProductImage(sellerId: String, productId: String, imageUri: Uri) = Result.success("")
        override suspend fun uploadProfileImage(userId: String, imageUri: Uri) = Result.success("")
        override suspend fun deleteImage(imageUrl: String) = Result.success(Unit)
    }

    private fun shot(name: String, content: @Composable () -> Unit) {
        rule.setContent { MyApplicationTheme { content() } }
        rule.waitForIdle()
        rule.onRoot().captureRoboImage(filePath = "src/test/screenshots/doc/$name.png")
    }

    @Test fun login() = shot("01_login") { LoginScreen(AuthViewModel(FakeAuth()), {}, {}) }

    @Test fun registro() = shot("02_registro") { RegisterScreen(AuthViewModel(FakeAuth()), {}, {}) }

    @Test fun inicio() = shot("03_inicio") {
        HomeScreen(buyer, 3, HomeViewModel(FakeProducts()), {}, {}, {})
    }

    @Test fun catalogo() = shot("04_catalogo") {
        CatalogScreen(CatalogViewModel(FakeProducts()), 3, null, {}, {})
    }

    @Test fun filtros() = shot("05_filtros") {
        FilterBottomSheet("", "", "todos", 0, { _, _, _, _ -> }, {}, {})
    }

    @Test fun detalleProducto() = shot("06_detalle_producto") {
        ProductDetailScreen(
            buyer, 3,
            ProductDetailViewModel("p1", FakeProducts(), FakeRatings(), FakeCart(), FakeChats(), FakeUsers()),
            {}, {}, {}, {}
        )
    }

    @Test fun calificar() = shot("07_calificar") {
        RatingBottomSheet(RatingViewModel("p1", buyerId, buyer.nombre, FakeRatings()), "Auriculares inalámbricos", {})
    }

    @Test fun carrito() = shot("08_carrito") {
        CartScreen(CartViewModel(buyerId, FakeCart(), FakeOrders(), FakeProducts()), {}, {}, {})
    }

    @Test fun pedidosComprador() = shot("09_pedidos_comprador") {
        OrdersScreen(buyer, OrderViewModel(buyerId, false, FakeOrders()), 3, {}, {}, {})
    }

    @Test fun pedidosVendedor() = shot("10_pedidos_vendedor") {
        OrdersScreen(seller, OrderViewModel(sellerId, true, FakeOrders()), 0, {}, {}, {})
    }

    @Test fun misProductos() = shot("11_mis_productos") {
        SellerPanelScreen(SellerProductsViewModel(sellerId, FakeProducts(), FakeStorage()), {}, {})
    }

    @Test fun agregarProducto() = shot("12_agregar_producto") {
        AddEditProductScreen(SellerProductsViewModel(sellerId, FakeProducts(), FakeStorage()), null, {})
    }

    @Test fun chats() = shot("13_chats") {
        ChatListScreen(ChatViewModel(buyerId, FakeChats()), 3, {}, { _, _ -> })
    }

    @Test fun chatDetalle() = shot("14_chat_detalle") {
        ChatDetailScreen("c1", "Auriculares inalámbricos", buyerId, ChatViewModel(buyerId, FakeChats()), {})
    }

    @Test fun perfil() = shot("15_perfil") {
        ProfileScreen(ProfileViewModel(buyerId, FakeUsers(), FakeStorage(), FakeAuth()), {})
    }
}
