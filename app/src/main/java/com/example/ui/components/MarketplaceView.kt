package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.NoraViewModel
import com.example.ProductItem
import com.example.ShopReview
import com.example.toLocaleString
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()
    val walletNCoins by viewModel.walletNCoins.collectAsState()
    val favoriteProductIds by viewModel.favoriteProductIds.collectAsState()
    val adminAdTitle by viewModel.adminAdTitle.collectAsState()
    val adminAdText by viewModel.adminAdText.collectAsState()
    val adminAdImageUrl by viewModel.adminAdImageUrl.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Tous") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Dialog state controllers
    var selectedProductDetails by remember { mutableStateOf<ProductItem?>(null) }
    var showReportDialog by remember { mutableStateOf<ProductItem?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var showKycDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showShopManagerDialog by remember { mutableStateOf(false) }

    // Filter products: hide banned, filter out administrator products, filter by category/search, prioritize user interests
    val filteredProducts = remember(products, searchQuery, selectedCategory, userProfile.interests) {
        products.filter { !it.isBanned }
            .filter { !it.shopId.lowercase().contains("admin") && !it.shopName.lowercase().contains("admin") }
            .filter { selectedCategory == "Tous" || it.category == selectedCategory }
            .filter { searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.shopName.contains(searchQuery, ignoreCase = true) }
            .sortedByDescending { userProfile.interests.contains(it.category) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Ad Banner Card + Search Icon Button side-by-side
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Ad Banner Card taking weight(1f)
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (adminAdImageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(adminAdImageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Affiche Publicitaire",
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF0F9F72), Color(0xFF007A5E))
                                    )
                                )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = adminAdTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = adminAdText,
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Search Icon Button placed right beside the banner ("à côté de la bannière")
            Surface(
                onClick = { isSearchExpanded = !isSearchExpanded },
                shape = CircleShape,
                color = if (isSearchExpanded || searchQuery.isNotEmpty()) Color(0xFF10B981) else Color.White,
                shadowElevation = 3.dp,
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSearchExpanded && searchQuery.isNotEmpty()) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Recherche",
                        tint = if (isSearchExpanded || searchQuery.isNotEmpty()) Color.White else Color(0xFF007A5E),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Expandable / Unfolding Search Input Field (Déroulant)
        AnimatedVisibility(
            visible = isSearchExpanded || searchQuery.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { 
                    Text(
                        text = "Rechercher vêtements, épices, kaba...", 
                        fontSize = 12.sp, 
                        color = Color.Gray.copy(alpha = 0.8f)
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = null, 
                        tint = Color(0xFF007A5E), 
                        modifier = Modifier.size(18.dp)
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Effacer",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        IconButton(onClick = { isSearchExpanded = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .height(46.dp)
                    .testTag("marketplace_search"),
                shape = RoundedCornerShape(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFFE2E8F0),
                    cursorColor = Color(0xFF007A5E)
                ),
                singleLine = true
            )
        }

        // Section Title: "FILTRER PAR CATÉGORIE" & "+ Créer une catégorie" button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILTRER PAR CATÉGORIE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF007A5E),
                letterSpacing = 0.5.sp
            )

            // + Créer une catégorie button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .clickable { showCreateCategoryDialog = true }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Créer une catégorie",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        // Category Selection Filter
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isSelected) Color(0xFF10B981) else Color.White)
                        .border(1.dp, if (isSelected) Color(0xFF10B981) else Color(0xFFE5E7EB), RoundedCornerShape(100.dp))
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF475569)
                    )
                }
            }
        }

        // Shop Creator CTA banner
        if (activeRole != "Admin") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = BorderStroke(1.dp, Color(0xFFD1FAE5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (userProfile.kycStatus) {
                                "Certifié" -> "Boutique: ${userProfile.shopName}"
                                "En Attente" -> "KYC Boutique en attente"
                                "Banni" -> "Boutique bannie"
                                "Arnaqueur" -> "Boutique signalée"
                                else -> "Devenez Vendeur Certifié (KYC)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = when (userProfile.kycStatus) {
                                "Certifié" -> "Publiez vos produits artisanaux."
                                "En Attente" -> "Examen en cours par l'admin."
                                "Banni" -> "Accès révoqué."
                                "Arnaqueur" -> "Profil bloqué."
                                else -> "Ouvrez votre boutique certifiée."
                            },
                            fontSize = 9.sp,
                            color = Color(0xFF047857),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (userProfile.kycStatus == "Certifié") {
                            Button(
                                onClick = { showShopManagerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Gérer Boutique", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (userProfile.kycStatus == "Aucun" || userProfile.kycStatus == "Révoqué") {
                                        showKycDialog = true
                                    } else {
                                        Toast.makeText(context, "Statut KYC: ${userProfile.kycStatus}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(
                                    text = "Gérer KYC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Product Grid list
        if (filteredProducts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Aucun produit ne correspond à vos filtres.", fontSize = 13.sp, color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredProducts) { item ->
                    ProductCardItem(
                        product = item,
                        userProfile = userProfile,
                        isFavorite = favoriteProductIds.contains(item.id),
                        onFavoriteToggle = { viewModel.toggleFavoriteProduct(item.id) },
                        onShopClick = { viewModel.selectShopAndNavigate(item.shopId) },
                        onClick = { selectedProductDetails = item }
                    )
                }
            }
        }
    }

    // Product Details & Order Dialog
    if (selectedProductDetails != null) {
        val prod = selectedProductDetails!!
        val coinsPrice = (prod.price.toDouble() / conversionRate).coerceAtLeast(0.1)

        val shopReviews by viewModel.shopReviews.collectAsState()
        val activeShopReviews = remember(shopReviews, prod.shopId) {
            shopReviews.filter { it.shopId == prod.shopId }
        }
        val averageRating = remember(activeShopReviews) {
            if (activeShopReviews.isEmpty()) 5f
            else activeShopReviews.map { it.rating }.average().toFloat()
        }
        var userRating by remember { mutableStateOf(5) }
        var userCommentText by remember { mutableStateOf("") }

        var useNCoinsDiscount by remember { mutableStateOf(false) }
        val maxAffordableCoins = walletNCoins
        val maxDiscountFCFA = prod.price * 0.05
        val maxDiscountCoins = maxDiscountFCFA / conversionRate
        val maxCoinsToUse = remember(maxAffordableCoins, maxDiscountCoins) {
            minOf(maxAffordableCoins, maxDiscountCoins)
        }
        var coinsToUseForDiscount by remember(maxCoinsToUse) { mutableStateOf(maxCoinsToUse) }

        val discountAmountFCFA = (coinsToUseForDiscount * conversionRate).toInt()
        val finalPriceFCFA = (prod.price - discountAmountFCFA).coerceAtLeast(0)

        val prodPhotoList = remember(prod) { prod.getPhotoList() }
        var activeDetailPhotoIndex by remember(prod) { mutableIntStateOf(0) }
        var selectedProductVariant by remember(prod) { mutableStateOf<String?>(prod.variants.firstOrNull()) }

        Dialog(onDismissRequest = { selectedProductDetails = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Image Header Gallery Carousel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(prodPhotoList.getOrElse(activeDetailPhotoIndex) { prod.imageUrl })
                                .crossfade(true)
                                .build(),
                            contentDescription = prod.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Photo Counter Badge
                        if (prodPhotoList.size > 1) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "${activeDetailPhotoIndex + 1} / ${prodPhotoList.size}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        // Scammer Badge Banner
                        if (prod.isScammer) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Red)
                                    .align(Alignment.BottomCenter)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚠️ ARNAQUEUR - NE PAS ACHETER",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Thumbnails Selector Row
                    if (prodPhotoList.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            prodPhotoList.forEachIndexed { index, pUrl ->
                                val isSel = index == activeDetailPhotoIndex
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (isSel) 2.5.dp else 1.dp,
                                            color = if (isSel) Color(0xFF10B981) else Color(0xFFCBD5E1),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { activeDetailPhotoIndex = index }
                                ) {
                                    AsyncImage(
                                        model = pUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prod.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                            Text(prod.location, fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = prod.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )

                    // Variants selector chips if product has variants
                    if (prod.variants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Variantes disponibles :", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                prod.variants.forEach { varName ->
                                    val isSelectedVariant = varName == selectedProductVariant
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelectedVariant) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelectedVariant) Color(0xFF047857) else Color(0xFFE2E8F0),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedProductVariant = varName }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = varName,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelectedVariant) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelectedVariant) Color.White else Color(0xFF334155)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${prod.price.toLocaleString()} FCFA",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827)
                        )
                        Text(
                            text = "ou ${coinsPrice.toLocaleString()} N Coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF059669)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Shop profile info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(prod.shopName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedProductDetails = null
                                    viewModel.selectShopAndNavigate(prod.shopId)
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prod.shopName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007A5E))
                                if (prod.isCertified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFD1FAE5))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = "Certified", tint = Color(0xFF065F46), modifier = Modifier.size(11.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Certifié", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF065F46))
                                    }
                                }
                            }
                            Text("Voir la boutique ➔", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.SemiBold)
                        }

                        // Follow Shop Button
                        val followedShops by viewModel.followedShops.collectAsState()
                        val isFollowed = followedShops.contains(prod.shopId)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFollowed) Color(0xFFE2E8F0) else Color(0xFF10B981))
                                .clickable {
                                    viewModel.toggleFollowShop(prod.shopId)
                                    val statusMsg = if (isFollowed) "Boutique retirée des favoris !" else "Boutique suivie !"
                                    Toast.makeText(context, statusMsg, Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isFollowed) "Suivi ✓" else "+ Suivre",
                                color = if (isFollowed) Color.DarkGray else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = prod.description,
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Delivery options info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (prod.offersDelivery) Icons.Default.DirectionsCar else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (prod.offersDelivery) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                if (prod.offersDelivery) {
                                    Text("Livraison disponible", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("Tarif: ${prod.deliveryCost.toLocaleString()} FCFA", fontSize = 10.sp, color = Color(0xFF64748B))
                                } else {
                                    Text("Pas de livraison proposée", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                    Text("À récupérer sur place chez le vendeur", fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Reviews section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Avis sur la boutique",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${String.format("%.1f", averageRating)}/5 (${activeShopReviews.size} avis)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of existing reviews
                    if (activeShopReviews.isEmpty()) {
                        Text(
                            text = "Aucun avis pour le moment. Soyez le premier à évaluer cette boutique !",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            activeShopReviews.forEach { review ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "@${review.reviewerName}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF374151)
                                            )
                                            Text(
                                                text = review.date,
                                                fontSize = 9.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            (1..5).forEach { star ->
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (star <= review.rating) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = review.comment,
                                            fontSize = 11.sp,
                                            color = Color(0xFF4B5563),
                                            lineHeight = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Write a review card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Noter cette boutique :",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Interactive Stars Rating picker
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rating Star $star",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { userRating = star }
                                            .testTag("star_picker_$star")
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when(userRating) {
                                        1 -> "Médiocre"
                                        2 -> "Passable"
                                        3 -> "Bien"
                                        4 -> "Très bien"
                                        5 -> "Excellent !"
                                        else -> ""
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Text field for review comment
                            OutlinedTextField(
                                value = userCommentText,
                                onValueChange = { userCommentText = it },
                                placeholder = { Text("Écrivez votre commentaire d'évaluation...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .testTag("shop_review_input_text"),
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFFCBD5E1),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = false
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Submit rating button
                            Button(
                                onClick = {
                                    if (userCommentText.isBlank()) {
                                        Toast.makeText(context, "Saisissez un commentaire d'évaluation !", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.addShopReview(
                                        shopId = prod.shopId,
                                        shopName = prod.shopName,
                                        reviewerName = userProfile.name,
                                        rating = userRating,
                                        comment = userCommentText
                                    )
                                    userCommentText = ""
                                    userRating = 5
                                    Toast.makeText(context, "Merci pour votre évaluation !", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .height(32.dp)
                                    .testTag("submit_shop_review_button"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Publier l'avis", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (prod.stock <= 0) {
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Rupture de Stock")
                            }
                        } else {
                            // --- Discount Selector Section ---
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MonetizationOn,
                                                contentDescription = null,
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = "Obtenir une réduction (Max 5%)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = "Solde : ${walletNCoins.toLocaleString()} N Coins (${(walletNCoins * conversionRate).toInt()} FCFA)",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                                Text(
                                                    text = "Limite : -5% du prix (${(prod.price * 0.05).toInt()} FCFA max)",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFFB45309)
                                                )
                                            }
                                        }
                                        val hasCoins = walletNCoins > 0.0
                                        Switch(
                                            checked = useNCoinsDiscount && hasCoins,
                                            onCheckedChange = { 
                                                if (!hasCoins) {
                                                    Toast.makeText(context, "Votre solde de N-Coins est à zéro !", Toast.LENGTH_SHORT).show()
                                                    return@Switch
                                                }
                                                useNCoinsDiscount = it
                                                if (it) {
                                                    coinsToUseForDiscount = maxCoinsToUse
                                                } else {
                                                    coinsToUseForDiscount = 0.0
                                                }
                                            },
                                            enabled = hasCoins,
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = Color.White,
                                                checkedTrackColor = Color(0xFF10B981)
                                            )
                                        )
                                    }

                                    if (useNCoinsDiscount && maxCoinsToUse > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        // Slider or Stepper for choosing coins
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Utiliser :",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF475569)
                                            )
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Minus button
                                                IconButton(
                                                    onClick = {
                                                        coinsToUseForDiscount = (coinsToUseForDiscount - 0.25).coerceAtLeast(0.0)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color(0xFF64748B))
                                                }

                                                Text(
                                                    text = "${coinsToUseForDiscount.toLocaleString()} N Coins",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )

                                                // Plus button
                                                IconButton(
                                                    onClick = {
                                                        coinsToUseForDiscount = (coinsToUseForDiscount + 0.25).coerceAtMost(maxCoinsToUse)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color(0xFF10B981))
                                                }
                                            }
                                        }

                                        Slider(
                                            value = coinsToUseForDiscount.toFloat(),
                                            onValueChange = { coinsToUseForDiscount = Math.round(it * 4.0) / 4.0 },
                                            valueRange = 0f..maxCoinsToUse.toFloat(),
                                            colors = SliderDefaults.colors(
                                                thumbColor = Color(0xFF10B981),
                                                activeTrackColor = Color(0xFF10B981),
                                                inactiveTrackColor = Color(0xFFE2E8F0)
                                            ),
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Divider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Réduction obtenue :", fontSize = 11.sp, color = Color.Gray)
                                            Text("-${discountAmountFCFA.toLocaleString()} FCFA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Nouveau prix à payer :", fontSize = 11.sp, color = Color.Gray)
                                            Text("${finalPriceFCFA.toLocaleString()} FCFA", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Buy with cash-on-delivery (FCFA)
                            Button(
                                onClick = {
                                    if (useNCoinsDiscount && coinsToUseForDiscount > walletNCoins) {
                                        Toast.makeText(context, "Solde de N Coins insuffisant pour cette réduction !", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    val order = viewModel.purchaseProduct(
                                        product = prod, 
                                        payInNCoins = false, 
                                        coinsUsedForDiscount = if (useNCoinsDiscount) coinsToUseForDiscount else 0.0
                                    )
                                    if (order != null) {
                                        if (useNCoinsDiscount && coinsToUseForDiscount > 0) {
                                            Toast.makeText(context, "Commande validée ! Réduction de ${discountAmountFCFA.toLocaleString()} FCFA appliquée en utilisant ${coinsToUseForDiscount.toLocaleString()} N Coins. Reste à payer : ${finalPriceFCFA.toLocaleString()} FCFA.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Commande validée ! Admin alerté pour coordonner la livraison.", Toast.LENGTH_LONG).show()
                                        }
                                        selectedProductDetails = null
                                    } else {
                                        Toast.makeText(context, "Échec de l'achat : vérifiez votre stock ou votre solde de N Coins !", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val buttonText = if (useNCoinsDiscount && coinsToUseForDiscount > 0) {
                                    "Acheter à la Livraison (${finalPriceFCFA.toLocaleString()} FCFA)"
                                } else {
                                    "Acheter à la Livraison (${prod.price.toLocaleString()} FCFA)"
                                }
                                Text(buttonText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Achat sécurisé : le paiement de 95% minimum du prix se fait à la livraison. La réduction via N-Coins est plafonnée à 5% maximum.",
                                        fontSize = 10.sp,
                                        color = Color(0xFF92400E),
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }



                        // If current user is the owner of this product, let them delete it!
                        if (prod.shopId == userProfile.id) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.deleteProduct(prod.id)
                                    Toast.makeText(context, "🗑️ Produit supprimé avec succès !", Toast.LENGTH_SHORT).show()
                                    selectedProductDetails = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Supprimer mon produit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Report button
                        TextButton(
                            onClick = {
                                showReportDialog = prod
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Report, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Signaler ce produit ou boutique", color = Color.Red, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // KYC Form Dialog (2 Steps Flow)
    if (showKycDialog) {
        var currentKycStep by remember { mutableStateOf(1) }
        var shopNameInput by remember { mutableStateOf("") }
        var shopDescInput by remember { mutableStateOf("") }
        var shopLocInput by remember { mutableStateOf("") }
        var shopCategoryInput by remember { mutableStateOf("Objets d'Art") }
        var idCardName by remember { mutableStateOf("") }
        var selfieName by remember { mutableStateOf("") }

        val cniPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                idCardName = it.toString()
                Toast.makeText(context, "Photo CNI sélectionnée depuis la galerie !", Toast.LENGTH_SHORT).show()
            }
        }

        val selfiePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selfieName = it.toString()
                Toast.makeText(context, "Selfie sélectionné depuis la galerie !", Toast.LENGTH_SHORT).show()
            }
        }

        Dialog(onDismissRequest = { showKycDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    if (currentKycStep == 1) {
                        Text("Ouvrir une Boutique", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        Text("Saisissez les informations de votre boutique et importez vos documents d'identité pour vérification.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Admin WhatsApp Direct Contact Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Besoin d'aide ou question KYC ?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                    Text("Contactez l'Admin Nora sur WhatsApp (+237 655 924 778)", fontSize = 10.sp, color = Color(0xFF15803D))
                                }
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=237655924778&text=Bonjour%20Admin%20Nora,%20je%20souhaite%20des%20informations%20sur%20la%20validation%20de%20ma%20boutique%20KYC."))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp Admin: +237 655 924 778", Toast.LENGTH_LONG).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A), contentColor = Color.White),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = shopNameInput,
                            onValueChange = { shopNameInput = it },
                            label = { Text("Nom de la Boutique") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = shopDescInput,
                            onValueChange = { shopDescInput = it },
                            label = { Text("Description des produits vendus") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = shopLocInput,
                            onValueChange = { shopLocInput = it },
                            label = { Text("Localisation / Ville au Cameroun") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Catégorie Principale", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Objets d'Art", "Mode & Vêtements", "Alimentation").forEach { cat ->
                                val isSel = shopCategoryInput == cat
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                        .clickable { shopCategoryInput = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(cat, fontSize = 10.sp, color = if (isSel) Color.White else Color.Black)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Identity Upload
                        Text("Téléchargement de Pièce d'Identité (CNI) 🪪", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        cniPickerLauncher.launch("image/*")
                                    } catch (e: Exception) {
                                        idCardName = "cni_photo_import.jpg"
                                        Toast.makeText(context, "CNI importée", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF10B981))
                                Column {
                                    Text(
                                        text = if (idCardName.isBlank()) "Sélectionner la photo de la CNI dans la Galerie" else "CNI Importée ✓",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idCardName.isBlank()) Color.Black else Color(0xFF10B981)
                                    )
                                    Text(
                                        text = if (idCardName.isBlank()) "Cliquez pour ouvrir vos photos (PNG, JPG)" else idCardName,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Selfie avec CNI en main 🤳", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    try {
                                        selfiePickerLauncher.launch("image/*")
                                    } catch (e: Exception) {
                                        selfieName = "selfie_photo_import.jpg"
                                        Toast.makeText(context, "Selfie importé", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF10B981))
                                Column {
                                    Text(
                                        text = if (selfieName.isBlank()) "Sélectionner le Selfie dans la Galerie" else "Selfie Importé ✓",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selfieName.isBlank()) Color.Black else Color(0xFF10B981)
                                    )
                                    Text(
                                        text = if (selfieName.isBlank()) "Cliquez pour prendre ou choisir votre photo" else selfieName,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showKycDialog = false }) { Text("Annuler") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (shopNameInput.isBlank() || shopDescInput.isBlank() || shopLocInput.isBlank()) {
                                        Toast.makeText(context, "Saisissez toutes les informations de votre boutique", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    if (idCardName.isBlank() || selfieName.isBlank()) {
                                        Toast.makeText(context, "Documents d'identité requis", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    viewModel.submitShopKyc(
                                        shopName = shopNameInput,
                                        shopDesc = shopDescInput,
                                        shopCategory = shopCategoryInput,
                                        location = shopLocInput,
                                        idCardName = idCardName,
                                        selfieName = selfieName
                                    )
                                    Toast.makeText(context, "Votre dossier de création de boutique a été soumis avec succès !", Toast.LENGTH_LONG).show()
                                    showKycDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Soumettre ma demande")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showShopManagerDialog) {
        ShopManagerDialog(
            onDismiss = { showShopManagerDialog = false },
            viewModel = viewModel
        )
    }

    // Add Product Dialog (For Certified Shops)
    if (showAddProductDialog) {
        var prodTitle by remember { mutableStateOf("") }
        var prodPrice by remember { mutableStateOf("") }
        var prodStock by remember { mutableStateOf("") }
        var prodCategory by remember { mutableStateOf("Objets d'Art") }
        var prodDesc by remember { mutableStateOf("") }
        var prodImage by remember { mutableStateOf("") }
        var prodAdditionalImages by remember { mutableStateOf("") }
        var prodVariants by remember { mutableStateOf("") }
        var offersDelivery by remember { mutableStateOf(false) }
        var deliveryCostInput by remember { mutableStateOf("") }

        val productImagePicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                grantUriReadPermission(context, it)
                prodImage = it.toString()
            }
        }

        Dialog(onDismissRequest = { showAddProductDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Ajouter un Produit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(value = prodTitle, onValueChange = { prodTitle = it }, label = { Text("Titre de l'article") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = prodPrice, onValueChange = { prodPrice = it }, label = { Text("Prix (en FCFA)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = prodStock, onValueChange = { prodStock = it }, label = { Text("Stock initial") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = prodDesc, onValueChange = { prodDesc = it }, label = { Text("Description détaillée") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = prodImage,
                        onValueChange = { prodImage = it },
                        label = { Text("Photo principale (URL ou Galerie)") },
                        placeholder = { Text("Ex: content://... ou https://...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { productImagePicker.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📱 Choisir photo depuis le téléphone", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prodAdditionalImages,
                        onValueChange = { prodAdditionalImages = it },
                        label = { Text("Photos supplémentaires (URLs séparées par virugles)") },
                        placeholder = { Text("https://img1.jpg, https://img2.jpg") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prodVariants,
                        onValueChange = { prodVariants = it },
                        label = { Text("Variantes du produit (Couleurs, Tailles...)") },
                        placeholder = { Text("Ex: Taille M, Taille L, Rouge, Vert, Noir") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Proposer la livraison", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = offersDelivery,
                            onCheckedChange = { offersDelivery = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                        )
                    }

                    if (offersDelivery) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = deliveryCostInput,
                            onValueChange = { deliveryCostInput = it },
                            label = { Text("Frais de livraison (FCFA)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Objets d'Art", "Mode & Vêtements", "Alimentation").forEach { cat ->
                            val isSel = prodCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                    .clickable { prodCategory = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 10.sp, color = if (isSel) Color.White else Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddProductDialog = false }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (prodTitle.isBlank() || prodPrice.isBlank()) {
                                    Toast.makeText(context, "Informations incomplètes", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val extraImagesList = prodAdditionalImages.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val variantsList = prodVariants.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                viewModel.addProduct(
                                    title = prodTitle,
                                    category = prodCategory,
                                    price = prodPrice.toIntOrNull() ?: 5000,
                                    stock = prodStock.toIntOrNull() ?: 5,
                                    shopName = userProfile.shopName,
                                    location = userProfile.shopLocation,
                                    description = prodDesc,
                                    imageUrl = prodImage,
                                    images = extraImagesList,
                                    variants = variantsList,
                                    offersDelivery = offersDelivery,
                                    deliveryCost = if (offersDelivery) (deliveryCostInput.toIntOrNull() ?: 0) else 0
                                )
                                Toast.makeText(context, "Produit mis en vente avec succès !", Toast.LENGTH_LONG).show()
                                showAddProductDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Publier l'article")
                        }
                    }
                }
            }
        }
    }

    // Reporting Dialog Form
    if (showReportDialog != null) {
        val prodItem = showReportDialog!!
        Dialog(onDismissRequest = { showReportDialog = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Signaler cet Article / Boutique", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Cible: ${prodItem.title} (${prodItem.shopName})", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Motif du signalement") },
                        placeholder = { Text("Ex: Plagiat, fraude, contrefaçon, harcèlement...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showReportDialog = null; reportReason = "" }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (reportReason.isBlank()) {
                                    Toast.makeText(context, "Raison obligatoire", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.reportItem(
                                    targetId = prodItem.id,
                                    targetName = "Produit: ${prodItem.title} - Boutique: ${prodItem.shopName}",
                                    reason = reportReason,
                                    type = "Produit"
                                )
                                Toast.makeText(context, "Signalement enregistré !", Toast.LENGTH_SHORT).show()
                                showReportDialog = null
                                reportReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Signaler", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Create Category Dialog
    if (showCreateCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showCreateCategoryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Créer une nouvelle catégorie", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text("Ajoutez une nouvelle préférence culturelle ou de produit sur la plateforme Nora Cameroun.", fontSize = 11.sp, color = Color.Gray)
                    
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("Ex: Masques et Statues, Épices...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB)
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showCreateCategoryDialog = false }) {
                            Text("Annuler", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val trimmed = newCategoryName.trim()
                                if (trimmed.isNotEmpty()) {
                                    viewModel.createCategory(trimmed)
                                    showCreateCategoryDialog = false
                                    Toast.makeText(context, "Catégorie '$trimmed' créée !", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Veuillez entrer un nom", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Créer", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardItem(
    product: ProductItem,
    userProfile: com.example.UserProfile,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onShopClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val isUserPreferred = userProfile.interests.contains(product.category)
    val photoList = remember(product) { product.getPhotoList() }
    var activePhotoIndex by remember(product) { mutableIntStateOf(0) }

    // Auto-rotate images every 5 seconds if product has multiple images
    LaunchedEffect(photoList) {
        if (photoList.size > 1) {
            while (true) {
                delay(5000L)
                activePhotoIndex = (activePhotoIndex + 1) % photoList.size
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clickable { onClick() }
            .testTag("product_item_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isUserPreferred) BorderStroke(2.dp, Color(0xFF10B981)) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Product Photo filling 100% space (auto-rotating every 5s)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photoList.getOrElse(activePhotoIndex) { product.imageUrl })
                    .crossfade(true)
                    .build(),
                contentDescription = product.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient Overlay for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // TOP ROW: Certified Index Badge (if certified) & Liker Heart Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Certified Index Badge
                if (product.isCertified) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF059669)) // Green certified
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Certifié",
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Certifié",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Liker Heart Button
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable { onFavoriteToggle() },
                    shadowElevation = 3.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // BOTTOM OVERLAY: Nom de la boutique, Nom du produit, Prix
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                // Nom de la boutique (+ icon storefront)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .clickable { onShopClick?.invoke() ?: onClick() }
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = product.shopName,
                        fontSize = 9.5.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Nom (Title)
                Text(
                    text = product.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Prix
                Text(
                    text = "${product.price.toLocaleString()} FCFA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF34D399) // High contrast emerald green
                )
            }
        }
    }
}
