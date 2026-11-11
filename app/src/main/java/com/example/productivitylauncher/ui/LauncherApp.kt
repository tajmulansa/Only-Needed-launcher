package com.example.productivitylauncher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.launchApp
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.service.UrgeButtonService
import com.example.productivitylauncher.ui.apps.AppDrawer
import com.example.productivitylauncher.ui.apps.HomePickerScreen
import com.example.productivitylauncher.ui.gate.TimeScreen
import com.example.productivitylauncher.ui.gate.TimesUpScreen
import com.example.productivitylauncher.ui.home.BottomBar
import com.example.productivitylauncher.ui.home.HomeScreen
import com.example.productivitylauncher.ui.onboarding.OnboardingScreen
import com.example.productivitylauncher.ui.settings.SettingsScreen
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.lookFilter
import com.example.productivitylauncher.ui.urge.UrgeScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun LauncherApp(state: LauncherState, homeSignal: Int) {
    val context = LocalContext.current

    var route by remember { mutableStateOf<Route>(if (state.onboarded) Route.Main else Route.Onboarding) }
    val stack = remember { ArrayList<Route>() }
    var drawerOpen by remember { mutableStateOf(false) }

    // The red button: while its timer runs, nothing but the lock screen is shown.
    val urgeNow by produceState(System.currentTimeMillis(), state.urgeEnd) {
        while (true) {
            value = System.currentTimeMillis()
            if (value >= state.urgeEnd) break
            delay(200L)
        }
    }
    val urgeActive = state.urgeEnd > urgeNow
    LaunchedEffect(urgeActive) {
        if (urgeActive) {
            UrgeButtonService.guard(context)
            stack.clear()
            if (route != Route.Onboarding) route = Route.Main
            drawerOpen = false
        }
    }

    val nav = remember {
        Nav(
            go = { r ->
                if (r == Route.Main) stack.clear() else stack.add(route)
                route = r
                drawerOpen = false
            },
            back = { route = if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) else Route.Main },
        )
    }

    // Pressing Home always returns to the Home page.
    LaunchedEffect(homeSignal) {
        if (homeSignal > 0) {
            stack.clear()
            if (route != Route.Onboarding) route = Route.Main
            drawerOpen = false
        }
    }

    // Runs each time the launcher comes back to the foreground.
    LaunchedEffect(state.resumeTick) {
        state.syncSession()
        withContext(Dispatchers.IO) {
            state.refreshApps(context.packageManager)
            state.refreshUsage()
        }
        if (route != Route.Onboarding && state.consumeTimesUp()) {
            val pkg = state.sessionPkg
            stack.clear()
            route = Route.TimesUp(pkg)
        }
    }

    // Keep screen time fresh while the launcher is open.
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            withContext(Dispatchers.IO) { state.refreshUsage() }
        }
    }

    // A launcher never closes: Back goes toward Home, and does nothing on Home.
    BackHandler {
        when {
            urgeActive -> Unit
            route == Route.Onboarding -> Unit
            drawerOpen -> drawerOpen = false
            route != Route.Main -> nav.back()
        }
    }

    /** Home apps open at once. Any other app asks how long you need it first. */
    fun openApp(app: AppEntry) {
        val pkg = app.packageName
        when {
            pkg in state.homeApps -> launchApp(context, app)
            state.sessionActive && state.sessionPkg == pkg -> launchApp(context, app)
            else -> nav.go(Route.Time(pkg))
        }
    }

    val filter = lookFilter(state.look)
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                if (filter != null) {
                    colorFilter = filter
                    compositingStrategy = CompositingStrategy.Offscreen
                }
            }
            .background(AppColors.phone)
            .systemBarsPadding(),
    ) {
        AnimatedContent(
            targetState = if (urgeActive) null else route,
            modifier = Modifier.fillMaxSize(),
            transitionSpec = {
                // One continuous surface: a soft cross-fade with a tiny scale, no sideways page jump.
                ((fadeIn(Motion.enter(320)) + slideInVertically(Motion.enter(380)) { 40 } + scaleIn(initialScale = 0.985f, animationSpec = Motion.enter(320))) togetherWith
                    (fadeOut(Motion.exit(220)) + scaleOut(targetScale = 1.015f, animationSpec = Motion.exit(220))))
                    .using(SizeTransform(clip = false))
            },
            label = "route",
        ) { r ->
            if (r == null) UrgeScreen(state.urgeEnd - urgeNow) else when (r) {
                Route.Main -> Box(Modifier.fillMaxSize()) {
                    HomeScreen(state, nav, onOpenApp = { openApp(it) }, onOpenDrawer = { drawerOpen = true })
                    BottomBar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        onApps = { drawerOpen = true },
                        onPhone = { openPhone(context) },
                        onMessages = { openMessages(context) },
                    )
                    AppDrawer(state, drawerOpen, { drawerOpen = false }, onOpenApp = { openApp(it) })
                }
                is Route.Time -> {
                    val app = state.appByPackage(r.pkg)
                    if (app == null) nav.back() else TimeScreen(state, app, nav)
                }
                is Route.TimesUp -> TimesUpScreen(state.appByPackage(r.pkg), nav)
                Route.Settings -> SettingsScreen(state, nav)
                Route.HomePicker -> HomePickerScreen(state, nav)
                Route.Onboarding -> OnboardingScreen(state, nav)
            }
        }
    }
}
