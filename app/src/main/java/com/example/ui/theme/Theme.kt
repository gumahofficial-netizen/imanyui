package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import com.example.ui.viewmodel.BackgroundOption

val LocalAccentColor = staticCompositionLocalOf { Color(0xFFD4AF37) }
val LocalQuranFontFamily = staticCompositionLocalOf<FontFamily> { FontFamily.Default }
val LocalBackgroundOption = staticCompositionLocalOf { BackgroundOption.SILENT_COSMIC }

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = TextWhite,
    secondary = GoldHex,
    onSecondary = DeepJadeBackground,
    tertiary = EmeraldMedium,
    background = DeepJadeBackground,
    onBackground = TextWhite,
    surface = SurfaceDarkJade,
    onSurface = TextWhite,
    outline = BorderJade,
    surfaceVariant = EmeraldDark,
    onSurfaceVariant = TextWhite
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldMedium,
    onPrimary = SurfaceLight,
    secondary = GoldDark,
    onSecondary = SurfaceLight,
    tertiary = EmeraldPrimary,
    background = LightBackground,
    onBackground = TextDarkGreen,
    surface = SurfaceLight,
    onSurface = TextDarkGreen,
    outline = BorderLight,
    surfaceVariant = EmeraldLight,
    onSurfaceVariant = TextDarkGreen
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
