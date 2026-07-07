package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import java.util.Locale
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NoraMainScreen()
            }
        }
    }
}

// Helper Extension for FCFA Money Formatting
fun Int.toLocaleString(): String {
    return String.format(Locale.FRANCE, "%,d", this)
}

@Composable
fun NoraSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF007A5E)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = R.drawable.img_app_icon_1783163003118,
                    contentDescription = "Logo Nora Cameroun",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = android.R.drawable.ic_menu_gallery)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NORA CAMEROUN",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF007A5E),
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Achetez. Vendez. Gagnez.",
                fontSize = 14.sp,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = Color(0xFF10B981),
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoraMainScreen(viewModel: NoraViewModel = viewModel()) {
    val context = LocalContext.current
    
    // Viewmodel States
    val activeRole by viewModel.activeRole.collectAsState()
    val currentTabIndex by viewModel.currentTabIndex.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val currentNotification by viewModel.currentNotification.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val hasUnreadNotifications by viewModel.hasUnreadNotifications.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()

    var showSplash by remember { mutableStateOf(true) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentNotification) {
        if (currentNotification != null) {
            delay(4000)
            viewModel.clearNotification()
        }
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val postNotificationGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        val readStorageGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false
        val readImagesGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: false
        } else {
            false
        }
        
        if (postNotificationGranted) {
            Toast.makeText(context, "Notifications Nora Cameroun activées !", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        delay(3000) // 3 seconds splash screen
        showSplash = false
    }

    LaunchedEffect(showSplash) {
        if (!showSplash) {
            val permissionsToRequest = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    if (showSplash) {
        NoraSplashScreen()
    } else if (!userProfile.isLoggedIn) {
        OnboardingScreen(viewModel = viewModel)
    } else {
        // Main Application Shell
        Box(modifier = Modifier.fillMaxSize()) {            Scaffold(
                topBar = {
                    if (currentTabIndex != 2 || activeChatId == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF007A5E))
                                .statusBarsPadding()
                        ) {
                            // Gorgeous NORA CAMEROUN Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left: White rounded logo box & dynamic app texts
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = "Logo",
                                            tint = Color(0xFF007A5E),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "NORA",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = "CAMEROUN",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "Achetez • Vendez • Gagnez",
                                            fontSize = 8.sp,
                                            color = Color(0xFFA7F3D0), // Soft emerald tint
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                // Right: Admin/Buyer status capsule pill & notification bell
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(100.dp))
                                                .background(Color(0x33FFFFFF)) // 20% white overlay
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0xFF34D399).copy(alpha = 0.5f),
                                                    shape = RoundedCornerShape(100.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // Circular icon placeholder inside capsule
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (activeRole) {
                                                        "Admin" -> Icons.Default.SupportAgent
                                                        else -> Icons.Default.Person
                                                    },
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                            Text(
                                                text = when (activeRole) {
                                                    "Admin" -> "ADMINISTRATEUR"
                                                    else -> "ACHETEUR"
                                                },
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                    }

                                    // Notification Bell Icon with active badge indicator
                                    Box {
                                        IconButton(
                                            onClick = {
                                                showNotificationDialog = true
                                                viewModel.markNotificationsAsRead()
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        if (hasUnreadNotifications && notifications.isNotEmpty()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Red)
                                                    .align(Alignment.TopEnd)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
            bottomBar = {
                if (currentTabIndex != 2 || activeChatId == null) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                    // Tab 0: Vidéos (Reels)
                    NavigationBarItem(
                        selected = currentTabIndex == 0,
                        onClick = { viewModel.setCurrentTabIndex(0) },
                        icon = {
                            Icon(
                                imageVector = if (currentTabIndex == 0) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                contentDescription = "Vidéos"
                            )
                        },
                        label = { Text("Vidéos", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                    
                    // Tab 1: Boutiques (Marketplace)
                    NavigationBarItem(
                        selected = currentTabIndex == 1,
                        onClick = { viewModel.setCurrentTabIndex(1) },
                        icon = {
                            Icon(
                                imageVector = if (currentTabIndex == 1) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                                contentDescription = "Boutiques"
                            )
                        },
                        label = { Text("Boutiques", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                    
                    // Tab 2: Messages (Conversations)
                    NavigationBarItem(
                        selected = currentTabIndex == 2,
                        onClick = { viewModel.setCurrentTabIndex(2) },
                        icon = {
                            Icon(
                                imageVector = if (currentTabIndex == 2) Icons.Filled.Chat else Icons.Outlined.Chat,
                                contentDescription = "Messages"
                            )
                        },
                        label = { Text("Messages", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                    
                    // Tab 3: Adaptative Role Dashboard
                    NavigationBarItem(
                        selected = currentTabIndex == 3,
                        onClick = { viewModel.setCurrentTabIndex(3) },
                        icon = {
                            Icon(
                                imageVector = when (activeRole) {
                                    "Admin" -> if (currentTabIndex == 3) Icons.Filled.Dashboard else Icons.Outlined.Dashboard
                                    else -> if (currentTabIndex == 3) Icons.Filled.AccountBox else Icons.Outlined.AccountBox
                                },
                                contentDescription = "Mon Profil"
                            )
                        },
                        label = {
                            Text(
                                text = when (activeRole) {
                                    "Admin" -> "Tableau Admin"
                                    else -> "Mon Profil"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF10B981),
                            selectedTextColor = Color(0xFF10B981),
                            indicatorColor = Color(0xFFEFF6FF)
                        )
                    )
                }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF8FAFC))
            ) {
                when (currentTabIndex) {
                    0 -> ReelsView(viewModel = viewModel)
                    1 -> MarketplaceView(viewModel = viewModel)
                    2 -> MessagesView(viewModel = viewModel)
                    3 -> {
                        when (activeRole) {
                            "Admin" -> AdminDashboardView(viewModel = viewModel)
                            else -> ProfileView(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Notification Center Modal Dialog
        if (showNotificationDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showNotificationDialog = false },
                properties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color(0xFF007A5E),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = "Notifications Nora",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            IconButton(
                                onClick = { showNotificationDialog = false },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Fermer",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        if (notifications.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsNone,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Text(
                                        text = "Aucune notification pour le moment.",
                                        color = Color.Gray,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(notifications) { notif ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF007A5E))
                                            )
                                            Text(
                                                text = notif,
                                                fontSize = 14.sp,
                                                color = Color(0xFF1E293B),
                                                lineHeight = 20.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    viewModel.clearNotificationHistory()
                                    Toast.makeText(context, "Historique supprimé", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFEE2E2),
                                    contentColor = Color(0xFFB91C1C)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer tout",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "EFFACER TOUT L'HISTORIQUE",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Notification Heads-Up Alert Overlay
        AnimatedVisibility(
            visible = currentNotification != null,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
                .zIndex(99f)
        ) {
            currentNotification?.let { text ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Nora Cameroun",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF34D399)
                            )
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.clearNotification() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fermer",
                                tint = Color.LightGray,
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
