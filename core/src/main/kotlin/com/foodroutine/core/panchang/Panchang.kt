package com.foodroutine.core.panchang

import com.foodroutine.core.astro.Astro
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Waxing / waning half of the lunar month. */
enum class Paksha { SHUKLA, KRISHNA }

/**
 * The 30 tithis of a lunar month. [index] is 1..30 counted from Shukla
 * Pratipada; [number] is the 1..15 number within the paksha.
 */
enum class Tithi(val index: Int, val paksha: Paksha, val displayName: String) {
    SHUKLA_PRATIPADA(1, Paksha.SHUKLA, "Shukla Pratipada"),
    SHUKLA_DWITIYA(2, Paksha.SHUKLA, "Shukla Dwitiya"),
    SHUKLA_TRITIYA(3, Paksha.SHUKLA, "Shukla Tritiya"),
    SHUKLA_CHATURTHI(4, Paksha.SHUKLA, "Shukla Chaturthi"),
    SHUKLA_PANCHAMI(5, Paksha.SHUKLA, "Shukla Panchami"),
    SHUKLA_SHASHTHI(6, Paksha.SHUKLA, "Shukla Shashthi"),
    SHUKLA_SAPTAMI(7, Paksha.SHUKLA, "Shukla Saptami"),
    SHUKLA_ASHTAMI(8, Paksha.SHUKLA, "Shukla Ashtami"),
    SHUKLA_NAVAMI(9, Paksha.SHUKLA, "Shukla Navami"),
    SHUKLA_DASHAMI(10, Paksha.SHUKLA, "Shukla Dashami"),
    SHUKLA_EKADASHI(11, Paksha.SHUKLA, "Shukla Ekadashi"),
    SHUKLA_DWADASHI(12, Paksha.SHUKLA, "Shukla Dwadashi"),
    SHUKLA_TRAYODASHI(13, Paksha.SHUKLA, "Shukla Trayodashi"),
    SHUKLA_CHATURDASHI(14, Paksha.SHUKLA, "Shukla Chaturdashi"),
    PURNIMA(15, Paksha.SHUKLA, "Purnima"),
    KRISHNA_PRATIPADA(16, Paksha.KRISHNA, "Krishna Pratipada"),
    KRISHNA_DWITIYA(17, Paksha.KRISHNA, "Krishna Dwitiya"),
    KRISHNA_TRITIYA(18, Paksha.KRISHNA, "Krishna Tritiya"),
    KRISHNA_CHATURTHI(19, Paksha.KRISHNA, "Krishna Chaturthi"),
    KRISHNA_PANCHAMI(20, Paksha.KRISHNA, "Krishna Panchami"),
    KRISHNA_SHASHTHI(21, Paksha.KRISHNA, "Krishna Shashthi"),
    KRISHNA_SAPTAMI(22, Paksha.KRISHNA, "Krishna Saptami"),
    KRISHNA_ASHTAMI(23, Paksha.KRISHNA, "Krishna Ashtami"),
    KRISHNA_NAVAMI(24, Paksha.KRISHNA, "Krishna Navami"),
    KRISHNA_DASHAMI(25, Paksha.KRISHNA, "Krishna Dashami"),
    KRISHNA_EKADASHI(26, Paksha.KRISHNA, "Krishna Ekadashi"),
    KRISHNA_DWADASHI(27, Paksha.KRISHNA, "Krishna Dwadashi"),
    KRISHNA_TRAYODASHI(28, Paksha.KRISHNA, "Krishna Trayodashi"),
    KRISHNA_CHATURDASHI(29, Paksha.KRISHNA, "Krishna Chaturdashi"),
    AMAVASYA(30, Paksha.KRISHNA, "Amavasya");

    val number: Int get() = if (index <= 15) index else index - 15
    val isEkadashi: Boolean get() = number == 11
    val isPurnima: Boolean get() = this == PURNIMA
    val isAmavasya: Boolean get() = this == AMAVASYA

    companion object {
        fun fromIndex(index: Int): Tithi = entries[(index - 1).mod(30)]
    }
}

/** Amanta (new-moon-ending) lunar months. */
enum class LunarMonth(val displayName: String) {
    CHAITRA("Chaitra"), VAISHAKHA("Vaishakha"), JYESHTHA("Jyeshtha"),
    ASHADHA("Ashadha"), SHRAVANA("Shravana"), BHADRAPADA("Bhadrapada"),
    ASHVINA("Ashvina"), KARTIKA("Kartika"), MARGASHIRSHA("Margashirsha"),
    PAUSHA("Pausha"), MAGHA("Magha"), PHALGUNA("Phalguna")
}

/** Geographic observer used for sunrise-based tithi assignment. */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val zone: ZoneId,
    val name: String = ""
) {
    companion object {
        /** Central India default, a reasonable pan-India approximation. */
        val DEFAULT = Location(23.18, 79.98, ZoneId.of("Asia/Kolkata"), "India (central)")
    }
}

/** Panchang details of one civil day. */
data class PanchangDay(
    val date: LocalDate,
    /** Tithi prevailing at local sunrise - the tithi "of the day". */
    val tithi: Tithi,
    /** Tithi prevailing at the midnight ending this day (for Janmashtami). */
    val tithiAtMidnight: Tithi,
    val lunarMonth: LunarMonth,
    val sunriseTime: LocalTime
)

object PanchangCalculator {

    /** Tithi at an arbitrary instant. */
    fun tithiAt(instant: Instant): Tithi {
        val index = (Astro.elongation(instant) / 12.0).toInt() + 1
        return Tithi.fromIndex(index)
    }

    /**
     * Amanta lunar month containing [instant]: named from the sidereal rashi
     * occupied by the Sun at the new moon that began the month
     * (Mina -> Chaitra, Mesha -> Vaishakha, ..., Karka -> Shravana, ...).
     */
    fun lunarMonthAt(instant: Instant): LunarMonth {
        val newMoon = Astro.newMoonBefore(instant)
        val rashi = (Astro.sunSiderealLongitude(newMoon) / 30.0).toInt() // 0 = Mesha
        // Mina (11) -> Chaitra (0), Mesha (0) -> Vaishakha (1), ...
        return LunarMonth.entries[(rashi + 1).mod(12)]
    }

    fun panchangFor(date: LocalDate, location: Location = Location.DEFAULT): PanchangDay {
        val sunrise = Astro.sunrise(date, location.latitude, location.longitude, location.zone)
        val sunriseInstant = sunrise.toInstant()
        val midnight = date.plusDays(1).atStartOfDay(location.zone).toInstant()
        return PanchangDay(
            date = date,
            tithi = tithiAt(sunriseInstant),
            tithiAtMidnight = tithiAt(midnight),
            lunarMonth = lunarMonthAt(sunriseInstant),
            sunriseTime = sunrise.toLocalTime()
        )
    }
}
