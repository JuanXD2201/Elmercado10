package com.example.presentation.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.domain.model.Chat
import com.example.domain.model.Mensaje
import com.example.domain.model.Pedido
import com.example.domain.model.Producto
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AppNotificationHelper {
    private const val CHANNEL_ORDERS = "orders_channel"
    private const val CHANNEL_CHATS = "chats_channel"
    private const val CHANNEL_INVENTORY = "inventory_channel"

    private var ordersListener: ListenerRegistration? = null
    private var chatsListener: ListenerRegistration? = null
    private var inventoryListener: ListenerRegistration? = null

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Tracks seen data to prevent duplicate notifications upon initial loading
    private val orderStatusMap = mutableMapOf<String, String>() // orderId -> status
    private val productStockMap = mutableMapOf<String, Int>() // productId -> stock
    private val chatActivityMap = mutableMapOf<String, Long>() // chatId -> ultimaActividad

    private var isOrdersFirstLoad = true
    private var isChatsFirstLoad = true
    private var isInventoryFirstLoad = true

    fun initChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val ordersChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Pedidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre el estado de tus pedidos"
            }

            val chatsChannel = NotificationChannel(
                CHANNEL_CHATS,
                "Chats y Mensajes",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes de compradores y vendedores"
            }

            val inventoryChannel = NotificationChannel(
                CHANNEL_INVENTORY,
                "Inventario",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas de productos agotados y ventas"
            }

            notificationManager.createNotificationChannel(ordersChannel)
            notificationManager.createNotificationChannel(chatsChannel)
            notificationManager.createNotificationChannel(inventoryChannel)
        }
    }

    fun startListening(context: Context, userId: String, isSeller: Boolean) {
        val db = FirebaseFirestore.getInstance() ?: return

        // Initialize channels
        initChannels(context)

        // Reset first load flags
        isOrdersFirstLoad = true
        isChatsFirstLoad = true
        isInventoryFirstLoad = true
        orderStatusMap.clear()
        productStockMap.clear()
        chatActivityMap.clear()

        stopListening()

        // 1. Listen to Orders (Compras o Ventas)
        val ordersQuery = if (isSeller) {
            db.collection("pedidos").whereArrayContains("vendedorIds", userId)
        } else {
            db.collection("pedidos").whereEqualTo("usuarioId", userId)
        }

        ordersListener = ordersQuery.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val orders = snapshot.toObjects(Pedido::class.java)

            if (isOrdersFirstLoad) {
                orders.forEach { order ->
                    orderStatusMap[order.id] = order.status
                }
                isOrdersFirstLoad = false
                return@addSnapshotListener
            }

            orders.forEach { order ->
                val prevStatus = orderStatusMap[order.id]
                if (prevStatus == null) {
                    // New order added!
                    if (isSeller) {
                        val productNames = order.items.joinToString { it.nombreProducto }
                        showNotification(
                            context,
                            CHANNEL_ORDERS,
                            1000 + order.id.hashCode(),
                            "¡Nueva Venta!",
                            "Te han comprado: $productNames por $${String.format("%.2f", order.totalAmount)}"
                        )
                    }
                    orderStatusMap[order.id] = order.status
                } else if (prevStatus != order.status) {
                    // Order status changed
                    if (!isSeller) {
                        val formattedStatus = when (order.status) {
                            "pendiente" -> "Pendiente"
                            "confirmado" -> "Confirmado"
                            "enviado" -> "En Camino"
                            "entregado" -> "Entregado"
                            "cancelado" -> "Cancelado"
                            else -> order.status
                        }
                        val itemsText = order.items.joinToString { it.nombreProducto }
                        showNotification(
                            context,
                            CHANNEL_ORDERS,
                            2000 + order.id.hashCode(),
                            "Estado de tu pedido actualizado",
                            "Tu pedido de '$itemsText' ahora está: $formattedStatus"
                        )
                    }
                    orderStatusMap[order.id] = order.status
                }
            }
        }

        // 2. Listen to Chats
        chatsListener = db.collection("chats")
            .whereArrayContains("participantes", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val chats = snapshot.toObjects(Chat::class.java)

                if (isChatsFirstLoad) {
                    chats.forEach { chat ->
                        chatActivityMap[chat.id] = chat.ultimaActividad
                    }
                    isChatsFirstLoad = false
                    return@addSnapshotListener
                }

                chats.forEach { chat ->
                    val prevActividad = chatActivityMap[chat.id]
                    if (prevActividad == null || chat.ultimaActividad > prevActividad) {
                        chatActivityMap[chat.id] = chat.ultimaActividad

                        scope.launch {
                            try {
                                val msgsSnap = db.collection("chats").document(chat.id)
                                    .collection("mensajes")
                                    .orderBy("timestamp", Query.Direction.DESCENDING)
                                    .limit(1)
                                    .get().await()

                                val lastMsgDoc = msgsSnap.documents.firstOrNull()
                                if (lastMsgDoc != null) {
                                    val lastMsg = lastMsgDoc.toObject(Mensaje::class.java)
                                    if (lastMsg != null && lastMsg.emisorId != userId) {
                                        val senderTitle = if (isSeller) "Mensaje de Comprador" else "Mensaje de Vendedor"
                                        showNotification(
                                            context,
                                            CHANNEL_CHATS,
                                            3000 + chat.id.hashCode(),
                                            "$senderTitle (${chat.nombreProducto})",
                                            lastMsg.texto
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("AppNotificationHelper", "Error checking message: ${e.message}")
                            }
                        }
                    }
                }
            }

        // 3. Listen to Inventory (Sellers only)
        if (isSeller) {
            inventoryListener = db.collection("productos")
                .whereEqualTo("vendedorId", userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val products = snapshot.toObjects(Producto::class.java)

                    if (isInventoryFirstLoad) {
                        products.forEach { prod ->
                            productStockMap[prod.id] = prod.cantidad
                        }
                        isInventoryFirstLoad = false
                        return@addSnapshotListener
                    }

                    products.forEach { prod ->
                        val prevStock = productStockMap[prod.id]
                        if (prevStock != null) {
                            if (prevStock > 0 && prod.cantidad == 0) {
                                showNotification(
                                    context,
                                    CHANNEL_INVENTORY,
                                    4000 + prod.id.hashCode(),
                                    "¡Producto Agotado!",
                                    "Tu producto '${prod.nombre}' se ha quedado sin stock."
                                )
                            }
                            productStockMap[prod.id] = prod.cantidad
                        } else {
                            productStockMap[prod.id] = prod.cantidad
                        }
                    }
                }
        }
    }

    fun stopListening() {
        ordersListener?.remove()
        ordersListener = null

        chatsListener?.remove()
        chatsListener = null

        inventoryListener?.remove()
        inventoryListener = null
    }

    private fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            android.util.Log.e("AppNotificationHelper", "Permission not granted: ${e.message}")
        }
    }
}
