package com.example.productivitylauncher.data

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.productivitylauncher.ui.theme.Look
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.ThemeMode
import org.json.JSONArray

/**
 * All launcher state, saved on the device in SharedPreferences. Nothing leaves the phone.
 * Fields are Compose state, so screens update by themselves when a value changes.
 */
class LauncherState(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    private fun str(k: String, d: String = ""): String = prefs.getString(k, d) ?: d
    private fun bool(k: String, d: Boolean): Boolean = prefs.getBoolean(k, d)
    private fun long(k: String, d: Long): Long = prefs.getLong(k, d)
    private fun save(block: SharedPreferences.Editor.() -> Unit) {
        val e = prefs.edit()
        e.block()
        e.apply()
    }

    /** Bumped whenever the launcher returns to the foreground, so screens re-check permissions. */
    var resumeTick by mutableIntStateOf(0)
        private set

    fun onResume() {
        // The red button over other apps can start the lock while the launcher is in the background.
        urgeEnd = long("urge_end", 0L)
        resumeTick++
    }

    // ------------------------------------------------------------ apps

    var apps by mutableStateOf<List<AppEntry>>(emptyList())
        private set

    fun refreshApps(pm: PackageManager) {
        apps = loadLaunchableApps(pm, appContext.packageName)
    }

    fun appByPackage(pkg: String): AppEntry? = apps.firstOrNull { it.packageName == pkg }

    /** The six apps on Home, in order. These open at once. Every other app needs its full name typed and a time chosen. */
    var homeApps by mutableStateOf(readStringList("home_apps"))
        private set

    private fun readStringList(k: String): List<String> = runCatching {
        val arr = JSONArray(str(k, "[]"))
        (0 until arr.length()).map { arr.getString(it) }
    }.getOrDefault(emptyList())

    fun updateHomeApps(list: List<String>) {
        homeApps = list.distinct().take(6)
        val arr = JSONArray(homeApps)
        save { putString("home_apps", arr.toString()) }
    }

    // ------------------------------------------------------------ onboarding and appearance

    var onboarded by mutableStateOf(bool("onboarded", false))
        private set

    fun finishOnboarding() { onboarded = true; save { putBoolean("onboarded", true) } }

    var themeMode by mutableStateOf(runCatching { ThemeMode.valueOf(str("theme", "Auto")) }.getOrDefault(ThemeMode.Auto))
        private set

    fun setTheme(m: ThemeMode) { themeMode = m; save { putString("theme", m.name) } }

    /** A colour treatment over the launcher: Standard, Gray, Warm or Contrast. */
    var look by mutableStateOf(runCatching { Look.valueOf(str("look", "Standard")) }.getOrDefault(Look.Standard))
        private set

    fun updateLook(l: Look) { look = l; save { putString("look", l.name) } }

    /** Turn off for a faster, calmer feel on slower phones. Every animation in the app follows this one switch. */
    var animations by mutableStateOf(bool("animations", true))
        private set

    init { Motion.enabled = animations }

    fun updateAnimations(on: Boolean) { animations = on; Motion.enabled = on; save { putBoolean("animations", on) } }

    // ------------------------------------------------------------ red button (urge lock)

    /** Epoch millis when the red button's timer ends, or 0. Saved, so closing the app does not end it. */
    var urgeEnd by mutableStateOf(long("urge_end", 0L))
        private set

    /** Show the red button over every app. Needs "Display over other apps". */
    var urgeButtonOn by mutableStateOf(bool("urge_button", true))
        private set

    fun updateUrgeButton(on: Boolean) { urgeButtonOn = on; save { putBoolean("urge_button", on) } }

    fun startUrgeLock(minutes: Int = URGE_LOCK_MINUTES) {
        urgeEnd = System.currentTimeMillis() + minutes * 60_000L
        save { putLong("urge_end", urgeEnd) }
    }

    // ------------------------------------------------------------ sessions (time chosen for an app)

    var sessionPkg by mutableStateOf(str("session_pkg"))
        private set
    var sessionEnd by mutableStateOf(long("session_end", 0L))
        private set
    var sessionNotified by mutableStateOf(bool("session_notified", true))
        private set

    val sessionActive: Boolean get() = sessionEnd > System.currentTimeMillis()

    fun startSession(pkg: String, minutes: Int) {
        sessionPkg = pkg
        sessionEnd = System.currentTimeMillis() + minutes * 60_000L
        sessionNotified = false
        save {
            putString("session_pkg", pkg)
            putLong("session_end", sessionEnd)
            putBoolean("session_notified", false)
        }
    }

    /** Re-reads session fields that the floating timer service may have changed. */
    fun syncSession() {
        sessionPkg = str("session_pkg")
        sessionEnd = long("session_end", 0L)
        sessionNotified = bool("session_notified", true)
    }

    fun endSessionEarly() {
        sessionEnd = System.currentTimeMillis()
        save { putLong("session_end", sessionEnd) }
    }

    /** True once, when a session has run out and the user has not yet seen "Time's up". */
    fun consumeTimesUp(): Boolean {
        if (sessionPkg.isNotEmpty() && !sessionNotified && sessionEnd <= System.currentTimeMillis()) {
            sessionNotified = true
            save { putBoolean("session_notified", true) }
            return true
        }
        return false
    }

    // ------------------------------------------------------------ usage numbers (read-only, from UsageStats)

    var usageAccess by mutableStateOf(false)
        private set

    /** Total screen time today across apps, without this launcher. Null until Usage access is allowed. */
    var screenTimeMs by mutableStateOf<Long?>(null)
        private set

    /** Reads usage numbers on the calling thread. Call it off the main thread. */
    fun refreshUsage() {
        val reader = UsageReader(appContext)
        if (!reader.hasAccess()) {
            usageAccess = false
            screenTimeMs = null
            return
        }
        val now = System.currentTimeMillis()
        val own = appContext.packageName
        val perApp = reader.perPackage(startOfDay(now), now)
        usageAccess = true
        screenTimeMs = perApp.filterKeys { it != own }.values.sum()
    }

    // ------------------------------------------------------------ reset

    /** Deletes everything this app stored. */
    fun resetAll() {
        prefs.edit().clear().apply()
        urgeEnd = 0L; urgeButtonOn = true
        homeApps = emptyList()
        sessionPkg = ""; sessionEnd = 0L; sessionNotified = true
        themeMode = ThemeMode.Auto; look = Look.Standard; animations = true; Motion.enabled = true; onboarded = false
    }
}
