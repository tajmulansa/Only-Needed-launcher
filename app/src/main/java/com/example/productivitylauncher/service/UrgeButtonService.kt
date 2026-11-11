package com.example.productivitylauncher.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.example.productivitylauncher.MainActivity
import com.example.productivitylauncher.data.URGE_LOCK_MINUTES

/**
 * A small red button drawn over every app. Tap it once and it asks for a second tap,
 * then the launcher opens with the red-button lock (a timer with only Call and Message).
 * It needs the "Display over other apps" permission, and it is hidden while the launcher itself is on screen.
 */
class UrgeButtonService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var button: TextView? = null
    private var armed = false

    private val disarm = Runnable { setArmed(false) }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_STOP || !Settings.canDrawOverlays(this)) {
            if (intent?.action == ACTION_STOP && Settings.canDrawOverlays(this) && lockRunning()) return START_NOT_STICKY
            removeButton()
            stopGuard()
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent.action == ACTION_GUARD) { startGuard(); return START_NOT_STICKY }
        if (button == null) addButton()
        button?.visibility = if (intent.action == ACTION_HIDE) View.GONE else View.VISIBLE
        if (intent.action == ACTION_HIDE) setArmed(false)
        return START_NOT_STICKY
    }

    // ---------------------------------------------------------------- strict red button lock

    private var guardView: View? = null
    private var guarding = false
    private var lastBounce = 0L
    private val guardTick = object : Runnable {
        override fun run() {
            if (!guarding) return
            if (!lockRunning()) { stopGuard(); return }
            val front = foregroundPackage()
            val now = System.currentTimeMillis()
            if (front != null && !isAllowed(front) && now - lastBounce > 800L) {
                lastBounce = now
                runCatching {
                    startActivity(
                        Intent(this@UrgeButtonService, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    )
                }
            }
            handler.postDelayed(this, 400L)
        }
    }

    private fun lockRunning() = getSharedPreferences("launcher", MODE_PRIVATE).getLong("urge_end", 0L) > System.currentTimeMillis()

    /**
     * While the lock runs: dismiss notifications, keep a tiny invisible window (which lets this service bring the launcher
     * back from the background) and watch which app is in front. Anything but the launcher, the phone and messages is sent back.
     */
    private fun startGuard() {
        if (!lockRunning()) return
        NotificationGuardService.instance?.dismissNow()
        if (guardView == null) {
            val v = View(this)
            val params = WindowManager.LayoutParams(
                1, 1, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT,
            ).apply { gravity = Gravity.TOP or Gravity.START }
            val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            runCatching { wm.addView(v, params) }.onSuccess { windowManager = wm; guardView = v }
        }
        if (!guarding) { guarding = true; handler.post(guardTick) }
    }

    private fun stopGuard() {
        guarding = false
        handler.removeCallbacks(guardTick)
        val v = guardView
        if (v != null) runCatching { windowManager?.removeView(v) }
        guardView = null
    }

    private fun foregroundPackage(): String? = runCatching {
        val um = getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val end = System.currentTimeMillis()
        val events = um.queryEvents(end - 10_000L, end)
        val e = android.app.usage.UsageEvents.Event()
        var pkg: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            if (e.eventType == android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED) pkg = e.packageName
        }
        pkg
    }.getOrNull()

    private fun resolvedPackages(): Set<String> {
        val out = mutableSetOf<String>()
        val pm = packageManager
        runCatching { pm.resolveActivity(Intent(Intent.ACTION_DIAL), 0)?.activityInfo?.packageName?.let { out += it } }
        runCatching { pm.resolveActivity(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING), 0)?.activityInfo?.packageName?.let { out += it } }
        runCatching { pm.resolveActivity(Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("smsto:")), 0)?.activityInfo?.packageName?.let { out += it } }
        return out
    }

    private val allowed: Set<String> by lazy { resolvedPackages() + setOf(packageName, "com.android.systemui", "android") }

    private fun isAllowed(pkg: String): Boolean =
        pkg in allowed || pkg.contains("incallui") || pkg.contains("telecom") || pkg.contains("dialer") ||
            pkg.contains("inputmethod") || pkg.contains("keyboard")

    override fun onDestroy() {
        stopGuard()
        removeButton()
        super.onDestroy()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun addButton() {
        val tv = TextView(this).apply {
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            contentDescription = "Red button. Tap twice to lock the phone for $URGE_LOCK_MINUTES minutes."
            setOnClickListener { onTap() }
        }
        button = tv
        setArmed(false)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            y = dp(120)
        }
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        runCatching { wm.addView(tv, params) }.onSuccess { windowManager = wm }.onFailure { button = null }
    }

    private fun setArmed(on: Boolean) {
        armed = on
        handler.removeCallbacks(disarm)
        val b = button ?: return
        if (on) {
            b.text = "Tap again to lock $URGE_LOCK_MINUTES min"
            b.textSize = 13f
            b.minimumHeight = dp(44)
            b.minimumWidth = dp(44)
            b.setPadding(dp(16), dp(10), dp(16), dp(10))
            b.background = GradientDrawable().apply { setColor(Color.parseColor("#C0392B")); cornerRadius = dp(22).toFloat() }
            b.alpha = 1f
            handler.postDelayed(disarm, 3000L)
        } else {
            b.text = ""
            b.minimumHeight = dp(40)
            b.minimumWidth = dp(40)
            b.setPadding(0, 0, 0, 0)
            // A round red dot tucked against the right edge, a little see-through so it stays out of the way.
            b.background = GradientDrawable().apply { setColor(Color.parseColor("#C0392B")); shape = GradientDrawable.OVAL }
            b.alpha = 0.8f
        }
    }

    private fun onTap() {
        if (!armed) { setArmed(true); return }
        setArmed(false)
        val prefs = getSharedPreferences("launcher", MODE_PRIVATE)
        if (prefs.getLong("urge_end", 0L) <= System.currentTimeMillis()) {
            prefs.edit().putLong("urge_end", System.currentTimeMillis() + URGE_LOCK_MINUTES * 60_000L).apply()
        }
        val i = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        runCatching { startActivity(i) }
    }

    private fun removeButton() {
        handler.removeCallbacks(disarm)
        val b = button
        if (b != null) runCatching { windowManager?.removeView(b) }
        button = null
    }

    companion object {
        private const val ACTION_HIDE = "hide"
        private const val ACTION_STOP = "stop"
        private const val ACTION_GUARD = "guard"

        /** Starts the strict lock watch for the red button timer (needs Display over other apps; Usage access makes it work). */
        fun guard(context: Context) {
            runCatching { context.startService(Intent(context, UrgeButtonService::class.java).setAction(ACTION_GUARD)) }
        }

        /** Shows the button over other apps (when the setting is on and the permission is allowed). */
        fun show(context: Context) {
            runCatching { context.startService(Intent(context, UrgeButtonService::class.java)) }
        }

        /** Keeps the service alive but hides the button, used while the launcher itself is on screen. */
        fun hide(context: Context) {
            runCatching { context.startService(Intent(context, UrgeButtonService::class.java).setAction(ACTION_HIDE)) }
        }

        fun stop(context: Context) {
            runCatching { context.startService(Intent(context, UrgeButtonService::class.java).setAction(ACTION_STOP)) }
        }
    }
}
