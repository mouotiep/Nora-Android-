package com.example.ui.components

import android.content.Intent
import android.net.Uri
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.layout.ContentScale
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

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfile(userProfile.name, userProfile.whatsappNumber, it.toString())
            Toast.makeText(context, "📸 Photo de profil mise à jour avec succès !", Toast.LENGTH_SHORT).show()
        }
    }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditShopDialog by remember { mutableStateOf(false) }
    var showShopManagerDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf<String?>(null) }
    var showPublishReelDialog by remember { mutableStateOf(false) }
    var showChangePhotoDialog by remember { mutableStateOf(false) }

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
                // Profile Avatar with edit camera overlay (WhatsApp-style)
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clickable { showChangePhotoDialog = true }
                        .testTag("profile_avatar_box"),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userProfile.profilePic.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(userProfile.profilePic)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Photo de profil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userProfile.name.take(2).uppercase(),
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // WhatsApp-style green camera edit badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Changer la photo",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
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
                            onClick = { showShopManagerDialog = true },
                            modifier = Modifier.weight(1f).testTag("manage_shop_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981), contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gérer Boutique", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        viewModel.logoutUser()
                        Toast.makeText(context, "Vous avez été déconnecté 👋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFB91C1C))
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Se déconnecter",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFB91C1C)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Se déconnecter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

        // --- Invitation & Parrainage (Referral Program) ---
        val referralCode = if (userProfile.referralCode.isNotBlank()) userProfile.referralCode else (userProfile.name.lowercase().replace(" ", "_") + "-ref")
        val referralLink = "https://nora-cameroun.com/invite/$referralCode"
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)), // light WhatsApp-green background
            border = BorderStroke(1.dp, Color(0xFFDCFCE7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Parrainage Nora Cameroun 👥",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Gagnez +0.25 N Coin pour chaque nouvel utilisateur qui s'inscrit via votre lien unique de parrainage !",
                    fontSize = 11.sp,
                    color = Color(0xFF15803D)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Link display box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = referralLink,
                        fontSize = 10.sp,
                        color = Color(0xFF166534),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Copy button
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(referralLink) })
                        Toast.makeText(context, "Lien de parrainage copié !", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF16A34A)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }


        // --- Followed Shops & Creators Section (WhatsApp-style tracking) ---
        val followedShops by viewModel.followedShops.collectAsState()
        val followedCreators by viewModel.followedCreators.collectAsState()
        val products by viewModel.products.collectAsState()

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vos Abonnements Nora 🌟", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                
                if (followedCreators.isEmpty() && followedShops.isEmpty()) {
                    Text(
                        text = "Vous ne suivez aucun créateur ni boutique pour le moment. Suivez-en dans les onglets Marché et Reels !",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (followedCreators.isNotEmpty()) {
                    Column {
                        Text("Créateurs de contenu", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            followedCreators.forEach { creator ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable {
                                        viewModel.toggleFollowCreator(creator)
                                        Toast.makeText(context, "Vous ne suivez plus $creator", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(creator.take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(creator, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text("Se désabonner", fontSize = 8.sp, color = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }

                if (followedShops.isNotEmpty() && followedCreators.isNotEmpty()) {
                    Divider(color = Color(0xFFF1F5F9))
                }

                if (followedShops.isNotEmpty()) {
                    Column {
                        Text("Boutiques favorites", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            followedShops.forEach { shopId ->
                                val shopName = products.find { it.shopId == shopId }?.shopName ?: "Boutique Nora"
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable {
                                            viewModel.toggleFollowShop(shopId)
                                            Toast.makeText(context, "Boutique retirée des favoris", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFD1FAE5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(shopName, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("Retirer", fontSize = 8.sp, color = Color.Red, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
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

        // --- Direct Admin WhatsApp Contact Card ---
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_admin_whatsapp_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp Admin",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Assistance & Support Admin Nora 💬",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF14532D)
                        )
                        Text(
                            text = "WhatsApp officiel : +237 655 924 778",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Posez vos questions sur la validation de boutique KYC, la livraison des commandes ou le programme N-Coins directement à l'administration.",
                    fontSize = 11.sp,
                    color = Color(0xFF15803D)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=237655924778&text=Bonjour%20Admin%20Nora,%20je%20vous%20contacte%20depuis%20l'application%20Nora."))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Numéro WhatsApp de l'Administration : +237 655 924 778", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("contact_admin_whatsapp_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A), contentColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Écrire directement sur WhatsApp (+237 655 924 778)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                        label = { Text("Numéro WhatsApp (+237 obligatoire)") },
                        placeholder = { Text("Ex: +237655924778") },
                        supportingText = { Text("Format: +237 suivi de 9 chiffres (Ex: +237655924778)", fontSize = 10.sp, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showEditProfileDialog = false }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (tempName.isBlank()) {
                                    Toast.makeText(context, "Veuillez saisir votre nom", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val validWhatsapp = NoraViewModel.validateAndFormatCameroonPhone(tempWhatsapp)
                                if (validWhatsapp == null) {
                                    Toast.makeText(context, "Numéro WhatsApp invalide ! Syntaxe obligatoire : +237 suivi de 9 chiffres (Ex: +237655924778)", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                viewModel.updateProfile(tempName, validWhatsapp, "")
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

    // Shop Manager & Dashboard Dialog
    if (showShopManagerDialog) {
        ShopManagerDialog(
            onDismiss = { showShopManagerDialog = false },
            viewModel = viewModel
        )
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
        var selectedMediaType by remember { mutableStateOf("Vidéo") } // "Vidéo" or "Photo"
        var videoDurationSec by remember { mutableFloatStateOf(35.0f) }
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
                    Text("Publier une Vidéo ou Photo (Reel)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Faites rayonner les couleurs locales du Cameroun en publiant un nouveau Reel.", fontSize = 11.sp, color = Color.Gray)

                    // Media Type Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedMediaType = "Vidéo" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMediaType == "Vidéo") Color(0xFF10B981) else Color(0xFFF1F5F9),
                                contentColor = if (selectedMediaType == "Vidéo") Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎥 Vidéo (≥ 30s)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedMediaType = "Photo" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedMediaType == "Photo") Color(0xFF10B981) else Color(0xFFF1F5F9),
                                contentColor = if (selectedMediaType == "Photo") Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📷 Photo (≥ 5s vue)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (selectedMediaType == "Vidéo") {
                        Column {
                            Text("Durée de la vidéo : ${videoDurationSec.toInt()} secondes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Slider(
                                value = videoDurationSec,
                                onValueChange = { videoDurationSec = it },
                                valueRange = 30.0f..180.0f,
                                colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                            )
                            Text("⚠️ Règle Nora : Une vidéo postée doit durer au moins 30 secondes.", fontSize = 9.sp, color = Color.Gray)
                        }
                    } else {
                        Text("ℹ️ Une photo nécessite au moins 5s de visionnage pour être comptée comme une vue.", fontSize = 10.sp, color = Color.Gray)
                    }

                    OutlinedTextField(
                        value = reelCaption,
                        onValueChange = { reelCaption = it },
                        label = { Text("Légende / Description") },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
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
                                if (selectedMediaType == "Vidéo" && videoDurationSec < 30.0f) {
                                    Toast.makeText(context, "⚠️ Une vidéo postée ne peut pas durer moins de 30 secondes !", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                viewModel.publishReel(
                                    caption = reelCaption,
                                    category = selectedCategory,
                                    mediaType = selectedMediaType
                                )
                                Toast.makeText(context, "Publication culturelle effectuée avec succès !", Toast.LENGTH_LONG).show()
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

    // --- Change Photo Dialog (WhatsApp-style) ---
    if (showChangePhotoDialog) {
        val avatarOptions = listOf(
            "https://api.dicebear.com/7.x/avataaars/png?seed=Leo" to "Dessin Homme 1",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Jack" to "Dessin Homme 2",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Felix" to "Dessin Homme 3",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Sam" to "Dessin Homme 4",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Max" to "Dessin Homme 5",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Alex" to "Dessin Homme 6",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Emma" to "Dessin Femme 1",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Sara" to "Dessin Femme 2",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Lily" to "Dessin Femme 3",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Maya" to "Dessin Femme 4",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Zoe" to "Dessin Femme 5",
            "https://api.dicebear.com/7.x/avataaars/png?seed=Luna" to "Dessin Femme 6"
        )

        Dialog(onDismissRequest = { showChangePhotoDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Photo de profil 📸",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = "Importez une photo depuis votre téléphone ou choisissez un avatar dessiné.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Real phone import button
                    Button(
                        onClick = {
                            photoPickerLauncher.launch("image/*")
                            showChangePhotoDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Importer depuis le téléphone 📱", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    Text(
                        text = "Dessins d'avatars (Masculins & Féminins)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    // Display avatar drawings in clean grid format
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        avatarOptions.chunked(3).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                chunk.forEach { (url, label) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                viewModel.updateProfile(userProfile.name, userProfile.whatsappNumber, url)
                                                Toast.makeText(context, "Photo de profil mise à jour !", Toast.LENGTH_SHORT).show()
                                                showChangePhotoDialog = false
                                            }
                                            .padding(4.dp)
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(url)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = label,
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(CircleShape)
                                                .border(1.dp, Color.LightGray, CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(label, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                                    }
                                }
                                // Fill empty spaces if chunk is not full
                                if (chunk.size < 3) {
                                    repeat(3 - chunk.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Simulated Selfie option for immediate testing
                    OutlinedButton(
                        onClick = {
                            val simulatedSelfie = "https://api.dicebear.com/7.x/avataaars/png?seed=simulatedSelfie_${(1..100).random()}"
                            viewModel.updateProfile(userProfile.name, userProfile.whatsappNumber, simulatedSelfie)
                            Toast.makeText(context, "📸 Selfie instantané capturé !", Toast.LENGTH_SHORT).show()
                            showChangePhotoDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color(0xFF10B981))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Prendre un selfie 📸", fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }

                    if (userProfile.profilePic.isNotBlank()) {
                        TextButton(
                            onClick = {
                                viewModel.updateProfile(userProfile.name, userProfile.whatsappNumber, "clear")
                                Toast.makeText(context, "Photo de profil supprimée", Toast.LENGTH_SHORT).show()
                                showChangePhotoDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Supprimer la photo actuelle", fontWeight = FontWeight.Bold)
                        }
                    }

                    TextButton(
                        onClick = { showChangePhotoDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Fermer", color = Color.Gray)
                    }
                }
            }
        }
    }
}
