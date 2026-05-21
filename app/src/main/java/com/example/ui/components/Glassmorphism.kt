package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

private val GoldHex: Color @Composable get() = LocalAccentColor.current

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = if (MaterialTheme.colorScheme.primary == EmeraldPrimary) Color(0x26FFFFFF) else BorderLight,
    backgroundColor: Color = if (MaterialTheme.colorScheme.primary == EmeraldPrimary) Color(0x0FFFFFFF) else Color(0xB3FFFFFF),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .border(
                BorderStroke(borderWidth, borderColor),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    val containerColor = if (isPrimary) {
        Brush.horizontalGradient(listOf(EmeraldPrimary, EmeraldMedium))
    } else {
        Brush.horizontalGradient(listOf(Color(0x33DFB76C), Color(0x1FDFB76C)))
    }
    
    val borderColor = if (isPrimary) EmeraldPrimary else GoldHex

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(50))
            .then(
                if (isPrimary) Modifier.background(containerColor)
                else Modifier
                    .background(Color(0x1AFFFFFF))
                    .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(50))
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isPrimary) Color.White else (if (MaterialTheme.colorScheme.primary == EmeraldPrimary) GoldHex else TextDarkGreen),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current.copy(
                shadow = if (isPrimary) Shadow(
                    color = Color(0x80000000),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f
                ) else null
            )
        )
    }
}

@Composable
fun IslamicTitle(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Text(
            text = title,
            color = if (MaterialTheme.colorScheme.primary == EmeraldPrimary) GoldHex else TextDarkGreen,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            style = LocalTextStyle.current
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = if (MaterialTheme.colorScheme.primary == EmeraldPrimary) TextMutedLight else TextMutedDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun SkeletonLoadingItem(
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    cornerRadius: Dp = 16.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.3f at 0
                0.8f at 500
                0.3f at 1000
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )

    val color = if (MaterialTheme.colorScheme.primary == EmeraldPrimary) {
        Color(0xFF0F261E).copy(alpha = alpha)
    } else {
        Color(0xFFE0E6E4).copy(alpha = alpha)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(cornerRadius))
    )
}
