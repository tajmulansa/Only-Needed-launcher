package com.example.productivitylauncher.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.PagerState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import kotlin.math.roundToInt
import kotlinx.coroutines.launch
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Intro
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.enterStagger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/** Home: a rolling clock, six numbered app slots and a search pill. Swipe up to search, swipe down for Settings. */
@Composable
fun HomeScreen(
    state: LauncherState,
    nav: Nav,
    onOpenApp: (AppEntry) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val dateLine = SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now)
    val cal = Calendar.getInstance().apply { time = now }
    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
    val use24 = DateFormat.is24HourFormat(context)
    val shownHour = if (use24) hour24 else (hour24 % 12).let { if (it == 0) 12 else it }
    val minute = cal.get(Calendar.MINUTE)

    Column(Modifier.fillMaxSize().verticalSwipes(onUp = onOpenSearch, onDown = { nav.go(Route.Settings) })) {
        // Top row: the date and Settings. Swipe up anywhere to search, swipe down from the top for Settings.
        Column {
            Row(
                Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, top = 12.dp).enterStagger(0, once = true),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AppText(dateLine.uppercase(), Modifier.padding(vertical = 10.dp), size = 13.sp, weight = FontWeight.Medium, color = AppColors.muted, letterSpacing = 1.2.sp, mono = true)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .border(1.dp, AppColors.line, CircleShape)
                            .semantics { contentDescription = "Settings" }
                            .clickableRole({ nav.go(Route.Settings) }),
                        contentAlignment = Alignment.Center,
                    ) { AppIcon(Ic.Gear, AppColors.text, size = 18.dp) }
                }
            }

            // The clock.
            Column(Modifier.padding(start = 28.dp, end = 28.dp, top = 14.dp).enterStagger(1, once = true)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hs = shownHour.toString().padStart(2, '0')
                    Odometer(hs[0].digitToInt(), 0, 112.sp)
                    Odometer(hs[1].digitToInt(), 90, 112.sp)
                    Colon(112.sp)
                    Odometer(minute / 10, 180, 112.sp)
                    Odometer(minute % 10, 270, 112.sp)
                }
            }
        }

        Hair(0, Modifier.padding(horizontal = 28.dp, vertical = 18.dp))

        // A quiet label for the list, with today's screen time on the right when Usage access is allowed.
        val screen = state.screenTimeMs
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp).enterStagger(2, once = true),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Caption("Your six")
            if (screen != null) Caption("Today \u00B7 ${formatMillis(screen)}")
        }

        Column(Modifier.weight(1f).fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 6.dp, bottom = 112.dp)) {
            // Always six slots, so Home keeps its shape. Rows that would not fully fit are left out instead of cut in half.
            FitColumn(Modifier.fillMaxWidth().weight(1f)) {
                val picked = state.homeApps.mapNotNull { state.appByPackage(it) }
                for (i in 0 until 6) {
                    val app = picked.getOrNull(i)
                    Box(Modifier.enterStagger(3 + i, once = true)) {
                        SlotRow(
                            number = i + 1, app = app, showAdd = i == picked.size,
                            onClick = { if (app != null) onOpenApp(app) else nav.go(Route.HomePicker) },
                        )
                    }
                }
                LaunchedEffect(Unit) { delay(900); Intro.played = true }
            }
        }
    }
}

/** Stacks children top to bottom and places only those that fit completely in the height given. */
@Composable
private fun FitColumn(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    androidx.compose.ui.layout.Layout(content, modifier) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity)
        val limit = constraints.maxHeight
        var y = 0
        val placed = ArrayList<Pair<androidx.compose.ui.layout.Placeable, Int>>()
        for (m in measurables) {
            val p = m.measure(loose)
            if (y + p.height > limit) break
            placed += p to y
            y += p.height
        }
        layout(constraints.maxWidth, if (constraints.hasBoundedHeight) limit else y) { placed.forEach { (p, py) -> p.place(0, py) } }
    }
}

