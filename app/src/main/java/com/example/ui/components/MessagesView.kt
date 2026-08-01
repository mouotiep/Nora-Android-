package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.Conversation
import com.example.NoraViewModel
import com.example.R
import com.example.domain.model.MessageStatus
import com.example.media.VoiceRecorder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ReplyTarget(
    val senderName: String,
    val messageText: String
)

private fun playBeep(type: Int = ToneGenerator.TONE_PROP_BEEP, durationMs: Int = 100) {
    try {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        toneGen.startTone(type, durationMs)
        Handler(Looper.getMainLooper()).postDelayed({
            try { toneGen.release() } catch (_: Exception) {}
        }, durationMs.toLong() + 50)
    } catch (_: Exception) {}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MessagesView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val rawConversations by viewModel.conversations.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableIntStateOf(0) }
    var replyingToMessage by remember { mutableStateOf<ReplyTarget?>(null) }

    // BUG 1 fix: Periodic ticker to trigger recomposition every 60s for time formatting update
    var ticker by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60000)
            ticker++
        }
    }

    // BUG 6 fix: Real Voice Recorder setup with permission handler
    val voiceRecorder = remember { VoiceRecorder(context) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecordingVoice = true
        } else {
            Toast.makeText(context, "Permission microphone requise pour enregistrer", Toast.LENGTH_SHORT).show()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceRecorder.cancel()
        }
    }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDurationSec = 0
            val started = voiceRecorder.start()
            if (!started) {
                isRecordingVoice = false
                Toast.makeText(context, "Impossible de démarrer l'enregistrement", Toast.LENGTH_SHORT).show()
            } else {
                playBeep(ToneGenerator.TONE_PROP_BEEP, 100)
                while (isRecordingVoice) {
                    delay(1000)
                    recordingDurationSec++
                    if (recordingDurationSec >= 120) { // Limit to 2 min
                        break
                    }
                }
            }
        }
    }

    // Dynamically filter and rename conversations depending on user role & ticker
    val displayConversations = remember(rawConversations, activeRole, userProfile, ticker) {
        if (activeRole == "Admin") {
            rawConversations.map { conv ->
                if (conv.id == "conv-3") {
                    conv.copy(contactName = "${userProfile.name} (Support)")
                } else {
                    conv
                }
            }
        } else {
            val filtered = rawConversations.filter { conv ->
                conv.id == "conv-3" || conv.contactName.equals(userProfile.name, ignoreCase = true)
            }
            if (filtered.isEmpty()) {
                rawConversations.filter { it.id == "conv-3" }.map { conv ->
                    conv.copy(contactName = "Administrateur NorA")
                }
            } else {
                filtered.map { conv ->
                    if (conv.id == "conv-3") {
                        conv.copy(contactName = "Administrateur NorA")
                    } else {
                        conv.copy(contactName = "Support NorA Cameroun")
                    }
                }
            }
        }
    }

    val activeChatSession = remember(displayConversations, activeChatId, rawConversations) {
        displayConversations.find { it.id == activeChatId }
            ?: if (activeChatId == "conv-3") rawConversations.find { it.id == "conv-3" }?.copy(contactName = "Administrateur NorA") else null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        if (activeChatSession == null) {
            // Conversations List Pane
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Text(
                    text = if (activeRole == "Admin") "Administration - Messages Support" else "Vos Échanges avec l'Administrateur",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF1F2937)
                )

                if (activeRole == "Admin") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Interface Administrateur : Vous communiquez directement avec les utilisateurs sous l'identité de 'NorA' pour préserver l'anonymat et garantir la sécurité des échanges.",
                                fontSize = 10.sp,
                                color = Color(0xFF1E40AF),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                if (displayConversations.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Aucune discussion en cours", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // BUG 2 fix: Stable key for LazyColumn conversation items
                        items(displayConversations, key = { it.id }) { conv ->
                            val isSupportChannel = conv.id == "conv-3"
                            ConversationRowItem(
                                conversation = conv,
                                isAdmin = isSupportChannel,
                                onClick = { viewModel.setActiveChatId(conv.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // Chat Detail Pane (WhatsApp Style)
            val currentChat = activeChatSession
            val listState = rememberLazyListState()

            // BUG 3 fix: Smart auto-scroll logic
            val isNearBottom by remember {
                derivedStateOf {
                    val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    val totalItems = listState.layoutInfo.totalItemsCount
                    totalItems == 0 || lastVisible >= totalItems - 2
                }
            }

            LaunchedEffect(currentChat.messages.size) {
                if (currentChat.messages.isNotEmpty() && isNearBottom) {
                    listState.animateScrollToItem(currentChat.messages.size - 1)
                }
            }

            val sendMessageAndScroll: (String, String, String) -> Unit = { textToSend, rText, rSender ->
                viewModel.sendMessage(
                    currentChat.id,
                    textToSend,
                    replyToText = rText,
                    replyToSender = rSender
                )
                chatTextInput = ""
                replyingToMessage = null
                coroutineScope.launch {
                    if (currentChat.messages.isNotEmpty()) {
                        listState.animateScrollToItem((currentChat.messages.size).coerceAtLeast(0))
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // FIXED WHATSAPP HEADER BAR
                Surface(
                    color = Color(0xFF007A5E),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setActiveChatId(null) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Retour",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Avatar Frame
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(if (currentChat.id == "conv-3" || activeRole != "Admin") RoundedCornerShape(8.dp) else CircleShape)
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), if (currentChat.id == "conv-3" || activeRole != "Admin") RoundedCornerShape(8.dp) else CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (currentChat.id == "conv-3" || activeRole != "Admin") {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_nora_logo),
                                    contentDescription = "Logo NorA",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = currentChat.contactName.take(2).uppercase(),
                                    color = Color(0xFF007A5E),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Title & Subtitle
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (activeRole == "Admin") currentChat.contactName else "Administrateur NorA",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF25D366))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (activeRole == "Admin") "CLIENT" else "SUPPORT OFFICIEL",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Text(
                                text = if (activeRole == "Admin") "🟢 Client NorA • Chat Actif" else "🟢 Support NorA • En ligne 24h/7j",
                                fontSize = 11.sp,
                                color = Color(0xFFD1FAE5)
                            )
                        }

                        // Prominent WhatsApp Direct Action Button
                        Button(
                            onClick = {
                                val phoneClean = currentChat.userPhone.replace(" ", "").replace("+", "")
                                val targetPhone = if (phoneClean.isNotBlank()) phoneClean else "237655924778"
                                val waUrl = "https://api.whatsapp.com/send?phone=$targetPhone"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "WhatsApp Support: +237 655 92 47 78", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "WhatsApp",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "WhatsApp",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                if (activeRole == "Admin") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                        shape = RoundedCornerShape(0.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "👤 Client : ${currentChat.contactName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                                Text(
                                    text = "📱 WhatsApp : ${currentChat.userPhone.ifBlank { "+237 6xx xxx xxx" }}  |  ✉️ ${currentChat.userEmail.ifBlank { "client@nora.cm" }}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF1E3A8A)
                                )
                            }
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFE5DDD5))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // BUG 2 fix: Stable key for message items
                    items(currentChat.messages, key = { it.id }) { message ->
                        val isMe = if (activeRole == "Admin") {
                            message.sender == "admin"
                        } else {
                            message.sender == "moi"
                        }

                        val senderName = if (activeRole == "Admin") {
                            if (message.sender == "admin") "Vous (Admin)" else currentChat.contactName
                        } else {
                            if (message.sender == "moi") "Vous" else "Administrateur NorA"
                        }

                        val displayMsgText = if (message.text.startsWith("[VoiceNote:")) {
                            "🎵 Note vocale"
                        } else {
                            message.text
                        }

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                            ) {
                                if (isMe) {
                                    IconButton(
                                        onClick = {
                                            replyingToMessage = ReplyTarget(senderName = senderName, messageText = displayMsgText)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Reply,
                                            contentDescription = "Répondre",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    // BUG 4 fix: Remove .clickable from bubble Box. Long click reserved for reply.
                                    Box(
                                        modifier = Modifier
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 12.dp,
                                                    topEnd = 12.dp,
                                                    bottomStart = if (isMe) 12.dp else 2.dp,
                                                    bottomEnd = if (isMe) 2.dp else 12.dp
                                                )
                                            )
                                            .background(
                                                if (isMe) Color(0xFFE7FFDB) else Color.White
                                            )
                                            .border(
                                                width = 0.5.dp,
                                                color = Color(0xFFCBD5E1),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .combinedClickable(
                                                onLongClick = {
                                                    replyingToMessage = ReplyTarget(senderName = senderName, messageText = displayMsgText)
                                                },
                                                onClick = {}
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Column {
                                            // Display Reply Quote if present
                                            if (message.replyToText.isNotBlank()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 6.dp)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Color(0xFF007A5E).copy(alpha = 0.08f))
                                                        .border(1.dp, Color(0xFF007A5E).copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .width(3.dp)
                                                                .height(26.dp)
                                                                .background(Color(0xFF007A5E))
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(
                                                                text = message.replyToSender.ifBlank { "Citation" },
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF007A5E)
                                                            )
                                                            Text(
                                                                text = message.replyToText,
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF374151),
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            val isVoiceNote = message.text.startsWith("[VoiceNote:")
                                            if (isVoiceNote) {
                                                VoiceNotePlayer(
                                                    messageText = message.text,
                                                    isMe = isMe,
                                                    senderName = senderName
                                                )
                                            } else {
                                                Text(
                                                    text = message.text,
                                                    fontSize = 13.5.sp,
                                                    color = Color(0xFF111827),
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp, start = 2.dp, end = 2.dp)
                                    ) {
                                        Text(
                                            text = "$senderName • ${message.time}",
                                            fontSize = 9.sp,
                                            color = Color(0xFF6B7280)
                                        )
                                        // BUG 8 fix: Visual sending status (SENDING, SENT, FAILED with retry)
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            when (message.status) {
                                                MessageStatus.SENDING -> {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(10.dp),
                                                        strokeWidth = 1.5.dp,
                                                        color = Color(0xFF9CA3AF)
                                                    )
                                                }
                                                MessageStatus.SENT -> {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Distribué",
                                                        tint = Color(0xFF10B981),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                MessageStatus.FAILED -> {
                                                    Icon(
                                                        imageVector = Icons.Default.ErrorOutline,
                                                        contentDescription = "Échec d'envoi. Appuyez pour réessayer.",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier
                                                            .size(14.dp)
                                                            .clickable {
                                                                viewModel.retryMessage(currentChat.id, message.id)
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (!isMe) {
                                    IconButton(
                                        onClick = {
                                            replyingToMessage = ReplyTarget(senderName = senderName, messageText = displayMsgText)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Reply,
                                            contentDescription = "Répondre",
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Replying Preview Banner
                if (replyingToMessage != null) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(30.dp)
                                    .background(Color(0xFF007A5E), RoundedCornerShape(2.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "En réponse à ${replyingToMessage!!.senderName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF007A5E)
                                )
                                Text(
                                    text = replyingToMessage!!.messageText,
                                    fontSize = 11.sp,
                                    color = Color(0xFF374151),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { replyingToMessage = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Annuler la réponse",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // TextInput Bottom bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF0F2F5))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRecordingVoice) {
                        // Live Recording UI
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(24.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var flashState by remember { mutableStateOf(true) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    delay(500)
                                    flashState = !flashState
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (flashState) Color.Red else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enregistrement vocal... ${recordingDurationSec / 60}:${(recordingDurationSec % 60).toString().padStart(2, '0')}",
                                color = Color(0xFF991B1B),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            // Cancel Button
                            IconButton(
                                onClick = {
                                    isRecordingVoice = false
                                    voiceRecorder.cancel()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Annuler",
                                    tint = Color(0xFF991B1B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Send Voice Note Button
                        IconButton(
                            onClick = {
                                playBeep(ToneGenerator.TONE_PROP_PROMPT, 100)
                                isRecordingVoice = false
                                val audioFile = voiceRecorder.stop()
                                val durationSec = recordingDurationSec.coerceAtLeast(1)
                                if (audioFile != null && audioFile.exists()) {
                                    coroutineScope.launch {
                                        val uploadResult = com.example.data.firebase.FirebaseManager.uploadFileToStorage(
                                            context,
                                            Uri.fromFile(audioFile),
                                            folder = "voice_notes"
                                        )
                                        val finalUrl = uploadResult.getOrNull()
                                        if (!finalUrl.isNullOrBlank()) {
                                            sendMessageAndScroll(
                                                "[VoiceNote:$durationSec|$finalUrl]",
                                                replyingToMessage?.messageText ?: "",
                                                replyingToMessage?.senderName ?: ""
                                            )
                                        } else {
                                            Toast.makeText(context, "Échec d'envoi de la note vocale (serveur)", Toast.LENGTH_SHORT).show()
                                        }
                                        audioFile.delete()
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007A5E))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Envoyer la note vocale",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        // BUG 7 fix: Multi-line text field (1 to 5 lines, max 2000 chars)
                        OutlinedTextField(
                            value = chatTextInput,
                            onValueChange = {
                                if (it.length <= 2000) chatTextInput = it
                            },
                            placeholder = {
                                Text(
                                    text = if (activeRole == "Admin") "Répondre au client..." else "Écrire à l'Administrateur NorA...",
                                    fontSize = 13.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input_text"),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF007A5E),
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            minLines = 1,
                            maxLines = 5,
                            singleLine = false
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (chatTextInput.trim().isEmpty()) {
                            // Microphone button to start voice recording with permission check
                            IconButton(
                                onClick = {
                                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                        isRecordingVoice = true
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF007A5E))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Enregistrer une note vocale",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        } else {
                            // Send text button
                            IconButton(
                                onClick = {
                                    if (chatTextInput.trim().isNotEmpty()) {
                                        sendMessageAndScroll(
                                            chatTextInput.trim(),
                                            replyingToMessage?.messageText ?: "",
                                            replyingToMessage?.senderName ?: ""
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF007A5E))
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Envoyer",
                                    tint = Color.White,
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

@Composable
fun ConversationRowItem(
    conversation: Conversation,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(if (isAdmin) RoundedCornerShape(8.dp) else CircleShape)
                    .background(if (isAdmin) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                if (isAdmin) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_nora_logo),
                        contentDescription = "NorA Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.contactName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF111827)
                        )
                        if (isAdmin) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD1FAE5))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("Officiel", color = Color(0xFF047857), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = conversation.lastTime,
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayLastMessage = if (conversation.lastMessage.startsWith("[VoiceNote:")) {
                        val duration = conversation.lastMessage.substringAfter("[VoiceNote:").substringBefore("|").substringBefore("]")
                        "🎵 Note vocale (${duration}s)"
                    } else {
                        conversation.lastMessage
                    }
                    Text(
                        text = displayLastMessage,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// BUG 6 fix: ExoPlayer-based VoiceNotePlayer
@Composable
fun VoiceNotePlayer(
    messageText: String,
    isMe: Boolean,
    senderName: String
) {
    val context = LocalContext.current
    val rawText = messageText.removePrefix("[VoiceNote:").removeSuffix("]")
    val parts = rawText.split("|", limit = 2)
    val durationStr = parts.getOrNull(0) ?: "5"
    val audioUrl = parts.getOrNull(1) ?: ""

    val durationSec = remember(durationStr) { durationStr.toIntOrNull()?.coerceAtLeast(1) ?: 5 }

    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var currentPosSec by remember { mutableIntStateOf(0) }

    val exoPlayer = remember(audioUrl) {
        if (audioUrl.isNotBlank() && (audioUrl.startsWith("http://") || audioUrl.startsWith("https://") || audioUrl.startsWith("content://") || audioUrl.startsWith("file://"))) {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(Uri.parse(audioUrl))
                setMediaItem(mediaItem)
                prepare()
            }
        } else {
            null
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.stop()
            exoPlayer?.release()
        }
    }

    LaunchedEffect(isPlaying, exoPlayer) {
        if (isPlaying && exoPlayer != null) {
            exoPlayer.play()
            while (isPlaying && exoPlayer.isPlaying) {
                delay(100)
                val current = exoPlayer.currentPosition
                val total = exoPlayer.duration.coerceAtLeast(1L)
                progress = (current.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                currentPosSec = (current / 1000).toInt()
            }
            if (exoPlayer.playbackState == Player.STATE_ENDED) {
                isPlaying = false
                progress = 0f
                currentPosSec = 0
                exoPlayer.seekTo(0)
            }
        } else {
            exoPlayer?.pause()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        IconButton(
            onClick = {
                if (exoPlayer != null) {
                    if (isPlaying) {
                        exoPlayer.pause()
                        isPlaying = false
                    } else {
                        if (exoPlayer.playbackState == Player.STATE_ENDED) {
                            exoPlayer.seekTo(0)
                        }
                        isPlaying = true
                    }
                } else {
                    Toast.makeText(context, "Audio non disponible", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF007A5E).copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lire la note vocale",
                tint = Color(0xFF007A5E),
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.width(160.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                val waveHeights = listOf(8, 14, 18, 10, 16, 20, 12, 8, 16, 12, 18, 10, 14, 8, 16, 10)
                waveHeights.forEachIndexed { index, height ->
                    val isPast = (index.toFloat() / waveHeights.size) < progress
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(height.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (isPast) Color(0xFF007A5E) else Color.LightGray
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPlaying) {
                        "0:${currentPosSec.toString().padStart(2, '0')} / 0:${durationSec.toString().padStart(2, '0')}"
                    } else {
                        "🎵 0:${durationSec.toString().padStart(2, '0')}"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151)
                )

                if (isPlaying) {
                    Text(
                        text = "▶ Audio...",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007A5E)
                    )
                }
            }
        }
    }
}
