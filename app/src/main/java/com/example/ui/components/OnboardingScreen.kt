package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.NoraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: NoraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    // Screen state: "auth" or "onboarding"
    val showOnboardingSetup = userProfile.isLoggedIn && !userProfile.onboardingCompleted

    if (showOnboardingSetup) {
        // --- 2. Onboarding / Profile Setup State ---
        var name by remember { mutableStateOf("") }
        var whatsapp by remember(userProfile.whatsappNumber) { mutableStateOf(userProfile.whatsappNumber) }
        val selectedInterests = remember { mutableStateListOf<String>() }
        var customCategoryInput by remember { mutableStateOf("") }

        val categoriesList by viewModel.categories.collectAsState()
        val interestOptions = remember(categoriesList) {
            categoriesList.filter { it != "Tous" }
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Configurez votre profil 🇨🇲",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Complétez ces informations pour accéder à l'application Nora.",
                fontSize = 13.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vos Centres d'Intérêt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111827),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(interestOptions) { interest ->
                            val isSelected = selectedInterests.contains(interest)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFFE6F4EA) else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFFE5E7EB),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (isSelected) {
                                            selectedInterests.remove(interest)
                                        } else {
                                            selectedInterests.add(interest)
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = interest,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFF10B981) else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customCategoryInput,
                            onValueChange = { customCategoryInput = it },
                            placeholder = { Text("Autre intérêt...", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                        Button(
                            onClick = {
                                val trimmed = customCategoryInput.trim()
                                if (trimmed.isNotEmpty()) {
                                    viewModel.createCategory(trimmed)
                                    if (!selectedInterests.contains(trimmed)) {
                                        selectedInterests.add(trimmed)
                                    }
                                    customCategoryInput = ""
                                    Toast.makeText(context, "Intérêt '$trimmed' ajouté !", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.height(40.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Text("Créer", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Votre Nom Complet") },
                        placeholder = { Text("Ex: Nora Kamga") },
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedLabelColor = Color(0xFF10B981)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("Numéro WhatsApp (Obligatoire)") },
                        placeholder = { Text("Ex: +237 677 88 99 00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth().testTag("onboarding_whatsapp_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFFE5E7EB),
                            focusedLabelColor = Color(0xFF10B981)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                Toast.makeText(context, "Veuillez entrer votre nom", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (whatsapp.isBlank()) {
                                Toast.makeText(context, "Numéro WhatsApp requis pour les transactions", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (selectedInterests.isEmpty()) {
                                Toast.makeText(context, "Sélectionnez au moins un intérêt", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            viewModel.selectInterestsAndLogin(
                                name = name,
                                whatsapp = whatsapp,
                                selectedInterests = selectedInterests.toList()
                            )
                            Toast.makeText(context, "Bienvenue sur Nora Cameroun !", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "COMMENCER L'EXPÉRIENCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    } else {
        // --- 1. Authentic Sign In / Sign Up Screen ---
        var isLoginTab by remember { mutableStateOf(true) }
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var whatsappInput by remember { mutableStateOf("") }
        var showPassword by remember { mutableStateOf(false) }

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF007A5E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "NORA CAMEROUN",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF007A5E),
                letterSpacing = 1.sp
            )

            Text(
                text = "Artisanat d'Art, Mode & Saveurs du Terroir",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (isLoginTab) Color.White else Color.Transparent)
                        .clickable { isLoginTab = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Se Connecter",
                        fontSize = 13.sp,
                        fontWeight = if (isLoginTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLoginTab) Color(0xFF007A5E) else Color(0xFF64748B)
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(100.dp))
                        .background(if (!isLoginTab) Color.White else Color.Transparent)
                        .clickable { isLoginTab = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Créer un Compte",
                        fontSize = 13.sp,
                        fontWeight = if (!isLoginTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isLoginTab) Color(0xFF007A5E) else Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isLoginTab) "Bon retour parmi nous !" else "Rejoignez l'aventure",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B)
                    )

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Adresse Email") },
                        placeholder = { Text("exemple@mail.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_email_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF007A5E),
                            focusedLabelColor = Color(0xFF007A5E)
                        )
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Mot de Passe") },
                        placeholder = { Text("••••••••") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("auth_password_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF007A5E),
                            focusedLabelColor = Color(0xFF007A5E)
                        )
                    )

                    if (!isLoginTab) {
                        OutlinedTextField(
                            value = whatsappInput,
                            onValueChange = { whatsappInput = it },
                            label = { Text("Numéro WhatsApp (Obligatoire)") },
                            placeholder = { Text("Ex: +237 677 88 99 00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_whatsapp_input"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF007A5E),
                                focusedLabelColor = Color(0xFF007A5E)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val email = emailInput.trim()
                            val password = passwordInput
                            if (email.isBlank() || password.isBlank()) {
                                Toast.makeText(context, "Champs requis manquants", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            if (isLoginTab) {
                                val success = viewModel.loginUser(email, password)
                                if (success) {
                                    Toast.makeText(context, "Connexion réussie !", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Email ou mot de passe incorrect", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                val whatsapp = whatsappInput.trim()
                                if (whatsapp.isBlank()) {
                                    Toast.makeText(context, "Numéro WhatsApp obligatoire pour s'inscrire", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                val success = viewModel.registerUser(email, password, whatsapp)
                                if (success) {
                                    Toast.makeText(context, "Compte créé ! Veuillez configurer votre profil.", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Cet email est déjà enregistré", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007A5E)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (isLoginTab) "Se Connecter" else "Créer mon Compte",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }


                }
            }
        }
    }
}
