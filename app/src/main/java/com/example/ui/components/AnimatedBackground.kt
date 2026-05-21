package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.BackgroundOption
import kotlin.math.sin

private val DeepSpaceDark = Color(0xFF041410)
private val LightEmerald = Color(0xFF10B981)
private val WarmGold = Color(0xFFD4AF37)

@Composable
fun AnimatedIslamicBackground(
    option: BackgroundOption,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "islamic_bg_transition")

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val timeFloat by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepSpaceDark)
    ) {
        when (option) {
            BackgroundOption.SILENT_COSMIC -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension * 0.7f * pulse
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x1910B981), Color.Transparent),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = radius
                        ),
                        radius = radius,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                }
            }
            BackgroundOption.GOLD_STARFIELD -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val starsCount = 15
                    for (i in 0 until starsCount) {
                        val seedX = (i * 1234.567f) % 1f
                        val seedY = (i * 9876.543f) % 1f
                        val seedSpeed = (i * 153.25f) % 1f + 0.2f
                        
                        val x = (seedX * size.width + timeFloat * 25f * seedSpeed) % size.width
                        val y = (seedY * size.height - timeFloat * 15f * seedSpeed) % size.height
                        
                        val starSize = ((sin((timeFloat * 2f + i).toDouble()) + 1f) / 2f * 6.dp.toPx() + 2.dp.toPx()).toFloat()
                        
                        val path = Path().apply {
                            moveTo(x, y - starSize)
                            lineTo(x + starSize / 2f, y)
                            lineTo(x, y + starSize)
                            lineTo(x - starSize / 2f, y)
                            close()
                        }
                        
                        drawPath(
                            path = path,
                            color = WarmGold.copy(alpha = (0.3f + 0.5f * sin((timeFloat + i).toDouble()).toFloat()).coerceIn(0f, 1f))
                        )
                    }
                }
            }
            BackgroundOption.FLOATING_LANTERNS -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val lanternCount = 7
                    for (i in 0 until lanternCount) {
                        val seedX = (i * 5432.10f) % 1f
                        val seedY = (i * 2468.13f) % 1f
                        val seedSpeed = (i * 48.5f) % 1f + 0.3f
                        
                        val x = seedX * size.width + sin((timeFloat + i).toDouble()).toFloat() * 20f
                        val y = (seedY * size.height - timeFloat * 30f * seedSpeed + size.height) % size.height
                        
                        val lanternWidth = 14.dp.toPx()
                        val lanternHeight = 22.dp.toPx()
                        
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(WarmGold.copy(alpha = 0.25f), Color.Transparent),
                                center = Offset(x + lanternWidth/2f, y + lanternHeight/2f),
                                radius = 25.dp.toPx()
                            ),
                            radius = 25.dp.toPx(),
                            center = Offset(x + lanternWidth/2f, y + lanternHeight/2f)
                        )
                        
                        val path = Path().apply {
                            moveTo(x, y + lanternHeight * 0.2f)
                            lineTo(x + lanternWidth / 2f, y)
                            lineTo(x + lanternWidth, y + lanternHeight * 0.2f)
                            lineTo(x + lanternWidth * 0.8f, y + lanternHeight * 0.8f)
                            lineTo(x + lanternWidth * 0.5f, y + lanternHeight)
                            lineTo(x + lanternWidth * 0.2f, y + lanternHeight * 0.8f)
                            close()
                        }
                        
                        drawPath(
                            path = path,
                            color = WarmGold.copy(alpha = (0.4f + 0.3f * sin((timeFloat * 1.5f + i).toDouble()).toFloat()).coerceIn(0f, 1f))
                        )
                    }
                }
            }
            BackgroundOption.EMERALD_WAVES -> {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val wavePath1 = Path()
                    val wavePath2 = Path()
                    val halfHeight = size.height / 2f
                    
                    wavePath1.moveTo(0f, halfHeight)
                    wavePath2.moveTo(0f, halfHeight + 100f)
                    
                    for (x in 0..size.width.toInt() step 15) {
                        val y1 = halfHeight + sin((x.toFloat() * 0.005f + timeFloat).toDouble()).toFloat() * 60f
                        val y2 = halfHeight + 120f + sin((x.toFloat() * 0.003f - timeFloat).toDouble()).toFloat() * 40f
                        
                        wavePath1.lineTo(x.toFloat(), y1)
                        if (x == 0) {
                            wavePath2.moveTo(x.toFloat(), y2)
                        } else {
                            wavePath2.lineTo(x.toFloat(), y2)
                        }
                    }
                    
                    drawPath(
                        path = wavePath1,
                        color = LightEmerald.copy(alpha = 0.25f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    drawPath(
                        path = wavePath2,
                        color = WarmGold.copy(alpha = 0.15f),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
    }
}
