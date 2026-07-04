package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.NoraViewModel
import com.example.ReportedItem
import com.example.UserProfile
import com.example.toLocaleString
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardView(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val kycApps by viewModel.kycApplications.collectAsState()
    val reportedItems by viewModel.reportedItems.collectAsState()
    val viewsRatio by viewModel.viewsRatio.collectAsState()
    val conversionRate by viewModel.conversionRate.collectAsState()
    val isBackingUp by viewModel.isBackingUp.collectAsState()
    val orders by viewModel.orders.collectAsState()

    val currentSubAdmin by viewModel.currentSubAdmin.collectAsState()
    val isAdmin1Assigned by viewModel.isAdmin1Assigned.collectAsState()
    val isAdmin2Assigned by viewModel.isAdmin2Assigned.collectAsState()

    var showScanSimDialog by remember { mutableStateOf(false) }
    var scanOrderIdInput by remember { mutableStateOf("") }

    val pendingOrders = remember(orders) { orders.filter { it.status == "En attente de livraison" } }
    val currentRoleOrders = remember(pendingOrders, currentSubAdmin) {
        if (currentSubAdmin == "Admin 1") {
            pendingOrders.take(10)
        } else if (currentSubAdmin == "Admin 2") {
            if (pendingOrders.size > 10) pendingOrders.drop(10) else emptyList()
        } else {
            emptyList()
        }
    }

    if (currentSubAdmin == null) {
        // Role Selection view
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Icon(
                imageVector = Icons.Default.SupervisorAccount,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(80.dp)
            )
            Text(
                text = "Portail Administrateur Nora",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Veuillez choisir votre session de sous-administrateur pour commencer à gérer les commandes et la modération.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Sub-Admin 1 Choice Card
            if (!isAdmin1Assigned) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectSubAdmin("Admin 1") }
                        .testTag("select_admin_1_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A1", fontWeight = FontWeight.Bold, color = Color(0xFF0284C7), fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sous-Administrateur 1", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                            Text("Prend en charge les 10 premières commandes du flux général. Gère l'opérationnel principal.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFCBD5E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A1", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sous-Administrateur 1", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Occupé / Effacé", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Session active en cours d'utilisation sur un autre appareil.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // Sub-Admin 2 Choice Card
            if (!isAdmin2Assigned) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectSubAdmin("Admin 2") }
                        .testTag("select_admin_2_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFDF2F8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A2", fontWeight = FontWeight.Bold, color = Color(0xFFDB2777), fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sous-Administrateur 2", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1F2937))
                            Text("Gère le débordement (commandes après la 10ème) uniquement si Admin 1 est saturé.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.Gray)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE2E8F0).copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFCBD5E1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("A2", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Sous-Administrateur 2", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFEE2E2))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Occupé / Effacé", color = Color(0xFFEF4444), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("Session active en cours d'utilisation sur un autre appareil.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Safety Reset Mechanism button
            Button(
                onClick = {
                    viewModel.resetAllSubAdmins()
                    Toast.makeText(context, "Toutes les sessions administrateurs ont été réinitialisées !", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("reset_all_sessions_button"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("REINITIALISER TOUTES LES SESSIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    } else {
        // Active Sub-Admin Dashboard View
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- active sub-admin Role Header Info ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (currentSubAdmin == "Admin 1") Color(0xFFF0F9FF) else Color(0xFFFDF2F8)),
                border = BorderStroke(1.dp, if (currentSubAdmin == "Admin 1") Color(0xFFBAE6FD) else Color(0xFFFBCFE8))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (currentSubAdmin == "Admin 1") Color(0xFFE0F2FE) else Color(0xFFFCE7F3)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentSubAdmin == "Admin 1") "A1" else "A2",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentSubAdmin == "Admin 1") Color(0xFF0369A1) else Color(0xFFC2185B),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Rôle: $currentSubAdmin",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                                Text(
                                    text = if (currentSubAdmin == "Admin 1") "Gestion de la file principale (Commandes 1-10)" else "Gestion du débordement (Commandes 11+)",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Exit role Button
                        Button(
                            onClick = {
                                viewModel.releaseSubAdmin()
                                Toast.makeText(context, "Session fermée. Rôle libéré.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(28.dp)
                                .testTag("exit_sub_admin_button")
                        ) {
                            Text("Quitter", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = if (currentSubAdmin == "Admin 1") Color(0xFFE0F2FE) else Color(0xFFFCE7F3))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Queue Capacity Indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fichier général des commandes :",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (currentSubAdmin == "Admin 1") {
                                        if (pendingOrders.size >= 10) Color(0xFFFEE2E2) else Color(0xFFD1FAE5)
                                    } else {
                                        if (pendingOrders.size > 10) Color(0xFFFEF3C7) else Color(0xFFE2E8F0)
                                    }
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (currentSubAdmin == "Admin 1") {
                                    if (pendingOrders.size >= 10) "⚠️ ADMIN 1 SATURÉ (10/10)" else "✅ DISPONIBLE (${pendingOrders.size}/10)"
                                } else {
                                    if (pendingOrders.size > 10) "🔥 DÉBORDEMENT ACTIF (${pendingOrders.size - 10} en surplus)" else "⏳ EN REPOS (Admin 1 gère tout)"
                                },
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentSubAdmin == "Admin 1") {
                                    if (pendingOrders.size >= 10) Color(0xFFEF4444) else Color(0xFF047857)
                                } else {
                                    if (pendingOrders.size > 10) Color(0xFFD97706) else Color.Gray
                                }
                            )
                        }
                    }
                }
            }

            // --- Custom Assigned Orders Queue (FILTRÉE PAR SOU-ADMIN) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Commandes Assignées à mon Rôle (${currentRoleOrders.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (currentSubAdmin == "Admin 2" && pendingOrders.size <= 10) {
                        // Special onboarding empty message for Admin 2
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.NotificationsPaused, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Admin 1 gère tout actuellement !",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "Les commandes s'afficheront chez vous uniquement si la file d'Admin 1 dépasse 10 commandes non traitées.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    } else if (currentRoleOrders.isEmpty()) {
                        Text(
                            text = "Aucune commande en attente de traitement.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        // Display assigned orders list
                        currentRoleOrders.forEach { order ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "CMD #${order.id}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFFEF3C7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = order.status,
                                                color = Color(0xFFD97706),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Text(
                                        text = order.productTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF0F172A)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Montant : " + if (order.payInNCoins) {
                                                "${order.coinsCost} N Coins"
                                            } else if (order.coinsCost > 0) {
                                                val discountFCFA = (order.coinsCost * conversionRate).toInt()
                                                val finalFCFA = (order.productPrice - discountFCFA).coerceAtLeast(0)
                                                "${finalFCFA.toLocaleString()} FCFA (-${order.coinsCost} N Coins)"
                                            } else {
                                                "${order.productPrice.toLocaleString()} FCFA"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF059669)
                                        )
                                        Text(
                                            text = "Boutique : ${order.sellerName}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Divider(color = Color(0xFFE2E8F0))

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Acheteur : ${order.buyerName} (${order.buyerWhatsApp})", fontSize = 10.sp, color = Color.Gray)
                                        Text("Date : ${order.date}", fontSize = 10.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Action Button to process order directly
                                    Button(
                                        onClick = {
                                            val ok = viewModel.scanDeliveryQrCode(order.id)
                                            if (ok) {
                                                Toast.makeText(context, "Livraison Validée ! Commission de 5% transférée au compte administrateur.", Toast.LENGTH_LONG).show()
                                            } else {
                                                Toast.makeText(context, "Erreur de validation de livraison", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                            .testTag("process_order_${order.id}_button"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("VALIDER LIVRAISON (5% COMM.)", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- QR Code Scanner Simulation ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validation de Livraison (QR Code)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Lors de la livraison, scannez le QR code de l'acheteur pour encaisser les N Coins ou valider la transaction cash et percevoir la commission légale de 5%.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                    Button(
                        onClick = { showScanSimDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("scan_delivery_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("SIMULER SCAN LIVRAISON QR CODE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- Monetary Policy (Politique Monétaire) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Politique Monétaire Nora", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }

                    // Emission ratio: Views per Coin
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("RATIO D'ÉMISSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("${viewsRatio.toInt()} Vues = 1 N Coin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Slider(
                            value = viewsRatio,
                            onValueChange = { viewModel.setViewsRatio(it) },
                            valueRange = 5f..50f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                        )
                    }

                    // Conversion rate slider: FCFA per Coin
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("VALEUR DE CONVERSION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text("1 N Coin = ${conversionRate.toInt()} FCFA (Modulable)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                        Slider(
                            value = conversionRate,
                            onValueChange = { viewModel.setConversionRate(it) },
                            valueRange = 1f..10f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF10B981), activeTrackColor = Color(0xFF10B981))
                        )
                        Text("Ajustez le taux d'échange légal de 1 F CFA à 10 F CFA par N Coin selon la liquidité du marché.", fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }

            // --- Shop KYC Applications ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Validation des Boutiques (KYC)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Vérifiez l'identité et activez les sanctions en cas de fraude ou de non-paiement de la commission de 5%.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (kycApps.isEmpty()) {
                        Text("Aucune demande de boutique en attente", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        kycApps.forEach { app ->
                            KycApplicationRow(
                                application = app,
                                onApprove = { viewModel.approveKyc(app.id); Toast.makeText(context, "Boutique '${app.shopName}' certifiée !", Toast.LENGTH_SHORT).show() },
                                onReject = { viewModel.sanctionKyc(app.id, "Révoqué"); Toast.makeText(context, "Certification révoquée pour '${app.shopName}'", Toast.LENGTH_SHORT).show() },
                                onBan = { viewModel.sanctionKyc(app.id, "Banni"); Toast.makeText(context, "Boutique '${app.shopName}' bannie !", Toast.LENGTH_SHORT).show() },
                                onScammer = { viewModel.sanctionKyc(app.id, "Arnaqueur"); Toast.makeText(context, "Boutique '${app.shopName}' signalée comme arnaqueur !", Toast.LENGTH_SHORT).show() }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }

            // --- Content & Users Reports (Modération) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Signalements & Modération", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Consultez et supprimez le contenu suspect (vidéos, produits, utilisateurs signalés).", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (reportedItems.isEmpty()) {
                        Text("Aucun signalement en attente", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 12.dp))
                    } else {
                        reportedItems.forEach { rep ->
                            ReportItemRow(
                                report = rep,
                                onIgnore = { viewModel.ignoreReport(rep.id) },
                                onDelete = { viewModel.removeReportedContent(rep.id, rep.title, rep.type); Toast.makeText(context, "Contenu supprimé avec succès !", Toast.LENGTH_SHORT).show() }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }

            // --- Forced Firebase Backup Action ---
            Button(
                onClick = {
                    viewModel.triggerBackup {
                        Toast.makeText(context, "Sauvegarde terminée", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("backup_db_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F2937)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    if (isBackingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBackingUp) "SAUVEGARDE EN COURS..." else "FORCER LA SAUVEGARDE FIREBASE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Scan Simulation Dialog modal (restricted to visible orders)
    if (showScanSimDialog) {
        Dialog(onDismissRequest = { showScanSimDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Simulateur de Scan QR Code", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Saisissez l'ID d'une commande assignée à votre rôle pour simuler la lecture du QR code de l'acheteur.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (currentRoleOrders.isEmpty()) {
                        Text("Aucune commande active assignée à votre rôle !", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Vos commandes actives :", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        currentRoleOrders.forEach { o ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color(0xFFF1F5F9))
                                    .clickable { scanOrderIdInput = o.id }
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("ID: #${o.id} - ${o.productTitle}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Acheteur: ${o.buyerName} - Total: ${if (o.payInNCoins) "${o.coinsCost} Coins" else "${o.productPrice} FCFA"}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = scanOrderIdInput,
                        onValueChange = { scanOrderIdInput = it },
                        label = { Text("Code Commande (Order ID)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showScanSimDialog = false }) { Text("Fermer") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (scanOrderIdInput.isBlank()) {
                                    Toast.makeText(context, "Saisissez ou cliquez sur un ID de commande", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val isAuthorized = currentRoleOrders.any { it.id == scanOrderIdInput }
                                if (!isAuthorized) {
                                    Toast.makeText(context, "Non autorisé: cette commande n'est pas assignée à votre rôle administrateur !", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val ok = viewModel.scanDeliveryQrCode(scanOrderIdInput)
                                if (ok) {
                                    Toast.makeText(context, "Scan Réussi ! Livraison confirmée et commission de 5% transférée au compte administrateur.", Toast.LENGTH_LONG).show()
                                    showScanSimDialog = false
                                    scanOrderIdInput = ""
                                } else {
                                    Toast.makeText(context, "Commande introuvable ou déjà livrée", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Simuler Scan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KycApplicationRow(
    application: UserProfile,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onBan: () -> Unit,
    onScammer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(application.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(application.kycStatus, color = Color(0xFFD97706), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text("WhatsApp: ${application.whatsappNumber}", fontSize = 11.sp, color = Color.Gray)
            Text("Boutique demandée: ${application.shopName} (${application.shopCategory})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Description: ${application.shopDescription}", fontSize = 10.sp, color = Color.Gray)
            Text("Localisation: ${application.shopLocation}", fontSize = 10.sp, color = Color.Gray)

            Divider(color = Color(0xFFE5E7EB))

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("📁 CNI: ${application.idCardPhoto}", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
                Text("📁 Selfie: ${application.selfiePhoto}", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
                Text("📜 Engagé à payer 5% de frais: Oui, validé", fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium)
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Certify
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Certifier", fontSize = 10.sp, color = Color.White)
                }

                // Revoke
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Révoquer", fontSize = 10.sp, color = Color.White)
                }

                // Fraud Arnaqueur banner
                Button(
                    onClick = onScammer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Arnaqueur", fontSize = 10.sp, color = Color.White)
                }

                // Ban
                Button(
                    onClick = onBan,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Bannir", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ReportItemRow(
    report: ReportedItem,
    onIgnore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = report.type.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
                Text(
                    text = "Par: ${report.reporterName}",
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }

            Text("Élément: ${report.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Motif: ${report.reason}", fontSize = 11.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(4.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onIgnore) {
                    Icon(Icons.Default.Close, contentDescription = "Ignore", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
