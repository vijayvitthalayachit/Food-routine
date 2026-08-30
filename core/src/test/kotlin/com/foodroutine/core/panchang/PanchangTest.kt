package com.foodroutine.core.panchang

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PanchangTest {

    private val calendar = FastingCalendar()

    /**
     * August 2025 has two Ekadashi observances; depending on the smarta
     * sunrise rule they fall on Aug 4/5 and Aug 18/19. Assert we flag
     * exactly two days and that they land inside those windows.
     */
    @Test
    fun twoEkadashisInAugust2025() {
        val ekadashis = (1..31)
            .map { LocalDate.of(2025, 8, it) }
            .filter {
                PanchangCalculator.panchangFor(it).tithi.isEkadashi
            }
        assertEquals("found $ekadashis", 2, ekadashis.size)
        val allowed = setOf(
            LocalDate.of(2025, 8, 4), LocalDate.of(2025, 8, 5),
            LocalDate.of(2025, 8, 18), LocalDate.of(2025, 8, 19)
        )
        assertTrue("found $ekadashis", ekadashis.all { it in allowed })
        assertTrue(
            "expected ~15 days apart: $ekadashis",
            ekadashis[1].toEpochDay() - ekadashis[0].toEpochDay() in 13..16
        )
    }

    /** Janmashtami 2025 was observed on 16 August 2025. */
    @Test
    fun janmashtami2025() {
        assertEquals(
            FastingReason.JANMASHTAMI,
            calendar.fastingReasonFor(LocalDate.of(2025, 8, 16))
        )
    }

    /** The total lunar eclipse of 7 Sep 2025 must be a Grahana fasting day. */
    @Test
    fun grahanaDayIsFasting() {
        assertEquals(
            FastingReason.GRAHANA,
            calendar.fastingReasonFor(LocalDate.of(2025, 9, 7))
        )
    }

    @Test
    fun customFastingDayIsHonoured() {
        val custom = LocalDate.of(2025, 10, 2)
        val cal = FastingCalendar(customFastingDates = setOf(custom))
        assertEquals(FastingReason.CUSTOM, cal.fastingReasonFor(custom))
        // Adjacent ordinary day should not be a fast.
        val next = custom.plusDays(1)
        val reason = cal.fastingReasonFor(next)
        assertTrue(reason == null || reason != FastingReason.CUSTOM)
    }

    /** Tithis must advance monotonically (mod 30) at one-day sampling. */
    @Test
    fun tithiSequenceIsMonotonic() {
        var prev = PanchangCalculator.panchangFor(LocalDate.of(2025, 8, 1)).tithi.index
        for (d in 2..31) {
            val cur = PanchangCalculator.panchangFor(LocalDate.of(2025, 8, d)).tithi.index
            val step = (cur - prev).mod(30)
            assertTrue("step from $prev to $cur on Aug $d", step in 0..2)
            prev = cur
        }
    }

    /** The amanta month around Janmashtami 2025 must be Shravana. */
    @Test
    fun lunarMonthAtJanmashtami2025IsShravana() {
        val p = PanchangCalculator.panchangFor(LocalDate.of(2025, 8, 16))
        assertEquals(LunarMonth.SHRAVANA, p.lunarMonth)
    }
}
