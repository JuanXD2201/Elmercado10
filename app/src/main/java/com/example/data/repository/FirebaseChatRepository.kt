package com.example.data.repository

import com.example.data.firebase.FirebaseManager
import com.example.domain.model.Chat
import com.example.domain.model.Mensaje
import com.example.domain.repository.ChatRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseChatRepository(
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : ChatRepository {

    override fun getUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("chats")
            .whereArrayContains("participantes", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chats = snapshot.toObjects(Chat::class.java)
                    trySend(chats.sortedByDescending { it.ultimaActividad })
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getOrCreateChat(
        buyerId: String,
        sellerId: String,
        productoId: String,
        productName: String
    ): Result<String> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            // Buscar si ya existe un chat para este producto entre este comprador y vendedor
            val existing = db.collection("chats")
                .whereArrayContains("participantes", buyerId)
                .get().await()

            val found = existing.documents.firstOrNull { doc ->
                val participantes = doc.get("participantes") as? List<*>
                val pId = doc.getString("productoId")
                participantes?.contains(sellerId) == true && pId == productoId
            }

            if (found != null) {
                Result.success(found.id)
            } else {
                val newChatRef = db.collection("chats").document()
                val chat = Chat(
                    id = newChatRef.id,
                    participantes = listOf(buyerId, sellerId),
                    productoId = productoId,
                    nombreProducto = productName,
                    ultimoMensaje = "Conversación iniciada",
                    ultimaActividad = System.currentTimeMillis(),
                    compradorId = buyerId,
                    vendedorId = sellerId
                )
                newChatRef.set(chat).await()
                Result.success(newChatRef.id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMessages(chatId: String): Flow<List<Mensaje>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection("chats").document(chatId)
            .collection("mensajes")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val msgs = snapshot.toObjects(Mensaje::class.java)
                    trySend(msgs)
                } else {
                    trySend(emptyList())
                }
            }

        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(chatId: String, emisorId: String, texto: String): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        if (texto.isBlank()) {
            return Result.failure(IllegalArgumentException("El mensaje no puede estar vacío."))
        }

        return try {
            val chatRef = db.collection("chats").document(chatId)
            val msgRef = chatRef.collection("mensajes").document()

            val nuevoMensaje = Mensaje(
                id = msgRef.id,
                emisorId = emisorId,
                texto = texto.trim(),
                timestamp = System.currentTimeMillis(),
                leido = false
            )

            val batch = db.batch()
            batch.set(msgRef, nuevoMensaje)
            batch.update(
                chatRef,
                mapOf(
                    "ultimoMensaje" to texto.trim(),
                    "ultimaActividad" to System.currentTimeMillis()
                )
            )
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            val unread = db.collection("chats").document(chatId)
                .collection("mensajes")
                .whereEqualTo("leido", false)
                .get().await()

            val batch = db.batch()
            var count = 0
            for (doc in unread.documents) {
                if (doc.getString("emisorId") != userId) {
                    batch.update(doc.reference, "leido", true)
                    count++
                }
            }
            if (count > 0) {
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
