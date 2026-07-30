package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.NoraViewModel
import com.example.domain.model.ProductItem
import com.example.domain.model.ReelVideo
import com.example.domain.model.ShopItem
import com.example.domain.model.ShopReview

@Composable
fun ShopsView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val selectedShopId by viewModel.selectedShopId.collectAsState()
    val shops by viewModel.shops.collectAsState()

    val currentShop = remember(selectedShopId, shops) {
        shops.find { it.id == selectedShopId }
    }

    if (currentShop != null) {
        ShopDetailScreen(
            shop = currentShop,
            viewModel = viewModel,
            onBack = { viewModel.selectShop(null) }
        )
    } else {
        ShopsExplorerScreen(
            viewModel = viewModel,
            onSelectShop = { shopId -> viewModel.selectShop(shopId) }
        )
    }
}

@Composable
fun ShopsExplorerScreen(
    viewModel: NoraViewModel,
    onSelectShop: (String) -> Unit
) {
    val context = LocalContext.current
    val shops by viewModel.shops.collectAsState()
    val followedShops by viewModel.followedShops.collectAsState()
    val products by viewModel.products.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Toutes") }

    val categories = listOf(
        "Toutes",
        "Suivies ⭐",
        "Mode & Vêtements",
        "Alimentation",
        "Accessoires & Bijou",
        "Objets d'Art"
    )

    // Filter and sort shops: Followed shops appear FIRST
    val filteredShops = remember(shops, followedShops, searchQuery, selectedCategory) {
        val query = searchQuery.trim().lowercase()
        shops.filter { shop ->
            val matchesSearch = query.isBlank() ||
                    shop.name.lowercase().contains(query) ||
                    shop.description.lowercase().contains(query) ||
                    shop.location.lowercase().contains(query) ||
                    shop.category.lowercase().contains(query)

            val matchesCategory = when (selectedCategory) {
                "Toutes" -> true
                "Suivies ⭐" -> followedShops.contains(shop.id)
                else -> shop.category.equals(selectedCategory, ignoreCase = true)
            }

            matchesSearch && matchesCategory
        }.sortedWith(
            compareByDescending<ShopItem> { followedShops.contains(it.id) }
                .thenByDescending { it.rating }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // --- Header Banner ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF007A5E), Color(0xFF065F46))
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color(0xFFA7F3D0),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Boutiques & Artisans",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Explorez les créateurs locaux, suivez vos boutiques préférées et commandez directement.",
                        fontSize = 12.sp,
                        color = Color(0xFFD1FAE5),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // --- Search Bar ---
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Rechercher une boutique, une ville, une catégorie...", fontSize = 12.sp, color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher", tint = Color(0xFF007A5E)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Effacer", tint = Color.Gray)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("shop_search_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Color(0xFF34D399),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // --- Category Filters Row ---
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            containerColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color(0xFF10B981) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // --- Followed Shops Header section (if any and not filtering "Suivies") ---
        val followedList = filteredShops.filter { followedShops.contains(it.id) }
        val otherList = filteredShops.filter { !followedShops.contains(it.id) }

        if (selectedCategory != "Suivies ⭐" && followedList.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                    Text(
                        text = "Boutiques que vous suivez (${followedList.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            items(followedList) { shop ->
                ShopCardItem(
                    shop = shop,
                    isFollowed = true,
                    productCount = products.count { it.shopId == shop.id || it.shopName.equals(shop.name, ignoreCase = true) },
                    onToggleFollow = {
                        viewModel.toggleFollowShop(shop.id)
                        Toast.makeText(context, "Boutique retirée des suivis", Toast.LENGTH_SHORT).show()
                    },
                    onClick = { onSelectShop(shop.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF007A5E), modifier = Modifier.size(18.dp))
                    Text(
                        text = "Autres boutiques disponibles (${otherList.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            items(otherList) { shop ->
                ShopCardItem(
                    shop = shop,
                    isFollowed = false,
                    productCount = products.count { it.shopId == shop.id || it.shopName.equals(shop.name, ignoreCase = true) },
                    onToggleFollow = {
                        viewModel.toggleFollowShop(shop.id)
                        Toast.makeText(context, "Boutique ${shop.name} suivie ! ⭐", Toast.LENGTH_SHORT).show()
                    },
                    onClick = { onSelectShop(shop.id) }
                )
            }
        } else {
            // Standard single list rendering
            if (filteredShops.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucune boutique ne correspond à votre recherche.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                items(filteredShops) { shop ->
                    val isFollowed = followedShops.contains(shop.id)
                    ShopCardItem(
                        shop = shop,
                        isFollowed = isFollowed,
                        productCount = products.count { it.shopId == shop.id || it.shopName.equals(shop.name, ignoreCase = true) },
                        onToggleFollow = {
                            viewModel.toggleFollowShop(shop.id)
                            val msg = if (isFollowed) "Boutique retirée des suivis" else "Boutique ${shop.name} suivie ! ⭐"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onClick = { onSelectShop(shop.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ShopCardItem(
    shop: ShopItem,
    isFollowed: Boolean,
    productCount: Int,
    onToggleFollow: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
            .testTag("shop_card_${shop.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Shop Logo
            Box(modifier = Modifier.size(60.dp)) {
                if (shop.logoUrl.isNotBlank()) {
                    AsyncImage(
                        model = shop.logoUrl,
                        contentDescription = shop.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF007A5E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shop.name.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }

                if (shop.isCertified) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Certifié",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Shop Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = shop.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📍 ${shop.location}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                    Text("•", fontSize = 11.sp, color = Color.LightGray)
                    Text(
                        text = shop.category,
                        fontSize = 11.sp,
                        color = Color(0xFF007A5E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Text(
                            text = "${shop.rating}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Text(
                        text = "$productCount article(s)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            // Follow Button
            Button(
                onClick = onToggleFollow,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowed) Color(0xFFE2E8F0) else Color(0xFF10B981)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("follow_shop_${shop.id}")
            ) {
                Text(
                    text = if (isFollowed) "Suivie ✓" else "+ Suivre",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFollowed) Color(0xFF334155) else Color.White
                )
            }
        }
    }
}

@Composable
fun ShopDetailScreen(
    shop: ShopItem,
    viewModel: NoraViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activeRole by viewModel.activeRole.collectAsState()
    val followedShops by viewModel.followedShops.collectAsState()
    val isFollowed = followedShops.contains(shop.id)

    val products by viewModel.products.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val reviews by viewModel.shopReviews.collectAsState()

    val shopProducts = remember(products, shop) {
        products.filter { it.shopId == shop.id || it.shopName.equals(shop.name, ignoreCase = true) }
    }

    val shopReels = remember(reels, shop) {
        reels.filter { it.creatorName.contains(shop.name, ignoreCase = true) }
    }

    val shopReviewsList = remember(reviews, shop) {
        reviews.filter { it.shopId == shop.id || it.shopName.equals(shop.name, ignoreCase = true) }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Produits, 1: Vidéos, 2: Avis
    var showReviewDialog by remember { mutableStateOf(false) }
    var selectedProductForDetail by remember { mutableStateOf<ProductItem?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // --- Top Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF007A5E),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour", tint = Color.White)
                }
                Text(
                    text = shop.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        viewModel.toggleFollowShop(shop.id)
                        val msg = if (isFollowed) "Boutique retirée" else "Vous suivez maintenant ${shop.name} ! ⭐"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFollowed) Color.White.copy(alpha = 0.2f) else Color(0xFF34D399)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (isFollowed) "Suivie ✓" else "+ Suivre",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // --- Shop Hero Banner & Header Info ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    if (shop.bannerUrl.isNotBlank()) {
                        AsyncImage(
                            model = shop.bannerUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF007A5E), Color(0xFF10B981))
                                    )
                                )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f))
                    )
                }

                // Shop Card Info Overlay
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-30).dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Logo
                            Box(modifier = Modifier.size(64.dp)) {
                                if (shop.logoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = shop.logoUrl,
                                        contentDescription = shop.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .border(2.dp, Color(0xFF10B981), CircleShape)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0xFF007A5E)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = shop.name.take(2).uppercase(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        )
                                    }
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = shop.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    if (shop.isCertified) {
                                        Icon(Icons.Default.Verified, contentDescription = "Certifié", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                                    }
                                }

                                Text(
                                    text = "📍 ${shop.location} • ${shop.category}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                        Text("${shop.rating} (${shop.reviewCount} avis)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text("👥 ${shop.followersCount + if (isFollowed) 1 else 0} abonnés", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        if (shop.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = shop.description,
                                fontSize = 12.sp,
                                color = Color(0xFF475569),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        if (activeRole == "Admin") {
                            // Action Buttons Row for Admin (WhatsApp, Call, Message)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val cleanNum = shop.phone.replace(" ", "").replace("+", "")
                                        val waUrl = "https://api.whatsapp.com/send?phone=$cleanNum&text=${Uri.encode("Bonjour ${shop.name}, je vous contacte en tant qu'administrateur NorA.")}"
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Numéro: ${shop.phone}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.adminContactUser(shop.name, "Bonjour ${shop.name}, l'administration NorA vous contacte.")
                                        Toast.makeText(context, "Messagerie ouverte avec ${shop.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Écrire", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF007A5E)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF007A5E), modifier = Modifier.size(16.dp))
                                }
                            }
                        } else {
                            // Info card for Buyers: Direct contact & transactions strictly handled by Admin
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Achetez en toute sécurité 🛡️",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534)
                                        )
                                        Text(
                                            text = "Les commandes, livraisons et échanges avec les boutiques sont entièrement centralisés et sécurisés par l'Administration NorA.",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF15803D),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- Navigation Tabs inside Shop Detail ---
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF007A5E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-16).dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Produits (${shopProducts.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Vidéos (${shopReels.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "Avis (${shopReviewsList.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // --- Content Based on Selected Tab ---
            when (selectedTab) {
                0 -> {
                    // Products List
                    if (shopProducts.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.ShoppingBag, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucun produit disponible pour cette boutique actuellement.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        items(shopProducts) { prod ->
                            ShopProductRowCard(
                                product = prod,
                                onAddToCart = {
                                    viewModel.addToCart(prod)
                                    Toast.makeText(context, "${prod.title} ajouté au panier", Toast.LENGTH_SHORT).show()
                                },
                                onBuyDirect = {
                                    selectedProductForDetail = prod
                                },
                                onClick = {
                                    selectedProductForDetail = prod
                                }
                            )
                        }
                    }
                }

                1 -> {
                    // Videos / Reels
                    if (shopReels.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.VideoLibrary, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Aucune vidéo publiée par cette boutique.", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    } else {
                        items(shopReels) { reel ->
                            ShopReelCard(reel = reel)
                        }
                    }
                }

                2 -> {
                    // Reviews & Ratings
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Avis des clients", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { showReviewDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A5E)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Laisser un avis", fontSize = 11.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (shopReviewsList.isEmpty()) {
                                Text("Aucun avis déposé pour le moment. Soyez le premier !", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                shopReviewsList.forEach { rev ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(rev.reviewerName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Row {
                                                    repeat(rev.rating) {
                                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(12.dp))
                                                    }
                                                }
                                            }
                                            Text(rev.comment, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Review Dialog
    if (showReviewDialog) {
        var rating by remember { mutableStateOf(5) }
        var comment by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Donner votre avis sur ${shop.name}", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Sélectionnez une note :", fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(onClick = { rating = star }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (star <= rating) Color(0xFFF59E0B) else Color.LightGray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        placeholder = { Text("Votre expérience avec cette boutique...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (comment.isNotBlank()) {
                            viewModel.addShopReview(shop.id, shop.name, "Client Nora", rating, comment)
                            Toast.makeText(context, "Avis enregistré !", Toast.LENGTH_SHORT).show()
                            showReviewDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A5E))
                ) {
                    Text("Publier", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (selectedProductForDetail != null) {
        ProductDetailDialog(
            prod = selectedProductForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedProductForDetail = null }
        )
    }
}

@Composable
fun ShopProductRowCard(
    product: ProductItem,
    onAddToCart: () -> Unit,
    onBuyDirect: () -> Unit,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick?.invoke() ?: onBuyDirect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.price} FCFA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF007A5E)
                )
                Text(
                    text = "Stock: ${product.stock} dispo",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Button(
                    onClick = onBuyDirect,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Commander", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = onAddToCart,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Panier", tint = Color(0xFF007A5E), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ShopReelCard(reel: ReelVideo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reel.caption,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("❤️ ${reel.likesCount} J'aime", fontSize = 10.sp, color = Color.Gray)
                    Text("👁️ ${reel.viewsCount} vues", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}
