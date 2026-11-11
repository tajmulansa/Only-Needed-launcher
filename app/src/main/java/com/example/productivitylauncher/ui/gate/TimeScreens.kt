package com.example.productivitylauncher.ui.gate

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.data.launchApp
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.service.SessionOverlayService
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.LocalPalette
import com.example.productivitylauncher.ui.theme.Motion

/** The big rounded call to action. Fades down when it can't be used yet. */
@Composable
private fun Cta(text: String, enabled: Boolean, onClick: () -> Unit) {
    val a by animateFloatAsState(if (enabled) 1f else 0.22f, Motion.state(300), label = "cta")
    Box(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .graphicsLayer { alpha = a }
            .background(AppColors.text, RadiusPill)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) { AppText(text, size = 16.sp, weight = FontWeight.Medium, color = AppColors.phone, display = false) }
}

@Composable
private fun Ghost(text: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().clickableRole(onClick).padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
        AppText(text, size = 15.sp, color = AppColors.muted, display = false)
    }
}

/**
 * Shown when an app that is not on Home is opened (found by typing its full name). One question, how long,
 * then the app opens and a small floating timer counts it down.
 */
@Composable
fun TimeScreen(state: LauncherState, app: AppEntry, nav: Nav) {
    val context = LocalContext.current
    val dark = LocalPalette.current.dark
    val presets = listOf(5, 10, 15, 30)
    var selected by remember { mutableIntStateOf(10) }
    var custom by remember { mutableStateOf(false) }
    var customMin by remember { mutableIntStateOf(20) }
    val minutes = if (custom) customMin else selected

    Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Box(
                Modifier.size(40.dp).border(1.dp, AppColors.line, CircleShape).semantics { contentDescription = "Close" }.clickableRole({ nav.back() }),
                contentAlignment = Alignment.Center,
            ) { AppIcon(Ic.Close, AppColors.text, size = 16.dp) }
        }
        Column(Modifier.weight(1f).fillMaxWidth().padding(top = 36.dp)) {
            Caption(app.label)
            AppText("How long do you\nneed?", Modifier.padding(top = 10.dp), size = 34.sp, weight = FontWeight.Medium)
            AppText("A small timer runs while you are in the app.", Modifier.padding(top = 10.dp), size = 15.sp, color = AppColors.muted, display = false)

            // Four big numbers; the chosen one lifts and a line slides under it.
            val cols = presets.size
            val idx = if (custom) -1 else presets.indexOf(selected).coerceAtLeast(0)
            Box(Modifier.padding(top = 36.dp).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth()) {
                    presets.forEach { m ->
                        val on = !custom && selected == m
                        val lift by animateDpAsState((if (on) -4 else 0).dp, Motion.morph(500), label = "lift")
                        val tint by animateColorAsState(if (on) AppColors.text else AppColors.muted, Motion.state(300), label = "tint")
                        Column(
                            Modifier.weight(1f).offset(y = lift).clickableRole({ custom = false; selected = m }).padding(top = 16.dp, bottom = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            AppText("$m", size = 56.sp, weight = FontWeight.Light, color = tint, letterSpacing = (-3).sp)
                            Caption("min", Modifier.padding(top = 8.dp), color = tint)
                        }
                    }
                }
                if (idx >= 0) {
                    val frac by animateFloatAsState(idx.toFloat() / cols, Motion.morph(600), label = "under")
                    Box(
                        Modifier.align(Alignment.BottomStart).fillMaxWidth(1f / cols).height(2.dp)
                            .offset(x = androidx.compose.ui.platform.LocalConfiguration.current.let { ((it.screenWidthDp - 56) * frac).dp })
                            .background(AppColors.text),
                    )
                }
            }
            Row(Modifier.padding(top = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TextLink(if (custom) "Custom" else "Custom time", { custom = true })
                if (custom) Stepper("$customMin min", { customMin = (customMin - 5).coerceAtLeast(1) }, { customMin = (customMin + 5).coerceAtMost(180) }, valueWidth = 90.dp)
            }
            if (!canDrawOverlays(context)) TextLink("Allow the floating timer", { openOverlaySettings(context) })
        }
        Cta("Open ${app.label} for $minutes min", true) {
            state.startSession(app.packageName, minutes)
            if (canDrawOverlays(context)) SessionOverlayService.start(context, app.label, state.sessionEnd, dark)
            nav.go(Route.Main)
            launchApp(context, app)
        }
        Ghost("Not now") { nav.back() }
    }
}

/** A plain full-screen message: small label, big title, quiet paragraph, one button. */
@Composable
private fun Message(label: String, title: String, body: String, primary: String, onPrimary: () -> Unit, secondary: String? = null, onSecondary: () -> Unit = {}) {
    Column(Modifier.fillMaxSize().padding(start = 28.dp, end = 28.dp, top = 56.dp, bottom = 24.dp)) {
        Caption(label)
        AppText(title, Modifier.padding(top = 10.dp), size = 34.sp, weight = FontWeight.Medium)
        AppText(body, Modifier.padding(top = 16.dp), size = 16.sp, color = AppColors.muted, lineHeight = 24.sp, display = false)
        Spacer(Modifier.weight(1f))
        Cta(primary, true, onPrimary)
        if (secondary != null) Ghost(secondary, onSecondary)
    }
}

/** "Time's up" after a session ends. */
@Composable
fun TimesUpScreen(app: AppEntry?, nav: Nav) {
    Message(
        label = "Done",
        title = "Time's up",
        body = "Your time in ${app?.label ?: "that app"} is over. Stopping on time is a real win.",
        primary = "Back to Home", onPrimary = { nav.go(Route.Main) },
        secondary = if (app != null) "I need a bit more" else null,
        onSecondary = { if (app != null) nav.go(Route.Time(app.packageName)) },
    )
}
