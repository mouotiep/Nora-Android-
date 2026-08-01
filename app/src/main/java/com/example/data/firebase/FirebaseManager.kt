package com.example.data.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.domain.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseManager {

    private const val TAG = "FirebaseManager"

    val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseAuth initialization warning: ${e.message}")
            null
        }
    }

    val firestore: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseFirestore initialization warning: ${e.message}")
            null
        }
    }

    val storage: FirebaseStorage? by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseStorage initialization warning: ${e.message}")
            null
        }
    }

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    fun isFirebaseAvailable(): Boolean {
        return try {
            auth != null && firestore != null
        } catch (e: Exception) {
            false
        }
    }

    fun initFirebase(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val app = FirebaseApp.initializeApp(context)
                if (app == null) {
                    val options = com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("1:977059813132:android:91b836e45ebe0cc7ab1b29")
                        .setProjectId("nora-cameroun")
                        .setApiKey("AIzaSyCYJ1wnQAz5F0ZAZaoCS7bFZQ4IT0DAw6c")
                        .setStorageBucket("nora-cameroun.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
                Log.d(TAG, "Firebase initialisé avec le projet nora-cameroun.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Init Firebase Error: ${e.message}")
        }
    }

    suspend fun ensureAuthenticated(): FirebaseUser? {
        val authInstance = auth ?: return null
        authInstance.currentUser?.let { return it }
        return try {
            val result = authInstance.signInAnonymously().await()
            Log.d(TAG, "Connexion anonyme Firebase réussie: ${result.user?.uid}")
            result.user
        } catch (e: Exception) {
            Log.e(TAG, "Échec connexion anonyme Firebase: ${e.message}")
            null
        }
    }

    suspend fun resolveActiveRole(uid: String): String {
        val db = firestore ?: return "Acheteur"
        return try {
            val doc = db.collection("admins").document(uid).get().await()
            if (doc.exists()) "Admin" else "Acheteur"
        } catch (e: Exception) {
            "Acheteur"
        }
    }

    suspend fun recordWalletEvent(userId: String, eventType: String, amount: Double, meta: String = ""): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore non initialisé"))
        ensureAuthenticated()
        return try {
            val eventId = UUID.randomUUID().toString()
            val data = mapOf(
                "eventId" to eventId,
                "userId" to userId,
                "eventType" to eventType,
                "amount" to amount,
                "meta" to meta,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("wallet_events").document(eventId).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun purchaseProductAtomic(productId: String, quantity: Int = 1): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore non initialisé"))
        ensureAuthenticated()
        return try {
            db.runTransaction { transaction ->
                val ref = db.collection("products").document(productId)
                val snapshot = transaction.get(ref)
                if (!snapshot.exists()) {
                    throw IllegalStateException("Produit introuvable")
                }
                val currentStock = (snapshot.getLong("stock") ?: 0L).toInt()
                if (currentStock < quantity) {
                    throw IllegalStateException("Stock insuffisant")
                }
                transaction.update(ref, "stock", currentStock - quantity)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- AUTHENTICATION ---

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        name: String,
        whatsapp: String
    ): Result<UserProfile> {
        val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase Auth non initialisé"))
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore non initialisé"))

        return try {
            val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return Result.failure(Exception("Création utilisateur échouée"))

            val profile = UserProfile(
                id = user.uid,
                name = name,
                email = email.trim(),
                whatsappNumber = whatsapp,
                isLoggedIn = true,
                onboardingCompleted = true
            )

            // Save user profile document in Firestore
            db.collection("users")
                .document(user.uid)
                .set(profileMap(profile))
                .await()

            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "SignUp Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<UserProfile> {
        val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase Auth non initialisé"))
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore non initialisé"))

        return try {
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), pass).await()
            val user = authResult.user ?: return Result.failure(Exception("Connexion échouée"))

            val snapshot = db.collection("users").document(user.uid).get().await()
            if (snapshot.exists()) {
                val data = snapshot.data ?: emptyMap()
                val profile = parseUserProfile(user.uid, data)
                Result.success(profile)
            } else {
                val profile = UserProfile(
                    id = user.uid,
                    email = user.email ?: email,
                    name = user.displayName ?: "Utilisateur Nora",
                    isLoggedIn = true,
                    onboardingCompleted = true
                )
                db.collection("users").document(user.uid).set(profileMap(profile)).await()
                Result.success(profile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn Error: ${e.message}")
            Result.failure(e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "SignOut Error: ${e.message}")
        }
    }

    // --- FIRESTORE PRODUCTS ---

    suspend fun saveProductToFirestore(product: ProductItem): Boolean {
        val db = firestore ?: return false
        ensureAuthenticated()
        return try {
            val id = if (product.id.isBlank()) UUID.randomUUID().toString() else product.id
            db.collection("products")
                .document(id)
                .set(productToMap(product.copy(id = id)))
                .await()
            true
        } catch (e: Exception) {
            val code = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code
            Log.e(TAG, "saveProduct Error: ${e.message} [code=$code]", e)
            false
        }
    }

    suspend fun deleteProductFromFirestore(productId: String): Boolean {
        val db = firestore ?: return false
        ensureAuthenticated()
        return try {
            db.collection("products").document(productId).delete().await()
            true
        } catch (e: Exception) {
            val code = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code
            Log.e(TAG, "deleteProduct Error: ${e.message} [code=$code]", e)
            false
        }
    }

    fun getProductsRealtime(): Flow<List<ProductItem>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("products")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    val code = e.code
                    Log.e(TAG, "Products listener error: ${e.message} [code=$code]", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { parseProduct(doc.id, it) }
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- FIRESTORE REELS ---

    suspend fun saveReelToFirestore(reel: ReelVideo): Boolean {
        val db = firestore ?: return false
        ensureAuthenticated()
        return try {
            val id = if (reel.id.isBlank()) UUID.randomUUID().toString() else reel.id
            db.collection("reels")
                .document(id)
                .set(reelToMap(reel.copy(id = id)))
                .await()
            true
        } catch (e: Exception) {
            val code = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code
            Log.e(TAG, "saveReel Error: ${e.message} [code=$code]", e)
            false
        }
    }

    fun getReelsRealtime(): Flow<List<ReelVideo>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("reels")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    val code = e.code
                    Log.e(TAG, "Reels listener error: ${e.message} [code=$code]", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { parseReel(doc.id, it) }
                    }
                    trySend(list)
                }
            }

        awaitClose { listener.remove() }
    }

    // --- FIRESTORE CONVERSATIONS & CHAT MESSAGES ---

    suspend fun saveMessageToFirestore(
        conversationId: String,
        contactName: String,
        message: Message,
        userPhone: String = "",
        userEmail: String = ""
    ): Boolean {
        val db = firestore ?: return false
        ensureAuthenticated()
        return try {
            val convRef = db.collection("conversations").document(conversationId)

            val convData = mapOf(
                "id" to conversationId,
                "contactName" to contactName,
                "lastMessage" to message.text,
                "lastTimestampMillis" to message.timestampMillis,
                "userPhone" to userPhone,
                "userEmail" to userEmail,
                "updatedAt" to System.currentTimeMillis()
            )
            convRef.set(convData, com.google.firebase.firestore.SetOptions.merge()).await()

            val msgId = message.id.ifBlank { UUID.randomUUID().toString() }
            val msgData = mapOf(
                "id" to msgId,
                "sender" to message.sender,
                "text" to message.text,
                "timestampMillis" to message.timestampMillis,
                "replyToText" to message.replyToText,
                "replyToSender" to message.replyToSender,
                "status" to message.status.name,
                "timestamp" to message.timestampMillis
            )
            convRef.collection("messages").document(msgId).set(msgData).await()
            true
        } catch (e: Exception) {
            val code = (e as? com.google.firebase.firestore.FirebaseFirestoreException)?.code
            Log.e(TAG, "saveMessage Error: ${e.message} [code=$code]", e)
            false
        }
    }

    fun getConversationsRealtime(): Flow<List<Conversation>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = db.collection("conversations")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    val code = e.code
                    Log.e(TAG, "Conversations listener error: ${e.message} [code=$code]", e)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val convDocs = snapshot.documents
                    if (convDocs.isEmpty()) {
                        trySend(emptyList())
                        return@addSnapshotListener
                    }
                    val conversationsList = mutableListOf<Conversation>()
                    var loadedCount = 0

                    for (doc in convDocs) {
                        val map = doc.data ?: continue
                        val id = doc.id
                        val contactName = (map["contactName"] as? String) ?: "Utilisateur NorA"
                        val lastMessage = (map["lastMessage"] as? String) ?: ""
                        val lastTimestamp = (map["lastTimestampMillis"] as? Long)
                            ?: (map["updatedAt"] as? Long)
                            ?: System.currentTimeMillis()
                        val userPhone = (map["userPhone"] as? String) ?: ""
                        val userEmail = (map["userEmail"] as? String) ?: ""

                        doc.reference.collection("messages")
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener { msgSnap ->
                                val messages = msgSnap.documents.mapNotNull { mDoc ->
                                    val mData = mDoc.data ?: return@mapNotNull null
                                    val mId = (mData["id"] as? String) ?: mDoc.id
                                    val ts = (mData["timestampMillis"] as? Long)
                                        ?: (mData["timestamp"] as? Long)
                                        ?: System.currentTimeMillis()
                                    val statusStr = (mData["status"] as? String) ?: "SENT"
                                    val status = try { com.example.domain.model.MessageStatus.valueOf(statusStr) } catch (_: Exception) { com.example.domain.model.MessageStatus.SENT }

                                    Message(
                                        id = mId,
                                        sender = (mData["sender"] as? String) ?: "moi",
                                        text = (mData["text"] as? String) ?: "",
                                        timestampMillis = ts,
                                        replyToText = (mData["replyToText"] as? String) ?: "",
                                        replyToSender = (mData["replyToSender"] as? String) ?: "",
                                        status = status
                                    )
                                }
                                synchronized(conversationsList) {
                                    conversationsList.add(
                                        Conversation(
                                            id = id,
                                            contactName = contactName,
                                            lastMessage = lastMessage,
                                            lastTimestampMillis = lastTimestamp,
                                            userPhone = userPhone,
                                            userEmail = userEmail,
                                            messages = messages
                                        )
                                    )
                                    loadedCount++
                                    if (loadedCount == convDocs.size) {
                                        trySend(conversationsList.toList())
                                    }
                                }
                            }
                            .addOnFailureListener {
                                synchronized(conversationsList) {
                                    loadedCount++
                                    if (loadedCount == convDocs.size) {
                                        trySend(conversationsList.toList())
                                    }
                                }
                            }
                    }
                }
            }

        awaitClose { listener.remove() }
    }

    // --- CLOUD STORAGE ---

    suspend fun uploadFileToStorage(context: Context, uri: Uri, folder: String = "uploads"): Result<String> {
        val st = storage ?: return Result.failure(IllegalStateException("Firebase Storage non disponible"))
        ensureAuthenticated()
        return try {
            val filename = "${UUID.randomUUID()}_media"
            val ref = st.reference.child("$folder/$filename")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            val code = (e as? com.google.firebase.storage.StorageException)?.errorCode
            Log.e(TAG, "Upload error: ${e.message} [code=$code]", e)
            Result.failure(e)
        }
    }

    // --- MAPPERS ---

    private fun profileMap(p: UserProfile) = mapOf(
        "id" to p.id,
        "name" to p.name,
        "email" to p.email,
        "whatsappNumber" to p.whatsappNumber,
        "shopName" to p.shopName,
        "hasShop" to p.hasShop,
        "kycStatus" to p.kycStatus,
        "profilePic" to p.profilePic,
        "nCoinsBalance" to p.nCoinsBalance
    )

    private fun parseUserProfile(id: String, map: Map<String, Any>): UserProfile {
        return UserProfile(
            id = id,
            name = (map["name"] as? String) ?: "",
            email = (map["email"] as? String) ?: "",
            whatsappNumber = (map["whatsappNumber"] as? String) ?: "",
            shopName = (map["shopName"] as? String) ?: "",
            hasShop = (map["hasShop"] as? Boolean) ?: false,
            kycStatus = (map["kycStatus"] as? String) ?: "Aucun",
            profilePic = (map["profilePic"] as? String) ?: "",
            nCoinsBalance = ((map["nCoinsBalance"] as? Number)?.toDouble()) ?: 1.0,
            isLoggedIn = true,
            onboardingCompleted = true
        )
    }

    private fun productToMap(p: ProductItem) = mapOf(
        "id" to p.id,
        "title" to p.title,
        "category" to p.category,
        "price" to p.price,
        "stock" to p.stock,
        "shopName" to p.shopName,
        "location" to p.location,
        "description" to p.description,
        "imageUrl" to p.imageUrl,
        "shopId" to p.shopId,
        "isCertified" to p.isCertified,
        "isScammer" to p.isScammer,
        "createdAt" to System.currentTimeMillis()
    )

    private fun parseProduct(id: String, map: Map<String, Any>): ProductItem {
        return ProductItem(
            id = id,
            title = (map["title"] as? String) ?: "",
            category = (map["category"] as? String) ?: "",
            price = ((map["price"] as? Number)?.toInt()) ?: 0,
            stock = ((map["stock"] as? Number)?.toInt()) ?: 0,
            shopName = (map["shopName"] as? String) ?: "",
            location = (map["location"] as? String) ?: "",
            description = (map["description"] as? String) ?: "",
            imageUrl = (map["imageUrl"] as? String) ?: "",
            shopId = (map["shopId"] as? String) ?: "",
            isCertified = (map["isCertified"] as? Boolean) ?: false,
            isScammer = (map["isScammer"] as? Boolean) ?: false
        )
    }

    private fun reelToMap(r: ReelVideo) = mapOf(
        "id" to r.id,
        "caption" to r.caption,
        "mediaUrl" to r.mediaUrl,
        "creatorName" to r.creatorName,
        "likesCount" to r.likesCount,
        "viewsCount" to r.viewsCount,
        "mediaType" to r.mediaType,
        "category" to r.category,
        "aspectRatio" to r.aspectRatio,
        "zoomLevel" to r.zoomLevel,
        "rotationAngle" to r.rotationAngle,
        "startSec" to r.startSec,
        "endSec" to r.endSec,
        "createdAt" to System.currentTimeMillis()
    )

    private fun parseReel(id: String, map: Map<String, Any>): ReelVideo {
        return ReelVideo(
            id = id,
            caption = (map["caption"] as? String) ?: "",
            mediaUrl = (map["mediaUrl"] as? String) ?: "",
            creatorName = (map["creatorName"] as? String) ?: "",
            likesCount = ((map["likesCount"] as? Number)?.toInt()) ?: 0,
            viewsCount = ((map["viewsCount"] as? Number)?.toInt()) ?: 0,
            isLiked = false,
            mediaType = (map["mediaType"] as? String) ?: "Vidéo",
            category = (map["category"] as? String) ?: "",
            aspectRatio = (map["aspectRatio"] as? String) ?: "9:16",
            zoomLevel = ((map["zoomLevel"] as? Number)?.toFloat()) ?: 1f,
            rotationAngle = ((map["rotationAngle"] as? Number)?.toFloat()) ?: 0f,
            startSec = ((map["startSec"] as? Number)?.toFloat()) ?: 0f,
            endSec = ((map["endSec"] as? Number)?.toFloat()) ?: 0f
        )
    }
}
