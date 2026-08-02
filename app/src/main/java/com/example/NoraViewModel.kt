package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.UUID

class NoraViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("nora_prefs", Context.MODE_PRIVATE)
    private val moshi: Moshi = try {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    } catch (t: Throwable) {
        Moshi.Builder().build()
    }



    // Active Role state: Acheteur, Créateur, Admin
    private val _activeRole = MutableStateFlow("Acheteur")
    val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    // Selected screen/tab index state
    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    // Selected Shop for detailed viewing
    private val _selectedShopId = MutableStateFlow<String?>(null)
    val selectedShopId: StateFlow<String?> = _selectedShopId.asStateFlow()

    fun selectShop(shopId: String?) {
        _selectedShopId.value = shopId
    }

    fun selectShopAndNavigate(shopId: String) {
        _selectedShopId.value = shopId
        _currentTabIndex.value = 1 // Switch to Boutiques tab
    }

    private val _shops = MutableStateFlow<List<ShopItem>>(
        listOf(
            ShopItem(
                id = "shop-noun",
                name = "Les Merveilles du Noun",
                description = "Pagnes traditionnels Bamoun, étoffes royales, motifs Ndop et artisanat d'exception issus du Royaume Bamoun à Foumban.",
                category = "Mode & Vêtements",
                location = "Foumban",
                logoUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800",
                phone = "+237 699 112 233",
                isCertified = true,
                rating = 4.9f,
                reviewCount = 34,
                followersCount = 320
            ),
            ShopItem(
                id = "shop-sawa",
                name = "Sawa Elegance",
                description = "Robes traditionnelles Kaba Ndondo en soie légère, tenues côtières authentiques et créations élégantes de Douala.",
                category = "Mode & Vêtements",
                location = "Douala",
                logoUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?w=800",
                phone = "+237 677 889 900",
                isCertified = true,
                rating = 4.8f,
                reviewCount = 28,
                followersCount = 210
            ),
            ShopItem(
                id = "shop-penja",
                name = "Saveurs du Cameroun",
                description = "Spécialiste du Poivre blanc de Penja certifié IGP, épices rares et arômes uniques du terroir camerounais.",
                category = "Alimentation",
                location = "Penja",
                logoUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=800",
                phone = "+237 655 443 322",
                isCertified = true,
                rating = 4.95f,
                reviewCount = 52,
                followersCount = 480
            ),
            ShopItem(
                id = "shop-nord",
                name = "Artisans du Nord",
                description = "Créations en perles de bois précieux d'ébène, maroquinerie faite main et artisanat authentique du Grand Nord Cameroun.",
                category = "Accessoires & Bijou",
                location = "Maroua",
                logoUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1566150905458-1bf1fc15a7a0?w=800",
                phone = "+237 690 123 456",
                isCertified = false,
                rating = 4.5f,
                reviewCount = 15,
                followersCount = 95
            ),
            ShopItem(
                id = "shop-sawa-eco",
                name = "Eco-Design Sawa",
                description = "Sacs écoresponsables en raphia tressé, vannerie d'art et accessoires de mode naturels faits à la main à Yaoundé.",
                category = "Accessoires & Bijou",
                location = "Yaoundé",
                logoUrl = "https://images.unsplash.com/photo-1566150905458-1bf1fc15a7a0?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?w=800",
                phone = "+237 680 987 654",
                isCertified = true,
                rating = 4.7f,
                reviewCount = 22,
                followersCount = 175
            ),
            ShopItem(
                id = "shop-delices",
                name = "Délices Verts",
                description = "Confitures traditionnelles cuites au feu de bois, mangue-passion, confitures d'ananas et sirops naturels de Bafoussam.",
                category = "Alimentation",
                location = "Bafoussam",
                logoUrl = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=800",
                phone = "+237 671 223 344",
                isCertified = false,
                rating = 4.6f,
                reviewCount = 19,
                followersCount = 130
            ),
            ShopItem(
                id = "user-kyc-1",
                name = "L'Artisanal Douala",
                description = "Sculptures en bois d'ébène, masques traditionnels et objets d'art précieux façonnés par des maîtres sculpteurs du Littoral.",
                category = "Objets d'Art",
                location = "Douala (Akwa)",
                logoUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800",
                phone = "+237 699 887 766",
                isCertified = true,
                rating = 4.85f,
                reviewCount = 31,
                followersCount = 260
            ),
            ShopItem(
                id = "user-kyc-2",
                name = "Awa Couture & Toghu",
                description = "Tenues royales Toghu brodées à la main avec fils d'or, tissus Kente et vêtements culturels d'exception de Bamenda.",
                category = "Mode & Vêtements",
                location = "Bamenda / Yaoundé",
                logoUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500",
                bannerUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?w=800",
                phone = "+237 677 112 233",
                isCertified = true,
                rating = 4.9f,
                reviewCount = 45,
                followersCount = 390
            )
        )
    )
    val shops: StateFlow<List<ShopItem>> = _shops.asStateFlow()

    // Followed shops and content creators global state
    private val _followedShops = MutableStateFlow<Set<String>>(emptySet())
    val followedShops: StateFlow<Set<String>> = _followedShops.asStateFlow()

    private val _followedCreators = MutableStateFlow<Set<String>>(emptySet())
    val followedCreators: StateFlow<Set<String>> = _followedCreators.asStateFlow()

    // Search and category filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tous")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Categories list (mutable state in-memory)
    private val _categories = MutableStateFlow(
        mutableListOf(
            "Tous",
            "Électronique & High-Tech",
            "Santé & Beauté",
            "Mode & Vêtements",
            "Maison & Décoration",
            "Véhicules & Auto",
            "Agroalimentaire & Bio",
            "Services & Emploi",
            "Immobilier",
            "Bijoux & Accessoires",
            "Sports & Loisirs",
            "Bébés & Enfants",
            "Objets d'Art & Artisanat",
            "Musique & Culture"
        )
    )
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Portefeuille (Wallet) states
    private val _walletNCoins = MutableStateFlow(1.0)
    val walletNCoins: StateFlow<Double> = _walletNCoins.asStateFlow()

    // Monetary configurations
    private val _viewsRatio = MutableStateFlow(1000f) // 1000 views = 1 N Coin
    val viewsRatio: StateFlow<Float> = _viewsRatio.asStateFlow()

    private val _conversionRate = MutableStateFlow(5f) // 1 N Coin = 5 FCFA (Default, ranges from 1 to 10)
    val conversionRate: StateFlow<Float> = _conversionRate.asStateFlow()

    // Backup loader
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    // Active conversation detail
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    fun setActiveChatId(id: String?) {
        _activeChatId.value = id
    }

    private val _lastIncomingMessageConvId = MutableStateFlow("conv-3")
    val lastIncomingMessageConvId: StateFlow<String> = _lastIncomingMessageConvId.asStateFlow()

    fun setLastIncomingMessageConvId(id: String) {
        _lastIncomingMessageConvId.value = id
    }

    // User Profile
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Favorite Product IDs
    private val _favoriteProductIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteProductIds: StateFlow<Set<String>> = _favoriteProductIds.asStateFlow()

    fun toggleFavoriteProduct(productId: String) {
        _favoriteProductIds.update { current ->
            if (current.contains(productId)) {
                current - productId
            } else {
                current + productId
            }
        }
    }

    // Orders lists
    private val _orders = MutableStateFlow<List<NoraOrder>>(emptyList())
    val orders: StateFlow<List<NoraOrder>> = _orders.asStateFlow()

    // Global admin stats
    private val _totalUsersCount = MutableStateFlow(1)
    val totalUsersCount: StateFlow<Int> = _totalUsersCount.asStateFlow()

    private val _activeUsersCount = MutableStateFlow(1)
    val activeUsersCount: StateFlow<Int> = _activeUsersCount.asStateFlow()

    private val _totalDistributedNCoins = MutableStateFlow(1.0)
    val totalDistributedNCoins: StateFlow<Double> = _totalDistributedNCoins.asStateFlow()

    // Notification system
    private val _notifications = MutableStateFlow<List<String>>(
        listOf(
            "Bienvenue sur Nora Cameroun ! 🎉",
            "Votre compte a été crédité de 1 N-Coin de bienvenue ! 🪙"
        )
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    private val _hasUnreadNotifications = MutableStateFlow(true)
    val hasUnreadNotifications: StateFlow<Boolean> = _hasUnreadNotifications.asStateFlow()

    private val _currentNotification = MutableStateFlow<String?>(null)
    val currentNotification: StateFlow<String?> = _currentNotification.asStateFlow()

    fun postNotification(text: String) {
        _notifications.update { listOf(text) + it }
        _currentNotification.value = text
        _hasUnreadNotifications.value = true
    }

    fun markNotificationsAsRead() {
        _hasUnreadNotifications.value = false
    }

    fun clearNotificationHistory() {
        _notifications.value = emptyList()
        _hasUnreadNotifications.value = false
    }

    fun clearNotification() {
        _currentNotification.value = null
    }

    fun cashCommission(orderId: String) {
        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    o.copy(commissionCashed = true)
                } else {
                    o
                }
            }
        }
        postNotification("La commission de la commande #$orderId a été encaissée avec succès ! ✅")
    }

    // Sub-admin states for multi-agent coordination
    private val _isAdmin1Assigned = MutableStateFlow(false)
    val isAdmin1Assigned: StateFlow<Boolean> = _isAdmin1Assigned.asStateFlow()

    private val _isAdmin2Assigned = MutableStateFlow(false)
    val isAdmin2Assigned: StateFlow<Boolean> = _isAdmin2Assigned.asStateFlow()

    private val _currentSubAdmin = MutableStateFlow<String?>(null)
    val currentSubAdmin: StateFlow<String?> = _currentSubAdmin.asStateFlow()

    fun selectSubAdmin(adminRole: String) {
        _currentSubAdmin.value = adminRole
        if (adminRole == "Admin 1") {
            _isAdmin1Assigned.value = true
        } else if (adminRole == "Admin 2") {
            _isAdmin2Assigned.value = true
        }
    }

    fun releaseSubAdmin() {
        val current = _currentSubAdmin.value
        if (current == "Admin 1") {
            _isAdmin1Assigned.value = false
        } else if (current == "Admin 2") {
            _isAdmin2Assigned.value = false
        }
        _currentSubAdmin.value = null
    }

    fun resetAllSubAdmins() {
        _isAdmin1Assigned.value = false
        _isAdmin2Assigned.value = false
        _currentSubAdmin.value = null
    }

    // Simulated list of pending Shop KYC applications for Admin dashboard
    private val _kycApplications = MutableStateFlow<List<UserProfile>>(
        listOf(
            UserProfile(
                id = "user-kyc-1",
                name = "Samuel Eto'o Artisanat",
                whatsappNumber = "+237 699 887 766",
                email = "samuel.art@nora.cm",
                kycStatus = "En Attente",
                shopName = "L'Artisanal Douala",
                shopDescription = "Sculptures en bois d'ébène, masques traditionnels et objets d'art du Littoral.",
                shopCategory = "Objets d'Art",
                shopLocation = "Douala (Akwa), Cameroun",
                idCardPhoto = "cni_recto_verso_samuel.jpg",
                selfiePhoto = "selfie_cni_samuel.jpg"
            ),
            UserProfile(
                id = "user-kyc-2",
                name = "Awa Kente Design",
                whatsappNumber = "+237 677 112 233",
                email = "awa.kente@nora.cm",
                kycStatus = "En Attente",
                shopName = "Awa Couture & Toghu",
                shopDescription = "Vêtements traditionnels Toghu brodés à la main et tissus Kente de haute qualité.",
                shopCategory = "Mode & Vêtements",
                shopLocation = "Bamenda / Yaoundé, Cameroun",
                idCardPhoto = "cni_cameroun_awa.png",
                selfiePhoto = "selfie_verification_awa.png"
            )
        )
    )
    val kycApplications: StateFlow<List<UserProfile>> = _kycApplications.asStateFlow()

    // Admin Custom Advertisement banner poster (Marketplace Header Background)
    private val _adminAdTitle = MutableStateFlow("Marché Local Camerounais")
    val adminAdTitle: StateFlow<String> = _adminAdTitle.asStateFlow()

    private val _adminAdText = MutableStateFlow("Commandez directement auprès des meilleurs artisans de Yaoundé, Douala, Bafoussam et Garoua.")
    val adminAdText: StateFlow<String> = _adminAdText.asStateFlow()

    private val _adminAdImageUrl = MutableStateFlow("")
    val adminAdImageUrl: StateFlow<String> = _adminAdImageUrl.asStateFlow()

    fun updateAdminAd(title: String, text: String, imageUrl: String) {
        _adminAdTitle.value = title
        _adminAdText.value = text
        _adminAdImageUrl.value = imageUrl
    }

    // Products catalog
    private val _products = MutableStateFlow<List<ProductItem>>(
        listOf(
            ProductItem(
                id = "prod-1",
                title = "Pagne de Fête Royal Bamoun",
                category = "Mode & Vêtements",
                price = 25000,
                stock = 8,
                shopName = "Les Merveilles du Noun",
                location = "Foumban",
                description = "Magnifique pagne traditionnel Bamoun tissé à la main, idéal pour les grandes occasions, cérémonies traditionnelles et célébrations royales.",
                imageUrl = "https://images.unsplash.com/photo-1544441893-675973e31985?w=500",
                images = listOf(
                    "https://images.unsplash.com/photo-1544441893-675973e31985?w=500",
                    "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500",
                    "https://images.unsplash.com/photo-1566150905458-1bf1fc15a7a0?w=500"
                ),
                variants = listOf("Bleu Royal (Standard)", "Rouge Cérémonie", "Vert Ndop", "Taille Unique (2m x 1m50)"),
                shopId = "shop-noun",
                isCertified = true
            ),
            ProductItem(
                id = "prod-2",
                title = "Kaba Ndondo en Soie Fleurie",
                category = "Mode & Vêtements",
                price = 18000,
                stock = 12,
                shopName = "Sawa Elegance",
                location = "Douala",
                description = "Robe traditionnelle Kaba Ndondo des côtes camerounaises, revisitée avec de la soie légère et d'élégants motifs floraux.",
                imageUrl = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500",
                images = listOf(
                    "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500",
                    "https://images.unsplash.com/photo-1544441893-675973e31985?w=500",
                    "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=500"
                ),
                variants = listOf("Taille S", "Taille M", "Taille L", "Taille XL", "Rose Floral", "Jaune Sawa"),
                shopId = "shop-sawa",
                isCertified = true
            ),
            ProductItem(
                id = "prod-3",
                title = "Épices Secrètes du Penja (Sachet)",
                category = "Alimentation",
                price = 3500,
                stock = 50,
                shopName = "Saveurs du Cameroun",
                location = "Penja",
                description = "Poivre blanc de Penja d'indication géographique protégée, mondialement réputé pour son parfum exceptionnel et ses arômes uniques.",
                imageUrl = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500",
                images = listOf(
                    "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500",
                    "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500"
                ),
                variants = listOf("Sachet 100g", "Sachet 250g (+2000 FCFA)", "Moulé Fin", "Grains Entiers"),
                shopId = "shop-penja",
                isCertified = true
            ),
            ProductItem(
                id = "prod-4",
                title = "Collier de Perles en Bois Précieux",
                category = "Accessoires & Bijou",
                price = 7500,
                stock = 15,
                shopName = "Artisans du Nord",
                location = "Maroua",
                description = "Collier traditionnel de perles fait à la main avec du bois d'ébène poncé, des graines naturelles et des perles de couleur.",
                imageUrl = "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=500",
                images = listOf(
                    "https://images.unsplash.com/photo-1599643478518-a784e5dc4c8f?w=500",
                    "https://images.unsplash.com/photo-1566150905458-1bf1fc15a7a0?w=500"
                ),
                variants = listOf("Ébène Noir", "Perles Ambrées", "Multicolore"),
                shopId = "shop-nord",
                isCertified = false
            ),
            ProductItem(
                id = "prod-5",
                title = "Sac en Raphia Tissé Sawa",
                category = "Accessoires & Bijou",
                price = 12000,
                stock = 6,
                shopName = "Eco-Design Sawa",
                location = "Yaoundé",
                description = "Sac d'été écologique fabriqué à partir de fibres de raphia naturel tressé, orné de coutures en cuir pour un style unique.",
                imageUrl = "https://images.unsplash.com/photo-1566150905458-1bf1fc15a7a0?w=500",
                images = listOf(
                    "https://images.unsplash.com/photo-1566150905458-1bf1fc15a7a0?w=500",
                    "https://images.unsplash.com/photo-1544441893-675973e31985?w=500"
                ),
                variants = listOf("Naturel Raphia", "Lanières Cuir Brun", "Grand Format Beach"),
                shopId = "shop-sawa-eco",
                isCertified = true
            ),
            ProductItem(
                id = "prod-6",
                title = "Confiture Artisanale Mangue & Passion",
                category = "Alimentation",
                price = 2500,
                stock = 20,
                shopName = "Délices Verts",
                location = "Bafoussam",
                description = "Savoureuse confiture cuite au feu de bois avec des mangues fraîches du verger et des jus de fruits de la passion sauvages d'Afrique.",
                imageUrl = "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500",
                images = listOf(
                    "https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=500",
                    "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500"
                ),
                variants = listOf("Pot 250g", "Pot 500g (+1500 FCFA)", "Sans sucre ajouté"),
                shopId = "shop-delices",
                isCertified = false
            )
        )
    )
    val products: StateFlow<List<ProductItem>> = _products.asStateFlow()

    // Reels videos catalog
    private val _reels = MutableStateFlow<List<ReelVideo>>(
        listOf(
            ReelVideo(
                id = "vid-1",
                caption = "Défilé de mode traditionnelle au palais de Foumban 👑 #ArtBamoun",
                creatorName = "Cindy_Nora",
                likesCount = 1420,
                viewsCount = 12500,
                isLiked = false,
                category = "Mode & Vêtements",
                comments = listOf(
                    ReelComment("com-1", "vid-1", "Amina Yaoundé", "Magnifique ! Le travail du tissu est exceptionnel ! 😍", "Il y a 3h", reactions = mapOf("❤️" to 12, "👍" to 8, "🔥" to 5)),
                    ReelComment("com-2", "vid-1", "Jean_Douala", "Est-ce qu'on peut commander ce modèle directement sur Nora ?", "Il y a 1h", reactions = mapOf("👍" to 4))
                )
            ),
            ReelVideo(
                id = "vid-2",
                caption = "Démonstration de tissage du Kaba Ndondo en direct de Douala 🧵 #SawaSavoir",
                creatorName = "CoutureSawa",
                likesCount = 890,
                viewsCount = 7800,
                isLiked = false,
                category = "Mode & Vêtements",
                comments = listOf(
                    ReelComment("com-3", "vid-2", "Marc_Fm", "Une vraie transmission culturelle ! Bravo 👏", "Il y a 5h", reactions = mapOf("👏" to 9, "🔥" to 6))
                )
            ),
            ReelVideo(
                id = "vid-3",
                caption = "Préparation du poivre blanc de Penja : de la récolte au séchage 🌶️ #CamerounSaveurs",
                creatorName = "NordConfection",
                likesCount = 2310,
                viewsCount = 19800,
                isLiked = false,
                category = "Alimentation",
                comments = listOf(
                    ReelComment("com-4", "vid-3", "GourmetKmer", "Le meilleur poivre du monde entier, sans aucun doute !", "Hier", reactions = mapOf("🔥" to 15, "👍" to 7))
                )
            )
        )
    )
    val reels: StateFlow<List<ReelVideo>> = _reels.asStateFlow()

    // Shop Reviews catalog
    private val _shopReviews = MutableStateFlow<List<ShopReview>>(
        listOf(
            ShopReview(
                id = "rev-1",
                shopId = "shop-noun",
                shopName = "Les Merveilles du Noun",
                reviewerName = "Amina Yaoundé",
                rating = 5,
                comment = "Le pagne Bamoun est incroyable ! Une qualité royale de tissage à la main.",
                date = "Il y a 2 jours"
            ),
            ShopReview(
                id = "rev-2",
                shopId = "shop-sawa",
                shopName = "Sawa Elegance",
                reviewerName = "Jean_Douala",
                rating = 4,
                comment = "Très jolis motifs floraux sur le Kaba Ndondo, la livraison a été rapide !",
                date = "Il y a 1 semaine"
            ),
            ShopReview(
                id = "rev-3",
                shopId = "shop-penja",
                shopName = "Saveurs du Cameroun",
                reviewerName = "Chef_Bayam",
                rating = 5,
                comment = "Ce poivre blanc du Penja a une odeur formidable. Tous mes clients adorent mes plats !",
                date = "Hier"
            ),
            ShopReview(
                id = "rev-4",
                shopId = "shop-noun",
                shopName = "Les Merveilles du Noun",
                reviewerName = "Oumarou",
                rating = 5,
                comment = "Excellent service ! Recommandé à 100% pour l'authenticité culturelle.",
                date = "Il y a 4 jours"
            )
        )
    )
    val shopReviews: StateFlow<List<ShopReview>> = _shopReviews.asStateFlow()

    // Conversations state
    private val _conversations = MutableStateFlow<List<Conversation>>(
        listOf(
            Conversation(
                id = "conv-3",
                contactName = "Administrateur Nora (Coordination)",
                lastMessage = "Bonjour, bienvenue chez Nora pour vous servir",
                lastTimestampMillis = System.currentTimeMillis() - 3600000,
                messages = listOf(
                    Message(
                        sender = "admin",
                        text = "Bonjour, bienvenue chez Nora pour vous servir",
                        timestampMillis = System.currentTimeMillis() - 3600000,
                        status = MessageStatus.SENT
                    )
                )
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    fun startChatWithSeller(shopName: String, productTitle: String) {
        val existing = _conversations.value.find { it.contactName == shopName }
        val convId = existing?.id ?: "conv-${UUID.randomUUID().toString().take(6)}"
        val newMsg = Message(
            sender = "user",
            text = "Bonjour $shopName, je suis intéressé par votre produit '$productTitle'",
            timestampMillis = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )
        if (existing != null) {
            _conversations.update { list ->
                list.map { conv ->
                    if (conv.id == convId) {
                        conv.copy(
                            lastMessage = newMsg.text,
                            lastTimestampMillis = newMsg.timestampMillis,
                            messages = conv.messages + newMsg
                        )
                    } else conv
                }
            }
        } else {
            val newConv = Conversation(
                id = convId,
                contactName = shopName,
                lastMessage = newMsg.text,
                lastTimestampMillis = newMsg.timestampMillis,
                messages = listOf(newMsg)
            )
            _conversations.update { listOf(newConv) + it }
        }
        _activeChatId.value = convId
        _currentTabIndex.value = 3
    }

    // Transactions ledger
    private val _transactions = MutableStateFlow<List<Transaction>>(
        listOf(
            Transaction("Création du compte", "Crédit de bienvenue", 1.0, "01 Juil 2026", true)
        )
    )
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Moderation reports
    private val _reportedItems = MutableStateFlow<List<ReportedItem>>(emptyList())
    val reportedItems: StateFlow<List<ReportedItem>> = _reportedItems.asStateFlow()

    // Simulated registered accounts database
    private val _registeredAccounts = MutableStateFlow<Map<String, Account>>(
        mapOf(
            "mouotiep@gmail.com" to Account(
                "mouotiep@gmail.com",
                "Mouotie1@,*",
                UserProfile(
                    id = "admin-mouotiep",
                    name = "admin Nora",
                    whatsappNumber = "+237 655 924 778",
                    isLoggedIn = false,
                    onboardingCompleted = true,
                    email = "mouotiep@gmail.com",
                    kycStatus = "Certifié"
                )
            )
        )
    )
    val registeredAccounts: StateFlow<Map<String, Account>> = _registeredAccounts.asStateFlow()

    companion object {
        const val ADMIN_WHATSAPP_DISPLAY = "+237 655 924 778"
        const val ADMIN_WHATSAPP_CLEAN = "237655924778"

        /**
         * Validates and formats a Cameroonian phone number.
         * Syntax requirement: Starts with +237 (or 237/local 9 digits) followed by 9 digits.
         * Example: +237655924778 or +237 655 924 778 or 655924778 -> "+237 655 924 778"
         */
        fun validateAndFormatCameroonPhone(input: String): String? {
            val raw = input.trim().replace(" ", "").replace("-", "")
            if (raw.isBlank()) return null

            val digitsOnly = raw.removePrefix("+")

            val numPart = when {
                // Full international format with 237 prefix: e.g. 237655924778 (12 digits)
                digitsOnly.startsWith("237") && digitsOnly.length == 12 -> {
                    digitsOnly.substring(3)
                }
                // Local 9 digits starting with 2, 3, 6, 8: e.g. 655924778
                digitsOnly.length == 9 -> {
                    digitsOnly
                }
                else -> null
            }

            if (numPart != null && numPart.matches(Regex("^[2368][0-9]{8}$"))) {
                return "+237 ${numPart.substring(0, 3)} ${numPart.substring(3, 6)} ${numPart.substring(6)}"
            }
            return null
        }
    }

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            onResult(false, "Email et mot de passe requis")
            return
        }

        viewModelScope.launch {
            val res = com.example.data.supabase.SupabaseManager.signInWithEmail(trimmedEmail, password)
            res.onSuccess { profile ->
                val role = com.example.data.supabase.SupabaseManager.resolveActiveRole(profile.id)
                _userProfile.value = profile.copy(isLoggedIn = true)
                _activeRole.value = role
                _walletNCoins.value = profile.nCoinsBalance
                onResult(true, null)
            }.onFailure { err ->
                Log.e("NoraViewModel", "Supabase Auth sign in failed", err)
                onResult(false, err.localizedMessage ?: "Échec de la connexion Supabase. Vérifiez vos identifiants ou votre connexion réseau.")
            }
        }
    }

    fun findUserByReferralCodeOrLink(input: String): UserProfile? {
        if (input.isBlank()) return null
        
        // Extract code if it is a URL
        val code = if (input.startsWith("http")) {
            input.substringAfterLast("/").trim()
        } else {
            input.trim()
        }
        val targetCode = code.lowercase()
        
        for (account in _registeredAccounts.value.values) {
            val profile = account.profile
            val profileCode = profile.referralCode.lowercase()
            val profileNameCode = profile.name.lowercase().replace(" ", "_")
            if (profileCode == targetCode || profileNameCode == targetCode) {
                if (profile.email.isNotEmpty()) {
                    return profile
                }
            }
        }
        return null
    }

    fun registerUser(
        email: String,
        password: String,
        whatsappNumber: String,
        referredByCode: String? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val trimmedEmail = email.trim()
        val formattedWhatsapp = validateAndFormatCameroonPhone(whatsappNumber)
        if (formattedWhatsapp == null) {
            onResult(false, "Numéro WhatsApp invalide ! Syntaxe obligatoire : +237 suivi de 9 chiffres")
            return
        }
        if (trimmedEmail.isBlank() || password.isBlank()) {
            onResult(false, "Email et mot de passe requis")
            return
        }

        viewModelScope.launch {
            val res = com.example.data.supabase.SupabaseManager.signUpWithEmail(trimmedEmail, password, "Nouveau Membre", formattedWhatsapp)
            res.onSuccess { profile ->
                _userProfile.value = profile.copy(isLoggedIn = true)
                _walletNCoins.value = 1.0
                _activeRole.value = "Acheteur"
                _currentTabIndex.value = 0
                _totalUsersCount.update { it + 1 }
                _activeUsersCount.update { it + 1 }
                _totalDistributedNCoins.update { it + 1 }
                postNotification("Nouveau membre inscrit : $trimmedEmail ! 🪙 +1 N-Coin de bienvenue offert !")
                onResult(true, null)
            }.onFailure { err ->
                Log.e("NoraViewModel", "Supabase Auth sign up failed", err)
                onResult(false, err.localizedMessage ?: "Échec de l'inscription Supabase. Cet email est peut-être déjà utilisé.")
            }
        }
    }

    fun logoutUser() {
        com.example.data.supabase.SupabaseManager.signOut()
        releaseSubAdmin()
        _userProfile.value = UserProfile()
        _activeRole.value = "Acheteur"
        _currentTabIndex.value = 0
    }

    // Onboarding interest selection
    fun selectInterestsAndLogin(name: String, whatsapp: String, selectedInterests: List<String>) {
        val formattedWhatsapp = validateAndFormatCameroonPhone(whatsapp) ?: ADMIN_WHATSAPP_DISPLAY
        _userProfile.update {
            it.copy(
                name = name.ifBlank { "Utilisateur Camerounais" },
                whatsappNumber = formattedWhatsapp,
                interests = selectedInterests,
                isLoggedIn = true,
                onboardingCompleted = true,
                kycStatus = "Aucun"
            )
        }
        val current = _userProfile.value
        if (current.email.isNotEmpty() && current.email != "mouotiep@gmail.com") {
            val currentAccount = _registeredAccounts.value[current.email]
            if (currentAccount != null) {
                _registeredAccounts.update {
                    it + (current.email to currentAccount.copy(profile = current))
                }
            }
        }
    }

    // Adapt user preferences dynamically based on likes or orders
    fun adaptInterestsOnActivity(category: String) {
        _userProfile.update { current ->
            if (!current.interests.contains(category)) {
                val updated = current.interests + category
                current.copy(interests = updated)
            } else {
                current
            }
        }
    }

    // Submit Shop application for KYC verification
    fun submitShopKyc(
        shopName: String,
        shopDesc: String,
        shopCategory: String,
        location: String,
        idCardName: String,
        selfieName: String
    ) {
        _userProfile.update {
            it.copy(
                kycStatus = "En Attente",
                shopName = shopName,
                shopDescription = shopDesc,
                shopCategory = shopCategory,
                shopLocation = location,
                idCardPhoto = idCardName,
                selfiePhoto = selfieName
            )
        }

        // Add this app to the admin's KYC application list
        val app = _userProfile.value
        _kycApplications.update { list ->
            list.filter { it.id != app.id } + app
        }
    }

    // Admin KYC approvals / actions
    fun approveKyc(userId: String) {
        // Find application
        val app = _kycApplications.value.find { it.id == userId }
        
        // Update applications list: mark as Certifié so admin can still inspect, save or delete documents
        _kycApplications.update { list ->
            list.map { item ->
                if (item.id == userId) {
                    item.copy(kycStatus = "Certifié", hasShop = true)
                } else item
            }
        }

        // Update active user profile if matching
        if (_userProfile.value.id == userId || app?.id == _userProfile.value.id) {
            _userProfile.update {
                it.copy(kycStatus = "Certifié", hasShop = true)
            }
            if (_activeRole.value == "Acheteur") {
                _activeRole.value = "Créateur"
            }
        }

        // Update stored account if matching
        _registeredAccounts.value.values.find { it.profile.id == userId }?.profile?.let { prof ->
            val updatedProfile = prof.copy(kycStatus = "Certifié", hasShop = true)
            _registeredAccounts.update { map ->
                map.toMutableMap().apply {
                    put(prof.email, Account(prof.email, map[prof.email]?.password ?: "", updatedProfile))
                }
            }
        }

        val shopName = app?.shopName ?: "Boutique"
        // Update products for this seller/shop
        _products.update { list ->
            list.map { prod ->
                if (prod.shopName == shopName) {
                    prod.copy(isCertified = true, isScammer = false, isBanned = false)
                } else {
                    prod
                }
            }
        }

        postNotification("🎉 Votre demande KYC a été validée ! Vous êtes maintenant Vendeur Certifié sur NorA.")
    }

    fun deleteKycDocuments(userId: String) {
        _kycApplications.update { list ->
            list.map { app ->
                if (app.id == userId) {
                    app.copy(idCardPhoto = "Document supprimé", selfiePhoto = "Document supprimé")
                } else app
            }
        }
        if (_userProfile.value.id == userId) {
            _userProfile.update {
                it.copy(idCardPhoto = "Document supprimé", selfiePhoto = "Document supprimé")
            }
        }
        _registeredAccounts.update { map ->
            map.mapValues { entry ->
                if (entry.value.profile.id == userId) {
                    val updatedProf = entry.value.profile.copy(idCardPhoto = "Document supprimé", selfiePhoto = "Document supprimé")
                    entry.value.copy(profile = updatedProf)
                } else entry.value
            }
        }
    }

    fun sanctionKyc(userId: String, action: String) {
        // action can be: "Bannir", "Révoquer", "Arnaqueur"
        _kycApplications.update { list ->
            list.filter { it.id != userId }
        }

        val app = _kycApplications.value.find { it.id == userId }

        if (_userProfile.value.id == userId) {
            _userProfile.update {
                it.copy(kycStatus = action, hasShop = action == "Arnaqueur")
            }
        }

        // Update product certifications/scam flags in product list
        _products.update { list ->
            list.map { prod ->
                if (prod.shopName == (app?.shopName ?: _userProfile.value.shopName)) {
                    when (action) {
                        "Bannir" -> prod.copy(isBanned = true)
                        "Révoquer" -> prod.copy(isCertified = false)
                        "Arnaqueur" -> prod.copy(isScammer = true)
                        else -> prod
                    }
                } else {
                    prod
                }
            }
        }
    }

    // Role switcher
    fun setActiveRole(role: String) {
        _activeRole.value = role
    }

    // Set tab index
    fun setCurrentTabIndex(index: Int) {
        _currentTabIndex.value = index
    }

    // Filters
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    // Category creation (Only Admin can create categories)
    fun createCategory(name: String): Boolean {
        if (_activeRole.value != "Admin") return false
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !_categories.value.contains(trimmed)) {
            _categories.update {
                val newList = ArrayList(it)
                newList.add(trimmed)
                newList
            }
            return true
        }
        return false
    }

    // Video Likes
    fun toggleLike(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val nextLiked = !reel.isLiked
                    val diff = if (nextLiked) 1 else -1
                    if (nextLiked) {
                        adaptInterestsOnActivity(reel.category)
                    }
                    reel.copy(
                        isLiked = nextLiked,
                        likesCount = reel.likesCount + diff
                    )
                } else {
                    reel
                }
            }
        }
    }

    // Follow and unfollow methods for shops and creators
    fun toggleFollowShop(shopId: String) {
        _followedShops.update { set ->
            if (set.contains(shopId)) set - shopId else set + shopId
        }
    }

    fun toggleFollowCreator(creatorName: String) {
        _followedCreators.update { set ->
            if (set.contains(creatorName)) set - creatorName else set + creatorName
        }
        // Sync isFollowing across all reels for this creator
        _reels.update { list ->
            list.map { reel ->
                if (reel.creatorName == creatorName) {
                    reel.copy(isFollowing = _followedCreators.value.contains(creatorName))
                } else {
                    reel
                }
            }
        }
    }

    // Video Follows
    fun toggleFollow(reelId: String) {
        val creatorName = _reels.value.find { it.id == reelId }?.creatorName ?: return
        toggleFollowCreator(creatorName)
    }

    fun addComment(reelId: String, text: String) {
        val activeUser = _userProfile.value
        val newComment = ReelComment(
            id = "com-${UUID.randomUUID().toString().take(6)}",
            reelId = reelId,
            authorName = activeUser.name,
            text = text.trim(),
            time = "À l'instant"
        )
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    reel.copy(comments = reel.comments + newComment)
                } else {
                    reel
                }
            }
        }
    }

    fun reportComment(reelId: String, commentId: String, text: String) {
        val activeUser = _userProfile.value
        val newReport = ReportedItem(
            id = "rep-${UUID.randomUUID().toString().take(6)}",
            title = text,
            reason = "Commentaire de @${activeUser.name}",
            type = "Commentaire",
            reporterName = activeUser.name
        )
        _reportedItems.update { it + newReport }
    }

    private val _userLikedComments = MutableStateFlow<Set<String>>(emptySet())
    val userLikedComments: StateFlow<Set<String>> = _userLikedComments.asStateFlow()

    fun addReactionToComment(reelId: String, commentId: String, emoji: String) {
        val commentKey = "$reelId-$commentId"
        val alreadyLiked = _userLikedComments.value.contains(commentKey)
        
        _userLikedComments.update { current ->
            if (alreadyLiked) current - commentKey else current + commentKey
        }

        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val updatedComments = reel.comments.map { comment ->
                        if (comment.id == commentId) {
                            val currentCount = comment.reactions[emoji] ?: 0
                            val updatedReactions = comment.reactions.toMutableMap()
                            if (alreadyLiked) {
                                updatedReactions[emoji] = (currentCount - 1).coerceAtLeast(0)
                            } else {
                                updatedReactions[emoji] = currentCount + 1
                            }
                            comment.copy(reactions = updatedReactions)
                        } else {
                            comment
                        }
                    }
                    reel.copy(comments = updatedComments)
                } else {
                    reel
                }
            }
        }
    }

    fun addShopReview(shopId: String, shopName: String, reviewerName: String, rating: Int, comment: String) {
        val newReview = ShopReview(
            id = "rev-${UUID.randomUUID().toString().take(6)}",
            shopId = shopId,
            shopName = shopName,
            reviewerName = reviewerName,
            rating = rating,
            comment = comment.trim(),
            date = "À l'instant"
        )
        _shopReviews.update { listOf(newReview) + it }
    }

    fun publishReel(
        caption: String,
        category: String,
        mediaType: String = "Vidéo",
        aspectRatio: String = "9:16",
        zoomLevel: Float = 1f,
        rotationAngle: Float = 0f,
        mediaUrl: String = "",
        startSec: Float = 0f,
        endSec: Float = 0f
    ) {
        viewModelScope.launch {
            publishReelSafely(
                caption = caption,
                category = category,
                mediaType = mediaType,
                aspectRatio = aspectRatio,
                zoomLevel = zoomLevel,
                rotationAngle = rotationAngle,
                mediaUrl = mediaUrl,
                startSec = startSec,
                endSec = endSec
            )
        }
    }

    suspend fun publishReelSafely(
        caption: String,
        category: String,
        mediaType: String = "Vidéo",
        aspectRatio: String = "9:16",
        zoomLevel: Float = 1f,
        rotationAngle: Float = 0f,
        mediaUrl: String = "",
        startSec: Float = 0f,
        endSec: Float = 0f
    ): Result<ReelVideo> {
        val activeUser = _userProfile.value
        val newReel = ReelVideo(
            id = "vid-${java.util.UUID.randomUUID().toString().take(6)}",
            caption = caption.ifBlank { "Nouveau Reel Nora d'artisanat" },
            creatorName = activeUser.name,
            likesCount = 0,
            viewsCount = 0,
            isLiked = false,
            isFollowing = false,
            category = category.ifBlank { "Mode & Vêtements" },
            comments = emptyList(),
            mediaType = mediaType,
            aspectRatio = aspectRatio,
            zoomLevel = zoomLevel,
            rotationAngle = rotationAngle,
            mediaUrl = mediaUrl,
            startSec = startSec,
            endSec = endSec
        )
        val saved = com.example.data.supabase.SupabaseManager.saveReelToSupabase(newReel)
        return if (saved) {
            _reels.update { listOf(newReel) + it }
            Result.success(newReel)
        } else {
            Result.failure(IllegalStateException("Échec de l'enregistrement dans Supabase. Vérifiez votre connexion internet."))
        }
    }

    // Tracks viewed reel IDs per user to ensure unique view counting
    private val _viewedReelIdsByCurrentUser = MutableStateFlow<Set<String>>(emptySet())
    val viewedReelIdsByCurrentUser: StateFlow<Set<String>> = _viewedReelIdsByCurrentUser.asStateFlow()

    fun calculateCoinsForViews(views: Int): Double {
        if (views <= 0) return 0.0
        val ratio = _viewsRatio.value.coerceAtLeast(1f)
        return (views.toDouble() / ratio.toDouble()).coerceIn(0.0, 100000.0)
    }

    fun recordUniqueView(reelId: String): Boolean {
        if (_viewedReelIdsByCurrentUser.value.contains(reelId)) {
            return false // Already counted as a view for this user
        }
        _viewedReelIdsByCurrentUser.update { it + reelId }
        
        // Increment the view count by 1 in the reels list
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val oldViews = reel.viewsCount
                    val nextViews = oldViews + 1
                    
                    val oldCoins = calculateCoinsForViews(oldViews)
                    val nextCoins = calculateCoinsForViews(nextViews)
                    val bonusGained = nextCoins - oldCoins
                    
                    if (bonusGained > 0) {
                        _walletNCoins.update { it + bonusGained.toDouble() }
                        _transactions.update { tList ->
                            val nList = ArrayList(tList)
                            nList.add(
                                0,
                                Transaction(
                                    title = "Bonus Créateur \"${reel.creatorName}\"",
                                    description = "Gains d'audience unique (${nextViews} vues totalisées)",
                                    amount = bonusGained.toDouble(),
                                    date = "Aujourd'hui",
                                    isPositive = true
                                )
                            )
                            nList
                        }
                    }
                    reel.copy(viewsCount = nextViews)
                } else {
                    reel
                }
            }
        }
        return true
    }

    // Simulate views increments on Reels to reward Creators
    fun simulateViews(reelId: String, amount: Int) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val oldViews = reel.viewsCount
                    val nextViews = oldViews + amount
                    
                    val oldCoins = calculateCoinsForViews(oldViews)
                    val nextCoins = calculateCoinsForViews(nextViews)
                    val bonusGained = nextCoins - oldCoins

                    if (bonusGained > 0) {
                        _walletNCoins.update { it + bonusGained.toDouble() }
                        _transactions.update { tList ->
                            val nList = ArrayList(tList)
                            nList.add(
                                0, // Insert at top
                                Transaction(
                                    title = "Bonus Créateur \"${reel.creatorName}\"",
                                    description = "Généré par +$amount vues de son Reel",
                                    amount = bonusGained.toDouble(),
                                    date = "Aujourd'hui",
                                    isPositive = true
                                )
                            )
                            nList
                        }
                    }

                    reel.copy(viewsCount = nextViews)
                } else {
                    reel
                }
            }
        }
    }

    // Simulate referral/invitation signup (Earn 0.25 N-Coins per registration)
    fun simulateReferralSignUp(refereeName: String) {
        val reward = 0.25
        _walletNCoins.update { it + reward }
        
        // Definitively register the user in the model accounts database
        val email = "${refereeName.lowercase().replace(" ", "")}${UUID.randomUUID().toString().take(4)}@nora.cm"
        val code = refereeName.lowercase().replace(" ", "") + "-" + UUID.randomUUID().toString().take(4)
        val profile = UserProfile(
            id = "user-${UUID.randomUUID()}",
            name = refereeName,
            email = email,
            isLoggedIn = false,
            onboardingCompleted = true,
            referralCode = code,
            nCoinsBalance = 1.0
        )
        val account = Account(email, "123456", profile)
        _registeredAccounts.update { it + (email to account) }
        _totalUsersCount.update { it + 1 }
        _totalDistributedNCoins.update { it + 1.25 } // 1.0 welcome reward + 0.25 referral bonus

        _transactions.update { tList ->
            val nList = ArrayList(tList)
            nList.add(
                0,
                Transaction(
                    title = "Parrainage d'un ami 👥",
                    description = "Inscription de $refereeName via votre lien",
                    amount = reward,
                    date = "Aujourd'hui",
                    isPositive = true
                )
            )
            nList
        }
    }

    // Add dynamic Product to Marketplace Catalog
    fun addProduct(
        title: String,
        category: String,
        price: Int,
        stock: Int,
        shopName: String,
        location: String,
        description: String,
        imageUrl: String,
        images: List<String> = emptyList(),
        variants: List<String> = emptyList(),
        offersDelivery: Boolean = false,
        deliveryCost: Int = 0
    ) {
        viewModelScope.launch {
            addProductSafely(
                title = title,
                category = category,
                price = price,
                stock = stock,
                shopName = shopName,
                location = location,
                description = description,
                imageUrl = imageUrl,
                images = images,
                variants = variants,
                offersDelivery = offersDelivery,
                deliveryCost = deliveryCost
            )
        }
    }

    suspend fun addProductSafely(
        title: String,
        category: String,
        price: Int,
        stock: Int,
        shopName: String,
        location: String,
        description: String,
        imageUrl: String,
        images: List<String> = emptyList(),
        variants: List<String> = emptyList(),
        offersDelivery: Boolean = false,
        deliveryCost: Int = 0
    ): Result<ProductItem> {
        val activeUser = _userProfile.value
        val mainPhoto = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1544441893-675973e31985?w=500" }
        val allImagesList = if (images.isEmpty()) listOf(mainPhoto) else images
        val newProduct = ProductItem(
            id = "prod-${UUID.randomUUID()}",
            title = title.ifEmpty { "Produit Artisanal" },
            category = if (category == "Tous") "Mode & Vêtements" else category,
            price = if (price <= 0) 5000 else price,
            stock = if (stock <= 0) 5 else stock,
            shopName = shopName.ifEmpty { activeUser.shopName.ifEmpty { "Artisan du Cameroun" } },
            location = location.ifEmpty { activeUser.shopLocation.ifEmpty { "Douala" } },
            description = description.ifEmpty { "Produit conçu à la main avec passion et authenticité camerounaise." },
            imageUrl = mainPhoto,
            images = allImagesList,
            variants = variants,
            shopId = activeUser.id,
            isCertified = activeUser.kycStatus == "Certifié",
            isScammer = activeUser.kycStatus == "Arnaqueur",
            isBanned = activeUser.kycStatus == "Banni",
            offersDelivery = offersDelivery,
            deliveryCost = deliveryCost
        )
        val saved = com.example.data.supabase.SupabaseManager.saveProductToSupabase(newProduct)
        return if (saved) {
            _products.update { it + newProduct }
            Result.success(newProduct)
        } else {
            Result.failure(IllegalStateException("Échec de l'enregistrement du produit dans Supabase. Vérifiez votre connexion."))
        }
    }

    fun deleteProduct(productId: String) {
        _products.update { list -> list.filter { it.id != productId } }
    }

    fun deleteReel(reelId: String) {
        _reels.update { list -> list.filter { it.id != reelId } }
    }

    fun addToCart(product: ProductItem) {
        purchaseProduct(product, payInNCoins = false)
    }

    // Purchase product & create order
    fun purchaseProduct(product: ProductItem, payInNCoins: Boolean, coinsUsedForDiscount: Double = 0.0): NoraOrder? {
        if (product.stock <= 0) return null

        // Enforce the 5% maximum discount rule from N-Coins
        val maxAllowedDiscountFCFA = product.price * 0.05
        val maxAllowedDiscountCoins = maxAllowedDiscountFCFA / _conversionRate.value

        if (payInNCoins) {
            // Under the new policy, paying 100% in N-Coins is not allowed as the discount is capped at 5%
            return null
        }

        if (coinsUsedForDiscount > maxAllowedDiscountCoins + 0.01) { // allow a very small float tolerance
            return null // Discount exceeds 5% of product price
        }

        val activeUser = _userProfile.value
        val costInCoins = coinsUsedForDiscount

        if (coinsUsedForDiscount > 0.0 && _walletNCoins.value < coinsUsedForDiscount) {
            return null // Insufficient coins for discount
        }

        // Deduct N Coins
        if (coinsUsedForDiscount > 0.0) {
            _walletNCoins.update { it - coinsUsedForDiscount }
            _transactions.update { tList ->
                val nList = ArrayList(tList)
                nList.add(
                    0,
                    Transaction(
                        title = "Réduction ${product.title}",
                        description = "Réduction de ${(coinsUsedForDiscount * _conversionRate.value).toInt()} FCFA appliquée (Max 5%)",
                        amount = -coinsUsedForDiscount,
                        date = "Aujourd'hui",
                        isPositive = false
                    )
                )
                nList
            }
            viewModelScope.launch {
                com.example.data.supabase.SupabaseManager.recordWalletEvent(
                    userId = activeUser.id,
                    eventType = "PURCHASE_DISCOUNT",
                    amount = -coinsUsedForDiscount,
                    meta = "Réduction sur le produit ${product.id}"
                )
            }
        }

        // Reduce stock atomically in Supabase & locally
        _products.update { pList ->
            pList.map { p ->
                if (p.id == product.id) {
                    p.copy(stock = (p.stock - 1).coerceAtLeast(0))
                } else {
                    p
                }
            }
        }
        viewModelScope.launch {
            com.example.data.supabase.SupabaseManager.purchaseProductAtomic(product.id, 1)
        }

        // Adapt user interest automatically based on this purchased product
        adaptInterestsOnActivity(product.category)

        // Create the Nora Order
        val newOrder = NoraOrder(
            id = "order-${UUID.randomUUID().toString().take(6)}",
            productId = product.id,
            productTitle = product.title,
            productPrice = product.price,
            buyerName = activeUser.name,
            buyerWhatsApp = activeUser.whatsappNumber,
            sellerName = product.shopName,
            sellerWhatsApp = "+237 655 924 778", // Admin/Seller WhatsApp
            payInNCoins = payInNCoins,
            coinsCost = costInCoins,
            status = "En attente de livraison",
            date = "Aujourd'hui",
            buyerEmail = activeUser.email,
            productImageUrl = product.imageUrl
        )

        _orders.update { it + newOrder }

        // Alert administrator via Push Notification with full order details
        postNotification("🛒 Nouvelle Commande #${newOrder.id} ! Acheteur: ${activeUser.name} (${activeUser.whatsappNumber}) | Produit: ${product.title} | Total: ${if (payInNCoins) "$costInCoins Coins" else "${product.price} FCFA"}")

        // Alert the administrator about this order & open a conversation
        _conversations.update { list ->
            val adminConv = list.find { it.id == "conv-3" }
            if (adminConv != null) {
                val updatedMessages = ArrayList(adminConv.messages)
                val priceDetail = if (payInNCoins) {
                    "$costInCoins N Coins"
                } else if (coinsUsedForDiscount > 0) {
                    "${product.price - (coinsUsedForDiscount * _conversionRate.value.toInt())} FCFA à la livraison (Réduction de ${coinsUsedForDiscount * _conversionRate.value.toInt()} FCFA via $coinsUsedForDiscount N Coins)"
                } else {
                    "${product.price} FCFA à la livraison"
                }
                val now = System.currentTimeMillis()
                updatedMessages.add(
                    Message(
                        sender = "admin",
                        text = "ALERTE COMMANDE : ${activeUser.name} a commandé '${product.title}' chez ${product.shopName}. Solde: $priceDetail. Je coordonne la livraison.",
                        timestampMillis = now,
                        status = MessageStatus.SENT
                    )
                )
                list.map { conv ->
                    if (conv.id == "conv-3") {
                        conv.copy(
                            lastMessage = "ALERTE COMMANDE: ${product.title}",
                            lastTimestampMillis = now,
                            messages = updatedMessages
                        )
                    } else {
                        conv
                    }
                }
            } else {
                list
            }
        }

        return newOrder
    }

    // QR Delivery Scan completion
    fun scanDeliveryQrCode(orderId: String): Boolean {
        val ord = _orders.value.find { it.id == orderId } ?: return false
        if (ord.status == "Livré & Payé") return false

        // Mark order as Delivered & Paid
        _orders.update { list ->
            list.map { o ->
                if (o.id == orderId) {
                    o.copy(status = "Livré & Payé")
                } else {
                    o
                }
            }
        }

        // Post order validation push notification with precise text requested
        postNotification("Commande validée par l'administrateur. Vous devez livrer. (CMD #$orderId)")

        // Log transaction and simulate fee collection/payouts
        val sellerGains = ord.productPrice
        val adminFee = (sellerGains * 0.05).toInt() // 5% Admin Fee
        val netSellerGains = sellerGains - adminFee

        // If paid in N Coins, convert to coins. Else log in transactions as Delivery Success
        _transactions.update { tList ->
            val nList = ArrayList(tList)
            nList.add(
                0,
                Transaction(
                    title = "Livraison Validée: #${ord.id}",
                    description = "Produit '${ord.productTitle}' reçu. Paiement de ${ord.productPrice} FCFA effectué.",
                    amount = if (ord.payInNCoins) ord.coinsCost else 0.0,
                    date = "À l'instant",
                    isPositive = true
                )
            )
            nList
        }

        // Update conversation with success message
        _conversations.update { list ->
            val adminConv = list.find { it.id == "conv-3" }
            if (adminConv != null) {
                val updatedMessages = ArrayList(adminConv.messages)
                val now = System.currentTimeMillis()
                updatedMessages.add(
                    Message(
                        sender = "admin",
                        text = "LIVRAISON CONFIRMÉE PAR SCAN QR! Transaction #${ord.id} finalisée. Commission de 5% (${adminFee} FCFA) déduite pour Nora Admin. Vendeur crédité de ${netSellerGains} FCFA.",
                        timestampMillis = now,
                        status = MessageStatus.SENT
                    )
                )
                list.map { conv ->
                    if (conv.id == "conv-3") {
                        conv.copy(
                            lastMessage = "Livraison validée par QR !",
                            lastTimestampMillis = now,
                            messages = updatedMessages
                        )
                    } else {
                        conv
                    }
                }
            } else {
                list
            }
        }

        return true
    }

    // Report content system
    fun reportItem(targetId: String, targetName: String, reason: String, type: String) {
        val activeUser = _userProfile.value
        val newReport = ReportedItem(
            id = "rep-${UUID.randomUUID().toString().take(6)}",
            title = targetName,
            reason = reason.ifBlank { "Contenu suspect ou inapproprié" },
            type = type,
            reporterName = activeUser.name
        )

        _reportedItems.update { it + newReport }
    }

    // Chat messaging
    fun sendMessage(conversationId: String, text: String, replyToText: String = "", replyToSender: String = "") {
        if (text.trim().isEmpty()) return
        
        val senderVal = if (_activeRole.value == "Admin") "admin" else "moi"
        val newMsgId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val newMsg = Message(
            id = newMsgId,
            sender = senderVal,
            text = text.trim(),
            timestampMillis = now,
            replyToText = replyToText,
            replyToSender = replyToSender,
            status = MessageStatus.SENDING
        )
        
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    val updatedMessages = ArrayList(conv.messages)
                    updatedMessages.add(newMsg)
                    conv.copy(
                        lastMessage = text.trim(),
                        lastTimestampMillis = now,
                        messages = updatedMessages
                    )
                } else {
                    conv
                }
            }
        }

        // Save message to online Supabase database
        viewModelScope.launch {
            val currentConv = _conversations.value.find { it.id == conversationId }
            val contactName = currentConv?.contactName ?: "Utilisateur NorA"
            val uPhone = currentConv?.userPhone ?: _userProfile.value.whatsappNumber
            val uEmail = currentConv?.userEmail ?: _userProfile.value.email
            val success = com.example.data.supabase.SupabaseManager.saveMessageToSupabase(
                conversationId = conversationId,
                contactName = contactName,
                message = newMsg.copy(status = MessageStatus.SENT),
                userPhone = uPhone,
                userEmail = uEmail
            )
            _conversations.update { list ->
                list.map { conv ->
                    if (conv.id == conversationId) {
                        val updatedMsgs = conv.messages.map { msg ->
                            if (msg.id == newMsgId) {
                                msg.copy(status = if (success) MessageStatus.SENT else MessageStatus.FAILED)
                            } else msg
                        }
                        conv.copy(messages = updatedMsgs)
                    } else conv
                }
            }
        }

        _lastIncomingMessageConvId.value = conversationId

        if (_activeRole.value == "Admin") {
            postNotification("Message de NorA Support : ${text.trim()}")
        } else if (conversationId == "conv-3" || conversationId.contains("admin")) {
            viewModelScope.launch {
                delay(1200)
                val replyText = if (text.startsWith("[VoiceNote:")) {
                    "NorA Support : Nous avons bien reçu votre note vocale. Un conseiller va vous assister."
                } else {
                    "NorA Support : Bonjour ! Nous avons bien reçu votre message. Un agent de support traite votre demande."
                }
                val replyNow = System.currentTimeMillis()
                val adminReplyMsg = Message(
                    sender = "admin",
                    text = replyText,
                    timestampMillis = replyNow,
                    status = MessageStatus.SENT
                )
                _conversations.update { list ->
                    list.map { conv ->
                        if (conv.id == conversationId) {
                            val updatedMessages = ArrayList(conv.messages)
                            updatedMessages.add(adminReplyMsg)
                            conv.copy(
                                lastMessage = replyText,
                                lastTimestampMillis = replyNow,
                                messages = updatedMessages
                            )
                        } else {
                            conv
                        }
                    }
                }
                try {
                    val currentConv = _conversations.value.find { it.id == conversationId }
                    com.example.data.supabase.SupabaseManager.saveMessageToSupabase(
                        conversationId = conversationId,
                        contactName = currentConv?.contactName ?: "NorA Support",
                        message = adminReplyMsg
                    )
                } catch (_: Throwable) {}
                postNotification("Nouveau message de NorA Support")
            }
        }
    }

    fun retryMessage(conversationId: String, messageId: String) {
        val conv = _conversations.value.find { it.id == conversationId } ?: return
        val targetMsg = conv.messages.find { it.id == messageId } ?: return

        _conversations.update { list ->
            list.map { c ->
                if (c.id == conversationId) {
                    val updatedMsgs = c.messages.map { m ->
                        if (m.id == messageId) m.copy(status = MessageStatus.SENDING) else m
                    }
                    c.copy(messages = updatedMsgs)
                } else c
            }
        }

        viewModelScope.launch {
            val contactName = conv.contactName.ifBlank { "Utilisateur NorA" }
            val uPhone = conv.userPhone.ifBlank { _userProfile.value.whatsappNumber }
            val uEmail = conv.userEmail.ifBlank { _userProfile.value.email }
            val success = com.example.data.supabase.SupabaseManager.saveMessageToSupabase(
                conversationId = conversationId,
                contactName = contactName,
                message = targetMsg.copy(status = MessageStatus.SENT),
                userPhone = uPhone,
                userEmail = uEmail
            )
            _conversations.update { list ->
                list.map { c ->
                    if (c.id == conversationId) {
                        val updatedMsgs = c.messages.map { m ->
                            if (m.id == messageId) {
                                m.copy(status = if (success) MessageStatus.SENT else MessageStatus.FAILED)
                            } else m
                        }
                        c.copy(messages = updatedMsgs)
                    } else c
                }
            }
        }
    }

    fun adminContactUser(contactName: String, initialMessage: String) {
        val cleanName = contactName.trim()
        val existing = _conversations.value.find { it.contactName.equals(cleanName, ignoreCase = true) }
        val convId = existing?.id ?: "conv-user-${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val msg = Message(
            sender = "admin",
            text = initialMessage,
            timestampMillis = now,
            status = MessageStatus.SENT
        )
        if (existing == null) {
            val newConv = Conversation(
                id = convId,
                contactName = cleanName,
                lastMessage = initialMessage,
                lastTimestampMillis = now,
                messages = listOf(msg)
            )
            _conversations.update { it + newConv }
        } else {
            val updatedMessages = ArrayList(existing.messages)
            updatedMessages.add(msg)
            _conversations.update { list ->
                list.map { conv ->
                    if (conv.id == existing.id) {
                        conv.copy(
                            lastMessage = initialMessage,
                            lastTimestampMillis = now,
                            messages = updatedMessages
                        )
                    } else {
                        conv
                    }
                }
            }
        }
        _lastIncomingMessageConvId.value = convId
        _activeChatId.value = convId
        _currentTabIndex.value = 2 // Switch to Chat tab

        viewModelScope.launch {
            try {
                com.example.data.supabase.SupabaseManager.saveMessageToSupabase(
                    conversationId = convId,
                    contactName = cleanName,
                    message = msg
                )
            } catch (_: Throwable) {}
        }
    }

    fun adminContactUserWithDetails(user: UserProfile, initialMessage: String) {
        val cleanName = user.name.trim().ifBlank { "Utilisateur" }
        val existing = _conversations.value.find { it.userId == user.id || it.contactName.equals(cleanName, ignoreCase = true) }
        val convId = existing?.id ?: "conv-user-${System.currentTimeMillis()}"
        val now = System.currentTimeMillis()
        val msg = Message(
            sender = "admin",
            text = initialMessage,
            timestampMillis = now,
            status = MessageStatus.SENT
        )
        if (existing == null) {
            val newConv = Conversation(
                id = convId,
                contactName = cleanName,
                lastMessage = initialMessage,
                lastTimestampMillis = now,
                messages = listOf(msg),
                userPhone = user.whatsappNumber,
                userEmail = user.email,
                userId = user.id
            )
            _conversations.update { it + newConv }
        } else {
            val updatedMessages = ArrayList(existing.messages)
            updatedMessages.add(msg)
            _conversations.update { list ->
                list.map { conv ->
                    if (conv.id == existing.id) {
                        conv.copy(
                            lastMessage = initialMessage,
                            lastTimestampMillis = now,
                            messages = updatedMessages,
                            userPhone = user.whatsappNumber.ifBlank { conv.userPhone },
                            userEmail = user.email.ifBlank { conv.userEmail },
                            userId = user.id.ifBlank { conv.userId }
                        )
                    } else {
                        conv
                    }
                }
            }
        }
        _lastIncomingMessageConvId.value = convId
        _activeChatId.value = convId
        _currentTabIndex.value = 2
        viewModelScope.launch {
            try {
                com.example.data.supabase.SupabaseManager.saveMessageToSupabase(
                    conversationId = convId,
                    contactName = cleanName,
                    message = msg,
                    userPhone = user.whatsappNumber,
                    userEmail = user.email
                )
            } catch (_: Throwable) {}
        }
    }

    fun deleteUser(userId: String) {
        _registeredAccounts.update { map ->
            map.filterValues { it.profile.id != userId }
        }
        _kycApplications.update { list ->
            list.filter { it.id != userId }
        }
        postNotification("Utilisateur supprimé de la base de données.")
    }

    fun banUser(userId: String) {
        _registeredAccounts.update { map ->
            map.mapValues { entry ->
                if (entry.value.profile.id == userId) {
                    entry.value.copy(
                        profile = entry.value.profile.copy(kycStatus = "Banni")
                    )
                } else {
                    entry.value
                }
            }
        }
        _kycApplications.update { list ->
            list.map { app ->
                if (app.id == userId) app.copy(kycStatus = "Banni") else app
            }
        }
        if (_userProfile.value.id == userId) {
            _userProfile.update { it.copy(kycStatus = "Banni") }
        }
        postNotification("Utilisateur banni de l'application.")
    }

    // Update profile
    fun updateProfile(name: String, whatsapp: String, profilePic: String) {
        val formattedWhatsapp = if (whatsapp.isNotBlank()) {
            validateAndFormatCameroonPhone(whatsapp) ?: _userProfile.value.whatsappNumber
        } else {
            _userProfile.value.whatsappNumber
        }
        _userProfile.update {
            it.copy(
                name = name.ifBlank { it.name },
                whatsappNumber = formattedWhatsapp,
                profilePic = when (profilePic) {
                    "clear" -> ""
                    else -> profilePic.ifBlank { it.profilePic }
                }
            )
        }
    }

    // Update shop profile
    fun updateShopProfile(shopName: String, shopDesc: String, shopLoc: String, shopPic: String) {
        val sName = shopName.ifBlank { _userProfile.value.shopName }
        val sDesc = shopDesc.ifBlank { _userProfile.value.shopDescription }
        val sLoc = shopLoc.ifBlank { _userProfile.value.shopLocation }
        val sPic = shopPic.ifBlank { _userProfile.value.shopPic }

        _userProfile.update {
            it.copy(
                shopName = sName,
                shopDescription = sDesc,
                shopLocation = sLoc,
                shopPic = sPic
            )
        }

        // Upsert into global _shops list so it is immediately visible in the Boutiques tab & shop list
        if (sName.isNotBlank()) {
            _shops.update { currentShops ->
                val existingIndex = currentShops.indexOfFirst { it.name.equals(sName, ignoreCase = true) || it.id == sName }
                val updatedShop = if (existingIndex >= 0) {
                    currentShops[existingIndex].copy(
                        name = sName,
                        description = sDesc,
                        location = sLoc,
                        logoUrl = sPic,
                        isCertified = (_userProfile.value.kycStatus == "Certifié" || _activeRole.value == "Créateur")
                    )
                } else {
                    ShopItem(
                        id = "shop-${java.util.UUID.randomUUID().toString().take(6)}",
                        name = sName,
                        description = sDesc.ifBlank { "Boutique officielle sur NorA Cameroun" },
                        category = "Général",
                        location = sLoc.ifBlank { "Douala, Cameroun" },
                        logoUrl = sPic,
                        bannerUrl = "",
                        phone = _userProfile.value.whatsappNumber.ifBlank { "+237 655 924 778" },
                        isCertified = (_userProfile.value.kycStatus == "Certifié" || _activeRole.value == "Créateur"),
                        followersCount = _userProfile.value.followersCount.coerceAtLeast(1)
                    )
                }
                if (existingIndex >= 0) {
                    currentShops.toMutableList().apply { set(existingIndex, updatedShop) }
                } else {
                    currentShops + updatedShop
                }
            }
        }
    }

    // Moderation controls
    fun ignoreReport(reportId: String) {
        _reportedItems.update { list ->
            list.filter { it.id != reportId }
        }
    }

    fun removeReportedContent(reportId: String, itemTitle: String, type: String) {
        // Remove from reports
        _reportedItems.update { list ->
            list.filter { it.id != reportId }
        }

        // Remove actual product or Reel content
        when {
            type.contains("Produit", ignoreCase = true) -> {
                _products.update { list ->
                    list.filter { it.title != itemTitle }
                }
            }
            type.contains("Vidéo", ignoreCase = true) || type.contains("Reel", ignoreCase = true) -> {
                _reels.update { list ->
                    list.filter { !it.caption.contains(itemTitle) }
                }
            }
            type.contains("Commentaire", ignoreCase = true) -> {
                _reels.update { list ->
                    list.map { reel ->
                        reel.copy(comments = reel.comments.filter { it.text != itemTitle })
                    }
                }
            }
            type.contains("Utilisateur", ignoreCase = true) -> {
                // Simulating banning user
                _kycApplications.update { list ->
                    list.filter { it.name != itemTitle }
                }
            }
            type.contains("Boutique", ignoreCase = true) -> {
                _products.update { list ->
                    list.filter { it.shopName != itemTitle }
                }
            }
        }
    }

    // Simulate backup with delay loader
    fun triggerBackup(onCompleted: () -> Unit) {
        _isBackingUp.value = true
    }
    
    fun setBackingUp(backingUp: Boolean) {
        _isBackingUp.value = backingUp
    }

    // Setters for sliders
    fun creditAdminWallet() {
        _walletNCoins.update { it + 10.0 }
    }

    fun setViewsRatio(ratio: Float) {
        _viewsRatio.value = ratio
    }

    fun setConversionRate(rate: Float) {
        _conversionRate.value = rate
    }

    private fun userProfileToJson(profile: UserProfile): String {
        return try {
            org.json.JSONObject().apply {
                put("id", profile.id)
                put("name", profile.name)
                put("whatsappNumber", profile.whatsappNumber)
                put("profilePic", profile.profilePic)
                put("isLoggedIn", profile.isLoggedIn)
                put("interests", org.json.JSONArray(profile.interests))
                put("kycStatus", profile.kycStatus)
                put("shopName", profile.shopName)
                put("shopDescription", profile.shopDescription)
                put("shopCategory", profile.shopCategory)
                put("shopLocation", profile.shopLocation)
                put("shopPic", profile.shopPic)
                put("agreedToFee", profile.agreedToFee)
                put("idCardPhoto", profile.idCardPhoto)
                put("selfiePhoto", profile.selfiePhoto)
                put("hasShop", profile.hasShop)
                put("email", profile.email)
                put("onboardingCompleted", profile.onboardingCompleted)
                put("nCoinsBalance", profile.nCoinsBalance)
                put("referralCode", profile.referralCode)
            }.toString()
        } catch (t: Throwable) {
            ""
        }
    }

    private fun jsonToUserProfile(jsonStr: String): UserProfile? {
        return try {
            val obj = org.json.JSONObject(jsonStr)
            val interestsList = mutableListOf<String>()
            val arr = obj.optJSONArray("interests")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    interestsList.add(arr.getString(i))
                }
            }
            UserProfile(
                id = obj.optString("id", "user-1"),
                name = obj.optString("name", "Visiteur Camerounais"),
                whatsappNumber = obj.optString("whatsappNumber", "+237 600 000 000"),
                profilePic = obj.optString("profilePic", ""),
                isLoggedIn = obj.optBoolean("isLoggedIn", false),
                interests = interestsList,
                kycStatus = obj.optString("kycStatus", "Aucun"),
                shopName = obj.optString("shopName", ""),
                shopDescription = obj.optString("shopDescription", ""),
                shopCategory = obj.optString("shopCategory", ""),
                shopLocation = obj.optString("shopLocation", ""),
                shopPic = obj.optString("shopPic", ""),
                agreedToFee = obj.optBoolean("agreedToFee", false),
                idCardPhoto = obj.optString("idCardPhoto", ""),
                selfiePhoto = obj.optString("selfiePhoto", ""),
                hasShop = obj.optBoolean("hasShop", false),
                email = obj.optString("email", ""),
                onboardingCompleted = obj.optBoolean("onboardingCompleted", false),
                nCoinsBalance = obj.optDouble("nCoinsBalance", 1.0),
                referralCode = obj.optString("referralCode", "")
            )
        } catch (t: Throwable) {
            null
        }
    }

    init {
        // 1. Load registered accounts from SharedPreferences (fallback to default value)
        val registeredAccountsJson = sharedPrefs.getString("registered_accounts_json", null)
        if (!registeredAccountsJson.isNullOrBlank()) {
            try {
                val accountsObj = org.json.JSONObject(registeredAccountsJson)
                val loadedMap = mutableMapOf<String, Account>()
                val keys = accountsObj.keys()
                while (keys.hasNext()) {
                    val email = keys.next()
                    val accObj = accountsObj.getJSONObject(email)
                    val accEmail = accObj.optString("email", email)
                    val pass = accObj.optString("password", "")
                    val profObj = accObj.optJSONObject("profile")
                    val prof = if (profObj != null) jsonToUserProfile(profObj.toString()) else null
                    if (prof != null) {
                        loadedMap[email] = Account(accEmail, pass, prof)
                    }
                }
                if (loadedMap.isNotEmpty()) {
                    _registeredAccounts.value = loadedMap
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // 2. Load user profile and check session expiry
        val userProfileJson = sharedPrefs.getString("user_profile_json", null)
        val expiryTime = sharedPrefs.getLong("session_expiry", 0L)
        val now = System.currentTimeMillis()
        
        if (!userProfileJson.isNullOrBlank() && now < expiryTime) {
            try {
                val loadedProfile = jsonToUserProfile(userProfileJson)
                if (loadedProfile != null && loadedProfile.isLoggedIn) {
                    _userProfile.value = loadedProfile
                    _walletNCoins.value = loadedProfile.nCoinsBalance
                    _activeRole.value = if (loadedProfile.email == "mouotiep@gmail.com") "Admin" else "Acheteur"
                    
                    // RENEW session for another 1 year upon app opening
                    val newExpiry = now + (365L * 24 * 60 * 60 * 1000)
                    sharedPrefs.edit().putLong("session_expiry", newExpiry).apply()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        // 3. Start observing updates to auto-save to SharedPreferences
        viewModelScope.launch {
            combine(_userProfile, _registeredAccounts) { profile, accounts ->
                Pair(profile, accounts)
            }.collect { (profile, accounts) ->
                saveSession(profile, accounts)
            }
        }

        // 4. Synchronize _walletNCoins and _userProfile (Two-way)
        viewModelScope.launch {
            _walletNCoins.collect { balance ->
                if (_userProfile.value.nCoinsBalance != balance) {
                    _userProfile.update { it.copy(nCoinsBalance = balance) }
                }
            }
        }
        viewModelScope.launch {
            _userProfile.collect { profile ->
                if (_walletNCoins.value != profile.nCoinsBalance) {
                    _walletNCoins.value = profile.nCoinsBalance
                }
            }
        }

        // 5. Synchronize _userProfile changes back to _registeredAccounts
        viewModelScope.launch {
            _userProfile.collect { profile ->
                if (profile.isLoggedIn && profile.email.isNotEmpty() && profile.email != "mouotiep@gmail.com") {
                    val existingAccount = _registeredAccounts.value[profile.email]
                    if (existingAccount != null && existingAccount.profile != profile) {
                        _registeredAccounts.update {
                            it + (profile.email to existingAccount.copy(profile = profile))
                        }
                    }
                }
            }
        }

        // 6. Connect to online Supabase database real-time synchronization
        viewModelScope.launch {
            try {
                com.example.data.supabase.SupabaseManager.ensureAuthenticated()
                com.example.data.supabase.SupabaseManager.getConversationsRealtime().collect { remoteConvs ->
                    if (remoteConvs.isNotEmpty()) {
                        _conversations.update { localList ->
                            val mergedMap = localList.associateBy { it.id }.toMutableMap()
                            for (remote in remoteConvs) {
                                val existing = mergedMap[remote.id]
                                if (existing == null) {
                                    mergedMap[remote.id] = remote
                                } else {
                                    val allMsgs = (existing.messages + remote.messages)
                                        .distinctBy { it.id }
                                        .sortedBy { it.timestampMillis }
                                    mergedMap[remote.id] = existing.copy(
                                        lastMessage = remote.lastMessage.ifBlank { existing.lastMessage },
                                        lastTimestampMillis = if (remote.lastTimestampMillis > existing.lastTimestampMillis) remote.lastTimestampMillis else existing.lastTimestampMillis,
                                        messages = allMsgs
                                    )
                                }
                            }
                            mergedMap.values.toList()
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            try {
                com.example.data.supabase.SupabaseManager.getProductsRealtime().collect { remoteProds ->
                    if (remoteProds.isNotEmpty()) {
                        _products.update { localList ->
                            val localIds = localList.map { it.id }.toSet()
                            val newOnes = remoteProds.filter { it.id !in localIds }
                            newOnes + localList
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }

        viewModelScope.launch {
            try {
                com.example.data.supabase.SupabaseManager.getReelsRealtime().collect { remoteReels ->
                    if (remoteReels.isNotEmpty()) {
                        _reels.update { localList ->
                            val localIds = localList.map { it.id }.toSet()
                            val newOnes = remoteReels.filter { it.id !in localIds }
                            newOnes + localList
                        }
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    private fun saveSession(profile: UserProfile, accounts: Map<String, Account>) {
        try {
            val userProfileJson = userProfileToJson(profile)
            val accountsObj = org.json.JSONObject()
            accounts.forEach { (email, account) ->
                val accObj = org.json.JSONObject().apply {
                    put("email", account.email)
                    put("password", account.password)
                    put("profile", org.json.JSONObject(userProfileToJson(account.profile)))
                }
                accountsObj.put(email, accObj)
            }
            val registeredAccountsJson = accountsObj.toString()
            
            val expiryTime = if (profile.isLoggedIn) {
                val currentExpiry = sharedPrefs.getLong("session_expiry", 0L)
                if (currentExpiry > System.currentTimeMillis()) currentExpiry else System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)
            } else {
                0L
            }
            
            sharedPrefs.edit()
                .putString("user_profile_json", userProfileJson)
                .putString("registered_accounts_json", registeredAccountsJson)
                .putLong("session_expiry", expiryTime)
                .apply()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}
