package com.example.data.repository

import com.example.data.api.NoraApiClient
import com.example.data.api.NoraApiService
import com.example.domain.model.*
import com.example.domain.repository.NoraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoraRepositoryImpl(
    private val apiService: NoraApiService = NoraApiClient.apiService
) : NoraRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Initial default seed items
    private val _products = MutableStateFlow<List<ProductItem>>(
        listOf(
            ProductItem(
                id = "p-1",
                title = "Statue Royale Bamoun en Bois d'Ébène",
                category = "Sculpture",
                price = 35000,
                stock = 4,
                shopName = "Artisanat Foumban Royal",
                location = "Foumban, Ouest Cameroun",
                description = "Statue sculptée à la main par les maîtres artisans du Palais Royal de Foumban.",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600",
                shopId = "shop-foumban"
            ),
            ProductItem(
                id = "p-2",
                title = "Tissu Ndop Traditionnel Bamiléké (2M)",
                category = "Mode & Vêtements",
                price = 28000,
                stock = 12,
                shopName = "Atelier Ndop Bafoussam",
                location = "Bafoussam, Ouest",
                description = "Authentique étoffe royale bamiléké teinte à l'indigo naturel.",
                imageUrl = "https://images.unsplash.com/photo-1590736704728-f4730bb30770?w=600",
                shopId = "shop-ndop"
            ),
            ProductItem(
                id = "p-3",
                title = "Sac en Cuir Tressé & Perles Bamiléké",
                category = "Accessoires & Bijou",
                price = 15000,
                stock = 8,
                shopName = "Maroquinerie du Noun",
                location = "Douala, Littoral",
                description = "Sac à main fait main orné de motifs géométriques camerounais.",
                imageUrl = "https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600",
                shopId = "shop-maroquin"
            )
        )
    )
    override val products: StateFlow<List<ProductItem>> = _products.asStateFlow()

    private val _reels = MutableStateFlow<List<ReelVideo>>(
        listOf(
            ReelVideo(
                id = "reel-1",
                caption = "Démonstration de tissage du Ndop royal dans notre atelier à Bafoussam 🧵✨",
                creatorName = "Atelier Ndop Bafoussam",
                likesCount = 1420,
                viewsCount = 12500,
                isLiked = false,
                mediaType = "Vidéo",
                category = "Mode & Vêtements"
            ),
            ReelVideo(
                id = "reel-2",
                caption = "Sculpture sur bois d'ébène au Palais de Foumban 👑🎨",
                creatorName = "Artisanat Foumban Royal",
                likesCount = 2890,
                viewsCount = 18900,
                isLiked = true,
                mediaType = "Vidéo",
                category = "Sculpture"
            )
        )
    )
    override val reels: StateFlow<List<ReelVideo>> = _reels.asStateFlow()

    private val _userProfile = MutableStateFlow(UserProfile())
    override val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _orders = MutableStateFlow<List<NoraOrder>>(emptyList())
    override val orders: StateFlow<List<NoraOrder>> = _orders.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(
        listOf(
            Transaction("Bonus d'Inscription", "Cadeau de bienvenue Nora Cameroun", 1.0, "Aujourd'hui", true)
        )
    )
    override val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    override val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _reportedItems = MutableStateFlow<List<ReportedItem>>(emptyList())
    override val reportedItems: StateFlow<List<ReportedItem>> = _reportedItems.asStateFlow()

    private val _kycApplications = MutableStateFlow<List<UserProfile>>(emptyList())
    override val kycApplications: StateFlow<List<UserProfile>> = _kycApplications.asStateFlow()

    private val _categories = MutableStateFlow(
        listOf(
            "Tous",
            "Mode & Vêtements",
            "Accessoires & Bijou",
            "Alimentation",
            "Objets d'Art",
            "Décoration d'Intérieur",
            "Sculpture",
            "Musique & Instruments",
            "Littérature Africaine",
            "Soin & Cosmétique Bio",
            "Poterie"
        )
    )
    override val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _walletNCoins = MutableStateFlow(1.0)
    override val walletNCoins: StateFlow<Double> = _walletNCoins.asStateFlow()

    override suspend fun fetchProductsRemote(): List<ProductItem> {
        return try {
            val response = apiService.getProducts()
            if (response.isSuccessful && response.body() != null) {
                val remoteList = response.body()!!
                _products.value = remoteList
                remoteList
            } else {
                _products.value
            }
        } catch (e: Exception) {
            _products.value
        }
    }

    override suspend fun addProduct(product: ProductItem) {
        val updated = listOf(product) + _products.value
        _products.value = updated
        scope.launch {
            try { apiService.createProduct(product) } catch (_: Exception) {}
        }
    }

    override suspend fun updateProduct(product: ProductItem) {
        val updated = _products.value.map { if (it.id == product.id) product else it }
        _products.value = updated
        scope.launch {
            try { apiService.updateProduct(product.id, product) } catch (_: Exception) {}
        }
    }

    override suspend fun deleteProduct(productId: String) {
        _products.value = _products.value.filter { it.id != productId }
        scope.launch {
            try { apiService.deleteProduct(productId) } catch (_: Exception) {}
        }
    }

    override suspend fun publishReel(reel: ReelVideo) {
        val updated = listOf(reel) + _reels.value
        _reels.value = updated
        scope.launch {
            try { apiService.createReel(reel) } catch (_: Exception) {}
        }
    }

    override suspend fun deleteReel(reelId: String) {
        _reels.value = _reels.value.filter { it.id != reelId }
        scope.launch {
            try { apiService.deleteReel(reelId) } catch (_: Exception) {}
        }
    }

    override suspend fun toggleLike(reelId: String) {
        _reels.value = _reels.value.map { reel ->
            if (reel.id == reelId) {
                val newLikedState = !reel.isLiked
                val newLikesCount = if (newLikedState) reel.likesCount + 1 else (reel.likesCount - 1).coerceAtLeast(0)
                reel.copy(isLiked = newLikedState, likesCount = newLikesCount)
            } else reel
        }
        scope.launch {
            try { apiService.likeReel(reelId) } catch (_: Exception) {}
        }
    }

    override suspend fun recordUniqueView(reelId: String): Boolean {
        var recorded = false
        _reels.value = _reels.value.map { reel ->
            if (reel.id == reelId) {
                recorded = true
                reel.copy(viewsCount = reel.viewsCount + 1)
            } else reel
        }
        return recorded
    }

    override suspend fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        scope.launch {
            try { apiService.updateUserProfile(profile) } catch (_: Exception) {}
        }
    }

    override suspend fun uploadMedia(fileUri: String, mediaType: String): String {
        return try {
            val response = apiService.uploadMedia(mediaType, fileUri)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()!!.mediaUrl
            } else {
                fileUri
            }
        } catch (e: Exception) {
            fileUri
        }
    }

    override suspend fun placeOrder(order: NoraOrder) {
        _orders.value = listOf(order) + _orders.value
        scope.launch {
            try { apiService.createOrder(order) } catch (_: Exception) {}
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        _orders.value = _orders.value.map { if (it.id == orderId) it.copy(status = newStatus) else it }
    }

    override suspend fun syncWithBackendApi() {
        try {
            fetchProductsRemote()
        } catch (_: Exception) {}
    }
}
