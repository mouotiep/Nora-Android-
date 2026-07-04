package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class NoraViewModel : ViewModel() {

    // Active Role state: Acheteur, Créateur, Admin
    private val _activeRole = MutableStateFlow("Acheteur")
    val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    // Selected screen/tab index state
    private val _currentTabIndex = MutableStateFlow(0)
    val currentTabIndex: StateFlow<Int> = _currentTabIndex.asStateFlow()

    // Search and category filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tous")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Categories list (mutable state in-memory)
    private val _categories = MutableStateFlow(
        mutableListOf(
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
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // Portefeuille (Wallet) states
    private val _walletNCoins = MutableStateFlow(850)
    val walletNCoins: StateFlow<Int> = _walletNCoins.asStateFlow()

    // Monetary configurations
    private val _viewsRatio = MutableStateFlow(10f) // 10 views = 1 N Coin
    val viewsRatio: StateFlow<Float> = _viewsRatio.asStateFlow()

    private val _conversionRate = MutableStateFlow(5f) // 1 N Coin = 5 FCFA (Default, ranges from 1 to 10)
    val conversionRate: StateFlow<Float> = _conversionRate.asStateFlow()

    // Backup loader
    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    // Active conversation detail
    private val _activeChatId = MutableStateFlow<String?>(null)
    val activeChatId: StateFlow<String?> = _activeChatId.asStateFlow()

    // User Profile
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // Orders lists
    private val _orders = MutableStateFlow<List<NoraOrder>>(
        listOf(
            NoraOrder("order-101", "p-1", "Kaba Ndondo Royal Sawa", 15000, "Amina Yaoundé", "+237 677 111 222", "Sawa Elegance", "+237 675 001 002", false, 0, "En attente de livraison", "Aujourd'hui"),
            NoraOrder("order-102", "p-2", "Pagne Bamoun Tissé", 35000, "Jean Douala", "+237 699 333 444", "Les Merveilles du Noun", "+237 675 001 003", true, 3500, "En attente de livraison", "Aujourd'hui"),
            NoraOrder("order-103", "p-3", "Poivre Blanc de Penja Premium", 5000, "Oumarou Garoua", "+237 688 555 666", "Saveurs du Cameroun", "+237 675 001 004", false, 0, "En attente de livraison", "Aujourd'hui"),
            NoraOrder("order-104", "p-4", "Panier Tressé de Maroua", 8000, "Chef Bayam", "+237 677 222 333", "Artisans du Nord", "+237 675 001 005", false, 0, "En attente de livraison", "Hier"),
            NoraOrder("order-105", "p-5", "Sac en Cuir Artisanal", 18000, "Marc Fm", "+237 655 444 555", "Artisans du Nord", "+237 675 001 005", true, 1800, "En attente de livraison", "Hier"),
            NoraOrder("order-106", "p-6", "Sculpture Girafe en Bois", 25000, "Florence Bafoussam", "+237 677 999 888", "Les Merveilles du Noun", "+237 675 001 003", false, 0, "En attente de livraison", "Hier"),
            NoraOrder("order-107", "p-7", "Kaba Moderne Évasé", 20000, "Sonia Douala", "+237 699 777 888", "Sawa Elegance", "+237 675 001 002", false, 0, "En attente de livraison", "Il y a 2 jours"),
            NoraOrder("order-108", "p-8", "Miel Sauvage de l'Adamaoua", 6000, "Pierre Yaoundé", "+237 655 888 999", "Saveurs du Cameroun", "+237 675 001 004", true, 600, "En attente de livraison", "Il y a 2 jours"),
            NoraOrder("order-109", "p-9", "Boubou d'apparat brodé", 45000, "Aliou Garoua", "+237 688 222 111", "Artisans du Nord", "+237 675 001 005", false, 0, "En attente de livraison", "Il y a 2 jours"),
            NoraOrder("order-110", "p-10", "Masque Traditionnel Bamoun", 30000, "Idriss Yaoundé", "+237 677 333 444", "Les Merveilles du Noun", "+237 675 001 003", true, 3000, "En attente de livraison", "Il y a 3 jours"),
            NoraOrder("order-111", "p-11", "Épices de Penja pour Achu", 4500, "Mireille Buea", "+237 655 111 000", "Saveurs du Cameroun", "+237 675 001 004", false, 0, "En attente de livraison", "Il y a 3 jours"),
            NoraOrder("order-112", "p-12", "Tissu Ndop Authentique", 50000, "Samuel Douala", "+237 699 000 111", "Les Merveilles du Noun", "+237 675 001 003", false, 0, "En attente de livraison", "Il y a 3 jours"),
            NoraOrder("order-113", "p-13", "Calebasse Ornée du Noun", 12000, "Therese Yaoundé", "+237 677 444 555", "Les Merveilles du Noun", "+237 675 001 003", true, 1200, "En attente de livraison", "Il y a 4 jours")
        )
    )
    val orders: StateFlow<List<NoraOrder>> = _orders.asStateFlow()

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
                id = "user-alice",
                name = "Alice Kamga",
                whatsappNumber = "+237 677 889 900",
                profilePic = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
                kycStatus = "En Attente",
                shopName = "Bafoussam Royal Crafts",
                shopDescription = "Tissages et sculptures artisanales de l'Ouest Cameroun.",
                shopCategory = "Objets d'Art",
                shopLocation = "Bafoussam",
                agreedToFee = true,
                idCardPhoto = "Carte Nationale d'Identité - Validée",
                selfiePhoto = "Selfie avec CNI en main - Reçu"
            ),
            UserProfile(
                id = "user-bob",
                name = "Bob Sawa",
                whatsappNumber = "+237 699 112 233",
                profilePic = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
                kycStatus = "En Attente",
                shopName = "Sawa Sea Products",
                shopDescription = "Épices et poissons fumés traditionnels des côtes sawa.",
                shopCategory = "Alimentation",
                shopLocation = "Douala",
                agreedToFee = true,
                idCardPhoto = "Passeport - Reçu",
                selfiePhoto = "Selfie d'identité - Reçu"
            )
        )
    )
    val kycApplications: StateFlow<List<UserProfile>> = _kycApplications.asStateFlow()

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
                id = "conv-1",
                contactName = "Atelier de Foumban",
                lastMessage = "Merci pour votre commande !",
                lastTime = "Il y a 2h",
                messages = listOf(
                    Message("moi", "Bonjour, je souhaite commander un Pagne Royal Bamoun.", "10:15"),
                    Message("contact", "Bonjour ! Oui bien sûr, nous en avons encore 8 en stock.", "10:20"),
                    Message("moi", "Parfait, je valide le paiement !", "10:25"),
                    Message("contact", "Merci pour votre commande ! Elle sera prête cet après-midi.", "10:30")
                )
            ),
            Conversation(
                id = "conv-2",
                contactName = "Créatrice Sawa",
                lastMessage = "Le Kaba Ndondo est disponible en taille M et L.",
                lastTime = "Hier",
                messages = listOf(
                    Message("moi", "Bonjour, quelles sont les tailles disponibles pour le Kaba Ndondo ?", "Hier"),
                    Message("contact", "Le Kaba Ndondo est disponible en taille M et L.", "Hier")
                )
            ),
            Conversation(
                id = "conv-3",
                contactName = "Administrateur Nora (Coordination)",
                lastMessage = "Bonjour, bienvenue chez Nora pour vous servir",
                lastTime = "Aujourd'hui",
                messages = listOf(
                    Message("admin", "Bonjour, bienvenue chez Nora pour vous servir", "08:00")
                )
            )
        )
    )
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    // Transactions ledger
    private val _transactions = MutableStateFlow<List<Transaction>>(
        listOf(
            Transaction("Création du compte", "Crédit de bienvenue", 50, "01 Juil 2026", true),
            Transaction("Bonus Création de Vidéo", "Généré par 780 vues sur vid-2", 78, "03 Juil 2026", true)
        )
    )
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    // Moderation reports
    private val _reportedItems = MutableStateFlow<List<ReportedItem>>(
        listOf(
            ReportedItem("rep-1", "Reel de @mauvais_acteur", "Contenu inapproprié ou mensonger", "Vidéo", "Cindy_Nora"),
            ReportedItem("rep-2", "Robe Copiée Bamenda", "Plagiat suspecté d'une boutique déposée", "Produit", "Artisans du Nord")
        )
    )
    val reportedItems: StateFlow<List<ReportedItem>> = _reportedItems.asStateFlow()

    // Onboarding interest selection
    fun selectInterestsAndLogin(name: String, whatsapp: String, selectedInterests: List<String>) {
        _userProfile.update {
            it.copy(
                name = name.ifBlank { "Utilisateur Camerounais" },
                whatsappNumber = whatsapp.ifBlank { "+237 600 000 000" },
                interests = selectedInterests,
                isLoggedIn = true,
                kycStatus = "Aucun"
            )
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
        selfieName: String,
        agreed: Boolean
    ) {
        _userProfile.update {
            it.copy(
                kycStatus = "En Attente",
                shopName = shopName,
                shopDescription = shopDesc,
                shopCategory = shopCategory,
                shopLocation = location,
                agreedToFee = agreed,
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
        val app = _kycApplications.value.find { it.id == userId } ?: return
        
        // Update applications
        _kycApplications.update { list ->
            list.filter { it.id != userId }
        }

        // If it's the active user
        if (_userProfile.value.id == userId) {
            _userProfile.update {
                it.copy(kycStatus = "Certifié", hasShop = true)
            }
        }

        // Add the shop products/shop verification in the marketplace
        _products.update { list ->
            list.map { prod ->
                if (prod.shopName == app.shopName) {
                    prod.copy(isCertified = true, isScammer = false, isBanned = false)
                } else {
                    prod
                }
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

    // Category creation (Admins / Sellers can call)
    fun createCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotEmpty() && !_categories.value.contains(trimmed)) {
            _categories.update {
                val newList = ArrayList(it)
                newList.add(trimmed)
                newList
            }
        }
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

    // Video Follows
    fun toggleFollow(reelId: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    reel.copy(isFollowing = !reel.isFollowing)
                } else {
                    reel
                }
            }
        }
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

    fun addReactionToComment(reelId: String, commentId: String, emoji: String) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val updatedComments = reel.comments.map { comment ->
                        if (comment.id == commentId) {
                            val currentCount = comment.reactions[emoji] ?: 0
                            val updatedReactions = comment.reactions.toMutableMap()
                            updatedReactions[emoji] = currentCount + 1
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
        rotationAngle: Float = 0f
    ) {
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
            rotationAngle = rotationAngle
        )
        _reels.update { listOf(newReel) + it }
    }

    // Simulate views increments on Reels to reward Creators
    fun simulateViews(reelId: String, amount: Int) {
        _reels.update { list ->
            list.map { reel ->
                if (reel.id == reelId) {
                    val oldViews = reel.viewsCount
                    val nextViews = oldViews + amount
                    
                    // Calculate how many new N Coins this rewards.
                    val oldCoins = (oldViews / _viewsRatio.value).toInt()
                    val nextCoins = (nextViews / _viewsRatio.value).toInt()
                    val bonusGained = nextCoins - oldCoins

                    if (bonusGained > 0) {
                        _walletNCoins.update { it + bonusGained }
                        _transactions.update { tList ->
                            val nList = ArrayList(tList)
                            nList.add(
                                0, // Insert at top
                                Transaction(
                                    title = "Bonus Créateur \"${reel.creatorName}\"",
                                    description = "Généré par +$amount vues de son Reel",
                                    amount = bonusGained,
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

    // Add dynamic Product to Marketplace Catalog
    fun addProduct(
        title: String,
        category: String,
        price: Int,
        stock: Int,
        shopName: String,
        location: String,
        description: String,
        imageUrl: String
    ) {
        val activeUser = _userProfile.value
        val newProduct = ProductItem(
            id = "prod-${UUID.randomUUID()}",
            title = title.ifEmpty { "Produit Artisanal" },
            category = if (category == "Tous") "Mode & Vêtements" else category,
            price = if (price <= 0) 5000 else price,
            stock = if (stock <= 0) 5 else stock,
            shopName = shopName.ifEmpty { activeUser.shopName.ifEmpty { "Artisan du Cameroun" } },
            location = location.ifEmpty { activeUser.shopLocation.ifEmpty { "Douala" } },
            description = description.ifEmpty { "Produit conçu à la main avec passion et authenticité camerounaise." },
            imageUrl = imageUrl.ifEmpty { "https://images.unsplash.com/photo-1544441893-675973e31985?w=500" },
            shopId = activeUser.id,
            isCertified = activeUser.kycStatus == "Certifié",
            isScammer = activeUser.kycStatus == "Arnaqueur",
            isBanned = activeUser.kycStatus == "Banni"
        )
        _products.update { it + newProduct }
    }

    // Purchase product & create order
    fun purchaseProduct(product: ProductItem, payInNCoins: Boolean, coinsUsedForDiscount: Int = 0): NoraOrder? {
        if (product.stock <= 0) return null

        val activeUser = _userProfile.value
        val costInCoins = if (payInNCoins) {
            (product.price / _conversionRate.value).toInt().coerceAtLeast(1)
        } else {
            coinsUsedForDiscount
        }

        if (payInNCoins && _walletNCoins.value < costInCoins) {
            return null // Insufficient funds
        }
        if (!payInNCoins && coinsUsedForDiscount > 0 && _walletNCoins.value < coinsUsedForDiscount) {
            return null // Insufficient coins for discount
        }

        // Deduct N Coins
        if (payInNCoins) {
            _walletNCoins.update { it - costInCoins }
            _transactions.update { tList ->
                val nList = ArrayList(tList)
                nList.add(
                    0,
                    Transaction(
                        title = "Achat ${product.title}",
                        description = "Payé entièrement en N Coins",
                        amount = -costInCoins,
                        date = "Aujourd'hui",
                        isPositive = false
                    )
                )
                nList
            }
        } else if (coinsUsedForDiscount > 0) {
            _walletNCoins.update { it - coinsUsedForDiscount }
            _transactions.update { tList ->
                val nList = ArrayList(tList)
                nList.add(
                    0,
                    Transaction(
                        title = "Réduction ${product.title}",
                        description = "Réduction de ${coinsUsedForDiscount * _conversionRate.value.toInt()} FCFA appliquée",
                        amount = -coinsUsedForDiscount,
                        date = "Aujourd'hui",
                        isPositive = false
                    )
                )
                nList
            }
        }

        // Reduce stock
        _products.update { pList ->
            pList.map { p ->
                if (p.id == product.id) {
                    p.copy(stock = p.stock - 1)
                } else {
                    p
                }
            }
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
            sellerWhatsApp = "+237 675 001 002", // Simulating seller's WhatsApp
            payInNCoins = payInNCoins,
            coinsCost = costInCoins,
            status = "En attente de livraison",
            date = "Aujourd'hui"
        )

        _orders.update { it + newOrder }

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
                updatedMessages.add(
                    Message(
                        sender = "admin",
                        text = "ALERTE COMMANDE : ${activeUser.name} a commandé '${product.title}' chez ${product.shopName}. Solde: $priceDetail. Je coordonne la livraison.",
                        time = "À l'instant"
                    )
                )
                list.map { conv ->
                    if (conv.id == "conv-3") {
                        conv.copy(
                            lastMessage = "ALERTE COMMANDE: ${product.title}",
                            lastTime = "À l'instant",
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
                    amount = if (ord.payInNCoins) ord.coinsCost else 0,
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
                updatedMessages.add(
                    Message(
                        sender = "admin",
                        text = "LIVRAISON CONFIRMÉE PAR SCAN QR! Transaction #${ord.id} finalisée. Commission de 5% (${adminFee} FCFA) déduite pour Nora Admin. Vendeur crédité de ${netSellerGains} FCFA.",
                        time = "À l'instant"
                    )
                )
                list.map { conv ->
                    if (conv.id == "conv-3") {
                        conv.copy(
                            lastMessage = "Livraison validée par QR !",
                            lastTime = "À l'instant",
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
    fun sendMessage(conversationId: String, text: String) {
        if (text.trim().isEmpty()) return
        
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    val updatedMessages = ArrayList(conv.messages)
                    updatedMessages.add(Message("moi", text.trim(), "À l'instant"))
                    conv.copy(
                        lastMessage = text.trim(),
                        lastTime = "À l'instant",
                        messages = updatedMessages
                    )
                } else {
                    conv
                }
            }
        }
    }

    // Update profile
    fun updateProfile(name: String, whatsapp: String, profilePic: String) {
        _userProfile.update {
            it.copy(
                name = name.ifBlank { it.name },
                whatsappNumber = whatsapp.ifBlank { it.whatsappNumber },
                profilePic = profilePic.ifBlank { it.profilePic }
            )
        }
    }

    // Update shop profile
    fun updateShopProfile(shopName: String, shopDesc: String, shopLoc: String, shopPic: String) {
        _userProfile.update {
            it.copy(
                shopName = shopName.ifBlank { it.shopName },
                shopDescription = shopDesc.ifBlank { it.shopDescription },
                shopLocation = shopLoc.ifBlank { it.shopLocation },
                shopPic = shopPic.ifBlank { it.shopPic }
            )
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
    fun setViewsRatio(ratio: Float) {
        _viewsRatio.value = ratio
    }

    fun setConversionRate(rate: Float) {
        _conversionRate.value = rate
    }
}
