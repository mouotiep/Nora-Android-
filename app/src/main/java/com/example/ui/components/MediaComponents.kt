package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Universal Video Player for playing both local device videos (content:// or file://)
 * and web streaming videos using ExoPlayer with auto-play, progressive buffering,
 * and responsive aspect-ratio scaling (RESIZE_MODE_ZOOM / RESIZE_MODE_FIT).
 */
@OptIn(UnstableApi::class)
@Composable
fun UniversalVideoPlayer(
    videoUrl: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    showControls: Boolean = false,
    startSec: Float = 0f,
    endSec: Float = 0f,
    zoomLevel: Float = 1f,
    rotationAngle: Float = 0f,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }
    var isPrepared by remember { mutableStateOf(false) }

    if (videoUrl.isBlank() || hasError) {
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
        return
    }

    val exoPlayer = remember(videoUrl) {
        ExoPlayer.Builder(context).build().apply {
            try {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
                repeatMode = if (endSec <= 0f) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
                prepare()
                if (startSec > 0f) seekTo((startSec * 1000).toLong())
                playWhenReady = autoPlay
            } catch (e: Exception) {
                hasError = true
            }
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                hasError = true
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    isPrepared = true
                }
                if (state == Player.STATE_ENDED && (startSec > 0f || endSec > 0f)) {
                    exoPlayer.seekTo((startSec * 1000).toLong())
                    if (autoPlay) exoPlayer.play()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(autoPlay) {
        exoPlayer.playWhenReady = autoPlay
        if (autoPlay) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = showControls
                    this.resizeMode = resizeMode
                }
            },
            update = { playerView ->
                playerView.resizeMode = resizeMode
            },
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomLevel,
                    scaleY = zoomLevel,
                    rotationZ = rotationAngle
                )
        )

        if (!isPrepared && !hasError) {
            CircularProgressIndicator(
                color = Color(0xFF10B981),
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

/**
 * Renders either a Video or an Image based on mediaType or URI extension.
 */
@OptIn(UnstableApi::class)
@Composable
fun UniversalMediaView(
    mediaUrl: String,
    mediaType: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    autoPlayVideo: Boolean = true,
    startSec: Float = 0f,
    endSec: Float = 0f,
    zoomLevel: Float = 1f,
    rotationAngle: Float = 0f,
    resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
) {
    val isVideo = mediaType.contains("Vidéo", ignoreCase = true) ||
            mediaType.contains("Video", ignoreCase = true) ||
            mediaUrl.endsWith(".mp4", ignoreCase = true) ||
            mediaUrl.contains("video", ignoreCase = true)

    if (isVideo && mediaUrl.isNotBlank()) {
        UniversalVideoPlayer(
            videoUrl = mediaUrl,
            modifier = modifier,
            autoPlay = autoPlayVideo,
            startSec = startSec,
            endSec = endSec,
            zoomLevel = zoomLevel,
            rotationAngle = rotationAngle,
            resizeMode = resizeMode
        )
    } else if (mediaUrl.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Média",
            contentScale = contentScale,
            modifier = modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = zoomLevel,
                    scaleY = zoomLevel,
                    rotationZ = rotationAngle
                )
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