/** One digit that rolls up a strip of 0..9 to its value. With animations off it simply changes. */
@Composable
private fun Odometer(digit: Int, delayMs: Int, size: TextUnit) {
    val density = LocalDensity.current
    val cell = with(density) { size.toDp() }
    var target by remember { mutableStateOf(if (Intro.played) digit else 0) }
    LaunchedEffect(digit) { target = digit }
    val y by animateFloatAsState(
        target.toFloat(),
        if (Motion.enabled) tween<Float>(900, delayMillis = if (Intro.played) 0 else delayMs, easing = Motion.Soft) else snap(),
        label = "digit",
    )
    Box(
        Modifier
            .clipToBounds()
            .layout { m, c ->
                val cellPx = cell.roundToPx()
                val p = m.measure(c.copy(minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity))
                layout(p.width, cellPx) { p.place(0, 0) }
            },
    ) {
        Column(
            Modifier.graphicsLayer { translationY = -y * cell.toPx() },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            for (d in 0..9) {
                Box(Modifier.height(cell)) { ClockGlyph(d.toString(), size, AppColors.text, (-0.06).em) }
            }
        }
    }
}

/**
 * One clock glyph with its baseline pinned at 0.83 em from the top of a 1 em tall cell. That is where the prototype's
 * `line-height: 1` puts Bricolage Grotesque (ascent 0.93, descent 0.27), so the digits sit exactly where they do there
 * and the gap between the date and the clock matches. Every glyph, colon included, reports the same cell height.
 */
@Composable
private fun ClockGlyph(text: String, size: TextUnit, color: androidx.compose.ui.graphics.Color, tracking: TextUnit) {
    AppText(
        text,
        Modifier.layout { m, c ->
            val p = m.measure(c.copy(minWidth = 0, minHeight = 0, maxHeight = androidx.compose.ui.unit.Constraints.Infinity))
            val cellPx = size.roundToPx()
            val y = (0.83f * cellPx).roundToInt() - p[FirstBaseline]
            layout(p.width, cellPx) { p.place(0, y) }
        },
        size = size, weight = FontWeight.Light, color = color, letterSpacing = tracking, lineHeight = size, tnum = true,
    )
}

@Composable
private fun Colon(size: TextUnit) {
    val alpha = if (Motion.enabled) {
        val t = rememberInfiniteTransition(label = "colon")
        val a by t.animateFloat(
            1f, 0.25f,
            infiniteRepeatable(keyframes { durationMillis = 2000; 1f at 0; 1f at 900; 0.25f at 1000; 0.25f at 1900 }, RepeatMode.Restart),
            label = "colonAlpha",
        )
        a
    } else 1f
    val cell = with(LocalDensity.current) { size.toDp() }
    Box(Modifier.width(cell * 0.3f).height(cell).graphicsLayer { this.alpha = alpha }, contentAlignment = Alignment.TopCenter) {
        ClockGlyph(":", size, AppColors.muted, (-0.05).em)
    }
}

/** A hairline that draws itself in from the left. */
@Composable
fun Hair(index: Int, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(Intro.played || !Motion.enabled) }
    LaunchedEffect(Unit) { shown = true }
    val p by animateFloatAsState(
        if (shown) 1f else 0f,
        if (Motion.enabled) tween<Float>(1000, delayMillis = if (Intro.played) 0 else 200 + index * 120, easing = Motion.Soft) else snap(),
        label = "hair",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .graphicsLayer { scaleX = p; transformOrigin = TransformOrigin(0f, 0.5f) }
            .background(AppColors.line),
    )
}

