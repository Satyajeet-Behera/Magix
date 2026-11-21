package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.domain.model.ThemeMode

private val MagixDarkColorScheme = darkColorScheme(
    primary = MagixCyan,
    onPrimary = MagixVoid,
    primaryContainer = MagixSurface2,
    onPrimaryContainer = MagixCyanSpark,
    secondary = MagixCyanGlow,
    onSecondary = MagixVoid,
    secondaryContainer = MagixSurface2,
    onSecondaryContainer = MagixTextPrimary,
    tertiary = MagixPurpleExec,
    onTertiary = MagixVoid,
    background = MagixVoid,
    onBackground = MagixTextPrimary,
    surface = MagixSurface0,
    onSurface = MagixTextPrimary,
    surfaceVariant = MagixSurface1,
    onSurfaceVariant = MagixTextSecondary,
    outline = MagixBorder,
    outlineVariant = MagixSurface3,
    error = MagixRedError,
    onError = MagixVoid
)

private val MagixLightColorScheme = lightColorScheme(
    primary = MagixLightCyan,
    onPrimary = MagixLightSurface0,
    primaryContainer = MagixLightSurface2,
    onPrimaryContainer = MagixLightTextPrimary,
    secondary = MagixLightCyanGlow,
    onSecondary = MagixLightSurface0,
    secondaryContainer = MagixLightSurface1,
    onSecondaryContainer = MagixLightTextPrimary,
    tertiary = MagixLightPurpleExec,
    onTertiary = MagixLightSurface0,
    background = MagixLightVoid,
    onBackground = MagixLightTextPrimary,
    surface = MagixLightSurface0,
    onSurface = MagixLightTextPrimary,
    surfaceVariant = MagixLightSurface1,
    onSurfaceVariant = MagixLightTextSecondary,
    outline = MagixLightBorder,
    outlineVariant = MagixLightSurface3,
    error = MagixLightRedError,
    onError = MagixLightSurface0
)

val LocalMagixColors = staticCompositionLocalOf { DarkMagixColors }

object MagixTheme {
    val colors: MagixColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMagixColors.current

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MagixTypography
}

@Composable
fun MagixTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    darkTheme: Boolean = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    },
    content: @Composable () -> Unit
) {
    val resolvedDark = darkTheme
    val colors = if (resolvedDark) DarkMagixColors else LightMagixColors
    val colorScheme = if (resolvedDark) MagixDarkColorScheme else MagixLightColorScheme

    CompositionLocalProvider(
        LocalMagixColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MagixTypography,
            content = content
        )
    }
}

