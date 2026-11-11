package com.example.productivitylauncher.ui.settings

/** The privacy policy, shown in setup and in Settings. Keep docs/PRIVACY.md in sync with this. */
val PRIVACY_SECTIONS: List<Pair<String, String>> = listOf(
    "Short version" to "Everything this launcher does happens on your phone. It has no internet permission, so it cannot send your data anywhere. There are no accounts, ads, analytics or trackers.",
    "What is stored" to "Your six Home apps and your settings are saved in this app's private storage on your phone. Delete all my data in Settings erases them. Uninstalling the app erases them too.",
    "Usage access" to "If you allow it, the launcher reads how long apps were used on this phone to show today's screen time on Home. It is never uploaded or shared.",
    "Display over other apps" to "Used only to show the small countdown timer over an app you chose a time for, and the red button over other apps if you turn it on.",
    "Notification access" to "Used only to dismiss notifications while the red button lock runs, so there is nothing to tap that opens an app. The launcher checks which app posted a notification and nothing else. Notification content is never read, stored or shared.",
    "Installed apps" to "The launcher lists the apps installed on your phone so you can open them. The list stays on your phone.",
    "Open source and contact" to "The full source code is public so anyone can check these claims. Questions or concerns: open an issue in the project's repository.",
)
