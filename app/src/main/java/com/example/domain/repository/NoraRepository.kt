package com.example.domain.repository

import com.example.domain.model.*
import kotlinx.coroutines.flow.StateFlow

interface NoraRepository {
    val products: StateFlow<List<ProductItem>>
    val reels: StateFlow<List<ReelVideo>>
    val userProfile: StateFlow<UserProfile>
    val orders: StateFlow<List<NoraOrder>>
    val transactions: StateFlow<List<Transaction>>
    val conversations: StateFlow<List<Conversation>>
    val reportedItems: StateFlow<List<ReportedItem>>
    val kycApplications: StateFlow<List<UserProfile>>
    val categories: StateFlow<List<String>>
    val walletNCoins: StateFlow<Double>

    suspend fun fetchProductsRemote(): List<ProductItem>
    suspend fun addProduct(product: ProductItem)
    suspend fun updateProduct(product: ProductItem)
    suspend fun deleteProduct(productId: String)

    suspend fun publishReel(reel: ReelVideo)
    suspend fun deleteReel(reelId: String)
    suspend fun toggleLike(reelId: String)
    suspend fun recordUniqueView(reelId: String): Boolean

    suspend fun updateUserProfile(profile: UserProfile)
    suspend fun uploadMedia(fileUri: String, mediaType: String): String

    suspend fun placeOrder(order: NoraOrder)
    suspend fun updateOrderStatus(orderId: String, newStatus: String)
    
    suspend fun syncWithBackendApi()
}
