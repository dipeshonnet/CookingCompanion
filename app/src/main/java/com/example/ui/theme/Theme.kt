package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = WarmOrangePrimary,
    onPrimary = SophisticatedWhite,
    primaryContainer = Color(0xFFFFDBC7),             // Warm peach terracotta light backing from HTML
    onPrimaryContainer = Color(0xFF321300),           // Dark terracotta brown text from HTML
    secondary = WarmOrangeSecondary,
    onSecondary = Color(0xFF321300),
    secondaryContainer = Color(0xFFF2E0D6),           // Soft grey sand separator from HTML
    onSecondaryContainer = SlateTextDark,
    tertiary = SageGreenTertiary,
    onTertiary = SophisticatedWhite,
    background = WarmCreamBackground,                 // `#FEF7F3` from HTML
    onBackground = SlateTextDark,                     // `#201A18` from HTML
    surface = SophisticatedWhite,
    onSurface = SlateTextDark,
    surfaceVariant = GrayLightBorder,                 // `#F2E0D6` from HTML
    onSurfaceVariant = SlateTextMedium,               // `#52443C` from HTML
    outline = Color(0xFF85736B),                      // `#85736B` from HTML
    error = CrimsonRose
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFCC80),                      // Golden honey warm accent
    onPrimary = Color(0xFF321300),
    primaryContainer = Color(0xFFA04E00),             // Terracotta deep focus
    onPrimaryContainer = Color(0xFFFFDBC7),
    secondary = Color(0xFFFFB300),
    onSecondary = Color(0xFF321300),
    secondaryContainer = Color(0xFF3E3530),
    onSecondaryContainer = Color(0xFFFEF7F3),
    tertiary = Color(0xFFA5D6A7),
    onTertiary = Color(0xFF1B5E20),
    background = Color(0xFF1B1512),                   // Deep baked coffee black
    onBackground = Color(0xFFFEF7F3),
    surface = Color(0xFF221A17),                      // Espresso dark clay surface
    onSurface = Color(0xFFFEF7F3),
    surfaceVariant = Color(0xFF3A302B),
    onSurfaceVariant = Color(0xFFD7CFCB),
    outline = Color(0xFF85736B),
    error = Color(0xFFEF5350)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to force our curated cozy brand colors!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
