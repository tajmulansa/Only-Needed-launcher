package com.example.productivitylauncher.data

/** One launchable app, as shown in the app list. */
data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
)
