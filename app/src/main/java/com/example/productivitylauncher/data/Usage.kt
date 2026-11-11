package com.example.productivitylauncher.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process

/** Reads how long apps were used. Needs the "Usage access" special permission, granted by the user. */
class UsageReader(private val context: Context) {
    private val manager: UsageStatsManager? =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

    fun hasAccess(): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /** Foreground milliseconds per package between [start] and [end]. */
    fun perPackage(start: Long, end: Long): Map<String, Long> {
        val m = manager ?: return emptyMap()
        return runCatching {
            m.queryAndAggregateUsageStats(start, end).mapValues { it.value.totalTimeInForeground }
        }.getOrDefault(emptyMap())
    }

    /** Milliseconds any other app was in the foreground during the last [windowMs]. */
    @Suppress("DEPRECATION")
    fun activeMsInLast(windowMs: Long): Long {
        val m = manager ?: return 0L
        val end = System.currentTimeMillis()
        val start = end - windowMs
        return runCatching {
            val events = m.queryEvents(start, end)
            val event = UsageEvents.Event()
            val opened = HashMap<String, Long>()
            var total = 0L
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName ?: continue
                if (pkg == context.packageName) continue
                when (event.eventType) {
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> opened[pkg] = event.timeStamp
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val s = opened.remove(pkg)
                        if (s != null) total += event.timeStamp - s
                    }
                }
            }
            opened.values.forEach { total += end - it }
            total
        }.getOrDefault(0L)
    }
}
