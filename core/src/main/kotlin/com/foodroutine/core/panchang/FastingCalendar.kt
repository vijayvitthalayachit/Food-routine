package com.foodroutine.core.panchang

import java.time.LocalDate

/** Why a given day is a fasting day (no meal plan). */
enum class FastingReason(val displayName: String) {
    EKADASHI("Ekadashi"),
    JANMASHTAMI("Janmashtami"),
    GRAHANA("Grahana (eclipse)"),
    CUSTOM("Custom fast")
}

/**
 * Bundled Grahana (solar + lunar eclipse) dates. Dates are the local Indian
 * calendar date on which the eclipse occurs. Users can always add further
 * Grahana observances as custom fasting days; this list is a convenience.
 */
object EclipseDates {
    val dates: Set<LocalDate> = setOf(
        // 2025
        LocalDate.of(2025, 3, 14),  // total lunar eclipse
        LocalDate.of(2025, 3, 29),  // partial solar eclipse
        LocalDate.of(2025, 9, 7),   // total lunar eclipse (visible in India)
        LocalDate.of(2025, 9, 21),  // partial solar eclipse
        // 2026
        LocalDate.of(2026, 2, 17),  // annular solar eclipse
        LocalDate.of(2026, 3, 3),   // total lunar eclipse (visible in India)
        LocalDate.of(2026, 8, 12),  // total solar eclipse
        LocalDate.of(2026, 8, 28),  // partial lunar eclipse
        // 2027
        LocalDate.of(2027, 2, 6),   // annular solar eclipse
        LocalDate.of(2027, 8, 2),   // total solar eclipse (visible in India)
        // 2028
        LocalDate.of(2028, 1, 12),  // partial lunar eclipse
        LocalDate.of(2028, 1, 26),  // annular solar eclipse
        LocalDate.of(2028, 7, 6),   // partial lunar eclipse
        LocalDate.of(2028, 7, 22),  // total solar eclipse
        LocalDate.of(2028, 12, 31)  // total lunar eclipse (visible in India)
    )
}

/**
 * Decides whether a day is a fasting day: Ekadashi, Janmashtami, Grahana or
 * a user-defined custom fast. Fasting days get no meal plan.
 */
class FastingCalendar(
    private val location: Location = Location.DEFAULT,
    private val customFastingDates: Set<LocalDate> = emptySet(),
    private val extraGrahanaDates: Set<LocalDate> = emptySet()
) {

    fun fastingReasonFor(date: LocalDate): FastingReason? {
        if (date in customFastingDates) return FastingReason.CUSTOM
        if (date in EclipseDates.dates || date in extraGrahanaDates) return FastingReason.GRAHANA
        val panchang = PanchangCalculator.panchangFor(date, location)
        if (isJanmashtami(panchang)) return FastingReason.JANMASHTAMI
        if (panchang.tithi.isEkadashi) return FastingReason.EKADASHI
        return null
    }

    fun isFastingDay(date: LocalDate): Boolean = fastingReasonFor(date) != null

    /**
     * Janmashtami: Krishna Ashtami of the amanta month of Shravana, decided
     * by the tithi prevailing at midnight (Nishita), per common observance.
     */
    private fun isJanmashtami(panchang: PanchangDay): Boolean {
        if (panchang.lunarMonth != LunarMonth.SHRAVANA) return false
        return panchang.tithiAtMidnight == Tithi.KRISHNA_ASHTAMI ||
            (panchang.tithi == Tithi.KRISHNA_ASHTAMI &&
                panchang.tithiAtMidnight.index > Tithi.KRISHNA_ASHTAMI.index)
    }
}
