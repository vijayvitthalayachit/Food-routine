package com.foodroutine.core.astro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.abs

class AstroTest {

    /** Elongation should be ~0 deg at known new-moon instants. */
    @Test
    fun elongationNearZeroAtKnownNewMoons() {
        val newMoons = listOf(
            "2025-03-29T10:58:00Z",
            "2025-07-24T19:11:00Z",
            "2025-09-21T19:54:00Z",
            "2026-08-12T17:37:00Z"
        )
        for (nm in newMoons) {
            val e = Astro.elongation(Instant.parse(nm))
            val dist = minOf(e, 360.0 - e)
            assertTrue("elongation at $nm was $e", dist < 0.5)
        }
    }

    /** Elongation should be ~180 deg at known full-moon instants. */
    @Test
    fun elongationNear180AtKnownFullMoons() {
        val fullMoons = listOf(
            "2025-09-07T18:09:00Z", // total lunar eclipse
            "2025-03-14T06:55:00Z"  // total lunar eclipse
        )
        for (fm in fullMoons) {
            val e = Astro.elongation(Instant.parse(fm))
            assertTrue("elongation at $fm was $e", abs(e - 180.0) < 0.5)
        }
    }

    @Test
    fun newMoonSearchFindsConjunction() {
        val found = Astro.newMoonBefore(Instant.parse("2025-08-10T00:00:00Z"))
        val expected = Instant.parse("2025-07-24T19:11:00Z")
        val diffMinutes = abs(found.epochSecond - expected.epochSecond) / 60
        assertTrue("new moon found at $found", diffMinutes < 90)
    }

    @Test
    fun sunriseInIndiaIsMorning() {
        val rise = Astro.sunrise(
            LocalDate.of(2025, 8, 16), 23.18, 79.98, ZoneId.of("Asia/Kolkata")
        )
        assertEquals(LocalDate.of(2025, 8, 16), rise.toLocalDate())
        assertTrue("sunrise hour was ${rise.hour}", rise.hour in 5..7)
    }
}
