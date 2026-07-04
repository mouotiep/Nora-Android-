package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
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
import java.util.Locale

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoraMainScreen(viewModel: NoraViewModel = viewModel()) {
    val context = LocalContext.current
    
    // Viewmodel States
    val activeRole by viewModel.activeRole.collectAsState()
    val currentTabIndex by viewModel.currentTabIndex.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    // If user is not logged in / has not completed interest selection onboarding
    if (!userProfile.isLoggedIn) {
        OnboardingScreen(viewModel = viewModel)
    } else {
        // Main Application Shell
        Scaffold(
            topBar = {
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

                        // Right: Interactive Admin/Creator/Buyer status capsule pill & notification bell
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var showRoleDropdown by remember { mutableStateOf(false) }
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
                                        .clickable { showRoleDropdown = true }
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
                                                "Créateur" -> Icons.Default.Videocam
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
                                            "Créateur" -> "CRÉATEUR"
                                            else -> "ACHETEUR"
                                        },
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                DropdownMenu(
                                    expanded = showRoleDropdown,
                                    onDismissRequest = { showRoleDropdown = false }
                                ) {
                                    listOf("Acheteur", "Créateur", "Admin").forEach { role ->
                                        val isCurrent = activeRole == role
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = role,
                                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isCurrent) Color(0xFF007A5E) else Color(0xFF1F2937)
                                                )
                                            },
                                            onClick = {
                                                viewModel.setActiveRole(role)
                                                showRoleDropdown = false
                                                Toast.makeText(context, "Profil simulé : $role", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }

                            // Notification Bell Icon with active badge indicator
                            Box {
                                IconButton(
                                    onClick = {
                                        Toast.makeText(context, "Pas de nouvelles notifications", Toast.LENGTH_SHORT).show()
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
                            }
                        }
                    }
                }
            },
            bottomBar = {
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
                                    "Créateur" -> if (currentTabIndex == 3) Icons.Filled.BarChart else Icons.Outlined.BarChart
                                    else -> if (currentTabIndex == 3) Icons.Filled.AccountBox else Icons.Outlined.AccountBox
                                },
                                contentDescription = "Tableau de Bord"
                            )
                        },
                        label = {
                            Text(
                                text = when (activeRole) {
                                    "Admin" -> "Tableau Admin"
                                    "Créateur" -> "Tab. Créateur"
                                    else -> "Mon Tableau"
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
                            "Créateur" -> CreatorDashboardView(viewModel = viewModel)
                            else -> ProfileView(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
