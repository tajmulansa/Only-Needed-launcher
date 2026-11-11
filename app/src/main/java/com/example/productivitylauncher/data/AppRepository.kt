package com.example.productivitylauncher.data

import android.content.Intent
import android.content.pm.PackageManager

/**
 * Returns every app that has a launcher icon, sorted by name, without this app itself.
 * The manifest `<queries>` block is what allows this to work on Android 11 and newer.
 */
fun loadLaunchableApps(pm: PackageManager, ownPackage: String): List<AppEntry> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    @Suppress("DEPRECATION")
    val resolved = pm.queryIntentActivities(intent, 0)

    return resolved
        .filter { it.activityInfo.packageName != ownPackage }
        .map {
            AppEntry(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                activityName = it.activityInfo.name,
            )
        }
        .sortedBy { it.label.lowercase() }
}
