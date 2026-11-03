package com.example.presentation.viewmodel

import com.example.data.repository.FirebaseAuthRepository
import com.example.data.repository.FirebaseCartRepository
import com.example.data.repository.FirebaseChatRepository
import com.example.data.repository.FirebaseOrderRepository
import com.example.data.repository.FirebaseProductRepository
import com.example.data.repository.FirebaseRatingRepository
import com.example.data.repository.FirebaseStorageRepository
import com.example.data.repository.FirebaseUserRepository
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.CartRepository
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.OrderRepository
import com.example.domain.repository.ProductRepository
import com.example.domain.repository.RatingRepository
import com.example.domain.repository.StorageRepository
import com.example.domain.repository.UserRepository

object AppContainer {
    val authRepository: AuthRepository by lazy { FirebaseAuthRepository() }
    val userRepository: UserRepository by lazy { FirebaseUserRepository() }
    val productRepository: ProductRepository by lazy { FirebaseProductRepository() }
    val ratingRepository: RatingRepository by lazy { FirebaseRatingRepository() }
    val cartRepository: CartRepository by lazy { FirebaseCartRepository() }
    val orderRepository: OrderRepository by lazy { FirebaseOrderRepository() }
    val chatRepository: ChatRepository by lazy { FirebaseChatRepository() }
    val storageRepository: StorageRepository by lazy { FirebaseStorageRepository() }
}
