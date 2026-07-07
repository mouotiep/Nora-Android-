package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Conversation
import com.example.NoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val rawConversations by viewModel.conversations.collectAsState()
    val activeRole by viewModel.activeRole.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingDurationSec by remember { mutableStateOf(0) }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingDurationSec = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingDurationSec++
            }
        }
    }

    // Dynamically filter and rename conversations depending on user role
    val displayConversations = remember(rawConversations, activeRole, userProfile) {
        if (activeRole == "Admin") {
            rawConversations.map { conv ->
                if (conv.id == "conv-3") {
                    conv.copy(contactName = "${userProfile.name} (Support)")
                } else {
                    conv
                }
            }
        } else {
            // Non-admin users ONLY see the NorA official conversation, renamed to "NorA"
            rawConversations.filter { it.id == "conv-3" }.map { conv ->
                conv.copy(contactName = "NorA")
            }
        }
    }

    val activeChatSession = remember(displayConversations, activeChatId) {
        displayConversations.find { it.id == activeChatId }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        if (activeChatSession == null) {
            // Conversations List Pane
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (activeRole == "Admin") "Administration - Messages Support" else "Vos Échanges avec NorA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp),
                    color = Color(0xFF1F2937)
                )

                // Warning Banner (Adaptive for Admin or regular user)
                if (activeRole == "Admin") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEFF6FF)
                        ),
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
                        items(displayConversations) { conv ->
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
            // Chat Detail Pane
            val currentChat = activeChatSession!!
            val isCurrentChatAdmin = currentChat.id == "conv-3" || activeRole == "Admin"

            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.setActiveChatId(null) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.Black)
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isCurrentChatAdmin) Color(0xFF10B981) else Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentChat.contactName.take(2).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(currentChat.contactName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (isCurrentChatAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFD1FAE5))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (activeRole == "Admin") "Utilisateur" else "Officiel",
                                        color = Color(0xFF065F46),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (activeRole == "Admin") "Session Support Active" else "En ligne - Support Clientèle Nora",
                            fontSize = 10.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(currentChat.messages) { message ->
                        // Dual perspective message rendering
                        val isMe = if (activeRole == "Admin") {
                            message.sender == "admin"
                        } else {
                            message.sender == "moi"
                        }

                        val senderName = if (activeRole == "Admin") {
                            if (message.sender == "admin") "Vous (Admin)" else currentChat.contactName
                        } else {
                            if (message.sender == "moi") "Vous" else "NorA"
                        }

                        val isOtherAdminReply = !isMe && (message.sender == "admin" || message.sender == "contact" && currentChat.id == "conv-3")

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
                        ) {
                            Column(
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isMe) 12.dp else 0.dp,
                                                bottomEnd = if (isMe) 0.dp else 12.dp
                                            )
                                        )
                                        .background(
                                            if (message.sender == "admin") {
                                                Color(0xFF2563EB) // Blue for admin
                                            } else {
                                                Color.White // White for client/vendeur
                                            }
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (message.sender != "admin") Color(0xFFE2E8F0) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    val isVoiceNote = message.text.startsWith("[VoiceNote:")
                                    if (isVoiceNote) {
                                        val durationSec = message.text.substringAfter("[VoiceNote:").substringBefore("]")
                                        VoiceNotePlayer(durationStr = durationSec, isMe = isMe)
                                    } else {
                                        Text(
                                            text = message.text,
                                            fontSize = 13.sp,
                                            color = if (message.sender == "admin") Color.White else Color(0xFF1F2937),
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = "$senderName • ${message.time}",
                                        fontSize = 9.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                // TextInput Bottom bar
                if (isCurrentChatAdmin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isRecordingVoice) {
                            // Recording UI
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFEF2F2))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Flashing Red Dot simulation
                                var flashState by remember { mutableStateOf(true) }
                                LaunchedEffect(Unit) {
                                    while (true) {
                                        delay(500)
                                        flashState = !flashState
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (flashState) Color.Red else Color.Transparent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Enregistrement... 0:${recordingDurationSec.toString().padStart(2, '0')}",
                                    color = Color(0xFF991B1B),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Cancel Button
                                IconButton(
                                    onClick = { isRecordingVoice = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Annuler",
                                        tint = Color(0xFF991B1B)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))
                            
                            // Send Voice Note Button
                            IconButton(
                                onClick = {
                                    viewModel.sendMessage(
                                        currentChat.id,
                                        "[VoiceNote:${recordingDurationSec.coerceAtLeast(1)}]"
                                    )
                                    isRecordingVoice = false
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Envoyer la note vocale",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            // Standard text field UI
                            OutlinedTextField(
                                value = chatTextInput,
                                onValueChange = { chatTextInput = it },
                                placeholder = {
                                    Text(
                                        text = if (activeRole == "Admin") "Répondre en tant que NorA..." else "Écrire à l'assistance NorA...",
                                        fontSize = 13.sp
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_text"),
                                shape = RoundedCornerShape(20.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            if (chatTextInput.trim().isEmpty()) {
                                // Microphone icon to start recording
                                IconButton(
                                    onClick = { isRecordingVoice = true },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Note",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                // Send text button
                                IconButton(
                                    onClick = {
                                        if (chatTextInput.trim().isNotEmpty()) {
                                            viewModel.sendMessage(currentChat.id, chatTextInput)
                                            chatTextInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                        .testTag("chat_send_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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
                    .clip(CircleShape)
                    .background(if (isAdmin) Color(0xFFD1FAE5) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAdmin) Icons.Default.SupportAgent else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isAdmin) Color(0xFF047857) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
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
                                Text("Sécurisé", color = Color(0xFF047857), fontSize = 8.sp, fontWeight = FontWeight.Bold)
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
                        val duration = conversation.lastMessage.substringAfter("[VoiceNote:").substringBefore("]")
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
                    if (!isAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("Lecture seule", color = Color(0xFF991B1B), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceNotePlayer(
    durationStr: String,
    isMe: Boolean
) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val durationSec = durationStr.toIntOrNull() ?: 5
            val steps = durationSec * 10
            for (i in 1..steps) {
                if (!isPlaying) break
                delay(100)
                progress = i.toFloat() / steps
            }
            isPlaying = false
            progress = 0f
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isMe) Color.White.copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Lire",
                tint = if (isMe) Color.White else Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.width(150.dp)) {
            // Audio wave simulation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.fillMaxWidth().height(24.dp)
            ) {
                val waveHeights = listOf(8, 12, 16, 10, 14, 18, 12, 8, 14, 10, 16, 12, 18, 10, 14, 8)
                waveHeights.forEachIndexed { index, height ->
                    val isPast = (index.toFloat() / waveHeights.size) < progress
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(height.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(
                                if (isPast) {
                                    if (isMe) Color.White else Color(0xFF10B981)
                                } else {
                                    if (isMe) Color.White.copy(alpha = 0.4f) else Color.LightGray
                                }
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isPlaying) {
                    val durationSec = durationStr.toIntOrNull() ?: 5
                    val currentSec = (progress * durationSec).toInt()
                    "0:${currentSec.toString().padStart(2, '0')} / 0:${durationStr.padStart(2, '0')}"
                } else {
                    "0:${durationStr.padStart(2, '0')}"
                },
                fontSize = 10.sp,
                color = if (isMe) Color.White.copy(alpha = 0.8f) else Color.Gray
            )
        }
    }
}
