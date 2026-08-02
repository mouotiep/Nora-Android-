package com.example.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    val name: String = "Visiteur",
    val email: String = "",
    @SerialName("whatsapp_number") val whatsappNumber: String = "",
    @SerialName("avatar_url") val avatarUrl: String = "",
    @SerialName("kyc_status") val kycStatus: String = "Non vérifié",
    @SerialName("n_coins_balance") val nCoinsBalance: Double = 1.0
)

@Serializable
data class AdminCheckDto(
    @SerialName("user_id") val userId: String
)

@Serializable
data class ProductDto(
    val id: String,
    val title: String = "",
    val category: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    @SerialName("shop_name") val shopName: String = "",
    val location: String = "",
    val description: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("shop_id") val shopId: String = "",
    @SerialName("is_certified") val isCertified: Boolean = true,
    @SerialName("is_scammer") val isScammer: Boolean = false
)

@Serializable
data class ReelDto(
    val id: String,
    val caption: String = "",
    @SerialName("creator_name") val creatorName: String = "",
    val category: String = "Mode & Vêtements",
    @SerialName("media_type") val mediaType: String = "Vidéo",
    @SerialName("media_url") val mediaUrl: String = "",
    @SerialName("aspect_ratio") val aspectRatio: String = "9:16",
    @SerialName("zoom_level") val zoomLevel: Float = 1f,
    @SerialName("rotation_angle") val rotationAngle: Float = 0f,
    @SerialName("start_sec") val startSec: Float = 0f,
    @SerialName("end_sec") val endSec: Float = 0f,
    @SerialName("likes_count") val likesCount: Int = 0,
    @SerialName("views_count") val viewsCount: Int = 0
)

@Serializable
data class ConversationDto(
    val id: String,
    @SerialName("contact_name") val contactName: String = "Utilisateur NorA",
    @SerialName("last_message") val lastMessage: String = "",
    @SerialName("user_phone") val userPhone: String = "",
    @SerialName("user_email") val userEmail: String = ""
)

@Serializable
data class MessageDto(
    val id: String,
    @SerialName("conversation_id") val conversationId: String,
    val sender: String,
    val text: String,
    @SerialName("reply_to_text") val replyToText: String = "",
    @SerialName("reply_to_sender") val replyToSender: String = "",
    val status: String = "SENT"
)

@Serializable
data class WalletEventDto(
    @SerialName("user_id") val userId: String,
    @SerialName("event_type") val eventType: String,
    val amount: Double,
    val meta: String = ""
)

@Serializable
data class ProductStockDto(val stock: Int)
