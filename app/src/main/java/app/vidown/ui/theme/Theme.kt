package app.vidown.ui.theme

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

private val FallbackDark = darkColorScheme(
    primary = Color(0xFFE2E2E9),
    onPrimary = Color(0xFF1B1B21),
    primaryContainer = Color(0xFF46464F),
    onPrimaryContainer = Color(0xFFE2E2E9),
    secondary = Color(0xFFD1C4E9),
    onSecondary = Color(0xFF311B92),
    background = Color(0xFF111111),
    onBackground = Color(0xFFF3F3F1),
    surface = Color(0xFF111111),
    onSurface = Color(0xFFF3F3F1),
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = Color(0xFFC7C5D0),
    outline = Color(0xFF46464F),
)

private val FallbackLight = lightColorScheme(
    primary = Color(0xFF111111),
    onPrimary = Color(0xFFF3F3F1),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFF6200EE),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFF3F3F1),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFF3F3F1),
    onSurface = Color(0xFF111111),
    surfaceVariant = Color(0xFFE6E6E6),
    onSurfaceVariant = Color(0xFF46464F),
    outline = Color(0xFFD1D1D1),
)

@Composable
fun VidownTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(
            context
        )

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
        darkTheme -> FallbackDark
        else -> FallbackLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VidownTypography,
        shapes = VidownShapes,
        content = content,
    )
}
