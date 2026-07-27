package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import coil.request.ImageRequest
import com.example.NoraViewModel
import com.example.domain.model.ProductItem
import com.example.domain.model.ReelVideo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesView(viewModel: NoraViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val productsList by viewModel.products.collectAsState()
    val favoriteProductIds by viewModel.favoriteProductIds.collectAsState()
    val reelsList by viewModel.reels.collectAsState()
    val walletNCoins by viewModel.walletNCoins.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()

    val favoriteProducts = remember(productsList, favoriteProductIds) {
        productsList.filter { favoriteProductIds.contains(it.id) }
    }

    val likedReels = remember(reelsList) {
        reelsList.filter { it.isLiked }
    }

    var selectedTabFilter by remember { mutableStateOf("Tous") } // "Tous", "Articles", "Vidéos"
    var selectedProductDetails by remember { mutableStateOf<ProductItem?>(null) }
    var selectedReelPreview by remember { mutableStateOf<ReelVideo?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 14.dp)
            .testTag("favorites_view")
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mes Favoris ❤️",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "${favoriteProducts.size} article(s) • ${likedReels.size} vidéo(s)",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedTabFilter == "Tous",
                onClick = { selectedTabFilter = "Tous" },
                label = { Text("Tous (${favoriteProducts.size + likedReels.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF334155)
                )
            )

            FilterChip(
                selected = selectedTabFilter == "Articles",
                onClick = { selectedTabFilter = "Articles" },
                label = { Text("Articles (${favoriteProducts.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF334155)
                )
            )

            FilterChip(
                selected = selectedTabFilter == "Vidéos",
                onClick = { selectedTabFilter = "Vidéos" },
                label = { Text("Reels (${likedReels.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF10B981),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF334155)
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content
        val showProducts = selectedTabFilter == "Tous" || selectedTabFilter == "Articles"
        val showReels = selectedTabFilter == "Tous" || selectedTabFilter == "Vidéos"

        val isTotallyEmpty = favoriteProducts.isEmpty() && likedReels.isEmpty()
        val isProductsEmpty = showProducts && favoriteProducts.isEmpty() && !showReels
        val isReelsEmpty = showReels && likedReels.isEmpty() && !showProducts

        if (isTotallyEmpty || isProductsEmpty || isReelsEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Aucun favori pour le moment",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Text(
                        text = "Appuyez sur le cœur ❤️ présent sur les produits de la boutique ou sur les vidéos culturelles pour les retrouver ici à tout moment !",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.setCurrentTabIndex(0) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explorer la Boutique", fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Section Articles Favoris
                if (showProducts && favoriteProducts.isNotEmpty()) {
                    item {
                        Text(
                            text = "🛍️ Articles Favoris (${favoriteProducts.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(favoriteProducts.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (item in pair) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductCardItem(
                                        product = item,
                                        userProfile = userProfile,
                                        isFavorite = true,
                                        onFavoriteToggle = { viewModel.toggleFavoriteProduct(item.id) },
                                        onClick = { selectedProductDetails = item }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // Section Reels Favoris
                if (showReels && likedReels.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "🎬 Vidéos Likées (${likedReels.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(likedReels) { reel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReelPreview = reel },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.Black)
                                ) {
                                    if (reel.mediaUrl.isNotBlank()) {
                                        UniversalMediaView(
                                            mediaUrl = reel.mediaUrl,
                                            mediaType = reel.mediaType,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = if (reel.mediaType == "Photo") Icons.Default.Photo else Icons.Default.PlayCircleFilled,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier
                                                .size(32.dp)
                                                .align(Alignment.Center)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reel.caption,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Par @${reel.creatorName} • ${reel.category}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(12.dp))
                                        Text("${reel.likesCount} j'aime", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.toggleLike(reel.id) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Retirer des favoris",
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Product Details & Ordering Dialog Modal
    if (selectedProductDetails != null) {
        val prod = selectedProductDetails!!
        val coinsPrice = (prod.price.toDouble() / conversionRate).coerceAtLeast(0.1)

        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedProductDetails = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
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
                        IconButton(
                            onClick = { selectedProductDetails = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(prod.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("Vendeur : ${prod.shopName} • ${prod.location}", fontSize = 12.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${prod.price} FCFA",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF007A5E)
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "ou ${String.format("%.1f", coinsPrice)} N-Coins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = prod.description,
                        fontSize = 12.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val message = "Bonjour ${prod.shopName}, je suis intéressé par votre article '${prod.title}' sur Nora Cameroun (${prod.price} FCFA)."
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=237655924778&text=${Uri.encode(message)}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Commander WhatsApp", fontSize = 11.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                viewModel.purchaseProduct(
                                    product = prod,
                                    payInNCoins = walletNCoins >= coinsPrice,
                                    coinsUsedForDiscount = if (walletNCoins >= coinsPrice) coinsPrice else 0.0
                                )
                                Toast.makeText(context, "Commande effectuée avec succès !", Toast.LENGTH_LONG).show()
                                selectedProductDetails = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Acheter Direct", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Reel Preview Dialog
    if (selectedReelPreview != null) {
        val reel = selectedReelPreview!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedReelPreview = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    UniversalMediaView(
                        mediaUrl = reel.mediaUrl,
                        mediaType = reel.mediaType,
                        modifier = Modifier.fillMaxSize()
                    )

                    IconButton(
                        onClick = { selectedReelPreview = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                )
                            )
                    ) {
                        Text("@${reel.creatorName}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(reel.caption, fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
