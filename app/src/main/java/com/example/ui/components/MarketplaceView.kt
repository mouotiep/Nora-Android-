package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tous") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }

    // Dialog state controllers
    var selectedProductDetails by remember { mutableStateOf<ProductItem?>(null) }
    var showReportDialog by remember { mutableStateOf<ProductItem?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var showKycDialog by remember { mutableStateOf(false) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    // Filter products: hide banned, filter by category/search, prioritize user interests
    val filteredProducts = remember(products, searchQuery, selectedCategory, userProfile.interests) {
        products.filter { !it.isBanned }
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

        // Gorgeous Emerald-to-Teal Gradient Hero Banner Card with Cameroon theme
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0F9F72), Color(0xFF007A5E))
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Marché Local Camerounais",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "🇨🇲",
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Commandez directement auprès des meilleurs artisans de Yaoundé, Douala, Bafoussam et Garoua.",
                    fontSize = 11.5.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Integrated Search Input Bar (White Pill Form)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { 
                        Text(
                            text = "Rechercher vêtements, épices, kaba, co...", 
                            fontSize = 13.sp, 
                            color = Color.Gray.copy(alpha = 0.8f)
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = null, 
                            tint = Color.Gray, 
                            modifier = Modifier.size(20.dp)
                        ) 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("marketplace_search"),
                    shape = RoundedCornerShape(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        cursorColor = Color(0xFF007A5E)
                    ),
                    singleLine = true
                )
            }
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
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = BorderStroke(1.dp, Color(0xFFD1FAE5))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (userProfile.kycStatus) {
                                "Certifié" -> "Votre Boutique: ${userProfile.shopName}"
                                "En Attente" -> "KYC Boutique en attente"
                                "Banni" -> "Boutique bannie"
                                "Arnaqueur" -> "Boutique signalée fraude"
                                else -> "Devenez Vendeur Certifié !"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                        Text(
                            text = when (userProfile.kycStatus) {
                                "Certifié" -> "Publiez vos produits artisanaux dès maintenant."
                                "En Attente" -> "L'administrateur examine vos documents d'identité."
                                "Banni" -> "Votre accès vendeur est révoqué définitivement."
                                "Arnaqueur" -> "Profil bloqué en mode arnaqueur suspecté."
                                else -> "Ouvrez votre boutique en validant votre identité (KYC)."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF047857)
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (userProfile.kycStatus == "Certifié") {
                                showAddProductDialog = true
                            } else if (userProfile.kycStatus == "Aucun" || userProfile.kycStatus == "Révoqué") {
                                showKycDialog = true
                            } else {
                                Toast.makeText(context, "Statut KYC actuel: ${userProfile.kycStatus}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (userProfile.kycStatus == "Certifié") "Ajouter Produit" else "Gérer KYC",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
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
                        onClick = { selectedProductDetails = item }
                    )
                }
            }
        }
    }

    // Product Details & Order Dialog
    if (selectedProductDetails != null) {
        val prod = selectedProductDetails!!
        val coinsPrice = (prod.price / conversionRate).toInt().coerceAtLeast(1)

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
        val coinsNeededForFullPrice = (prod.price / conversionRate).toInt().coerceAtLeast(1)
        val maxCoinsToUse = remember(maxAffordableCoins, coinsNeededForFullPrice) {
            minOf(maxAffordableCoins, coinsNeededForFullPrice)
        }
        var coinsToUseForDiscount by remember(maxCoinsToUse) { mutableStateOf(maxCoinsToUse) }

        val discountAmountFCFA = (coinsToUseForDiscount * conversionRate).toInt()
        val finalPriceFCFA = (prod.price - discountAmountFCFA).coerceAtLeast(0)

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
                    // Image Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(prod.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = prod.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
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
                            text = "ou $coinsPrice N Coins",
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prod.shopName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (prod.isCertified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Certified", tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                                }
                            }
                            Text("Artisan Vérifié", fontSize = 10.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = prod.description,
                        fontSize = 12.sp,
                        color = Color(0xFF4B5563),
                        lineHeight = 16.sp
                    )

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
                                                    text = "Obtenir une réduction",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Text(
                                                    text = "Solde : $walletNCoins N Coins (${(walletNCoins * conversionRate).toInt()} FCFA)",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        Switch(
                                            checked = useNCoinsDiscount,
                                            onCheckedChange = { 
                                                useNCoinsDiscount = it
                                                if (it) {
                                                    coinsToUseForDiscount = maxCoinsToUse
                                                } else {
                                                    coinsToUseForDiscount = 0
                                                }
                                            },
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
                                                        coinsToUseForDiscount = (coinsToUseForDiscount - 10).coerceAtLeast(0)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color(0xFF64748B))
                                                }

                                                Text(
                                                    text = "$coinsToUseForDiscount N Coins",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF0F172A)
                                                )

                                                // Plus button
                                                IconButton(
                                                    onClick = {
                                                        coinsToUseForDiscount = (coinsToUseForDiscount + 10).coerceAtMost(maxCoinsToUse)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = Color(0xFF10B981))
                                                }
                                            }
                                        }

                                        Slider(
                                            value = coinsToUseForDiscount.toFloat(),
                                            onValueChange = { coinsToUseForDiscount = it.toInt() },
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
                                    val order = viewModel.purchaseProduct(
                                        product = prod, 
                                        payInNCoins = false, 
                                        coinsUsedForDiscount = if (useNCoinsDiscount) coinsToUseForDiscount else 0
                                    )
                                    if (order != null) {
                                        if (useNCoinsDiscount && coinsToUseForDiscount > 0) {
                                            Toast.makeText(context, "Commande validée ! Réduction de ${discountAmountFCFA.toLocaleString()} FCFA appliquée en utilisant $coinsToUseForDiscount N Coins. Reste à payer : ${finalPriceFCFA.toLocaleString()} FCFA.", Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, "Commande validée ! Admin alerté pour coordonner la livraison.", Toast.LENGTH_LONG).show()
                                        }
                                        selectedProductDetails = null
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
 
                             // Buy with N Coins
                             Button(
                                 onClick = {
                                     if (walletNCoins < coinsPrice) {
                                         Toast.makeText(context, "N Coins insuffisants. Gagnez-en en publiant des Reels !", Toast.LENGTH_LONG).show()
                                         return@Button
                                     }
                                     val order = viewModel.purchaseProduct(prod, payInNCoins = true)
                                     if (order != null) {
                                         Toast.makeText(context, "Paiement en N Coins effectué ! Admin alerté.", Toast.LENGTH_LONG).show()
                                         selectedProductDetails = null
                                     }
                                 },
                                 modifier = Modifier.fillMaxWidth(),
                                 colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                 shape = RoundedCornerShape(8.dp)
                             ) {
                                 Text("Acheter avec N Coins (-$coinsPrice)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                             }
                        }

                        // WhatsApp Contact Simulation
                        OutlinedButton(
                            onClick = {
                                val url = "https://api.whatsapp.com/send?phone=+237675001002&text=Bonjour,%20je%20suis%20intéressé%20par%20votre%20produit%20'${prod.title}'"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(url)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp non disponible, numéro direct: +237 675 001 002", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contacter sur WhatsApp (+237)", fontSize = 11.sp)
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

    // KYC Form Dialog
    if (showKycDialog) {
        var shopNameInput by remember { mutableStateOf("") }
        var shopDescInput by remember { mutableStateOf("") }
        var shopLocInput by remember { mutableStateOf("") }
        var shopCategoryInput by remember { mutableStateOf("Objets d'Art") }
        var idCardName by remember { mutableStateOf("") }
        var selfieName by remember { mutableStateOf("") }
        var agreedToFee by remember { mutableStateOf(false) }

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
                    Text("Ouvrir une Boutique (KYC)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text("Soumettez vos documents officiels pour obtenir la certification verte.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))

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

                    // Simulated Identity Upload
                    Text("Téléchargement de Pièce d'Identité (Simulé)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    OutlinedTextField(
                        value = idCardName,
                        onValueChange = { idCardName = it },
                        label = { Text("Fichier Photo CNI (Ex: cni_kole.jpg)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = selfieName,
                        onValueChange = { selfieName = it },
                        label = { Text("Selfie avec CNI en main (Ex: selfie_kole.jpg)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Legal text checkbox agreement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(
                            checked = agreedToFee,
                            onCheckedChange = { agreedToFee = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Je m'engage à verser 5% de ce que je gagne à l'administrateur de Nora Cameroun après avoir livré chaque colis sous peine de poursuites judiciaires, de bannissement ou d'affichage d'une bannière de fraudeur (Arnaqueur).",
                            fontSize = 10.sp,
                            color = Color(0xFFDC2626),
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
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
                                if (!agreedToFee) {
                                    Toast.makeText(context, "Vous devez accepter l'engagement légal de 5% de commission", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                viewModel.submitShopKyc(
                                    shopName = shopNameInput,
                                    shopDesc = shopDescInput,
                                    shopCategory = shopCategoryInput,
                                    location = shopLocInput,
                                    idCardName = idCardName,
                                    selfieName = selfieName,
                                    agreed = true
                                )
                                Toast.makeText(context, "Votre dossier KYC a été soumis à l'administrateur avec succès !", Toast.LENGTH_LONG).show()
                                showKycDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Soumettre KYC")
                        }
                    }
                }
            }
        }
    }

    // Add Product Dialog (For Certified Shops)
    if (showAddProductDialog) {
        var prodTitle by remember { mutableStateOf("") }
        var prodPrice by remember { mutableStateOf("") }
        var prodStock by remember { mutableStateOf("") }
        var prodCategory by remember { mutableStateOf("Objets d'Art") }
        var prodDesc by remember { mutableStateOf("") }
        var prodImage by remember { mutableStateOf("") }

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
                    OutlinedTextField(value = prodImage, onValueChange = { prodImage = it }, label = { Text("URL de la photo") }, placeholder = { Text("Ex: https://...") }, modifier = Modifier.fillMaxWidth())
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
                                viewModel.addProduct(
                                    title = prodTitle,
                                    category = prodCategory,
                                    price = prodPrice.toIntOrNull() ?: 5000,
                                    stock = prodStock.toIntOrNull() ?: 5,
                                    shopName = userProfile.shopName,
                                    location = userProfile.shopLocation,
                                    description = prodDesc,
                                    imageUrl = prodImage
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
    onClick: () -> Unit
) {
    val isUserPreferred = userProfile.interests.contains(product.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_item_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isUserPreferred) BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f)) else null
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(product.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = product.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // White circular badge with red favorite heart icon (top-right)
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp),
                    shadowElevation = 2.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                // Green location badge/capsule with dynamic location text (bottom-left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF047857)) // Capsule green
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = product.location,
                            color = Color.White,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Scammer Caution
                if (product.isScammer) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red.copy(alpha = 0.9f))
                            .align(Alignment.BottomCenter)
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚠️ ARNAQUEUR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = product.category,
                    fontSize = 9.sp,
                    color = Color(0xFF10B981),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = product.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.price.toLocaleString()} F",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    
                    Text(
                        text = "Stock: ${product.stock}",
                        fontSize = 10.sp,
                        color = if (product.stock < 5) Color.Red else Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = product.shopName,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (product.isCertified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Certified",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}
