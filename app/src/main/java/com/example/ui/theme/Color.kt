package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// MAGIX Obsidian & Graphite Palette (Dark Mode)
val MagixVoid = Color(0xFF080B11)           // Deepest near-black base
val MagixSurface0 = Color(0xFF0D111A)        // Base canvas surface
val MagixSurface1 = Color(0xFF141A26)        // Card and container background
val MagixSurface2 = Color(0xFF1B2333)        // Elevated modal and active card
val MagixSurface3 = Color(0xFF242F45)        // Focused interactive container
val MagixBorder = Color(0xFF222D3D)          // Subtle structural border
val MagixBorderGlow = Color(0xFF38BDF8)      // Cool blue focus border

// MAGIX Pure Quantum Palette (Light Mode)
val MagixLightVoid = Color(0xFFF6F8FC)       // Crisp off-white slate foundation
val MagixLightSurface0 = Color(0xFFFFFFFF)   // Pure white surface
val MagixLightSurface1 = Color(0xFFEFF3F8)   // High-tech soft slate card container
val MagixLightSurface2 = Color(0xFFE2E8F0)   // Elevated container / hover state
val MagixLightSurface3 = Color(0xFFCBD5E1)   // Active segmented pill container
val MagixLightBorder = Color(0xFFDCE3ED)     // Precision boundary hairline
val MagixLightBorderGlow = Color(0xFF0284C7) // Quantum sapphire glow

// Restrained Ice-Blue / Cyan Accents
val MagixCyan = Color(0xFF38BDF8)            // Primary quantum cyan (Sky 400)
val MagixCyanGlow = Color(0xFF80D8FF)        // Luminescent pulse
val MagixCyanSpark = Color(0xFFE0F7FA)       // High-energy core center spark
val MagixBlueDeep = Color(0xFF1D4ED8)        // Royal cosmic blue
val MagixPurpleExec = Color(0xFFA78BFA)      // Execution state amethyst
val MagixGreenSuccess = Color(0xFF34D399)    // Success state emerald mint
val MagixRedError = Color(0xFFF87171)        // Subtle error rose crimson
val MagixAmberWarm = Color(0xFFFBBF24)       // Warning / attention amber

// Light Mode Accent Overrides for contrast
val MagixLightCyan = Color(0xFF0284C7)       // Sapphire cyan (Sky 600)
val MagixLightCyanGlow = Color(0xFF0EA5E9)
val MagixLightPurpleExec = Color(0xFF7C3AED)
val MagixLightGreenSuccess = Color(0xFF059669)
val MagixLightRedError = Color(0xFFDC2626)

// Text & Typography Scale (Dark)
val MagixTextPrimary = Color(0xFFF8FAFC)     // Slate 50 high contrast
val MagixTextSecondary = Color(0xFF94A3B8)   // Slate 400 calm readability
val MagixTextTertiary = Color(0xFF64748B)    // Slate 500 metadata
val MagixTextAccent = Color(0xFF7DD3FC)      // Ice Blue highlighted text

// Text & Typography Scale (Light)
val MagixLightTextPrimary = Color(0xFF0F172A)   // Slate 900 high contrast
val MagixLightTextSecondary = Color(0xFF475569) // Slate 600 readability
val MagixLightTextTertiary = Color(0xFF94A3B8)  // Slate 400 metadata
val MagixLightTextAccent = Color(0xFF0284C7)    // Sapphire Blue text highlight

@Immutable
data class MagixColors(
    val void: Color,
    val surface0: Color,
    val surface1: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val borderGlow: Color,
    val cyan: Color,
    val cyanGlow: Color,
    val cyanSpark: Color,
    val purpleExec: Color,
    val greenSuccess: Color,
    val redError: Color,
    val amberWarm: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textAccent: Color,
    val isDark: Boolean
) {
    val success: Color get() = greenSuccess
    val error: Color get() = redError
    val warning: Color get() = amberWarm
    val primary: Color get() = cyan
    val accent: Color get() = cyanGlow
    val borderSubtle: Color get() = border
    val textMuted: Color get() = textTertiary
}

val DarkMagixColors = MagixColors(
    void = MagixVoid,
    surface0 = MagixSurface0,
    surface1 = MagixSurface1,
    surface2 = MagixSurface2,
    surface3 = MagixSurface3,
    border = MagixBorder,
    borderGlow = MagixBorderGlow,
    cyan = MagixCyan,
    cyanGlow = MagixCyanGlow,
    cyanSpark = MagixCyanSpark,
    purpleExec = MagixPurpleExec,
    greenSuccess = MagixGreenSuccess,
    redError = MagixRedError,
    amberWarm = MagixAmberWarm,
    textPrimary = MagixTextPrimary,
    textSecondary = MagixTextSecondary,
    textTertiary = MagixTextTertiary,
    textAccent = MagixTextAccent,
    isDark = true
)

val LightMagixColors = MagixColors(
    void = MagixLightVoid,
    surface0 = MagixLightSurface0,
    surface1 = MagixLightSurface1,
    surface2 = MagixLightSurface2,
    surface3 = MagixLightSurface3,
    border = MagixLightBorder,
    borderGlow = MagixLightBorderGlow,
    cyan = MagixLightCyan,
    cyanGlow = MagixLightCyanGlow,
    cyanSpark = MagixCyanSpark,
    purpleExec = MagixLightPurpleExec,
    greenSuccess = MagixLightGreenSuccess,
    redError = MagixLightRedError,
    amberWarm = MagixAmberWarm,
    textPrimary = MagixLightTextPrimary,
    textSecondary = MagixLightTextSecondary,
    textTertiary = MagixLightTextTertiary,
    textAccent = MagixLightTextAccent,
    isDark = false
)

