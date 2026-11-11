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
import android.widget.LinearLayout
import android.widget.TextView
import com.example.productivitylauncher.MainActivity
import com.example.productivitylauncher.data.formatClock

/**
 * Draws the small floating timer over a gated app while a session runs.
 * It needs the "Display over other apps" permission. Without it the session still ends,
 * the user just sees "Time's up" the next time they return to the launcher.
 */
class SessionOverlayService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var pill: View? = null
    private var timeView: TextView? = null
    private var endAt = 0L

    private val tick = object : Runnable {
        override fun run() {
            val left = endAt - System.currentTimeMillis()
            if (left <= 0) {
                finishSession()
            } else {
                timeView?.text = formatClock(left)
                handler.postDelayed(this, 1000L)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == ACTION_STOP || !Settings.canDrawOverlays(this)) {
            removePill()
            stopSelf()
            return START_NOT_STICKY
        }
        endAt = intent.getLongExtra(EXTRA_END, 0L)
        removePill()
        showPill(intent.getStringExtra(EXTRA_LABEL) ?: "", intent.getBooleanExtra(EXTRA_DARK, false))
        handler.removeCallbacks(tick)
        handler.post(tick)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        removePill()
        super.onDestroy()
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun showPill(label: String, dark: Boolean) {
        val bg = if (dark) Color.parseColor("#EDEAE3") else Color.parseColor("#363431")
        val fg = if (dark) Color.parseColor("#1B1A18") else Color.parseColor("#FFFFFF")

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(18), dp(8), dp(8), dp(8))
            background = GradientDrawable().apply {
                setColor(bg)
                cornerRadius = dp(40).toFloat()
            }
        }
        val labelView = TextView(this).apply {
            text = label
            setTextColor(fg)
            alpha = 0.75f
            textSize = 13f
            setPadding(0, 0, dp(12), 0)
        }
        val time = TextView(this).apply {
            text = formatClock(endAt - System.currentTimeMillis())
            setTextColor(fg)
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, dp(12), 0)
        }
        val end = TextView(this).apply {
            text = "End"
            setTextColor(fg)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            minimumHeight = dp(44)
            minimumWidth = dp(56)
            setPadding(dp(14), 0, dp(14), 0)
            background = GradientDrawable().apply {
                setColor((fg and 0x00FFFFFF) or 0x2E000000)
                cornerRadius = dp(40).toFloat()
            }
            setOnClickListener { endEarly() }
        }
        box.addView(labelView)
        box.addView(time)
        box.addView(end)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = dp(48)
        }
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        runCatching { wm.addView(box, params) }.onSuccess {
            windowManager = wm
            pill = box
            timeView = time
        }
    }

    private fun removePill() {
        val p = pill
        if (p != null) runCatching { windowManager?.removeView(p) }
        pill = null
        timeView = null
    }

    private fun endEarly() {
        getSharedPreferences("launcher", MODE_PRIVATE).edit()
            .putLong("session_end", System.currentTimeMillis())
            .putBoolean("session_notified", true)
            .apply()
        bringLauncherBack(timesUp = false)
        removePill()
        stopSelf()
    }

    private fun finishSession() {
        bringLauncherBack(timesUp = true)
        removePill()
        stopSelf()
    }

    private fun bringLauncherBack(timesUp: Boolean) {
        val i = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .putExtra(EXTRA_TIMES_UP, timesUp)
        runCatching { startActivity(i) }
    }

    companion object {
        const val EXTRA_LABEL = "label"
        const val EXTRA_END = "end"
        const val EXTRA_DARK = "dark"
        const val EXTRA_TIMES_UP = "times_up"
        const val ACTION_STOP = "stop"

        fun start(context: Context, label: String, endAt: Long, dark: Boolean) {
            val i = Intent(context, SessionOverlayService::class.java)
                .putExtra(EXTRA_LABEL, label)
                .putExtra(EXTRA_END, endAt)
                .putExtra(EXTRA_DARK, dark)
            runCatching { context.startService(i) }
        }

        fun stop(context: Context) {
            val i = Intent(context, SessionOverlayService::class.java).setAction(ACTION_STOP)
            runCatching { context.startService(i) }
        }
    }
}
