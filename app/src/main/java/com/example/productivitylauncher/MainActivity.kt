package com.example.productivitylauncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.service.UrgeButtonService
import com.example.productivitylauncher.ui.LauncherApp
import com.example.productivitylauncher.ui.theme.LauncherTheme
import com.example.productivitylauncher.ui.theme.resolvePalette

/**
 * The single activity of the app. It is registered as a HOME activity in the manifest,
 * so Android can offer it as the default launcher.
 */
/** 1.0 is Android's normal size. */
private const val DESIGN_WIDTH_DP = 390f

class MainActivity : ComponentActivity() {

    private lateinit var state: LauncherState

    /** Incremented every time the user presses Home while we are already open. */
    private var homeSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = LauncherState(this)
        setContent {
            val palette = resolvePalette(state.themeMode)
            val dark = palette.dark
            // Keep the system bar icons readable on both themes, including a manual theme choice.
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                    navigationBarStyle = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                )
                onDispose { }
            }
            // Everything is drawn a little smaller than the Android default: sizes and text scale together.
            // Like the prototype, whatever the phone: the screen is always 390 design units wide, so every proportion matches.
            val density = LocalDensity.current
            val cfg = androidx.compose.ui.platform.LocalConfiguration.current
            val widthDp = minOf(cfg.screenWidthDp, cfg.screenHeightDp)
            val scale = (widthDp / DESIGN_WIDTH_DP).coerceIn(0.8f, 1.3f)
            CompositionLocalProvider(LocalDensity provides Density(density.density * scale, density.fontScale)) {
                LauncherTheme(palette) {
                    LauncherApp(state = state, homeSignal = homeSignal)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        state.onResume()
        // The launcher is on screen, so the red button over other apps is not needed here.
        if (state.urgeButtonOn && canDrawOverlays(this)) UrgeButtonService.hide(this) else UrgeButtonService.stop(this)
    }

    override fun onPause() {
        // Leaving the launcher for another app: bring the red button back.
        if (state.urgeButtonOn && canDrawOverlays(this)) UrgeButtonService.show(this)
        super.onPause()
    }

    // launchMode="singleTask": pressing Home again arrives here instead of creating a new activity.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        homeSignal++
    }
}
