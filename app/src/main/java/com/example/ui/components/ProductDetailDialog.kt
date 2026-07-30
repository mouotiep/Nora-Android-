package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.NoraViewModel
import com.example.ProductItem
import com.example.toLocaleString

@Composable
fun ProductDetailDialog(
    prod: ProductItem,
    viewModel: NoraViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val shopReviews by viewModel.shopReviews.collectAsState()
    val activeShopReviews = shopReviews.filter { it.shopId == prod.shopId }
    
    val averageRating = if (activeShopReviews.isNotEmpty()) {
        activeShopReviews.map { it.rating.toDouble() }.average()
    } else 5.0

    var userRating by remember { mutableStateOf(5) }
    var userCommentText by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }

    // Discount N-Coins state
    var useNCoinsDiscount by remember { mutableStateOf(false) }
    val conversionRateState by viewModel.conversionRate.collectAsState()
    val conversionRate = conversionRateState.toDouble()
    val walletNCoins = userProfile.nCoinsBalance
    val maxDiscountFCFA = prod.price * 0.05
    val maxDiscountNCoins = maxDiscountFCFA / conversionRate
    val maxCoinsToUse = Math.min(walletNCoins, maxDiscountNCoins)
    var coinsToUseForDiscount by remember { mutableStateOf(0.0) }

    val discountAmountFCFA = coinsToUseForDiscount * conversionRate
    val finalPriceFCFA = (prod.price - discountAmountFCFA).coerceAtLeast(0.0)
    val coinsPrice = prod.price / conversionRate

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(max = 680.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Top Bar with Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Détails du produit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Media / Image Preview Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        UniversalMediaView(
                            mediaUrl = prod.imageUrl,
                            mediaType = "Image",
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(prod.category, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title & Stock Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = prod.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (prod.stock > 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (prod.stock > 0) "En Stock (${prod.stock})" else "Rupture",
                                color = if (prod.stock > 0) Color(0xFF15803D) else Color(0xFFB91C1C),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Price Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${prod.price.toLocaleString()} FCFA",
                            fontSize = 18.sp,
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shop Profile Header Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(prod.shopName.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onDismiss()
                                    viewModel.selectShopAndNavigate(prod.shopId)
                                }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(prod.shopName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF007A5E))
                                if (prod.isCertified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFD1FAE5))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = "Certifié", tint = Color(0xFF065F46), modifier = Modifier.size(11.dp))
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

                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

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
                            activeShopReviews.take(3).forEach { review ->
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
                                        if (review.comment.isNotEmpty()) {
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

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                (1..5).forEach { star ->
                                    Icon(
                                        imageVector = if (star <= userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $star",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable { userRating = star }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = userCommentText,
                                onValueChange = { userCommentText = it },
                                placeholder = { Text("Écrivez votre commentaire d'évaluation...", fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp),
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
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Publier l'avis", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Purchase Action Buttons
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
                            // Option 1: Direct Chat with Seller
                            OutlinedButton(
                                onClick = {
                                    viewModel.startChatWithSeller(prod.shopName, prod.title)
                                    Toast.makeText(context, "Discussion ouverte avec ${prod.shopName} !", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF007A5E)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Discuter avec le vendeur sur l'App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Option 2: WhatsApp
                            Button(
                                onClick = {
                                    try {
                                        val message = "Bonjour ${prod.shopName}, je souhaite commander votre produit '${prod.title}' (${prod.price} FCFA) vu sur NorA."
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=237655924778&text=${Uri.encode(message)}"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "WhatsApp non installé sur l'appareil", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Commander via WhatsApp Direct", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Option 3: Add to Cart
                            Button(
                                onClick = {
                                    viewModel.addToCart(prod)
                                    Toast.makeText(context, "${prod.title} ajouté à votre Panier !", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ajouter au Panier NorA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Option 4: Pay with N Coins
                            Button(
                                onClick = {
                                    val order = viewModel.purchaseProduct(prod, payInNCoins = true)
                                    if (order != null) {
                                        Toast.makeText(context, "Achat réussi avec ${String.format("%.2f", coinsPrice)} N Coins !", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Solde N Coins insuffisant ! Rechargez dans l'onglet Profil.", Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Payer immédiatement en N Coins (${String.format("%.2f", coinsPrice)} NC)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = Color(0xFFE2E8F0))
                            Spacer(modifier = Modifier.height(4.dp))

                            // Discount Selector Section
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Stars, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Réduction N-Coins (Max 5%)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                        }
                                        Switch(
                                            checked = useNCoinsDiscount,
                                            onCheckedChange = { useNCoinsDiscount = it },
                                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF10B981))
                                        )
                                    }

                                    if (useNCoinsDiscount) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Utilisez vos N-Coins pour réduire le prix à la livraison (1 NC = ${conversionRate.toInt()} FCFA). Solde dispo: ${walletNCoins.toLocaleString()} NC",
                                            fontSize = 10.sp,
                                            color = Color(0xFF047857),
                                            lineHeight = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (maxCoinsToUse <= 0) {
                                            Text("⚠️ Votre solde de N Coins est à 0.", fontSize = 10.sp, color = Color.Red)
                                        } else {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "N Coins à consommer : ${coinsToUseForDiscount.toLocaleString()} NC",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF065F46)
                                                )
                                                Row {
                                                    IconButton(
                                                        onClick = { coinsToUseForDiscount = (coinsToUseForDiscount - 0.25).coerceAtLeast(0.0) },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color(0xFF10B981))
                                                    }
                                                    IconButton(
                                                        onClick = { coinsToUseForDiscount = (coinsToUseForDiscount + 0.25).coerceAtMost(maxCoinsToUse) },
                                                        modifier = Modifier.size(24.dp)
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
                                            HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 0.5.dp)
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
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Buy on Delivery (FCFA)
                            Button(
                                onClick = {
                                    if (useNCoinsDiscount && coinsToUseForDiscount > walletNCoins) {
                                        Toast.makeText(context, "Solde de N Coins insuffisant !", Toast.LENGTH_LONG).show()
                                        return@Button
                                    }
                                    val order = viewModel.purchaseProduct(
                                        product = prod,
                                        payInNCoins = false,
                                        coinsUsedForDiscount = if (useNCoinsDiscount) coinsToUseForDiscount else 0.0
                                    )
                                    if (order != null) {
                                        Toast.makeText(context, "Commande validée ! Reste à payer : ${finalPriceFCFA.toLocaleString()} FCFA.", Toast.LENGTH_LONG).show()
                                        onDismiss()
                                    } else {
                                        Toast.makeText(context, "Échec de l'achat !", Toast.LENGTH_LONG).show()
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
                        }

                        // Delete button for owner
                        if (prod.shopId == userProfile.id) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    viewModel.deleteProduct(prod.id)
                                    Toast.makeText(context, "🗑️ Produit supprimé avec succès !", Toast.LENGTH_SHORT).show()
                                    onDismiss()
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
                            onClick = { showReportDialog = true },
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

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Signaler le produit", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Un rapport sera envoyé à l'administration NorA pour analyser ce produit/boutique.", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Signalement transmis à l'équipe NorA. Merci !", Toast.LENGTH_SHORT).show()
                        showReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Envoyer le signalement", fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Annuler", fontSize = 11.sp)
                }
            }
        )
    }
}
