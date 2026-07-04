package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
  lightColorScheme(
    primary = NoraPrimary,
    onPrimary = NoraWhite,
    secondary = NoraSecondary,
    background = NoraBackground,
    onBackground = NoraTextDark,
    surface = NoraSurface,
    onSurface = NoraTextDark,
    outline = NoraBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Always light mode as requested
  dynamicColor: Boolean = false, // Disable dynamic colors to keep pure emerald palette
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