/** One of the six slots: a small number, then the app name. Empty slots stay quiet, the next free one says "Add an app". */
@Composable
private fun SlotRow(number: Int, app: AppEntry?, showAdd: Boolean, onClick: () -> Unit) {
    val source = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val shift by animateFloatAsState(if (pressed) 8f else 0f, Motion.press(), label = "shift")
    val numColor by animateColorAsState(if (pressed) AppColors.text else if (app != null) AppColors.muted else AppColors.dot, Motion.state(200), label = "num")
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
            .graphicsLayer { translationX = shift.dp.toPx(); scaleX = if (pressed) 0.985f else 1f; scaleY = if (pressed) 0.985f else 1f }
            .clickable(interactionSource = source, indication = null, onClick = onClick)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppText(number.toString().padStart(2, '0'), Modifier.width(36.dp), size = 12.sp, weight = FontWeight.Medium, color = numColor, letterSpacing = 1.sp, mono = true)
        when {
            app != null -> AppText(
                app.label, Modifier.weight(1f), size = 28.sp, weight = FontWeight.Medium, letterSpacing = (-1.1).sp, lineHeight = 32.sp,
                color = AppColors.text, maxLines = 1,
            )
            showAdd -> Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppIcon(Ic.Plus, AppColors.muted, size = 18.dp)
                AppText("Add an app", size = 20.sp, weight = FontWeight.Medium, color = AppColors.muted, maxLines = 1)
            }
            else -> Box(Modifier.weight(1f).height(1.dp).background(AppColors.line))
        }
    }
}

/**
 * One vertical swipe detector that never steals touches from children or the pager: up opens All apps, down from the
 * upper half opens Quick actions. It fires the moment a mostly vertical drag is long enough, or on release after a
 * short quick flick, so it feels immediate and works from anywhere on the page. Horizontal drags are left to the pager.
 */
fun Modifier.verticalSwipes(onUp: () -> Unit, onDown: () -> Unit): Modifier = pointerInput(Unit) {
    val dragDistance = 56.dp.toPx()
    val flickDistance = 24.dp.toPx()
    val flickSpeed = 700.dp.toPx()
    val downZone = size.height * 0.55f
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val tracker = androidx.compose.ui.input.pointer.util.VelocityTracker()
        tracker.addPosition(down.uptimeMillis, down.position)
        var last = down.position
        var fired = false
        fun fire(dy: Float) {
            fired = true
            if (dy < 0f) onUp() else if (down.position.y < downZone) onDown()
        }
        while (true) {
            val e = awaitPointerEvent(PointerEventPass.Initial)
            val c = e.changes.firstOrNull { it.id == down.id } ?: break
            last = c.position
            tracker.addPosition(c.uptimeMillis, c.position)
            if (!c.pressed) break
            val dy = last.y - down.position.y
            val dx = last.x - down.position.x
            if (!fired && kotlin.math.abs(dy) > dragDistance && kotlin.math.abs(dy) > kotlin.math.abs(dx) * 1.4f) fire(dy)
        }
        if (!fired) {
            val dy = last.y - down.position.y
            val dx = last.x - down.position.x
            val vy = tracker.calculateVelocity().y
            if (kotlin.math.abs(dy) > flickDistance && kotlin.math.abs(dy) > kotlin.math.abs(dx) * 1.4f && kotlin.math.abs(vy) > flickSpeed && (vy < 0f) == (dy < 0f)) fire(dy)
        }
    }
}

/** The bar at the bottom: a search pill that fills the width, and two round buttons, Phone and Messages. */
@Composable
fun BottomBar(onSearch: () -> Unit, onPhone: () -> Unit, onMessages: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.38f to AppColors.phone, 1f to AppColors.phone))
            .padding(start = 22.dp, end = 22.dp, bottom = 22.dp, top = 34.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .semantics { contentDescription = "Find an app" }
                    .background(AppColors.card, RadiusPill)
                    .border(1.dp, AppColors.line, RadiusPill)
                    .clickableRole(onSearch)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                AppIcon(Ic.Search, AppColors.muted, size = 18.dp)
                AppText("Type an app's name", size = 15.sp, color = AppColors.muted, display = false, maxLines = 1)
            }
            DockButton(Ic.Phone, "Phone", onPhone)
            DockButton(Ic.Message, "Messages", onMessages)
        }
    }
}

@Composable
private fun DockButton(ic: Ic, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .border(1.dp, AppColors.line, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, AppColors.text, size = 20.dp)
    }
}
