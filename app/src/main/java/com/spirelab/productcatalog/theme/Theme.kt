package com.spirelab.productcatalog.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF5B5D72),
    tertiary = Color(0xFF77536D),
)

private val DarkColors = darkColorScheme(
    background = Color(0xFF0A0A0A),
    surface = Color(0xFF151515),
    surfaceVariant = Color(0xFF1F1F1F),
    primary = Color(0xFF8B5CF6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF8B5CF6),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF22D3EE),
    onSecondary = Color.White,
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF9A9A9A),
)

/** Star color for ratings; fixed so it reads as a rating in both themes. */
val RatingStar = Color(0xFF22D3EE)

@Composable
fun ProductCatalogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
