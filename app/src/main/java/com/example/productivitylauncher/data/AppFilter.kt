package com.example.productivitylauncher.data

/**
 * Case-insensitive "contains" search over app labels. Pure Kotlin on purpose,
 * so it can be unit tested without Android (see AppFilterTest).
 */
fun filterApps(apps: List<AppEntry>, query: String): List<AppEntry> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return apps
    return apps.filter { it.label.lowercase().contains(q) }
}
