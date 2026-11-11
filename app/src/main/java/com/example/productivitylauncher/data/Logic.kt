package com.example.productivitylauncher.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/*
 * Pure Kotlin helpers. Nothing here touches Android, so it is unit tested (see LogicTest).
 */

const val DAY_MS = 24L * 60 * 60 * 1000

/** An app outside Home is found only by typing its full name (any capitals, spaces at the ends ignored). */
fun exactNameMatches(apps: List<AppEntry>, query: String): List<AppEntry> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return apps.filter { it.label.trim().equals(q, ignoreCase = true) }
}

fun formatMinutes(totalMinutes: Long): String {
    val m = totalMinutes.coerceAtLeast(0)
    val h = m / 60
    val r = m % 60
    return if (h > 0) "${h}h ${r}m" else "${r}m"
}

fun formatMillis(ms: Long): String = formatMinutes(ms / 60_000)

/** mm:ss for countdowns. */
fun formatClock(ms: Long): String {
    val total = (ms.coerceAtLeast(0) + 999) / 1000
    return "%02d:%02d".format(Locale.US, total / 60, total % 60)
}

fun dayKey(millis: Long = System.currentTimeMillis()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun hourOf(millis: Long = System.currentTimeMillis()): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return c.get(Calendar.HOUR_OF_DAY)
}

/** Alphabetical section header for an app label ("#" for anything that is not a letter). */
/** The first whole character of a label (an emoji is two Chars, so never split it). */
private fun firstSymbol(label: String): String {
    val s = label.trim()
    if (s.isEmpty()) return ""
    return String(Character.toChars(s.codePointAt(0)))
}

/** The list section for an app: A to Z, with accents dropped ("École" is E), or # for everything else. */
fun sectionOf(label: String): String {
    val first = firstSymbol(label)
    if (first.isEmpty()) return "#"
    val base = java.text.Normalizer.normalize(first, java.text.Normalizer.Form.NFD).firstOrNull()?.uppercaseChar() ?: return "#"
    return if (base in 'A'..'Z') base.toString() else "#"
}

/** The big letter on an app badge. Emoji and non-Latin first characters stay whole. */
fun letterOf(label: String): String {
    val first = firstSymbol(label)
    return if (first.isEmpty()) "?" else first.uppercase()
}

/** 7:00 AM style text for minutes after midnight. */
fun formatTimeOfDay(min: Int): String {
    val h = (min / 60) % 24
    val m = min % 60
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "%d:%02d %s".format(h12, m, if (h < 12) "AM" else "PM")
}
