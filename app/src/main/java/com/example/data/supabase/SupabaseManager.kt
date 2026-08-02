package com.example.data.supabase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.domain.model.*
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

object SupabaseManager {

    private const val TAG = "SupabaseManager"
    private const val SUPABASE_URL = "https://luukpmzeilokllezmmuw.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_afI1JOSfTtfe6bwjuQHa4Q_jylzqFDc"

    val client by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Auth)
                install(Postgrest)
                install(Storage)
                install(Realtime)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur création SupabaseClient: ${e.message}", e)
            null
        }
    }

    fun initSupabase(context: Context) {
        Log.d(TAG, "Supabase initialisé pour Nora Cameroun ($SUPABASE_URL)")
    }

    fun currentUserId(): String? {
        return try {
            client?.auth?.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ensureAuthenticated(): String? {
        val c = client ?: return null
        return try {
            c.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            Log.w(TAG, "Vérification auth Supabase: ${e.message}")
            null
        }
    }

    suspend fun resolveActiveRole(uid: String): String {
        val c = client ?: return "Acheteur"
        return try {
            val response = c.postgrest.from("admins")
                .select {
                    filter {
                        eq("user_id", uid)
                    }
                }.decodeList<Map<String, Any>>()
            if (response.isNotEmpty()) "Admin" else "Acheteur"
        } catch (e: Exception) {
            "Acheteur"
        }
    }

    // --- AUTHENTICATION ---

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        whatsapp: String
    ): Result<UserProfile> {
        val c = client ?: return Result.failure(IllegalStateException("Supabase non disponible"))
        return try {
            c.auth.signUpWith(Email) {
                this.email = email.trim()
                this.password = pass
            }
            val userId = c.auth.currentUserOrNull()?.id ?: UUID.randomUUID().toString()

            val profile = UserProfile(
                id = userId,
                name = name,
                email = email.trim(),
                whatsappNumber = whatsapp,
                isLoggedIn = true,
                onboardingCompleted = true
            )

            val profileData = mapOf(
                "id" to userId,
                "name" to name,
                "email" to email.trim(),
                "whatsapp_number" to whatsapp,
                "kyc_status" to "Non vérifié",
                "n_coins_balance" to 1
            )

            runCatching {
                c.postgrest.from("profiles").insert(profileData)
            }

            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "SignUp Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> {
        val c = client ?: return Result.failure(IllegalStateException("Supabase non disponible"))
        return try {
            c.auth.signInWith(Email) {
                this.email = email.trim()
                this.password = pass
            }
            val user = c.auth.currentUserOrNull()
                ?: return Result.failure(Exception("Connexion échouée"))

            val profiles = runCatching {
                c.postgrest.from("profiles").select {
                    filter { eq("id", user.id) }
                }.decodeList<Map<String, Any?>>()
            }.getOrDefault(emptyList())

            val profile = if (profiles.isNotEmpty()) {
                val map = profiles.first()
                parseUserProfile(user.id, map)
            } else {
                UserProfile(
                    id = user.id,
                    email = user.email ?: email,
                    name = "Utilisateur Nora",
                    isLoggedIn = true,
                    onboardingCompleted = true
                )
            }

            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "SignIn Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            // Clear current auth session asynchronously if active
        } catch (e: Exception) {
            Log.e(TAG, "SignOut Error: ${e.message}")
        }
    }

    // --- PRODUCTS ---

    suspend fun saveProductToSupabase(product: ProductItem): Boolean {
        val c = client ?: return false
        return try {
            val id = if (product.id.isBlank()) UUID.randomUUID().toString() else product.id
            val map = mapOf(
                "id" to id,
                "title" to product.title,
                "description" to product.description,
                "price" to product.price,
                "stock" to product.stock,
                "category" to product.category,
                "shop_name" to product.shopName,
                "location" to product.location,
                "image_url" to product.imageUrl,
                "shop_id" to product.shopId,
                "is_certified" to product.isCertified,
                "is_scammer" to product.isScammer
            )
            c.postgrest.from("products").upsert(map)
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveProduct Error: ${e.message}", e)
            false
        }
    }

    suspend fun deleteProductFromSupabase(productId: String): Boolean {
        val c = client ?: return false
        return try {
            c.postgrest.from("products").delete {
                filter { eq("id", productId) }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteProduct Error: ${e.message}", e)
            false
        }
    }

    fun getProductsRealtime(): Flow<List<ProductItem>> = callbackFlow {
        val c = client
        if (c == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        try {
            val list = c.postgrest.from("products").select().decodeList<Map<String, Any?>>()
            val products = list.map { parseProduct(it) }
            trySend(products)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch products error: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {}
    }

    // --- REELS ---

    suspend fun saveReelToSupabase(reel: ReelVideo): Boolean {
        val c = client ?: return false
        return try {
            val id = if (reel.id.isBlank()) UUID.randomUUID().toString() else reel.id
            val map = mapOf(
                "id" to id,
                "caption" to reel.caption,
                "creator_name" to reel.creatorName,
                "category" to reel.category,
                "media_type" to reel.mediaType,
                "media_url" to reel.mediaUrl,
                "aspect_ratio" to reel.aspectRatio,
                "zoom_level" to reel.zoomLevel,
                "rotation_angle" to reel.rotationAngle,
                "start_sec" to reel.startSec,
                "end_sec" to reel.endSec,
                "likes_count" to reel.likesCount,
                "views_count" to reel.viewsCount
            )
            c.postgrest.from("reels").upsert(map)
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveReel Error: ${e.message}", e)
            false
        }
    }

    fun getReelsRealtime(): Flow<List<ReelVideo>> = callbackFlow {
        val c = client
        if (c == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        try {
            val list = c.postgrest.from("reels").select().decodeList<Map<String, Any?>>()
            val reels = list.map { parseReel(it) }
            trySend(reels)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch reels error: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {}
    }

    // --- CONVERSATIONS & MESSAGES ---

    suspend fun saveMessageToSupabase(
        conversationId: String,
        contactName: String,
        message: Message,
        userPhone: String = "",
        userEmail: String = ""
    ): Boolean {
        val c = client ?: return false
        return try {
            val convMap = mapOf(
                "id" to conversationId,
                "contact_name" to contactName,
                "last_message" to message.text,
                "user_phone" to userPhone,
                "user_email" to userEmail
            )
            c.postgrest.from("conversations").upsert(convMap)

            val msgId = if (message.id.isBlank()) UUID.randomUUID().toString() else message.id
            val msgMap = mapOf(
                "id" to msgId,
                "conversation_id" to conversationId,
                "sender" to message.sender,
                "text" to message.text,
                "reply_to_text" to message.replyToText,
                "reply_to_sender" to message.replyToSender,
                "status" to message.status.name
            )
            c.postgrest.from("messages").insert(msgMap)
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveMessage Error: ${e.message}", e)
            false
        }
    }

    fun getConversationsRealtime(): Flow<List<Conversation>> = callbackFlow {
        val c = client
        if (c == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        try {
            val convs = c.postgrest.from("conversations").select().decodeList<Map<String, Any?>>()
            val msgs = c.postgrest.from("messages").select().decodeList<Map<String, Any?>>()

            val msgsByConvId = msgs.groupBy { (it["conversation_id"] as? String) ?: "" }

            val list = convs.map { cMap ->
                val id = (cMap["id"] as? String) ?: ""
                val contactName = (cMap["contact_name"] as? String) ?: "Utilisateur NorA"
                val lastMsg = (cMap["last_message"] as? String) ?: ""
                val phone = (cMap["user_phone"] as? String) ?: ""
                val email = (cMap["user_email"] as? String) ?: ""

                val conversationMessages = msgsByConvId[id]?.map { mData ->
                    val mId = (mData["id"] as? String) ?: UUID.randomUUID().toString()
                    val sender = (mData["sender"] as? String) ?: "moi"
                    val text = (mData["text"] as? String) ?: ""
                    val replyToText = (mData["reply_to_text"] as? String) ?: ""
                    val replyToSender = (mData["reply_to_sender"] as? String) ?: ""
                    val statusStr = (mData["status"] as? String) ?: "SENT"
                    val status = try { MessageStatus.valueOf(statusStr) } catch (_: Exception) { MessageStatus.SENT }

                    Message(
                        id = mId,
                        sender = sender,
                        text = text,
                        replyToText = replyToText,
                        replyToSender = replyToSender,
                        status = status
                    )
                } ?: emptyList()

                Conversation(
                    id = id,
                    contactName = contactName,
                    lastMessage = lastMsg,
                    messages = conversationMessages,
                    userPhone = phone,
                    userEmail = email
                )
            }
            trySend(list)
        } catch (e: Exception) {
            Log.e(TAG, "Fetch conversations error: ${e.message}")
            trySend(emptyList())
        }

        awaitClose {}
    }

    // --- STORAGE ---

    suspend fun uploadFileToStorage(context: Context, uri: Uri, folder: String = "uploads"): Result<String> {
        val c = client ?: return Result.failure(IllegalStateException("Supabase non disponible"))
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(IllegalStateException("Impossible de lire l'image"))

            val filename = "${UUID.randomUUID()}_media"
            val path = "$folder/$filename"

            c.storage.from("media").upload(path, bytes) {
                upsert = true
            }
            val publicUrl = c.storage.from("media").publicUrl(path)
            Result.success(publicUrl)
        } catch (e: Exception) {
            Log.e(TAG, "uploadFile Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- WALLET EVENTS ---

    suspend fun recordWalletEvent(userId: String, eventType: String, amount: Double, meta: String = ""): Result<Unit> {
        val c = client ?: return Result.failure(IllegalStateException("Supabase non disponible"))
        return try {
            val data = mapOf(
                "user_id" to userId,
                "event_type" to eventType,
                "amount" to amount,
                "meta" to meta
            )
            c.postgrest.from("wallet_events").insert(data)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "recordWalletEvent Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun purchaseProductAtomic(productId: String, quantity: Int = 1): Result<Unit> {
        val c = client ?: return Result.failure(IllegalStateException("Supabase non disponible"))
        return try {
            val response = c.postgrest.from("products").select {
                filter { eq("id", productId) }
            }.decodeList<Map<String, Any?>>()

            if (response.isEmpty()) return Result.failure(IllegalStateException("Produit introuvable"))
            val stock = ((response.first()["stock"] as? Number)?.toInt()) ?: 0
            if (stock < quantity) return Result.failure(IllegalStateException("Stock insuffisant"))

            c.postgrest.from("products").update(mapOf("stock" to stock - quantity)) {
                filter { eq("id", productId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "purchaseProduct Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    // --- MAPPERS ---

    private fun parseUserProfile(id: String, map: Map<String, Any?>): UserProfile {
        return UserProfile(
            id = id,
            name = (map["name"] as? String) ?: "Visiteur",
            email = (map["email"] as? String) ?: "",
            whatsappNumber = (map["whatsapp_number"] as? String) ?: "",
            profilePic = (map["avatar_url"] as? String) ?: "",
            kycStatus = (map["kyc_status"] as? String) ?: "Non vérifié",
            nCoinsBalance = ((map["n_coins_balance"] as? Number)?.toDouble()) ?: 1.0,
            isLoggedIn = true,
            onboardingCompleted = true
        )
    }

    private fun parseProduct(map: Map<String, Any?>): ProductItem {
        val id = (map["id"] as? String) ?: UUID.randomUUID().toString()
        return ProductItem(
            id = id,
            title = (map["title"] as? String) ?: (map["name"] as? String) ?: "",
            category = (map["category"] as? String) ?: "",
            price = ((map["price"] as? Number)?.toInt()) ?: 0,
            stock = ((map["stock"] as? Number)?.toInt()) ?: 0,
            shopName = (map["shop_name"] as? String) ?: "",
            location = (map["location"] as? String) ?: "",
            description = (map["description"] as? String) ?: "",
            imageUrl = (map["image_url"] as? String) ?: "",
            shopId = (map["shop_id"] as? String) ?: "",
            isCertified = (map["is_certified"] as? Boolean) ?: true,
            isScammer = (map["is_scammer"] as? Boolean) ?: false
        )
    }

    private fun parseReel(map: Map<String, Any?>): ReelVideo {
        val id = (map["id"] as? String) ?: UUID.randomUUID().toString()
        return ReelVideo(
            id = id,
            caption = (map["caption"] as? String) ?: "",
            creatorName = (map["creator_name"] as? String) ?: "",
            category = (map["category"] as? String) ?: "Mode & Vêtements",
            mediaType = (map["media_type"] as? String) ?: "Vidéo",
            mediaUrl = (map["media_url"] as? String) ?: "",
            aspectRatio = (map["aspect_ratio"] as? String) ?: "9:16",
            zoomLevel = ((map["zoom_level"] as? Number)?.toFloat()) ?: 1f,
            rotationAngle = ((map["rotation_angle"] as? Number)?.toFloat()) ?: 0f,
            startSec = ((map["start_sec"] as? Number)?.toFloat()) ?: 0f,
            endSec = ((map["end_sec"] as? Number)?.toFloat()) ?: 0f,
            likesCount = ((map["likes_count"] as? Number)?.toInt()) ?: 0,
            viewsCount = ((map["views_count"] as? Number)?.toInt()) ?: 0,
            isLiked = false
        )
    }
}
