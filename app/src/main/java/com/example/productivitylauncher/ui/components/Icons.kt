package com.example.productivitylauncher.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Line icons drawn from SVG path data (24x24, stroke 2), so no icon library is needed. */
enum class Ic(val path: String, val filled: Boolean = false) {
    Phone("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"),
    Message("M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"),
    Moon("M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"),
    Target("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M12 6a6 6 0 1 0 0 12 6 6 0 0 0 0-12z M12 10a2 2 0 1 0 0 4 2 2 0 0 0 0-4z"),
    Search("M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z M21 21l-4.35-4.35"),
    Plus("M12 5v14 M5 12h14"),
    Minus("M5 12h14"),
    Check("M20 6L9 17l-5-5"),
    Back("M19 12H5 M12 19l-7-7 7-7"),
    Chevron("M9 18l6-6-6-6"),
    Close("M18 6L6 18 M6 6l12 12"),
    Sliders("M4 7h9 M17 7h3 M4 17h3 M11 17h9 M15 5a2 2 0 1 0 0 4 2 2 0 0 0 0-4z M9 15a2 2 0 1 0 0 4 2 2 0 0 0 0-4z"),
    Steady("M6 5h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z M4 12h16"),
    Timer("M12 5.5a7.5 7.5 0 1 0 0 15 7.5 7.5 0 0 0 0-15z M12 9v4l2.5 1.5 M9.5 2.5h5"),
    Drop("M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z", filled = true),
    Play("M6 4l14 8-14 8z", filled = true),
    SkipPrev("M19 5v14L9 12z M5 5h2v14H5z", filled = true),
    SkipNext("M5 5v14l10-7z M17 5h2v14h-2z", filled = true),
    Pause("M6 4h4v16H6z M14 4h4v16h-4z", filled = true),
    Lock("M5 11h14v10H5z M7 11V7a5 5 0 0 1 10 0v4"),
    Trash("M3 6h18 M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6 M10 11v6 M14 11v6 M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"),
    Calendar("M3 4h18v18H3z M16 2v4 M8 2v4 M3 10h18"),
    Bulb("M9 21h6 M12 3a6 6 0 0 0-4 10.5V17h8v-3.5A6 6 0 0 0 12 3z"),
    Chart("M18 20V10 M12 20V4 M6 20v-6"),
    Shield("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"),
    Home("M3 11l9-8 9 8 M5 10v10h5v-6h4v6h5V10"),
    Gear("M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"),
    Edit("M12 20h9 M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4z"),
    Up("M18 15l-6-6-6 6"),
    Down("M6 9l6 6 6-6"),
    Star("M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01z"),
    Contrast("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M12 2v20 M12 2a10 10 0 0 1 0 20z"),
    Bolt("M13 2L3 14h9l-1 8 10-12h-9z"),
}

private val cache = HashMap<Ic, ImageVector>()

private fun vectorOf(ic: Ic): ImageVector = cache.getOrPut(ic) {
    ImageVector.Builder(
        name = ic.name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(ic.path),
        fill = if (ic.filled) SolidColor(Color.Black) else null,
        stroke = if (ic.filled) null else SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}

@Composable
fun AppIcon(ic: Ic, tint: Color, modifier: Modifier = Modifier, size: Dp = 24.dp, description: String? = null) {
    val v = remember(ic) { vectorOf(ic) }
    Icon(imageVector = v, contentDescription = description, tint = tint, modifier = modifier.then(Modifier.sizeCompat(size)))
}
