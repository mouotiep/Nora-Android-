package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.style.TextOverflow
import com.example.NoraViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CreatorDashboardView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeUser by viewModel.userProfile.collectAsState()
    val reels by viewModel.reels.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val walletNCoins by viewModel.walletNCoins.collectAsState()

    // Filter reels created by this user
    val creatorName = activeUser.name
    val myReels = reels.filter { it.creatorName == creatorName }
    val myTotalViews = myReels.sumOf { it.viewsCount }
    val myTotalLikes = myReels.sumOf { it.likesCount }

    // State for publishing reel
    var showPublishDialog by remember { mutableStateOf(false) }
    var reelCaption by remember { mutableStateOf("") }
    var reelCategory by remember { mutableStateOf("Mode & Vêtements") }

    // Selected media states for publishing
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }
    var selectedDemoId by remember { mutableStateOf<String?>(null) } // "ebene", "tissu", "poivre"
    var selectedMediaType by remember { mutableStateOf("Vidéo") } // "Photo" or "Vidéo"
    var showCroppingTool by remember { mutableStateOf(false) }

    // Cropping & Editing workspace states
    var cropAspectRatio by remember { mutableStateOf("9:16") } // "1:1", "9:16", "16:9", "4:5"
    var cropZoom by remember { mutableStateOf(1f) } // 1f to 3f
    var cropRotation by remember { mutableStateOf(0f) } // 0f, 90f, 180f, 270f
    var cropTimeStart by remember { mutableStateOf(0f) } // seconds
    var cropTimeEnd by remember { mutableStateOf(15f) } // seconds
    var isMediaCropped by remember { mutableStateOf(false) }

    // Activity launcher for phone file picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
            selectedDemoId = null
            selectedMediaType = if (uri.toString().contains("video", ignoreCase = true) || uri.toString().contains("mp4", ignoreCase = true)) "Vidéo" else "Photo"
            showCroppingTool = true
        }
    }

    // Categories available
    val categories = listOf("Mode & Vêtements", "Accessoires & Bijou", "Alimentation", "Objets d'Art")
    var categoryExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Welcome Creator Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                            )
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = creatorName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = creatorName,
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Certifié",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "Créateur de Contenu Artisanal",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.25f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Niveau: Ambassadeur Nora",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Action Publier un Reel ---
        item {
            Button(
                onClick = { showPublishDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_new_reel_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publier une Vidéo Culturelle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // --- 3. Dynamic Stats Grid ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Performances de vos Vidéos",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stat 1: Publications
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Publications", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "${myReels.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                    }

                    // Stat 2: Vues totales
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Vues Totales", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "$myTotalViews", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                    }

                    // Stat 3: Likes reçus
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Likes Reçus", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "$myTotalLikes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                        }
                    }
                }
            }
        }

        // --- 4. Content list & Simulation tool ---
        if (myReels.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Vos Vidéos En Ligne",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Touchez une vidéo pour simuler instantanément +500 Vues et gagner des N Coins !",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    myReels.forEach { r ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    viewModel.simulateViews(r.id, 500)
                                    Toast.makeText(context, "Simulation réussie: +500 Vues sur \"${r.caption.take(20)}...\"", Toast.LENGTH_SHORT).show()
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = r.caption,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = Color(0xFF1F2937)
                                    )
                                    Text(
                                        text = "Catégorie: ${r.category} • ${r.comments.size} commentaires",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.RemoveRedEye, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${r.viewsCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Text(text = "Simuler +500", color = Color(0xFF10B981), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Creator Wallet & Transactions ---
        item {
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
                        Text(
                            text = "Portefeuille Créateur",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937)
                        )
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vos N Coins accumulés", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = "$walletNCoins N Coins",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF047857)
                            )
                        }

                        Button(
                            onClick = {
                                Toast.makeText(context, "Paiement en cours vers Mobile Money (+237)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Retirer (FCFA)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Historique des gains",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4B5563),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Display creator reward transactions or mock positive cashflows
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        transactions.filter { it.title.contains("création", ignoreCase = true) || it.amount > 0 }
                            .take(4)
                            .forEach { tx ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFD1FAE5)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TrendingUp,
                                                contentDescription = null,
                                                tint = Color(0xFF065F46),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(text = tx.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Text(text = tx.date, fontSize = 9.sp, color = Color.Gray)
                                        }
                                    }
                                    Text(
                                        text = "+${tx.amount} N Coins",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    // --- Publishing Dialog ---
    if (showPublishDialog) {
        Dialog(onDismissRequest = { showPublishDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Publier une Œuvre Culturelle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Mettez en avant l'artisanat ou les traditions du Cameroun. Vos créations s'affichent instantanément dans le flux Nora !",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = reelCaption,
                        onValueChange = { reelCaption = it },
                        label = { Text("Légende de la publication") },
                        placeholder = { Text("Ex: Démonstration de sculpture du bois d'ébène à Foumban 🪵 #SavoirFaire") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_reel_caption_input"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Media Upload and Crop section
                    Text(
                        text = "Média de la publication",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF374151),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (!isMediaCropped) {
                        // Not cropped / no media selected state
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF10B981).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ajouter un média depuis votre téléphone",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "Vous pourrez le rogner, zoomer et pivoter librement",
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Gallery Picker Button
                                    Button(
                                        onClick = { galleryLauncher.launch("*/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1.5f)
                                            .height(36.dp)
                                            .testTag("upload_from_gallery_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Télécharger média", fontSize = 11.sp, color = Color.White)
                                    }

                                    // Demo Model Button
                                    OutlinedButton(
                                        onClick = {
                                            selectedDemoId = "ebene"
                                            selectedMediaUri = null
                                            selectedMediaType = "Vidéo"
                                            showCroppingTool = true
                                        },
                                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("upload_demo_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color(0xFF10B981)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Démo", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Media selected and cropped state
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF3F4F6)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (selectedMediaType == "Photo") Icons.Default.Photo else Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (selectedMediaUri != null) "Média : ${selectedMediaUri.toString().substringAfterLast("/")}" else "Démo : ${selectedDemoId?.uppercase()}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${selectedMediaType} • Format: ${cropAspectRatio} • Zoom: ${cropZoom}x" +
                                                (if (selectedMediaType == "Vidéo") " • Trim: ${cropTimeStart.toInt()}-${cropTimeEnd.toInt()}s" else ""),
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    Row(
                                        modifier = Modifier.padding(top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "✂️ Rogner / Ajuster",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            modifier = Modifier
                                                .clickable { showCroppingTool = true }
                                                .testTag("re_crop_button")
                                        )
                                        Text(
                                            text = "🗑️ Supprimer",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Red,
                                            modifier = Modifier.clickable {
                                                isMediaCropped = false
                                                selectedMediaUri = null
                                                selectedDemoId = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category selection dropdown container
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = reelCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie d'artisanat") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { categoryExpanded = true }
                                .testTag("publish_reel_category_input"),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = {
                                IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        reelCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showPublishDialog = false
                            reelCaption = ""
                            isMediaCropped = false
                            selectedMediaUri = null
                            selectedDemoId = null
                        }) {
                            Text("Annuler", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (reelCaption.isBlank()) {
                                    Toast.makeText(context, "Saisissez une légende pour votre publication", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.publishReel(
                                    caption = reelCaption,
                                    category = reelCategory,
                                    mediaType = selectedMediaType,
                                    aspectRatio = if (isMediaCropped) cropAspectRatio else "9:16",
                                    zoomLevel = if (isMediaCropped) cropZoom else 1f,
                                    rotationAngle = if (isMediaCropped) cropRotation else 0f
                                )
                                Toast.makeText(context, "Œuvre publiée avec succès !", Toast.LENGTH_LONG).show()
                                showPublishDialog = false
                                reelCaption = ""
                                isMediaCropped = false
                                selectedMediaUri = null
                                selectedDemoId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("publish_reel_submit_button")
                        ) {
                            Text("Publier", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- Atelier de Rognage Nora ---
    if (showCroppingTool) {
        Dialog(onDismissRequest = { showCroppingTool = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F172A), // Premium dark editor background
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showCroppingTool = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                        Text(
                            text = "Ajuster & Rogner",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                isMediaCropped = true
                                showCroppingTool = false
                                Toast.makeText(context, "Cadrage appliqué !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("confirm_crop_done_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color(0xFF10B981))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cropping visual workspace
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        // Media container with dynamic scaling, rotation and dimensions
                        Box(
                            modifier = Modifier
                                .size(
                                    width = when(cropAspectRatio) {
                                        "1:1" -> 160.dp
                                        "9:16" -> 110.dp
                                        "16:9" -> 210.dp
                                        "4:5" -> 135.dp
                                        else -> 160.dp
                                    },
                                    height = when(cropAspectRatio) {
                                        "1:1" -> 160.dp
                                        "9:16" -> 195.dp
                                        "16:9" -> 118.dp
                                        "4:5" -> 168.dp
                                        else -> 160.dp
                                    }
                                )
                                .graphicsLayer(
                                    scaleX = cropZoom,
                                    scaleY = cropZoom,
                                    rotationZ = cropRotation
                                )
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedMediaType == "Photo") Icons.Default.Photo else Icons.Default.PlayCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981).copy(alpha = 0.8f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (selectedMediaUri != null) "Média Local" else "Démo Ébène Foumban",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            // 3x3 Overlay Grid lines
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(w / 3f, 0f), end = Offset(w / 3f, h),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(2f * w / 3f, 0f), end = Offset(2f * w / 3f, h),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(0f, h / 3f), end = Offset(w, h / 3f),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawLine(
                                    color = Color.White.copy(alpha = 0.4f),
                                    start = Offset(0f, 2f * h / 3f), end = Offset(w, 2f * h / 3f),
                                    strokeWidth = 1.dp.toPx()
                                )
                                drawRect(
                                    color = Color(0xFF10B981),
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }

                        // Aspect ratio badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Ratio: $cropAspectRatio",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Media Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Média :",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(44.dp)
                        )
                        listOf("Photo", "Vidéo").forEach { type ->
                            val isSel = selectedMediaType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF10B981) else Color(0xFF334155))
                                    .clickable { selectedMediaType = type }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (type == "Photo") "📷 Photo" else "🎥 Vidéo",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Crop Aspect Ratio selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ratio :",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(44.dp)
                        )
                        listOf("1:1", "9:16", "16:9", "4:5").forEach { ratio ->
                            val isSel = cropAspectRatio == ratio
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) Color(0xFF10B981).copy(alpha = 0.25f) else Color(0xFF1E293B))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSel) Color(0xFF10B981) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { cropAspectRatio = ratio }
                                    .padding(vertical = 6.dp)
                                    .testTag("crop_ratio_$ratio"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ratio,
                                    color = if (isSel) Color(0xFF10B981) else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Zoom adjustment
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zoom :",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(44.dp)
                        )
                        Slider(
                            value = cropZoom,
                            onValueChange = { cropZoom = it },
                            valueRange = 1f..3f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF10B981),
                                activeTrackColor = Color(0xFF10B981),
                                inactiveTrackColor = Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("crop_zoom_slider")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${String.format("%.1f", cropZoom)}x",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.width(28.dp)
                        )
                    }

                    // Rotation Control Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Pivot :",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(44.dp)
                        )
                        IconButton(
                            onClick = {
                                cropRotation = (cropRotation + 90f) % 360f
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E293B))
                                .size(36.dp)
                                .testTag("crop_rotate_button")
                        ) {
                            Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White)
                        }
                        Text(
                            text = "Faire pivoter de 90° (${cropRotation.toInt()}°)",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Video duration trimming segment (only visible when video is selected)
                    if (selectedMediaType == "Vidéo") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rognage temporel (Vidéo)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${cropTimeStart.toInt()}s - ${cropTimeEnd.toInt()}s",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Début", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(36.dp))
                                Slider(
                                    value = cropTimeStart,
                                    onValueChange = {
                                        cropTimeStart = it.coerceAtMost(cropTimeEnd - 1f)
                                    },
                                    valueRange = 0f..30f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF34D399),
                                        activeTrackColor = Color(0xFF34D399)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Fin", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(36.dp))
                                Slider(
                                    value = cropTimeEnd,
                                    onValueChange = {
                                        cropTimeEnd = it.coerceAtLeast(cropTimeStart + 1f)
                                    },
                                    valueRange = 0f..30f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color(0xFF10B981),
                                        activeTrackColor = Color(0xFF10B981)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Validate button
                    Button(
                        onClick = {
                            isMediaCropped = true
                            showCroppingTool = false
                            Toast.makeText(context, "Modifications appliquées !", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("apply_crop_submit_button")
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmer le Rognage", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
