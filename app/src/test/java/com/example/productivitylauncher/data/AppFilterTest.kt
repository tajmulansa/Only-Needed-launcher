package com.example.productivitylauncher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFilterTest {

    private val apps = listOf(
        AppEntry("Notes", "com.example.notes", "com.example.notes.Main"),
        AppEntry("Calendar", "com.example.calendar", "com.example.calendar.Main"),
        AppEntry("Camera", "com.example.camera", "com.example.camera.Main"),
    )

    @Test
    fun blankQueryReturnsEverything() {
        assertEquals(apps, filterApps(apps, "   "))
    }

    @Test
    fun matchesCaseInsensitively() {
        val labels = filterApps(apps, "CA").map { it.label }
        assertEquals(listOf("Calendar", "Camera"), labels)
    }

    @Test
    fun noMatchReturnsEmptyList() {
        assertTrue(filterApps(apps, "zzz").isEmpty())
    }
}
