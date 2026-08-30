package com.foodroutine.core.astro

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.asin
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

/**
 * Compact astronomical routines (based on Jean Meeus, "Astronomical
 * Algorithms") sufficient for Panchang work: apparent geocentric ecliptic
 * longitudes of the Sun and the Moon, lunar phase angle (elongation),
 * new-moon search, an approximate Lahiri ayanamsa and NOAA-style
 * sunrise/sunset. Accuracy of the elongation is a few arc-minutes, i.e.
 * tithi boundaries are correct to within a couple of minutes of time.
 */
object Astro {

    private fun rad(deg: Double) = Math.toRadians(deg)

    private fun norm360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    /** Julian Day (UT) for an instant. */
    fun julianDay(instant: Instant): Double =
        instant.epochSecond / 86400.0 + instant.nano / 86.4e12 + 2440587.5

    /** Julian centuries since J2000.0. */
    private fun centuries(jd: Double) = (jd - 2451545.0) / 36525.0

    /** Apparent geocentric ecliptic longitude of the Sun, degrees. */
    fun sunLongitude(instant: Instant): Double {
        val t = centuries(julianDay(instant))
        val l0 = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val m = rad(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(m) +
            (0.019993 - 0.000101 * t) * sin(2 * m) +
            0.000289 * sin(3 * m)
        val trueLong = l0 + c
        val omega = rad(125.04 - 1934.136 * t)
        // Apparent longitude (nutation + aberration).
        return norm360(trueLong - 0.00569 - 0.00478 * sin(omega))
    }

    /** Apparent geocentric ecliptic longitude of the Moon, degrees. */
    fun moonLongitude(instant: Instant): Double {
        val t = centuries(julianDay(instant))
        val t2 = t * t
        val t3 = t2 * t
        val t4 = t3 * t

        // Mean elements (degrees).
        val lp = norm360(
            218.3164477 + 481267.88123421 * t - 0.0015786 * t2 +
                t3 / 538841.0 - t4 / 65194000.0
        )
        val d = rad(
            norm360(
                297.8501921 + 445267.1114034 * t - 0.0018819 * t2 +
                    t3 / 545868.0 - t4 / 113065000.0
            )
        )
        val m = rad(norm360(357.5291092 + 35999.0502909 * t - 0.0001536 * t2 + t3 / 24490000.0))
        val mp = rad(
            norm360(
                134.9633964 + 477198.8675055 * t + 0.0087414 * t2 +
                    t3 / 69699.0 - t4 / 14712000.0
            )
        )
        val f = rad(
            norm360(
                93.2720950 + 483202.0175233 * t - 0.0036539 * t2 -
                    t3 / 3526000.0 + t4 / 863310000.0
            )
        )
        val e = 1.0 - 0.002516 * t - 0.0000074 * t2
        val e2 = e * e

        // Principal periodic terms of the ELP truncation in Meeus ch. 47.
        // Each row: coefficient (1e-6 deg), multiples of D, M, M', F.
        var sum = 0.0
        val terms = arrayOf(
            doubleArrayOf(6288774.0, 0.0, 0.0, 1.0, 0.0),
            doubleArrayOf(1274027.0, 2.0, 0.0, -1.0, 0.0),
            doubleArrayOf(658314.0, 2.0, 0.0, 0.0, 0.0),
            doubleArrayOf(213618.0, 0.0, 0.0, 2.0, 0.0),
            doubleArrayOf(-185116.0, 0.0, 1.0, 0.0, 0.0),
            doubleArrayOf(-114332.0, 0.0, 0.0, 0.0, 2.0),
            doubleArrayOf(58793.0, 2.0, 0.0, -2.0, 0.0),
            doubleArrayOf(57066.0, 2.0, -1.0, -1.0, 0.0),
            doubleArrayOf(53322.0, 2.0, 0.0, 1.0, 0.0),
            doubleArrayOf(45758.0, 2.0, -1.0, 0.0, 0.0),
            doubleArrayOf(-40923.0, 0.0, 1.0, -1.0, 0.0),
            doubleArrayOf(-34720.0, 1.0, 0.0, 0.0, 0.0),
            doubleArrayOf(-30383.0, 0.0, 1.0, 1.0, 0.0),
            doubleArrayOf(15327.0, 2.0, 0.0, 0.0, -2.0),
            doubleArrayOf(-12528.0, 0.0, 0.0, 1.0, 2.0),
            doubleArrayOf(10980.0, 0.0, 0.0, 1.0, -2.0),
            doubleArrayOf(10675.0, 4.0, 0.0, -1.0, 0.0),
            doubleArrayOf(10034.0, 0.0, 0.0, 3.0, 0.0),
            doubleArrayOf(8548.0, 4.0, 0.0, -2.0, 0.0),
            doubleArrayOf(-7888.0, 2.0, 1.0, -1.0, 0.0),
            doubleArrayOf(-6766.0, 2.0, 1.0, 0.0, 0.0),
            doubleArrayOf(-5163.0, 1.0, 0.0, -1.0, 0.0),
            doubleArrayOf(4987.0, 1.0, 1.0, 0.0, 0.0),
            doubleArrayOf(4036.0, 2.0, -1.0, 1.0, 0.0),
            doubleArrayOf(3994.0, 2.0, 0.0, 2.0, 0.0),
            doubleArrayOf(3861.0, 4.0, 0.0, 0.0, 0.0),
            doubleArrayOf(3665.0, 2.0, 0.0, -3.0, 0.0),
            doubleArrayOf(-2689.0, 0.0, 1.0, -2.0, 0.0),
            doubleArrayOf(-2602.0, 2.0, 0.0, -1.0, 2.0),
            doubleArrayOf(2390.0, 2.0, -1.0, -2.0, 0.0),
            doubleArrayOf(-2348.0, 1.0, 0.0, 1.0, 0.0),
            doubleArrayOf(2236.0, 2.0, -2.0, 0.0, 0.0),
            doubleArrayOf(-2120.0, 0.0, 1.0, 2.0, 0.0),
            doubleArrayOf(-2069.0, 0.0, 2.0, 0.0, 0.0),
            doubleArrayOf(2048.0, 2.0, -2.0, -1.0, 0.0),
            doubleArrayOf(-1773.0, 2.0, 0.0, 1.0, -2.0),
            doubleArrayOf(-1595.0, 2.0, 0.0, 0.0, 2.0),
            doubleArrayOf(1215.0, 4.0, -1.0, -1.0, 0.0),
            doubleArrayOf(-1110.0, 0.0, 0.0, 2.0, 2.0),
            doubleArrayOf(-892.0, 3.0, 0.0, -1.0, 0.0),
            doubleArrayOf(-810.0, 2.0, 1.0, 1.0, 0.0),
            doubleArrayOf(759.0, 4.0, -1.0, -2.0, 0.0),
            doubleArrayOf(-713.0, 0.0, 2.0, -1.0, 0.0),
            doubleArrayOf(-700.0, 2.0, 2.0, -1.0, 0.0),
            doubleArrayOf(691.0, 2.0, 1.0, -2.0, 0.0),
            doubleArrayOf(596.0, 2.0, -1.0, 0.0, -2.0),
            doubleArrayOf(549.0, 4.0, 0.0, 1.0, 0.0),
            doubleArrayOf(537.0, 0.0, 0.0, 4.0, 0.0),
            doubleArrayOf(520.0, 4.0, -1.0, 0.0, 0.0),
            doubleArrayOf(-487.0, 1.0, 0.0, -2.0, 0.0)
        )
        for (row in terms) {
            var coeff = row[0]
            val mMult = row[2]
            if (mMult == 1.0 || mMult == -1.0) coeff *= e
            if (mMult == 2.0 || mMult == -2.0) coeff *= e2
            val arg = row[1] * d + row[2] * m + row[3] * mp + row[4] * f
            sum += coeff * sin(arg)
        }
        // Additive corrections (Venus, Jupiter, flattening terms).
        val a1 = rad(norm360(119.75 + 131.849 * t))
        val a2 = rad(norm360(53.09 + 479264.290 * t))
        sum += 3958.0 * sin(a1) + 1962.0 * sin(rad(lp) - f) + 318.0 * sin(a2)

        // Nutation in longitude (dominant term).
        val omega = rad(125.04 - 1934.136 * t)
        val nutation = -0.00461 * sin(omega)

        return norm360(lp + sum / 1e6 + nutation)
    }

    /** Angular distance Moon - Sun in [0, 360). Zero at new moon, 180 at full moon. */
    fun elongation(instant: Instant): Double =
        norm360(moonLongitude(instant) - sunLongitude(instant))

    /**
     * Approximate Lahiri (Chitrapaksha) ayanamsa in degrees for an instant.
     * Linear model anchored at J2000 (23.85 deg) with the precession rate;
     * good to a couple of arc-minutes over 1950-2100.
     */
    fun ayanamsa(instant: Instant): Double {
        val t = centuries(julianDay(instant))
        return 23.853 + 1.39697 * t
    }

    /** Sidereal (nirayana) longitude of the Sun, degrees. */
    fun sunSiderealLongitude(instant: Instant): Double =
        norm360(sunLongitude(instant) - ayanamsa(instant))

    /**
     * Instant of the new moon (conjunction, elongation = 0) at or before
     * [instant]. Binary search over the monotonically increasing elongation.
     */
    fun newMoonBefore(instant: Instant): Instant {
        // Step back until elongation "wraps" (goes from small to large),
        // which brackets the conjunction.
        var hi = instant
        var lo = instant.minusSeconds(86_400)
        while (elongation(lo) < elongation(hi)) {
            hi = lo
            lo = lo.minusSeconds(86_400)
        }
        // Bisect: elongation(lo) is just below 360, elongation(hi) just above 0.
        repeat(40) {
            val mid = lo.plusSeconds((hi.epochSecond - lo.epochSecond) / 2)
            if (elongation(mid) > 180.0) lo = mid else hi = mid
        }
        return hi
    }

    /**
     * NOAA-style sunrise for [date] at [latitude]/[longitude] in [zone].
     * Falls back to 06:00 local time in polar edge cases.
     */
    fun sunrise(date: LocalDate, latitude: Double, longitude: Double, zone: ZoneId): ZonedDateTime {
        val n = date.toEpochDay() - LocalDate.of(2000, 1, 1).toEpochDay() + 1
        val jStar = n.toDouble() - longitude / 360.0
        val m = norm360(357.5291 + 0.98560028 * jStar)
        val c = 1.9148 * sin(rad(m)) + 0.02 * sin(rad(2 * m)) + 0.0003 * sin(rad(3 * m))
        val lambda = norm360(m + c + 180.0 + 102.9372)
        val jTransit = 2451545.0 + jStar + 0.0053 * sin(rad(m)) - 0.0069 * sin(rad(2 * lambda))
        val delta = asin(sin(rad(lambda)) * sin(rad(23.4397)))
        val cosH = (sin(rad(-0.833)) - sin(rad(latitude)) * sin(delta)) /
            (cos(rad(latitude)) * cos(delta))
        if (cosH < -1.0 || cosH > 1.0) {
            return ZonedDateTime.of(LocalDateTime.of(date, LocalTime.of(6, 0)), zone)
        }
        val h = Math.toDegrees(acos(cosH))
        val jRise = jTransit - h / 360.0
        val epochSeconds = ((jRise - 2440587.5) * 86400.0)
        val rise = Instant.ofEpochSecond(epochSeconds.toLong()).atZone(zone)
        // The day-number approximation can land on the previous/next civil
        // day near the antimeridian; clamp to the requested date.
        return if (rise.toLocalDate() == date) rise
        else ZonedDateTime.of(LocalDateTime.of(date, rise.toLocalTime()), zone)
    }
}
