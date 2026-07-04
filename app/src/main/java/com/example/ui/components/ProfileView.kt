package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.NoraViewModel
import com.example.toLocaleString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val walletNCoins by viewModel.walletNCoins.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val orders by viewModel.orders.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditShopDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf<String?>(null) }
    var showPublishReelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- User Identity Card Header ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Avatar with initials
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userProfile.name.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(userProfile.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text("WhatsApp: ${userProfile.whatsappNumber}", fontSize = 12.sp, color = Color.Gray)
                
                // Status KYC tag
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (userProfile.kycStatus) {
                                "Certifié" -> Color(0xFFD1FAE5)
                                "En Attente" -> Color(0xFFFEF3C7)
                                "Banni", "Arnaqueur" -> Color(0xFFFEE2E2)
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (userProfile.kycStatus == "Certifié") {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = when (userProfile.kycStatus) {
                                "Certifié" -> "Vendeur Certifié (KYC)"
                                "En Attente" -> "KYC En Attente"
                                "Banni" -> "Compte Banni"
                                "Arnaqueur" -> "Bannière Fraude (Arnaqueur)"
                                else -> "Acheteur Standard"
                            },
                            color = when (userProfile.kycStatus) {
                                "Certifié" -> Color(0xFF065F46)
                                "En Attente" -> Color(0xFFD97706)
                                "Banni", "Arnaqueur" -> Color(0xFFB91C1C)
                                else -> Color.Gray
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.weight(1f).testTag("edit_profile_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color.Black)
                    ) {
                        Text("Modifier Profil", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (userProfile.kycStatus == "Certifié") {
                        Button(
                            onClick = { showEditShopDialog = true },
                            modifier = Modifier.weight(1f).testTag("edit_shop_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECFDF5), contentColor = Color(0xFF047857))
                        ) {
                            Text("Modifier Boutique", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Active Orders / Buyer QR Codes ---
        if (orders.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vos Commandes & QR Codes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Présentez ce QR Code au livreur pour confirmer la livraison et le transfert de paiement.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    orders.forEach { ord ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(Color(0xFFF8FAFC))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ord.productTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Vendeur: ${ord.sellerName} • Statut: ${ord.status}", fontSize = 10.sp, color = Color.Gray)
                            }
                            
                            if (ord.status == "En attente de livraison") {
                                Button(
                                    onClick = { showQrDialog = ord.id },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mon QR", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Paid", tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- Wallet & Transaction ledger ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Solde Portefeuille Nora", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("${walletNCoins.toLocaleString()}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("N Coins", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Historique des Transactions", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Spacer(modifier = Modifier.height(8.dp))

                transactions.forEach { trans ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(trans.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(trans.description, fontSize = 10.sp, color = Color.Gray)
                            Text(trans.date, fontSize = 9.sp, color = Color.LightGray)
                        }

                        Text(
                            text = if (trans.isPositive) "+${trans.amount}" else "${trans.amount}",
                            color = if (trans.isPositive) Color(0xFF10B981) else Color.Red,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- Content Creation Card (For any user!) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Création de Contenu Culturel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "Même en tant qu'acheteur, vous pouvez créer du contenu interactif et partager des courtes vidéos (Reels) pour valoriser la culture camerounaise.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showPublishReelDialog = true },
                    modifier = Modifier.fillMaxWidth().testTag("profile_publish_reel_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Videocam, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Publier une Vidéo (Reel)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }

    // Edit Profile details dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userProfile.name) }
        var tempWhatsapp by remember { mutableStateOf(userProfile.whatsappNumber) }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Modifier votre Profil", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Nom Complet") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempWhatsapp,
                        onValueChange = { tempWhatsapp = it },
                        label = { Text("Numéro WhatsApp (Obligatoire)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditProfileDialog = false }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tempName.isBlank() || tempWhatsapp.isBlank()) {
                                    Toast.makeText(context, "Saisie incomplète", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.updateProfile(tempName, tempWhatsapp, "")
                                Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                                showEditProfileDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }

    // Edit Shop Profile Dialog
    if (showEditShopDialog) {
        var tempShopName by remember { mutableStateOf(userProfile.shopName) }
        var tempShopDesc by remember { mutableStateOf(userProfile.shopDescription) }
        var tempShopLoc by remember { mutableStateOf(userProfile.shopLocation) }

        Dialog(onDismissRequest = { showEditShopDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Modifier votre Boutique", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = tempShopName,
                        onValueChange = { tempShopName = it },
                        label = { Text("Nom de la Boutique") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempShopDesc,
                        onValueChange = { tempShopDesc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = tempShopLoc,
                        onValueChange = { tempShopLoc = it },
                        label = { Text("Localisation / Ville") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditShopDialog = false }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tempShopName.isBlank()) {
                                    Toast.makeText(context, "Nom requis", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.updateShopProfile(tempShopName, tempShopDesc, tempShopLoc, "")
                                Toast.makeText(context, "Boutique mise à jour !", Toast.LENGTH_SHORT).show()
                                showEditShopDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("Enregistrer")
                        }
                    }
                }
            }
        }
    }

    // QR Code Representation Display Dialog
    if (showQrDialog != null) {
        val orderId = showQrDialog!!
        Dialog(onDismissRequest = { showQrDialog = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("QR Code de Livraison", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Commande #${orderId}", fontSize = 12.sp, color = Color.Gray)
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Simulated Vector QR Code
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.White)
                            .border(1.5.dp, Color.Black)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing high contrast grid pixels to look exactly like a real QR code
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(10) { row ->
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    repeat(10) { col ->
                                        val isFilled = (row % 3 == 0 && col % 2 == 0) || (row % 4 == 0) || (col % 3 == 0) || (row < 3 && col < 3) || (row > 6 && col > 6) || (row < 3 && col > 6)
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(if (isFilled) Color.Black else Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Le livreur doit scanner ce code avec son application Nora pour finaliser le versement.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showQrDialog = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Fermer", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Publish Reel Dialog (For any general user!)
    if (showPublishReelDialog) {
        var reelCaption by remember { mutableStateOf("") }
        val categoriesList by viewModel.categories.collectAsState()
        val selectableCategories = remember(categoriesList) {
            categoriesList.filter { it != "Tous" }
        }
        var selectedCategory by remember { mutableStateOf(selectableCategories.firstOrNull() ?: "Mode & Vêtements") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showPublishReelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Publier une Vidéo (Reel)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Faites rayonner les couleurs locales du Cameroun en publiant un nouveau Reel.", fontSize = 11.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = reelCaption,
                        onValueChange = { reelCaption = it },
                        label = { Text("Légende / Description de la vidéo") },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        maxLines = 3,
                        placeholder = { Text("Ex: Magnifique kaba ndondo traditionnel fait main...") }
                    )

                    // Category selection dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie du Reel") },
                            modifier = Modifier.fillMaxWidth().clickable { dropdownExpanded = true },
                            trailingIcon = {
                                IconButton(onClick = { dropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            selectableCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showPublishReelDialog = false }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (reelCaption.isBlank()) {
                                    Toast.makeText(context, "Saisissez une légende pour votre publication", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.publishReel(
                                    caption = reelCaption,
                                    category = selectedCategory,
                                    mediaType = "Vidéo"
                                )
                                Toast.makeText(context, "Vidéo culturelle publiée avec succès !", Toast.LENGTH_LONG).show()
                                showPublishReelDialog = false
                                reelCaption = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Publier", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
