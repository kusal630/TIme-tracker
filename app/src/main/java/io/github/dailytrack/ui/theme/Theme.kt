package io.github.dailytrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF0D47A1),
    primaryContainer = Color(0xFF1565C0),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFCE93D8),
    onSecondary = Color(0xFF4A148C),
    secondaryContainer = Color(0xFF6A1B9A),
    onSecondaryContainer = Color(0xFFEDDCFF),
    tertiary = Color(0xFFA5D6A7),
    onTertiary = Color(0xFF1B5E20),
    tertiaryContainer = Color(0xFF2E7D32),
    onTertiaryContainer = Color(0xFFC8E6C9),
    background = Color(0xFF0F0F12),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1A1F),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF2C2C31),
    onSurfaceVariant = Color(0xFFC7C5CA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF444449),
    surfaceTint = Color(0xFF90CAF9)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1565C0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF6A1B9A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDDCFF),
    onSecondaryContainer = Color(0xFF4A148C),
    tertiary = Color(0xFF2E7D32),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFC8E6C9),
    onTertiaryContainer = Color(0xFF1B5E20),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    surfaceTint = Color(0xFF1565C0)
)

@Composable
fun DailyTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
        typography = Typography(),
        content = content
    )
}
