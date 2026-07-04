package com.example

data class ProductItem(
    val id: String,
    val title: String,
    val category: String,
    val price: Int, // in FCFA
    val stock: Int,
    val shopName: String,
    val location: String,
    val description: String,
    val imageUrl: String,
    val shopId: String = "shop-default",
    val isCertified: Boolean = true,
    val isScammer: Boolean = false,
    val isBanned: Boolean = false
)

data class ReelComment(
    val id: String,
    val reelId: String,
    val authorName: String,
    val text: String,
    val time: String,
    val isReported: Boolean = false,
    val reactions: Map<String, Int> = emptyMap()
)

data class ShopReview(
    val id: String,
    val shopId: String,
    val shopName: String,
    val reviewerName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val date: String
)

data class ReelVideo(
    val id: String,
    val caption: String,
    val creatorName: String,
    val likesCount: Int,
    val viewsCount: Int,
    val isLiked: Boolean,
    val isFollowing: Boolean = false,
    val category: String = "Mode & Vêtements", // Used for preference filtering
    val comments: List<ReelComment> = emptyList(),
    val mediaType: String = "Vidéo",
    val aspectRatio: String = "9:16",
    val zoomLevel: Float = 1f,
    val rotationAngle: Float = 0f
)

data class Message(
    val sender: String, // "moi", "contact", or "admin"
    val text: String,
    val time: String
)

data class Conversation(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val lastTime: String,
    val messages: List<Message>
)

data class Transaction(
    val title: String,
    val description: String,
    val amount: Int, // in N Coins (positive or negative)
    val date: String,
    val isPositive: Boolean
)

data class ReportedItem(
    val id: String,
    val title: String,
    val reason: String,
    val type: String, // "Utilisateur", "Vidéo", "Produit", "Boutique"
    val reporterName: String = "Anonyme"
)

data class UserProfile(
    val id: String = "user-1",
    val name: String = "Visiteur Camerounais",
    val whatsappNumber: String = "+237 600 000 000",
    val profilePic: String = "",
    val isLoggedIn: Boolean = false,
    val interests: List<String> = emptyList(),
    val kycStatus: String = "Aucun", // Aucun, En Attente, Certifié, Banni, Révoqué, Arnaqueur
    val shopName: String = "",
    val shopDescription: String = "",
    val shopCategory: String = "",
    val shopLocation: String = "",
    val shopPic: String = "",
    val agreedToFee: Boolean = false,
    val idCardPhoto: String = "",
    val selfiePhoto: String = "",
    val hasShop: Boolean = false
)

data class NoraOrder(
    val id: String,
    val productId: String,
    val productTitle: String,
    val productPrice: Int,
    val buyerName: String,
    val buyerWhatsApp: String,
    val sellerName: String,
    val sellerWhatsApp: String,
    val payInNCoins: Boolean,
    val coinsCost: Int,
    val status: String, // "En attente de livraison", "Livré & Payé"
    val date: String
)
