package com.example.productivitylauncher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LogicTest {
    private val apps = listOf(AppEntry("Camera", "com.cam", "com.cam.Main"), AppEntry("Clock", "com.clock", "com.clock.Main"))

    @Test fun exactNameIgnoresCaseAndEdgeSpaces() {
        assertEquals(listOf("Camera"), exactNameMatches(apps, "  camera ").map { it.label })
    }

    @Test fun partialNameFindsNothing() {
        assertEquals(emptyList<AppEntry>(), exactNameMatches(apps, "cam"))
    }

    @Test fun formatMinutesShowsHoursAndMinutes() {
        assertEquals("1h 5m", formatMinutes(65))
        assertEquals("9m", formatMinutes(9))
    }

    @Test fun clockCountsDownAsMinutesAndSeconds() {
        assertEquals("05:00", formatClock(300_000))
        assertEquals("00:01", formatClock(1))
    }
}
