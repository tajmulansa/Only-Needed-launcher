package com.example.productivitylauncher.ui

/** Every full screen the launcher can show. */
sealed interface Route {
    data object Main : Route
    /** Choose how long to use an app that is not on Home. */
    data class Time(val pkg: String) : Route
    data class TimesUp(val pkg: String) : Route
    data object Settings : Route
    data object HomePicker : Route
    data object Onboarding : Route
}

/** Passed to screens so they can move around without knowing about each other. */
class Nav(val go: (Route) -> Unit, val back: () -> Unit)
