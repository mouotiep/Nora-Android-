package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import com.example.NoraViewModel
import com.example.ReelVideo
import com.example.ReelComment
import com.example.toLocaleString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReelsView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val reels by viewModel.reels.collectAsState()
    val shops by viewModel.shops.collectAsState()
    val products by viewModel.products.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val viewsRatio by viewModel.viewsRatio.collectAsState()
    val pagerState = rememberPagerState(pageCount = { reels.size })

    // Report Dialog State
    var reportingReel by remember { mutableStateOf<ReelVideo?>(null) }
    var reportReason by remember { mutableStateOf("") }

    // Comments State
    var showCommentsReelId by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }

    // Publish Reel Dialog State
    var showPublishReelDialog by remember { mutableStateOf(false) }

    // Unique user views check on active page with required duration:
    // - Video: 25 seconds minimum
    // - Photo: 5 seconds minimum
    var watchSeconds by remember(pagerState.currentPage) { mutableIntStateOf(0) }
    var isViewCountedForCurrentPage by remember(pagerState.currentPage) { mutableStateOf(false) }

    val activeReel = reels.getOrNull(pagerState.currentPage)
    val isPhotoActive = activeReel?.mediaType?.contains("Photo", ignoreCase = true) == true
    val requiredWatchTimeSeconds = if (isPhotoActive) 5 else 25

    LaunchedEffect(pagerState.currentPage, reels) {
        watchSeconds = 0
        isViewCountedForCurrentPage = false
        if (reels.isNotEmpty() && pagerState.currentPage < reels.size) {
            val currentReel = reels[pagerState.currentPage]
            val isPhoto = currentReel.mediaType.contains("Photo", ignoreCase = true)
            val targetTime = if (isPhoto) 5 else 25

            while (watchSeconds < targetTime) {
                delay(1000L)
                watchSeconds++
            }

            val isNewView = viewModel.recordUniqueView(currentReel.id)
            isViewCountedForCurrentPage = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (reels.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Aucun Reel disponible", color = Color.White)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reels[page]
                val isCurrent = (page == pagerState.currentPage)
                val targetWatchTime = if (reel.mediaType.contains("Photo", ignoreCase = true)) 5 else 25

                ReelPageItem(
                    reel = reel,
                    viewsRatio = viewsRatio,
                    isMyReel = reel.creatorName == userProfile.name,
                    watchSeconds = if (isCurrent) watchSeconds else 0,
                    requiredWatchSeconds = targetWatchTime,
                    isViewCounted = if (isCurrent) isViewCountedForCurrentPage else false,
                    followersCount = run {
                        val shop = shops.find { it.name.equals(reel.creatorName, ignoreCase = true) || it.id == reel.creatorName }
                        shop?.followersCount ?: 210
                    },
                    isCurrent = isCurrent,
                    onCreatorClick = {
                        val shop = shops.find { it.name.equals(reel.creatorName, ignoreCase = true) || it.id == reel.creatorName }
                        val shopId = shop?.id ?: products.find { it.shopName.equals(reel.creatorName, ignoreCase = true) }?.shopId ?: "shop-noun"
                        viewModel.selectShopAndNavigate(shopId)
                    },
                    onLike = { viewModel.toggleLike(reel.id) },
                    onFollow = { viewModel.toggleFollow(reel.id) },
                    onCommentsClick = { showCommentsReelId = reel.id },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Nora Cameroun Reel")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Regardez ce Reel sur Nora Cameroun par ${reel.creatorName}: \"${reel.caption}\" \nTéléchargez Nora pour soutenir nos artisans !"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Partager via"))
                    },
                    onReport = { reportingReel = reel },
                    onDelete = {
                        viewModel.deleteReel(reel.id)
                        Toast.makeText(context, "🗑️ Séquence Reel supprimée !", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Absolute-positioned floating publish button (1.5x smaller)
        SmallFloatingActionButton(
            onClick = { showPublishReelDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            containerColor = Color(0xFF10B981),
            contentColor = Color.White,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Publier", modifier = Modifier.size(13.dp))
                Text("Publier", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Reporting Dialog modal
    if (reportingReel != null) {
        val targetReel = reportingReel!!
        Dialog(onDismissRequest = { reportingReel = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Signaler ce Reel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Créateur: ${targetReel.creatorName}",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("Raison du signalement") },
                        placeholder = { Text("Ex: Plagiat, contenu inapproprié, arnaque...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFEF4444),
                            focusedLabelColor = Color(0xFFEF4444)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            reportingReel = null
                            reportReason = ""
                        }) {
                            Text("Annuler", color = Color(0xFF4B5563))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (reportReason.isBlank()) {
                                    Toast.makeText(context, "Saisissez une raison", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.reportItem(
                                    targetId = targetReel.id,
                                    targetName = "Reel de ${targetReel.creatorName}",
                                    reason = reportReason,
                                    type = "Vidéo"
                                )
                                Toast.makeText(context, "Signalement envoyé à l'administrateur !", Toast.LENGTH_LONG).show()
                                reportingReel = null
                                reportReason = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Envoyer", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Comments Dialog Modal
    if (showCommentsReelId != null) {
        val reelId = showCommentsReelId!!
        val activeReel = reels.find { it.id == reelId }
        
        if (activeReel != null) {
            Dialog(onDismissRequest = { showCommentsReelId = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(480.dp)
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Commentaires",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = "${activeReel.comments.size} réactions au total",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { showCommentsReelId = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Fermer")
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                        // Comments list
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (activeReel.comments.isEmpty()) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Soyez le premier à commenter !", fontSize = 12.sp, color = Color.Gray)
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(activeReel.comments) { comment ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            // Avatar circle
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = comment.authorName.take(2).uppercase(),
                                                    color = Color(0xFF047857),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = comment.authorName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF1F2937)
                                                    )
                                                    Text(
                                                        text = comment.time,
                                                        fontSize = 9.sp,
                                                        color = Color.Gray
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = comment.text,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF374151),
                                                    lineHeight = 16.sp
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val availableEmojis = listOf("👍", "❤️", "🔥", "😂", "👏")
                                                    availableEmojis.forEach { emoji ->
                                                        val count = comment.reactions[emoji] ?: 0
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(12.dp))
                                                                .background(if (count > 0) Color(0xFF10B981).copy(alpha = 0.12f) else Color(0xFFE2E8F0).copy(alpha = 0.5f))
                                                                .border(
                                                                    width = 1.dp,
                                                                    color = if (count > 0) Color(0xFF10B981) else Color.Transparent,
                                                                    shape = RoundedCornerShape(12.dp)
                                                                )
                                                                .clickable {
                                                                    viewModel.addReactionToComment(activeReel.id, comment.id, emoji)
                                                                }
                                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                                                .testTag("comment_${comment.id}_react_${emoji}"),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                Text(text = emoji, fontSize = 11.sp)
                                                                if (count > 0) {
                                                                    Text(
                                                                        text = "$count",
                                                                        fontSize = 10.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = Color(0xFF047857)
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(6.dp))

                                            // Report comment option (Flag)
                                            IconButton(
                                                onClick = {
                                                    viewModel.reportComment(activeReel.id, comment.id, comment.text)
                                                    Toast.makeText(context, "Commentaire suspect signalé à l'administrateur !", Toast.LENGTH_LONG).show()
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OutlinedFlag,
                                                    contentDescription = "Signaler Commentaire",
                                                    tint = Color.Red,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                        // Text input bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                placeholder = { Text("Ajouter un commentaire...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_comment_input_text"),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (newCommentText.trim().isNotEmpty()) {
                                        viewModel.addComment(activeReel.id, newCommentText)
                                        newCommentText = ""
                                        Toast.makeText(context, "Commentaire ajouté !", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                                    .testTag("submit_comment_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Envoyer",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Media Upload & Cropping Dialog for Publishing Reels
    if (showPublishReelDialog) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var isUploadingReel by remember { mutableStateOf(false) }
        var captionInput by remember { mutableStateOf("") }
        var categoryInput by remember { mutableStateOf("Mode & Vêtements") }
        var isVideoSelected by remember { mutableStateOf(true) } // true: Video, false: Photo
        
        var selectedUri by remember { mutableStateOf<Uri?>(null) }
        var selectedFileName by remember { mutableStateOf<String?>(null) }
        
        // Cropping states
        var showCroppingControls by remember { mutableStateOf(false) }
        var cropAspectRatio by remember { mutableStateOf("9:16") }
        var cropZoomLevel by remember { mutableStateOf(1.0f) }
        var cropRotationAngle by remember { mutableStateOf(0f) }
        var videoStartSec by remember { mutableFloatStateOf(0.0f) }
        var videoEndSec by remember { mutableFloatStateOf(35.0f) }

        // System gallery picker launcher
        val mediaPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            if (uri != null) {
                grantUriReadPermission(context, uri)
                selectedUri = uri
                selectedFileName = uri.lastPathSegment ?: if (isVideoSelected) "video.mp4" else "photo.jpg"
                showCroppingControls = true
            }
        }

        Dialog(onDismissRequest = { showPublishReelDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Publier un Nouveau Reel 🎥",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Text(
                        text = "Importez une vidéo ou photo de votre artisanat et ajustez le cadrage.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Media Type Selector
                    Text("Type de Fichier", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isVideoSelected = true; selectedFileName = null; selectedUri = null; showCroppingControls = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVideoSelected) Color(0xFF10B981) else Color(0xFFF1F5F9),
                                contentColor = if (isVideoSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MovieFilter, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Séquence Vidéo", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { isVideoSelected = false; selectedFileName = null; selectedUri = null; showCroppingControls = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isVideoSelected) Color(0xFF10B981) else Color(0xFFF1F5F9),
                                contentColor = if (!isVideoSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo / Image", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real File Upload Section
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .clickable {
                                    // Launch real system media gallery picker on telephone
                                    val mimeType = if (isVideoSelected) "video/*" else "image/*"
                                    mediaPickerLauncher.launch(mimeType)
                                }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (selectedFileName == null) {
                                Text("Choisir un fichier sur le téléphone 📱", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                Text("Sélectionnez dans la galerie (MP4, MOV, JPG, PNG)", fontSize = 10.sp, color = Color.Gray)
                            } else {
                                Text("Fichier sélectionné avec succès !", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                Text(selectedFileName!!, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Cliquez pour choisir un autre fichier", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Simulated Rogner (Cropping) Interface
                    if (showCroppingControls && selectedFileName != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Ajuster & Rogner le Fichier ✂️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(6.dp))

                        // Live visual player preview box with zoom, rotation & startSec/endSec
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedUri != null) {
                                UniversalMediaView(
                                    mediaUrl = selectedUri.toString(),
                                    mediaType = if (isVideoSelected) "Vidéo" else "Photo",
                                    autoPlayVideo = true,
                                    startSec = videoStartSec,
                                    endSec = videoEndSec,
                                    zoomLevel = cropZoomLevel,
                                    rotationAngle = cropRotationAngle,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Aspect Ratio overlay boundary
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFF10B981),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Text("Format: $cropAspectRatio", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls for Cropping (Aspect Ratio, Zoom, Rotate)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Aspect Ratio select
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Recadrage:", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(64.dp))
                                listOf("9:16", "1:1", "4:5").forEach { ratio ->
                                    val isSel = cropAspectRatio == ratio
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                            .clickable { cropAspectRatio = ratio }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(ratio, fontSize = 9.sp, color = if (isSel) Color.White else Color.Black)
                                    }
                                }
                            }

                            // Zoom level slider
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Zoom: ${String.format("%.1f", cropZoomLevel)}x", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(64.dp))
                                Slider(
                                    value = cropZoomLevel,
                                    onValueChange = { cropZoomLevel = it },
                                    valueRange = 1.0f..3.0f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981)),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Rotation Angle button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Rotation:", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(64.dp))
                                listOf(0f, 90f, 180f, 270f).forEach { angle ->
                                    val isSel = cropRotationAngle == angle
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                            .clickable { cropRotationAngle = angle }
                                            .padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Text("${angle.toInt()}°", fontSize = 9.sp, color = if (isSel) Color.White else Color.Black)
                                    }
                                }
                            }

                            // Video-specific temporal trimming sliders (Début & Fin)
                            if (isVideoSelected) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Divider(color = Color(0xFFE2E8F0))
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Rognage Temporel (Sélectionnez début et fin) ⏱️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Début: ${String.format("%.1f", videoStartSec)}s", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(72.dp))
                                    Slider(
                                        value = videoStartSec,
                                        onValueChange = { videoStartSec = it.coerceAtMost(videoEndSec - 1f) },
                                        valueRange = 0.0f..30.0f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Fin: ${String.format("%.1f", videoEndSec)}s", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(72.dp))
                                    Slider(
                                        value = videoEndSec,
                                        onValueChange = { videoEndSec = it.coerceAtLeast(videoStartSec + 1f) },
                                        valueRange = 1.0f..60.0f,
                                        colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(
                                    text = "Durée du clip vidéo : ${String.format("%.1f", videoEndSec - videoStartSec)} secondes (Rogné)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text fields
                    OutlinedTextField(
                        value = captionInput,
                        onValueChange = { captionInput = it },
                        label = { Text("Légende de la vidéo / Description", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Catégorie de l'Artisanat", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Mode & Vêtements", "Objets d'Art", "Alimentation").forEach { cat ->
                            val isSel = categoryInput == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) Color(0xFF10B981) else Color(0xFFF1F5F9))
                                    .clickable { categoryInput = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 9.sp, color = if (isSel) Color.White else Color.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPublishReelDialog = false }) { Text("Annuler") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            enabled = !isUploadingReel,
                            onClick = {
                                if (selectedFileName == null || selectedUri == null) {
                                    Toast.makeText(context, "Veuillez sélectionner et importer un fichier depuis votre téléphone", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (isVideoSelected) {
                                    val videoDuration = videoEndSec - videoStartSec
                                    if (videoDuration < 30.0f) {
                                        Toast.makeText(
                                            context,
                                            "⚠️ Une vidéo postée ne peut pas durer moins de 30 secondes ! (Durée sélectionnée: ${String.format("%.1f", videoDuration)}s)",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        return@Button
                                    }
                                }

                                scope.launch {
                                    isUploadingReel = true
                                    var finalMediaUrl = selectedUri.toString()

                                    // Upload file to Firebase Storage if selected from local gallery
                                    if (finalMediaUrl.startsWith("content://") || finalMediaUrl.startsWith("file://")) {
                                        Toast.makeText(context, "⏳ Téléversement du média sur Firebase Storage...", Toast.LENGTH_SHORT).show()
                                        val uploadResult = com.example.data.firebase.FirebaseManager.uploadFileToStorage(
                                            context = context,
                                            uri = selectedUri!!,
                                            folder = "reels"
                                        )
                                        if (uploadResult.isFailure) {
                                            val err = uploadResult.exceptionOrNull()?.message ?: "Erreur de stockage"
                                            Toast.makeText(context, "❌ Échec de l'envoi du média sur Firebase Storage : $err. Publication annulée.", Toast.LENGTH_LONG).show()
                                            isUploadingReel = false
                                            return@launch
                                        }
                                        finalMediaUrl = uploadResult.getOrNull() ?: ""
                                    }

                                    val publishResult = viewModel.publishReelSafely(
                                        caption = captionInput,
                                        category = categoryInput,
                                        mediaType = if (isVideoSelected) "Vidéo" else "Photo",
                                        aspectRatio = cropAspectRatio,
                                        zoomLevel = cropZoomLevel,
                                        rotationAngle = cropRotationAngle,
                                        mediaUrl = finalMediaUrl,
                                        startSec = videoStartSec,
                                        endSec = videoEndSec
                                    )

                                    isUploadingReel = false

                                    if (publishResult.isSuccess) {
                                        Toast.makeText(context, "Votre Reel a été publié avec succès !", Toast.LENGTH_SHORT).show()
                                        showPublishReelDialog = false
                                    } else {
                                        val err = publishResult.exceptionOrNull()?.message ?: "Erreur inconnue"
                                        Toast.makeText(context, "❌ Échec Firestore : $err", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isUploadingReel) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Téléversement...", fontSize = 11.sp, color = Color.White)
                            } else {
                                Text("Publier maintenant")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReelPageItem(
    reel: ReelVideo,
    viewsRatio: Float,
    isMyReel: Boolean,
    watchSeconds: Int = 0,
    requiredWatchSeconds: Int = 25,
    isViewCounted: Boolean = false,
    followersCount: Int = 180,
    isCurrent: Boolean = true,
    onCreatorClick: (() -> Unit)? = null,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onCommentsClick: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Responsive media canvas preserving video quality & ratio
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = reel.zoomLevel,
                    scaleY = reel.zoomLevel,
                    rotationZ = reel.rotationAngle
                )
                .background(Color.Black)
        ) {
            if (reel.mediaUrl.isNotBlank()) {
                UniversalMediaView(
                    mediaUrl = reel.mediaUrl,
                    mediaType = reel.mediaType,
                    contentScale = ContentScale.Crop,
                    autoPlayVideo = isCurrent,
                    startSec = reel.startSec,
                    endSec = reel.endSec,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (reel.mediaType == "Photo") Icons.Default.Photo else Icons.Default.MovieFilter,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (reel.mediaType == "Photo") "Séquence Photo Artisanat" else "Vidéo Reel Nora",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Dark gradient bottom overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        // Overlay Text Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { onCreatorClick?.invoke() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = reel.creatorName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column {
                        Text(
                            text = "@${reel.creatorName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "👥 $followersCount abonnés",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Follow toggle pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (reel.isFollowing) Color.Gray else Color(0xFF10B981))
                        .clickable { onFollow() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (reel.isFollowing) "Suivi" else "+ Suivre",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var isCaptionExpanded by remember(reel.id) { mutableStateOf(false) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reel.caption,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = if (isCaptionExpanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (reel.caption.length > 30) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCaptionExpanded) " Voir moins" else " ...plus",
                        color = Color(0xFF10B981),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isCaptionExpanded = !isCaptionExpanded }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Views tag indicator badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${reel.viewsCount} vues",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlay Right-Side Action Controls (1.5x smaller size = 30.dp)
        var showOverflowMenu by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Like button (30.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) Color.Red else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${reel.likesCount}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comments button (30.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentsClick,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("comments_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Commentaires",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "${reel.comments.size}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share button (30.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "Partager",
                    color = Color.White,
                    fontSize = 9.sp
                )
            }

            // (...) Overflow suspension menu grouping Signaler and Supprimer
            Box {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { showOverflowMenu = true },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Plus d'options",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Plus",
                        color = Color.White,
                        fontSize = 9.sp
                    )
                }

                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { showOverflowMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("🚩 Signaler la vidéo", fontSize = 12.sp, color = Color.Red) },
                        onClick = {
                            showOverflowMenu = false
                            onReport()
                        }
                    )
                    if (isMyReel) {
                        DropdownMenuItem(
                            text = { Text("🗑️ Supprimer la vidéo", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold) },
                            onClick = {
                                showOverflowMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
