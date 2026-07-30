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
                val options = com.google.firebase.FirebaseOptions.Builder()
                    .setApplicationId("1:977059813132:android:91b836e45ebe0cc7ab1b29")
                    .setProjectId("nora-cameroun")
                    .setApiKey("AIzaSyCYJ1wnQAz5F0ZAZaoCS7bFZQ4IT0DAw6c")
                    .setStorageBucket("nora-cameroun.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialisé avec le projet nora-cameroun.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Init Firebase Error: ${e.message}")
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
        return try {
            val id = if (product.id.isBlank()) UUID.randomUUID().toString() else product.id
            db.collection("products")
                .document(id)
                .set(productToMap(product.copy(id = id)))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveProduct Error: ${e.message}")
            false
        }
    }

    suspend fun deleteProductFromFirestore(productId: String): Boolean {
        val db = firestore ?: return false
        return try {
            db.collection("products").document(productId).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "deleteProduct Error: ${e.message}")
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
                    Log.e(TAG, "Products listener error: ${e.message}")
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
        return try {
            val id = if (reel.id.isBlank()) UUID.randomUUID().toString() else reel.id
            db.collection("reels")
                .document(id)
                .set(reelToMap(reel.copy(id = id)))
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveReel Error: ${e.message}")
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
                    Log.e(TAG, "Reels listener error: ${e.message}")
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
        return try {
            val convRef = db.collection("conversations").document(conversationId)

            val convData = mapOf(
                "id" to conversationId,
                "contactName" to contactName,
                "lastMessage" to message.text,
                "lastTime" to message.time,
                "userPhone" to userPhone,
                "userEmail" to userEmail,
                "updatedAt" to System.currentTimeMillis()
            )
            convRef.set(convData, com.google.firebase.firestore.SetOptions.merge()).await()

            val msgId = UUID.randomUUID().toString()
            val msgData = mapOf(
                "id" to msgId,
                "sender" to message.sender,
                "text" to message.text,
                "time" to message.time,
                "replyToText" to message.replyToText,
                "replyToSender" to message.replyToSender,
                "timestamp" to System.currentTimeMillis()
            )
            convRef.collection("messages").document(msgId).set(msgData).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "saveMessage Error: ${e.message}")
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
                    Log.e(TAG, "Conversations listener error: ${e.message}")
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
                        val lastTime = (map["lastTime"] as? String) ?: ""
                        val userPhone = (map["userPhone"] as? String) ?: ""
                        val userEmail = (map["userEmail"] as? String) ?: ""

                        doc.reference.collection("messages")
                            .orderBy("timestamp", Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener { msgSnap ->
                                val messages = msgSnap.documents.mapNotNull { mDoc ->
                                    val mData = mDoc.data ?: return@mapNotNull null
                                    Message(
                                        sender = (mData["sender"] as? String) ?: "moi",
                                        text = (mData["text"] as? String) ?: "",
                                        time = (mData["time"] as? String) ?: "",
                                        replyToText = (mData["replyToText"] as? String) ?: "",
                                        replyToSender = (mData["replyToSender"] as? String) ?: ""
                                    )
                                }
                                synchronized(conversationsList) {
                                    conversationsList.add(
                                        Conversation(
                                            id = id,
                                            contactName = contactName,
                                            lastMessage = lastMessage,
                                            lastTime = lastTime,
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
        return try {
            val filename = "${UUID.randomUUID()}_media"
            val ref = st.reference.child("$folder/$filename")
            ref.putFile(uri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Upload error: ${e.message}")
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
            category = (map["category"] as? String) ?: ""
        )
    }
}
