package com.doomfree.launcher.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.doomfree.launcher.data.AccentColor

// Near-black base tone that every screen's background/surface is tinted from.
private const val BASE_TONE = 0xFF14110EL

val OnSurfaceColor = Color(0xFFF4EFE9)
val MutedColor = Color(0xFF9A9187)

/**
 * Builds a full tonal palette from a single accent by blending it into the near-black base at
 * increasing strengths — this is what makes background/surface/outline shift with the theme
 * instead of only the primary color changing.
 */
fun colorSchemeFor(accent: AccentColor): ColorScheme {
    val accentArgb = accent.argb.toInt()
    val baseArgb = BASE_TONE.toInt()

    fun tone(strength: Float): Color = Color(ColorUtils.blendARGB(baseArgb, accentArgb, strength))

    val background = tone(0.04f)
    val surface = tone(0.13f)
    val surfaceVariant = tone(0.20f)
    val surfaceElevated = tone(0.30f)
    val outline = tone(0.5f)

    val accentColor = Color(accentArgb)
    val onAccent = if (ColorUtils.calculateLuminance(accentArgb) > 0.5) Color.Black else Color.White

    return darkColorScheme(
        primary = accentColor,
        onPrimary = onAccent,
        primaryContainer = surfaceElevated,
        onPrimaryContainer = OnSurfaceColor,
        secondary = accentColor,
        onSecondary = onAccent,
        tertiary = accentColor,
        onTertiary = onAccent,
        background = background,
        onBackground = OnSurfaceColor,
        surface = surface,
        onSurface = OnSurfaceColor,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = MutedColor,
        surfaceTint = accentColor,
        outline = outline,
        outlineVariant = tone(0.35f),
    )
}

/** A soft top-to-bottom wash from the accent into the background, for screen backdrops. */
@Composable
fun accentBackdropBrush(accent: AccentColor): androidx.compose.ui.graphics.Brush {
    val scheme = MaterialTheme.colorScheme
    return remember(accent, scheme.background) {
        androidx.compose.ui.graphics.Brush.verticalGradient(
            colors = listOf(
                Color(ColorUtils.blendARGB(scheme.background.toArgb(), accent.argb.toInt(), 0.16f)),
                scheme.background,
            ),
        )
    }
}

@Composable
fun DoomfreeTheme(accent: AccentColor, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colorSchemeFor(accent),
        typography = DoomfreeTypography,
        content = content,
    )
}
