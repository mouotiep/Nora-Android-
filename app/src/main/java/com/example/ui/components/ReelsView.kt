package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.NoraViewModel
import com.example.ReelVideo
import com.example.ReelComment
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
    val pagerState = rememberPagerState(pageCount = { reels.size })

    // Report Dialog State
    var reportingReel by remember { mutableStateOf<ReelVideo?>(null) }
    var reportReason by remember { mutableStateOf("") }

    // Comments State
    var showCommentsReelId by remember { mutableStateOf<String?>(null) }
    var newCommentText by remember { mutableStateOf("") }

    // Simulating auto views on active page
    LaunchedEffect(pagerState.currentPage) {
        if (reels.isNotEmpty()) {
            val activeReel = reels[pagerState.currentPage]
            // Wait 4 seconds on the active page to simulate viewing, then reward
            delay(4000)
            viewModel.simulateViews(activeReel.id, 10)
            Toast.makeText(context, "+10 Vues simulées ! Vous soutenez le créateur ${activeReel.creatorName}", Toast.LENGTH_SHORT).show()
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
                ReelPageItem(
                    reel = reel,
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
                    onReport = { reportingReel = reel }
                )
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
}

@Composable
fun ReelPageItem(
    reel: ReelVideo,
    onLike: () -> Unit,
    onFollow: () -> Unit,
    onCommentsClick: () -> Unit,
    onShare: () -> Unit,
    onReport: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Simulated video canvas background with colors and dynamic art
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF10B981).copy(alpha = 0.2f),
                            Color(0xFF0F172A)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Spinning vinyl or cultural visual placeholder
                Box(
                    modifier = Modifier
                        .size(
                            width = when(reel.aspectRatio) {
                                "1:1" -> 160.dp
                                "9:16" -> 120.dp
                                "16:9" -> 200.dp
                                "4:5" -> 140.dp
                                else -> 160.dp
                            },
                            height = when(reel.aspectRatio) {
                                "1:1" -> 160.dp
                                "9:16" -> 210.dp
                                "16:9" -> 112.dp
                                "4:5" -> 175.dp
                                else -> 160.dp
                            }
                        )
                        .graphicsLayer(
                            scaleX = reel.zoomLevel,
                            scaleY = reel.zoomLevel,
                            rotationZ = reel.rotationAngle
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .border(3.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                        .background(Color.DarkGray)
                ) {
                    Icon(
                        imageVector = if (reel.mediaType == "Photo") Icons.Default.Photo else Icons.Default.MovieFilter,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(56.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (reel.mediaType == "Photo") "[ 📷 Portrait Artisanat ]" else "[ 🎥 Séquence Vidéo Nora ]",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Format: ${reel.aspectRatio}",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (reel.zoomLevel > 1f || reel.rotationAngle > 0f) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Zoom ${reel.zoomLevel}x" + (if (reel.rotationAngle > 0) " • ${reel.rotationAngle.toInt()}°" else ""),
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (reel.mediaType == "Photo") "Image fixe rognée et optimisée" else "Séquence active (+10 Vues toutes les 4s)",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
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
                Box(
                    modifier = Modifier
                        .size(36.dp)
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

                Text(
                    text = "@${reel.creatorName}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

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

            Text(
                text = reel.caption,
                color = Color.White,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Views tag indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RemoveRedEye,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "${reel.viewsCount} vues • Catégorie: ${reel.category}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }
        }

        // Overlay Right-Side Action Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Like button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onLike,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (reel.isLiked) Color.Red else Color.White
                    )
                }
                Text(
                    text = "${reel.likesCount}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comments button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentsClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .testTag("comments_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Commentaires",
                        tint = Color.White
                    )
                }
                Text(
                    text = "${reel.comments.size}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShare,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Partager",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }

            // Report button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onReport,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Report,
                        contentDescription = "Report",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Signaler",
                    color = Color.White,
                    fontSize = 10.sp
                )
            }
        }
    }
}
