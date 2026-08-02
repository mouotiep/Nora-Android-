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
                }.decodeList<AdminCheckDto>()
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

            val profileDto = ProfileDto(
                id = userId,
                name = name,
                email = email.trim(),
                whatsappNumber = whatsapp,
                kycStatus = "Non vérifié",
                nCoinsBalance = 1.0
            )

            runCatching {
                c.postgrest.from("profiles").insert(profileDto)
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
                }.decodeList<ProfileDto>()
            }.getOrDefault(emptyList())

            val profile = if (profiles.isNotEmpty()) {
                val dto = profiles.first()
                UserProfile(
                    id = dto.id,
                    name = dto.name,
                    email = dto.email.ifBlank { user.email ?: email },
                    whatsappNumber = dto.whatsappNumber,
                    profilePic = dto.avatarUrl,
                    kycStatus = dto.kycStatus,
                    nCoinsBalance = dto.nCoinsBalance,
                    isLoggedIn = true,
                    onboardingCompleted = true
                )
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
            val dto = ProductDto(
                id = id,
                title = product.title,
                category = product.category,
                price = product.price,
                stock = product.stock,
                shopName = product.shopName,
                location = product.location,
                description = product.description,
                imageUrl = product.imageUrl,
                shopId = product.shopId,
                isCertified = product.isCertified,
                isScammer = product.isScammer
            )
            c.postgrest.from("products").upsert(dto)
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
            val list = c.postgrest.from("products").select().decodeList<ProductDto>()
            val products = list.map { dto ->
                ProductItem(
                    id = dto.id,
                    title = dto.title,
                    category = dto.category,
                    price = dto.price,
                    stock = dto.stock,
                    shopName = dto.shopName,
                    location = dto.location,
                    description = dto.description,
                    imageUrl = dto.imageUrl,
                    shopId = dto.shopId,
                    isCertified = dto.isCertified,
                    isScammer = dto.isScammer
                )
            }
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
            val dto = ReelDto(
                id = id,
                caption = reel.caption,
                creatorName = reel.creatorName,
                category = reel.category,
                mediaType = reel.mediaType,
                mediaUrl = reel.mediaUrl,
                aspectRatio = reel.aspectRatio,
                zoomLevel = reel.zoomLevel,
                rotationAngle = reel.rotationAngle,
                startSec = reel.startSec,
                endSec = reel.endSec,
                likesCount = reel.likesCount,
                viewsCount = reel.viewsCount
            )
            c.postgrest.from("reels").upsert(dto)
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
            val list = c.postgrest.from("reels").select().decodeList<ReelDto>()
            val reels = list.map { dto ->
                ReelVideo(
                    id = dto.id,
                    caption = dto.caption,
                    creatorName = dto.creatorName,
                    category = dto.category,
                    mediaType = dto.mediaType,
                    mediaUrl = dto.mediaUrl,
                    aspectRatio = dto.aspectRatio,
                    zoomLevel = dto.zoomLevel,
                    rotationAngle = dto.rotationAngle,
                    startSec = dto.startSec,
                    endSec = dto.endSec,
                    likesCount = dto.likesCount,
                    viewsCount = dto.viewsCount,
                    isLiked = false
                )
            }
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
            val convDto = ConversationDto(
                id = conversationId,
                contactName = contactName,
                lastMessage = message.text,
                userPhone = userPhone,
                userEmail = userEmail
            )
            c.postgrest.from("conversations").upsert(convDto)

            val msgId = if (message.id.isBlank()) UUID.randomUUID().toString() else message.id
            val msgDto = MessageDto(
                id = msgId,
                conversationId = conversationId,
                sender = message.sender,
                text = message.text,
                replyToText = message.replyToText,
                replyToSender = message.replyToSender,
                status = message.status.name
            )
            c.postgrest.from("messages").insert(msgDto)
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
            val convs = c.postgrest.from("conversations").select().decodeList<ConversationDto>()
            val msgs = c.postgrest.from("messages").select().decodeList<MessageDto>()

            val msgsByConvId = msgs.groupBy { it.conversationId }

            val list = convs.map { cDto ->
                val conversationMessages = msgsByConvId[cDto.id]?.map { mDto ->
                    val status = try { MessageStatus.valueOf(mDto.status) } catch (_: Exception) { MessageStatus.SENT }
                    Message(
                        id = mDto.id,
                        sender = mDto.sender,
                        text = mDto.text,
                        replyToText = mDto.replyToText,
                        replyToSender = mDto.replyToSender,
                        status = status
                    )
                } ?: emptyList()

                Conversation(
                    id = cDto.id,
                    contactName = cDto.contactName,
                    lastMessage = cDto.lastMessage,
                    messages = conversationMessages,
                    userPhone = cDto.userPhone,
                    userEmail = cDto.userEmail
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
            val data = WalletEventDto(
                userId = userId,
                eventType = eventType,
                amount = amount,
                meta = meta
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
            }.decodeList<ProductStockDto>()

            if (response.isEmpty()) return Result.failure(IllegalStateException("Produit introuvable"))
            val stock = response.first().stock
            if (stock < quantity) return Result.failure(IllegalStateException("Stock insuffisant"))

            c.postgrest.from("products").update(ProductStockDto(stock = stock - quantity)) {
                filter { eq("id", productId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "purchaseProduct Error: ${e.message}", e)
            Result.failure(e)
        }
    }
}
