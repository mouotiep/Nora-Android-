package com.example.ui.components

import android.widget.Toast
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.NoraViewModel
import com.example.ProductItem
import com.example.NoraOrder
import com.example.toLocaleString
import java.util.UUID

@Composable
fun ShopManagerDialog(
    onDismiss: () -> Unit,
    viewModel: NoraViewModel
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val products by viewModel.products.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val shopReviews by viewModel.shopReviews.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()

    // Filter data for this user's shop
    val myShopName = userProfile.shopName
    val myProducts = remember(products, myShopName) {
        products.filter { it.shopName == myShopName }
    }
    val myOrders = remember(orders, myShopName) {
        orders.filter { it.sellerName == myShopName }
    }
    val myReviews = remember(shopReviews, myShopName) {
        shopReviews.filter { it.shopName == myShopName }
    }

    // Performance Calculations
    val completedOrders = myOrders.filter { it.status == "Livré & Payé" }
    val totalSalesRevenue = completedOrders.sumOf { it.productPrice }
    val totalNCoinsEarned = completedOrders.filter { it.payInNCoins }.sumOf { it.coinsCost }
    val averageRating = if (myReviews.isEmpty()) 5.0 else myReviews.map { it.rating }.average()

    // Navigation/Tab state
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Performance", "Produits", "Commandes", "Modifier")

    // Add Product state
    var showAddForm by remember { mutableStateOf(false) }
    var newProdTitle by remember { mutableStateOf("") }
    var newProdPrice by remember { mutableStateOf("") }
    var newProdStock by remember { mutableStateOf("") }
    var newProdCategory by remember { mutableStateOf("Mode & Vêtements") }
    var newProdDesc by remember { mutableStateOf("") }
    var newProdImageUrl by remember { mutableStateOf("") }
    var newProdAdditionalImages by remember { mutableStateOf("") }
    var newProdVariants by remember { mutableStateOf("") }

    // Shop Profile Edit state
    var editShopName by remember { mutableStateOf(userProfile.shopName) }
    var editShopDesc by remember { mutableStateOf(userProfile.shopDescription) }
    var editShopLoc by remember { mutableStateOf(userProfile.shopLocation) }
    var editShopPic by remember { mutableStateOf(userProfile.shopPic) }

    val prodPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            grantUriReadPermission(context, it)
            newProdImageUrl = it.toString()
        }
    }

    val shopLogoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            grantUriReadPermission(context, it)
            editShopPic = it.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            color = Color(0xFFF8FAFC)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar (Custom Row instead of experimental/fragile SmallTopAppBar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(0.5.dp, Color(0xFFE2E8F0))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color(0xFF047857),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = myShopName.ifEmpty { "Ma Boutique Nora" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Boutique Certifiée • Dashboard",
                                    fontSize = 10.sp,
                                    color = Color(0xFF047857),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
                    }
                }

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.White,
                    contentColor = Color(0xFF047857)
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium) }
                        )
                    }
                }

                // Main Content View based on Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> { // Performance Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Welcome Banner
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.TrendingUp,
                                            contentDescription = null,
                                            tint = Color(0xFF047857),
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                "Tableau de Bord Commercial 📊",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF064E3B)
                                            )
                                            Text(
                                                "Analysez en temps réel l'impact de votre activité artisanale sur Nora Cameroun.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                    }
                                }

                                // Metrics Grid
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Chiffre d'Affaires Card
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Recettes Totales", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("${totalSalesRevenue.toLocaleString()} FCFA", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Ventes Livrées", fontSize = 9.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    // N-Coins Card
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("NCoins Encaissés", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("${totalNCoinsEarned.toLocaleString()} 🪙", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("Équiv: ${(totalNCoinsEarned * conversionRate).toInt()} FCFA", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Commands Done Card
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Commandes", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("${myOrders.size} Commandes", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${completedOrders.size} terminées, ${myOrders.size - completedOrders.size} en attente", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }

                                    // Rating Card
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("Satisfaction Client", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(String.format("%.1f", averageRating), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text("${myReviews.size} avis de la communauté", fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                }

                                // Commissions policy note
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                    border = BorderStroke(0.5.dp, Color(0xFFBBF7D0)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Charte Commerciale Nora Cameroun", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Conformément à nos engagements de développement mutuel, une commission de 5% de la valeur des transactions est automatiquement reversée à l'application Nora lors de la confirmation d'une livraison pour maintenir et optimiser la visibilité de vos produits.",
                                            fontSize = 10.sp,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }

                                // Recent Reviews list
                                Text("Derniers avis clients", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                if (myReviews.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(80.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Aucun avis déposé pour le moment.", fontSize = 11.sp, color = Color.Gray)
                                    }
                                } else {
                                    myReviews.forEach { rev ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(rev.reviewerName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    Row {
                                                        repeat(rev.rating) {
                                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(rev.comment, fontSize = 10.sp, color = Color.Gray)
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(rev.date, fontSize = 8.sp, color = Color.LightGray, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth())
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> { // Products Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Mes articles en vente (${myProducts.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Button(
                                        onClick = { showAddForm = !showAddForm },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(if (showAddForm) Icons.Default.Close else Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (showAddForm) "Fermer" else "Nouveau Produit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                AnimatedVisibility(
                                    visible = showAddForm,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .verticalScroll(rememberScrollState()),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text("Ajouter un Produit à ma Boutique", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF047857))
                                            
                                            OutlinedTextField(
                                                value = newProdTitle,
                                                onValueChange = { newProdTitle = it },
                                                label = { Text("Nom de l'article", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 1,
                                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(
                                                    value = newProdPrice,
                                                    onValueChange = { newProdPrice = it },
                                                    label = { Text("Prix (FCFA)", fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                                )
                                                OutlinedTextField(
                                                    value = newProdStock,
                                                    onValueChange = { newProdStock = it },
                                                    label = { Text("Stock disponible", fontSize = 10.sp) },
                                                    modifier = Modifier.weight(1f),
                                                    maxLines = 1,
                                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                                )
                                            }

                                            // Category selector
                                            var categoryExpanded by remember { mutableStateOf(false) }
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                OutlinedButton(
                                                    onClick = { categoryExpanded = true },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Catégorie : $newProdCategory", fontSize = 11.sp, color = Color.Black)
                                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.Gray)
                                                    }
                                                }
                                                DropdownMenu(
                                                    expanded = categoryExpanded,
                                                    onDismissRequest = { categoryExpanded = false }
                                                ) {
                                                    listOf("Mode & Vêtements", "Art & Artisanat", "Beauté & Cosmétiques", "Alimentation", "Épicerie", "Autres").forEach { cat ->
                                                        DropdownMenuItem(
                                                            text = { Text(cat, fontSize = 11.sp) },
                                                            onClick = {
                                                                newProdCategory = cat
                                                                categoryExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = newProdDesc,
                                                onValueChange = { newProdDesc = it },
                                                label = { Text("Description", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 3,
                                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                            )

                                            OutlinedTextField(
                                                value = newProdImageUrl,
                                                onValueChange = { newProdImageUrl = it },
                                                label = { Text("Photo principale (URL)", fontSize = 10.sp) },
                                                placeholder = { Text("https://exemple.com/image.png", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                maxLines = 1,
                                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Button(
                                                onClick = { prodPhotoLauncher.launch("image/*") },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("📱 Choisir photo principale depuis le téléphone", fontSize = 10.sp)
                                            }

                                            OutlinedTextField(
                                                value = newProdAdditionalImages,
                                                onValueChange = { newProdAdditionalImages = it },
                                                label = { Text("Photos supplémentaires (URLs séparées par des virgules)", fontSize = 10.sp) },
                                                placeholder = { Text("https://img1.png, https://img2.png", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                            )

                                            OutlinedTextField(
                                                value = newProdVariants,
                                                onValueChange = { newProdVariants = it },
                                                label = { Text("Variantes du produit (séparées par des virgules)", fontSize = 10.sp) },
                                                placeholder = { Text("Ex: Taille M, Taille L, Rouge, Vert, Noir", fontSize = 10.sp) },
                                                modifier = Modifier.fillMaxWidth(),
                                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                            )

                                            Button(
                                                onClick = {
                                                    if (newProdTitle.isBlank() || newProdPrice.isBlank()) {
                                                        Toast.makeText(context, "Veuillez remplir le nom et le prix !", Toast.LENGTH_SHORT).show()
                                                        return@Button
                                                    }
                                                    val priceInt = newProdPrice.toIntOrNull() ?: 0
                                                    val stockInt = newProdStock.toIntOrNull() ?: 5
                                                    val extraImagesList = newProdAdditionalImages.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                                    val variantsList = newProdVariants.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                                    viewModel.addProduct(
                                                        title = newProdTitle,
                                                        category = newProdCategory,
                                                        price = priceInt,
                                                        stock = stockInt,
                                                        shopName = myShopName,
                                                        location = userProfile.shopLocation,
                                                        description = newProdDesc,
                                                        imageUrl = newProdImageUrl,
                                                        images = extraImagesList,
                                                        variants = variantsList
                                                    )
                                                    Toast.makeText(context, "Produit '$newProdTitle' publié avec succès !", Toast.LENGTH_SHORT).show()
                                                    
                                                    // Reset
                                                    newProdTitle = ""
                                                    newProdPrice = ""
                                                    newProdStock = ""
                                                    newProdDesc = ""
                                                    newProdImageUrl = ""
                                                    newProdAdditionalImages = ""
                                                    newProdVariants = ""
                                                    showAddForm = false
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Mettre en vente l'article 📦", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }
                                }

                                if (myProducts.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Aucun article en vente dans votre boutique.", fontSize = 12.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(myProducts) { item ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(0.5.dp, Color(0xFFE2E8F0))
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(50.dp)
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(Color(0xFFF1F5F9))
                                                    ) {
                                                        if (item.imageUrl.isNotBlank()) {
                                                            AsyncImage(
                                                                model = item.imageUrl,
                                                                contentDescription = null,
                                                                modifier = Modifier.fillMaxSize(),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                        Text("${item.price.toLocaleString()} FCFA • Stock: ${item.stock}", fontSize = 10.sp, color = Color.Gray)
                                                        Text("Catégorie : ${item.category}", fontSize = 8.sp, color = Color.LightGray)
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteProduct(item.id)
                                                            Toast.makeText(context, "Produit supprimé !", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        2 -> { // Customer Orders Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Text("Commandes de mes clients (${myOrders.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("Gérez la livraison de vos articles et coordonnez avec vos acheteurs.", fontSize = 10.sp, color = Color.Gray)
                                
                                Spacer(modifier = Modifier.height(12.dp))

                                if (myOrders.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Aucune commande client pour l'instant.", fontSize = 12.sp, color = Color.Gray)
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(myOrders) { ord ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (ord.status == "Livré & Payé") Color(0xFFF8FAFC) else Color(0xFFFFFBEB)
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (ord.status == "Livré & Payé") Color(0xFFE2E8F0) else Color(0xFFFEF3C7)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("Commande #${ord.id.take(6).uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(4.dp))
                                                                .background(
                                                                    if (ord.status == "Livré & Payé") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)
                                                                )
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = ord.status,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (ord.status == "Livré & Payé") Color(0xFF065F46) else Color(0xFFB45309)
                                                            )
                                                        }
                                                    }
                                                    
                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    Text(ord.productTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                    Text(
                                                        text = if (ord.payInNCoins) "Payé en N Coins : ${ord.coinsCost.toLocaleString()} Coins"
                                                               else "Prix : ${ord.productPrice.toLocaleString()} FCFA à la livraison",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = Color(0xFF047857)
                                                    )

                                                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0), thickness = 0.5.dp)

                                                    Text("Client : ${ord.buyerName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    Text("WhatsApp : ${ord.buyerWhatsApp}", fontSize = 10.sp, color = Color.Gray)
                                                    
                                                    Spacer(modifier = Modifier.height(10.dp))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        // WhatsApp contact button
                                                        Button(
                                                            onClick = {
                                                                val url = "https://api.whatsapp.com/send?phone=${ord.buyerWhatsApp.replace(" ", "")}&text=Bonjour%20${ord.buyerName},%20je%20suis%20le%20vendeur%20de%20'${ord.productTitle}'%20sur%20Nora.%20Je%20vous%20contacte%20pour%20coordonner%20la%20livraison."
                                                                try {
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                                    context.startActivity(intent)
                                                                } catch (e: Exception) {
                                                                    Toast.makeText(context, "Impossible d'ouvrir WhatsApp.", Toast.LENGTH_SHORT).show()
                                                                }
                                                             },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.weight(1f).height(32.dp),
                                                            contentPadding = PaddingValues(0.dp)
                                                        ) {
                                                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("WhatsApp", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }

                                                        // Scan / Complete delivery simulator button
                                                        if (ord.status == "En attente de livraison") {
                                                            Button(
                                                                onClick = {
                                                                    val success = viewModel.scanDeliveryQrCode(ord.id)
                                                                    if (success) {
                                                                        Toast.makeText(context, "Livraison confirmée ! Compte crédité et commission payée.", Toast.LENGTH_LONG).show()
                                                                    } else {
                                                                        Toast.makeText(context, "Erreur lors du scan.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                },
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                                shape = RoundedCornerShape(6.dp),
                                                                modifier = Modifier.weight(1.2f).height(32.dp),
                                                                contentPadding = PaddingValues(0.dp)
                                                            ) {
                                                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("Simuler Scan Client", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
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

                        3 -> { // Edit Shop Tab
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text("Paramètres de la Boutique", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("Modifiez la description publique et les informations de localisation de votre atelier.", fontSize = 10.sp, color = Color.Gray)
                                
                                OutlinedTextField(
                                    value = editShopName,
                                    onValueChange = { editShopName = it },
                                    label = { Text("Nom de la Boutique", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = editShopDesc,
                                    onValueChange = { editShopDesc = it },
                                    label = { Text("Description d'activité", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = editShopLoc,
                                    onValueChange = { editShopLoc = it },
                                    label = { Text("Localisation de l'atelier (Ville, Quartier)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                OutlinedTextField(
                                    value = editShopPic,
                                    onValueChange = { editShopPic = it },
                                    label = { Text("Logo de la boutique (URL ou Galerie)", fontSize = 11.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { shopLogoLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📱 Choisir logo depuis le téléphone", fontSize = 10.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (editShopName.isBlank()) {
                                            Toast.makeText(context, "Le nom de la boutique ne peut pas être vide !", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        viewModel.updateShopProfile(
                                            shopName = editShopName,
                                            shopDesc = editShopDesc,
                                            shopLoc = editShopLoc,
                                            shopPic = editShopPic
                                        )
                                        Toast.makeText(context, "Boutique mise à jour avec succès !", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Enregistrer les modifications", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
