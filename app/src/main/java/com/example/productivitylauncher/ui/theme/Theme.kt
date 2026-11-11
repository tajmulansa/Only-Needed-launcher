package com.example.productivitylauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.productivitylauncher.R

/**
 * The Still palette (see docs/DESIGN.md): cool stone and ink, no accent colour.
 * Screens read colours only through [AppColors].
 */
class Palette(
    val phone: Color,
    /** A quiet tonal surface: tiles, fields, tracks. Never a shadowed card. */
    val card: Color,
    val text: Color,
    val muted: Color,
    /** Positive state. Ink, not green: the app has no accent colour. */
    val focus: Color,
    val onFocus: Color,
    /** The one warm colour, used only for gated apps and warnings. */
    val gate: Color,
    val gateSoft: Color,
    val onGate: Color,
    val line: Color,
    val dot: Color,
    val primary: Color,
    val onPrimary: Color,
    val dark: Boolean,
)

val LightPalette = Palette(
    phone = Color(0xFFECEDEA),
    card = Color(0xFFE1E3DF),
    text = Color(0xFF111312),
    muted = Color(0xFF7B7F79),
    focus = Color(0xFF111312),
    onFocus = Color(0xFFECEDEA),
    gate = Color(0xFFB5502C),
    gateSoft = Color(0xFFEBD9D1),
    onGate = Color(0xFFFFFFFF),
    line = Color(0x1F111312),
    dot = Color(0xFFC5C8C2),
    primary = Color(0xFF111312),
    onPrimary = Color(0xFFECEDEA),
    dark = false,
)

val DarkPalette = Palette(
    phone = Color(0xFF0B0C0B),
    card = Color(0xFF161816),
    text = Color(0xFFEEF0EC),
    muted = Color(0xFF777B74),
    focus = Color(0xFFEEF0EC),
    onFocus = Color(0xFF0B0C0B),
    gate = Color(0xFFE27A52),
    gateSoft = Color(0xFF2E1D17),
    onGate = Color(0xFF241109),
    line = Color(0x21EEF0EC),
    dot = Color(0xFF383B37),
    primary = Color(0xFFEEF0EC),
    onPrimary = Color(0xFF0B0C0B),
    dark = true,
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

/** Short accessors, e.g. AppColors.text. */
object AppColors {
    val phone: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.phone
    val card: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.card
    val text: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.text
    val muted: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.muted
    val focus: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.focus
    val onFocus: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onFocus
    val gate: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.gate
    val gateSoft: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.gateSoft
    val onGate: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onGate
    val line: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.line
    val dot: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.dot
    val primary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.primary
    val onPrimary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onPrimary
}

/** Geist for everything, bundled in res/font (no download, no network). Tight tracking carries the display sizes. */
val Sans = FontFamily(
    Font(R.font.geist_light, FontWeight.Light),
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_semibold, FontWeight.Bold),
    Font(R.font.geist_semibold, FontWeight.ExtraBold),
)

/** Bricolage Grotesque: display type (titles, clock, big numbers, app names), as in the Still prototype. */
val Display = FontFamily(
    Font(R.font.bricolage_light, FontWeight.Light),
    Font(R.font.bricolage_regular, FontWeight.Normal),
    Font(R.font.bricolage_medium, FontWeight.Medium),
    Font(R.font.bricolage_semibold, FontWeight.SemiBold),
    Font(R.font.bricolage_semibold, FontWeight.Bold),
    Font(R.font.bricolage_semibold, FontWeight.ExtraBold),
)

/** Geist Mono: small uppercase labels and data. */
val Mono = FontFamily(
    Font(R.font.geist_mono_regular, FontWeight.Normal),
    Font(R.font.geist_mono_medium, FontWeight.Medium),
    Font(R.font.geist_mono_medium, FontWeight.SemiBold),
    Font(R.font.geist_mono_medium, FontWeight.Bold),
    Font(R.font.geist_mono_medium, FontWeight.ExtraBold),
)

/** Theme choice saved in settings. Auto follows the phone's light or dark setting. */
enum class ThemeMode(val label: String) {
    Auto("Auto"),
    Light("Light"),
    Dark("Dark"),
}

/** A colour treatment laid over the launcher's own screens. Saved in settings and in the quick panel. */
enum class Look(val label: String) {
    Standard("Standard"),
    Gray("Gray"),
    Warm("Warm"),
    Contrast("Contrast"),
}

/** The colour filter for a look, or null for Standard (which costs nothing). */
fun lookFilter(look: Look): ColorFilter? = when (look) {
    Look.Standard -> null
    Look.Gray -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
    Look.Warm -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.05f, 0.05f, 0f, 0f, 6f,
                0.02f, 0.98f, 0f, 0f, 0f,
                0f, 0.05f, 0.80f, 0f, -4f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
    Look.Contrast -> ColorFilter.colorMatrix(
        ColorMatrix(
            floatArrayOf(
                1.2f, 0f, 0f, 0f, -25f,
                0f, 1.2f, 0f, 0f, -25f,
                0f, 0f, 1.2f, 0f, -25f,
                0f, 0f, 0f, 1f, 0f,
            ),
        ),
    )
}

/** The palette for a theme, or null for Auto (which depends on the phone). */
fun paletteOf(mode: ThemeMode): Palette? = when (mode) {
    ThemeMode.Auto -> null
    ThemeMode.Light -> LightPalette
    ThemeMode.Dark -> DarkPalette
}

@Composable
fun resolvePalette(mode: ThemeMode): Palette =
    paletteOf(mode) ?: if (isSystemInDarkTheme()) DarkPalette else LightPalette

@Composable
fun LauncherTheme(palette: Palette, content: @Composable () -> Unit) {
    val p = palette
    val scheme = if (p.dark) {
        darkColorScheme(primary = p.primary, background = p.phone, surface = p.phone, onBackground = p.text, onSurface = p.text)
    } else {
        lightColorScheme(primary = p.primary, background = p.phone, surface = p.phone, onBackground = p.text, onSurface = p.text)
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
