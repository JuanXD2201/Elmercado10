package com.example.domain.repository

import com.example.domain.model.Chat
import com.example.domain.model.Mensaje
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getUserChats(userId: String): Flow<List<Chat>>
    suspend fun getOrCreateChat(buyerId: String, sellerId: String, productoId: String, productName: String): Result<String>
    fun getMessages(chatId: String): Flow<List<Mensaje>>
    suspend fun sendMessage(chatId: String, emisorId: String, texto: String): Result<Unit>
    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit>
}
