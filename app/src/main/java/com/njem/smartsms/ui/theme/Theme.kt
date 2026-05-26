package com.njem.smartsms.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryColor = Color(0xFF6C63FF)
val SecondaryColor = Color(0xFF03DAC5)
val BackgroundDark = Color(0xFF0F0F1A)
val SurfaceDark = Color(0xFF1A1A2E)
val CardDark = Color(0xFF16213E)
val AccentColor = Color(0xFFE94560)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0C3)
val SpamColor = Color(0xFFE94560)
val BankColor = Color(0xFF00C9A7)
val OtpColor = Color(0xFFFFBE0B)
val PersonalColor = Color(0xFF6C63FF)
val AdsColor = Color(0xFFFF6B6B)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = SecondaryColor,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    tertiary = AccentColor
)

@Composable
fun SmartSmsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
