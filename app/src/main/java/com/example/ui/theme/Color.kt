package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val SlateDark950 = Color(0xFF020617) // Base Background
val SlateDark900 = Color(0xFF0F172A) // Surface / Card Background
val SlateDark800 = Color(0xFF1E293B) // Variant Surface / Border
val SlateDark700 = Color(0xFF334155) // Text Muted / Icon inactive

val ElectricIndigo = Color(0xFF6366F1) // Primary Accent
val ElectricIndigoVariant = Color(0xFF4F46E5)
val EmeraldGreen = Color(0xFF10B981)   // Success / Nearby Online Accent
val RoyaltyGold = Color(0xFFF59E0B)    // Badge / Royalty Accent

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextDark = Color(0xFF020617)

val DarkColorScheme = androidx.compose.material3.darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = ElectricIndigoVariant,
    onPrimaryContainer = Color.White,
    secondary = EmeraldGreen,
    onSecondary = TextDark,
    secondaryContainer = Color(0xFF064E3B),
    onSecondaryContainer = EmeraldGreen,
    tertiary = RoyaltyGold,
    onTertiary = TextDark,
    background = SlateDark950,
    onBackground = TextPrimary,
    surface = SlateDark900,
    onSurface = TextPrimary,
    surfaceVariant = SlateDark800,
    onSurfaceVariant = TextSecondary,
    outline = SlateDark700
)
