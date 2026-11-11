package com.example.productivitylauncher.service

import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * Runs only when the user allows "Notification access". It reads nothing but which app posted, and keeps no copy.
 * While the red button lock runs, every notification is dismissed, so there is nothing to tap that opens an app.
 */
class NotificationGuardService : NotificationListenerService() {
    private fun prefs() = getSharedPreferences("launcher", Context.MODE_PRIVATE)

    private fun shouldDismiss(sbn: StatusBarNotification): Boolean {
        if (sbn.packageName == packageName) return false
        return prefs().getLong("urge_end", 0L) > System.currentTimeMillis()
    }

    override fun onListenerConnected() {
        instance = this
        runCatching { activeNotifications?.forEach { if (shouldDismiss(it)) cancelNotification(it.key) } }
    }

    override fun onListenerDisconnected() {
        if (instance === this) instance = null
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (shouldDismiss(sbn)) cancelNotification(sbn.key)
    }

    /** Dismisses what is already in the shade, used when the red button lock starts. */
    fun dismissNow() {
        runCatching { activeNotifications?.forEach { if (shouldDismiss(it)) cancelNotification(it.key) } }
    }

    companion object {
        @Volatile var instance: NotificationGuardService? = null
    }
}
