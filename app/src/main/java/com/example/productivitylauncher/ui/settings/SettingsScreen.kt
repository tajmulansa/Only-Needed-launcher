package com.example.productivitylauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.data.hasNotificationAccess
import com.example.productivitylauncher.data.openNotificationAccessSettings
import com.example.productivitylauncher.data.isDefaultLauncher
import com.example.productivitylauncher.data.openDefaultLauncherSettings
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Divider
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.SettingRow
import com.example.productivitylauncher.ui.components.SettingsGroup
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.Toggle
import com.example.productivitylauncher.ui.components.ValueChevron
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.theme.Look
import com.example.productivitylauncher.ui.theme.ThemeMode

@Composable
fun SettingsScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }
    var showPolicy by remember { mutableStateOf(false) }
    // Re-read permissions every time the user comes back from a system settings page.
    val tick = state.resumeTick
    val usage = remember(tick, state.usageAccess) { state.usageAccess }
    val overlay = remember(tick) { canDrawOverlays(context) }
    val notifAccess = remember(tick) { hasNotificationAccess(context) }
    val isHome = remember(tick) { isDefaultLauncher(context) }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Preferences", onBack = nav.back, label = "Settings")
        androidx.compose.runtime.CompositionLocalProvider(com.example.productivitylauncher.ui.components.LocalRowLines provides false) {
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            SettingsGroup("Appearance") {
                Column(Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppText("Theme", size = 16.sp, weight = FontWeight.Medium)
                    Segmented(ThemeMode.entries.map { it.label }, state.themeMode.ordinal, { state.setTheme(ThemeMode.entries[it]) })
                }
                Column(Modifier.padding(vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppText("Look", size = 16.sp, weight = FontWeight.Medium)
                    Segmented(Look.entries.map { it.label }, state.look.ordinal, { state.updateLook(Look.entries[it]) })
                }
                SettingRow("Animations", subtitle = "Turn off for a faster, calmer feel on slower phones") {
                    Toggle(state.animations, { state.updateAnimations(it) }, "Animations")
                }
            }

            SettingsGroup("Apps") {
                SettingRow("Home apps", subtitle = "Up to six, they open at once", onClick = { nav.go(Route.HomePicker) }) {
                    ValueChevron("${state.homeApps.size} of 6")
                }
            }

            SettingsGroup("Red button") {
                SettingRow(
                    "Red button in every app",
                    subtitle = if (state.urgeButtonOn && !overlay) "On, but it needs Display over other apps (see Permissions)"
                    else "A small red dot over every app. Tap it twice to lock the phone for 5 minutes",
                ) {
                    Toggle(state.urgeButtonOn, { on ->
                        state.updateUrgeButton(on)
                        if (on && !overlay) openOverlaySettings(context)
                    }, "Red button in every app")
                }
            }

            SettingsGroup("Permissions") {
                SettingRow("Usage access", subtitle = "Shows today's screen time on Home", onClick = { openUsageAccessSettings(context) }) {
                    ValueChevron(if (usage) "Allowed" else "Allow", if (usage) AppColors.focus else AppColors.gate)
                }
                SettingRow("Display over other apps", subtitle = "For the floating timer and the red button", onClick = { openOverlaySettings(context) }) {
                    ValueChevron(if (overlay) "Allowed" else "Allow", if (overlay) AppColors.focus else AppColors.gate)
                }
                SettingRow("Notification access", subtitle = "Dismisses notifications during the red button lock", onClick = { openNotificationAccessSettings(context) }) {
                    ValueChevron(if (notifAccess) "Allowed" else "Allow", if (notifAccess) AppColors.focus else AppColors.gate)
                }
                SettingRow("Default home app", subtitle = "Make this your launcher", onClick = { openDefaultLauncherSettings(context) }) {
                    ValueChevron(if (isHome) "This app" else "Change", if (isHome) AppColors.focus else AppColors.muted)
                }
            }

            SettingsGroup("Data and privacy") {
                SettingRow("Privacy policy", subtitle = "What is stored and what each permission does", onClick = { showPolicy = true }) { ValueChevron("") }
                SettingRow("Stays on this phone", subtitle = "This app has no internet permission. Nothing is sent anywhere.") {}
                SettingRow("Delete all my data", titleColor = AppColors.gate, subtitle = "Resets your settings and Home apps", onClick = { confirmReset = true }) {}
            }

            SettingsGroup("About") {
                val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "" }
                SettingRow("Version", subtitle = "Use your phone with intention") { AppText(version, size = 15.sp, color = AppColors.muted, weight = FontWeight.Medium) }
            }
            Spacer(Modifier.height(16.dp))
        }
        }
    }

    if (showPolicy) {
        AlertDialog(
            onDismissRequest = { showPolicy = false },
            containerColor = AppColors.card,
            title = { AppText("Privacy policy", size = 20.sp, weight = FontWeight.SemiBold) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    PRIVACY_SECTIONS.forEach { (title, body) ->
                        AppText(title, size = 15.sp, weight = FontWeight.SemiBold, modifier = Modifier.padding(top = 10.dp))
                        AppText(body, size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPolicy = false }) { AppText("Close", weight = FontWeight.SemiBold) } },
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = AppColors.card,
            title = { AppText("Delete all data?", size = 20.sp, weight = FontWeight.SemiBold) },
            text = { AppText("This removes your Home apps and settings from this phone. It can't be undone.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp) },
            confirmButton = {
                TextButton(onClick = { state.resetAll(); confirmReset = false; nav.go(Route.Onboarding) }) {
                    AppText("Delete", color = AppColors.gate, weight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { AppText("Cancel", weight = FontWeight.SemiBold) } },
        )
    }
}
