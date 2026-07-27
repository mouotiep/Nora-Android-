package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Universal Video Player for playing both local device videos (content:// or file://)
 * and web streaming videos using native Android VideoView.
 */
@Composable
fun UniversalVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    showControls: Boolean = true
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isPrepared by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }

    if (videoUrl.isNotBlank() && !hasError) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        if (showControls) {
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                        }
                        try {
                            setVideoURI(Uri.parse(videoUrl))
                        } catch (e: Exception) {
                            hasError = true
                        }
                        setOnPreparedListener { mp ->
                            try {
                                mp.isLooping = true
                                mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                            } catch (e: Exception) {
                                // Fallback for older player drivers
                            }
                            isPrepared = true
                            if (autoPlay) {
                                start()
                                isPlaying = true
                            }
                        }
                        setOnErrorListener { _, _, _ ->
                            hasError = true
                            true
                        }
                    }
                },
                update = { view ->
                    try {
                        val currentUri = Uri.parse(videoUrl)
                        // If URI changed or needs playback toggle
                        if (isPlaying && !view.isPlaying && isPrepared) {
                            view.start()
                        } else if (!isPlaying && view.isPlaying) {
                            view.pause()
                        }
                    } catch (e: Exception) {
                        hasError = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (!isPrepared && !hasError) {
                CircularProgressIndicator(
                    color = Color(0xFF10B981),
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    } else {
        // Fallback view when no video URI is present or playback fails
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF111827)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MovieFilter,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (hasError) "Aperçu de la séquence vidéo" else "Aucune vidéo chargée",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Renders either a Video or an Image based on mediaType or URI extension.
 */
@Composable
fun UniversalMediaView(
    mediaUrl: String,
    mediaType: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    autoPlayVideo: Boolean = true
) {
    val isVideo = mediaType.contains("Vidéo", ignoreCase = true) ||
            mediaType.contains("Video", ignoreCase = true) ||
            mediaUrl.endsWith(".mp4", ignoreCase = true) ||
            mediaUrl.contains("video", ignoreCase = true)

    if (isVideo && mediaUrl.isNotBlank()) {
        UniversalVideoPlayer(
            videoUrl = mediaUrl,
            modifier = modifier,
            autoPlay = autoPlayVideo
        )
    } else if (mediaUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Média",
            contentScale = contentScale,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Image,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

/**
 * Reusable Media Picker launcher component that requests gallery selection from the device.
 */
@Composable
fun rememberGalleryPicker(
    mediaTypeFilter: String = "*/*", // "image/*", "video/*", or "*/*"
    onMediaSelected: (Uri) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    if (uri != null) {
        onMediaSelected(uri)
    }
}

/**
 * Helper to take persistable read permission for selected content URIs.
 */
fun grantUriReadPermission(context: Context, uri: Uri) {
    try {
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
    } catch (e: Exception) {
        // Ignored if persistent grant is not supported by provider
    }
}
