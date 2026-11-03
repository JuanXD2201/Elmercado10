package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary & Secondary Brand Colors (Elmercado10 - Contemporary Luxury Editorial)
val PrimaryIndigo = Color(0xFF18181B)          // Refined Zinc Black
val PrimaryIndigoDark = Color(0xFF09090B)      // Obsidian Black
val PrimaryIndigoLight = Color(0xFF3F3F46)     // Zinc Slate
val PrimaryContainerLight = Color(0xFFF4F4F5)   // Off-white / Soft Gray
val PrimaryContainerDark = Color(0xFF27272A)    // Deep Zinc

val SecondaryTeal = Color(0xFF52525B)          // Muted Charcoal
val SecondaryTealLight = Color(0xFFA1A1AA)     // Soft Muted Gray
val SecondaryContainerLight = Color(0xFFFAFAFA)
val SecondaryContainerDark = Color(0xFF18181B)

val AccentCoral = Color(0xFF9A3412)            // Terracotta Rust (Sophisticated Warm Highlight)
val AccentCoralLight = Color(0xFFC2410C)
val AccentOrange = Color(0xFFD97706)           // Deep Amber
val AccentAmber = Color(0xFFB45309)            // Bronze Gold
val AccentPurple = Color(0xFF6D28D9)           // Deep Royal Violet
val AccentPink = Color(0xFFBE185D)             // Elegant Crimson

// Semantic Status Colors
val SuccessEmerald = Color(0xFF166534)          // Deep Pine Green
val SuccessEmeraldLight = Color(0xFFF0FDF4)
val WarningAmber = Color(0xFF854D0E)           // Muted Gold
val WarningAmberLight = Color(0xFFFEF9C3)
val ErrorRose = Color(0xFF991B1B)              // Deep Burgundy Red
val ErrorRoseLight = Color(0xFFFEF2F2)
val InfoSky = Color(0xFF075985)                // Muted Slate Blue
val InfoSkyLight = Color(0xFFF0F9FF)

// Surface & Neutral Grays (Deep Obsidian & Slate Scale)
val Slate950 = Color(0xFF09090B)
val Slate900 = Color(0xFF18181B)
val Slate800 = Color(0xFF27272A)
val Slate700 = Color(0xFF3F3F46)
val Slate600 = Color(0xFF52525B)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Card & Background specific
val CardSurfaceLight = Color(0xFFFFFFFF)
val CardSurfaceDark = Color(0xFF18181B)
val BackgroundLight = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF09090B)
val BorderLight = Color(0xFFE4E4E7)
val BorderDark = Color(0xFF27272A)

// Ultra-Vibrant Brand Gradients for visual delight & glassmorphism
val BrandGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF18181B), Color(0xFF52525B))
)
val HeroGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF18181B), Color(0xFF27272A), Color(0xFF52525B))
)
val DealGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF7C2D12), Color(0xFF9A3412))
)
val GoldGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF78350F), Color(0xFFB45309))
)
val SuccessGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF14532D), Color(0xFF166534))
)
val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
)

// Aliases for backwards compatibility
val PrimaryBlue = PrimaryIndigo
val PrimaryLight = PrimaryIndigoLight
val PrimaryDark = PrimaryIndigoDark
val SuccessGreen = SuccessEmerald
val DangerRed = ErrorRose


