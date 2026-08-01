package com.example.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NoraBadge(
    text: String,
    backgroundColor: Color = NoraPrimary,
    textColor: Color = NoraWhite,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(NoraRadius.small))
            .background(backgroundColor)
            .padding(horizontal = NoraSpacing.xs + 2.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun NoraInitialAvatar(
    name: String,
    size: Dp = 40.dp,
    backgroundColor: Color = NoraPrimary,
    textColor: Color = NoraWhite,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "?",
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 2.2).sp
        )
    }
}

@Composable
fun NoraLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = NoraPrimary,
    text: String? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(NoraSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = color,
            strokeWidth = 2.5.dp,
            modifier = Modifier.size(32.dp)
        )
        if (!text.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(NoraSpacing.sm))
            Text(
                text = text,
                color = NoraTextGray,
                fontSize = 12.sp
            )
        }
    }
}
