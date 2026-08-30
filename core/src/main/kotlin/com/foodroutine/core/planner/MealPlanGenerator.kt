package com.foodroutine.core.planner

import com.foodroutine.core.model.MealPlan
import com.foodroutine.core.model.MealType
import com.foodroutine.core.model.PlannedDay
import com.foodroutine.core.model.PlannedMeal
import com.foodroutine.core.model.Recipe
import com.foodroutine.core.panchang.FastingCalendar
import com.foodroutine.core.panchang.Location
import com.foodroutine.core.panchang.PanchangCalculator
import java.time.LocalDate

/**
 * Builds a paksha meal plan: starting from (or at) Pratipada and running
 * up to the next Purnima or Amavasya - at most 15 tithi days - assigning
 * three distinct meals per non-fasting day from the recipe pool.
 */
class MealPlanGenerator(
    private val location: Location = Location.DEFAULT,
    private val fastingCalendar: FastingCalendar = FastingCalendar(location)
) {

    /**
     * Generate a plan beginning at the first Pratipada on/after [from]
     * (set [snapToPratipada] false to start exactly at [from]) and ending at
     * the next Purnima/Amavasya, capped at 15 civil days beyond safety margin.
     *
     * Recipes are reused as little as possible: every slot prefers a recipe
     * not used anywhere in the plan yet; only when the pool for a meal type
     * is exhausted does it fall back to the least-recently-used recipe.
     */
    fun generate(
        from: LocalDate,
        recipes: List<Recipe>,
        snapToPratipada: Boolean = true
    ): MealPlan {
        require(recipes.isNotEmpty()) { "At least one recipe is required to build a meal plan" }

        val start = if (snapToPratipada) nextPratipada(from) else from
        val days = mutableListOf<PlannedDay>()
        val usedRecipeIds = mutableSetOf<Long>()
        val lastUsedAt = mutableMapOf<Long, Int>() // recipe id -> day ordinal
        var slotCounter = 0

        var date = start
        // A paksha spans at most 15 tithis; with kshaya/vriddhi tithis the
        // civil-day count stays within ~16. Hard stop prevents runaways.
        var guard = 0
        while (guard < 17) {
            val panchang = PanchangCalculator.panchangFor(date, location)
            val fasting = fastingCalendar.fastingReasonFor(date)

            if (fasting != null) {
                days += PlannedDay(date, panchang.tithi.displayName, fasting.displayName)
            } else {
                val meals = MealType.entries.map { mealType ->
                    val recipe = pickRecipe(mealType, recipes, usedRecipeIds, lastUsedAt, slotCounter)
                    usedRecipeIds += recipe.id
                    lastUsedAt[recipe.id] = slotCounter
                    slotCounter++
                    PlannedMeal(mealType, recipe)
                }
                days += PlannedDay(date, panchang.tithi.displayName, null, meals)
            }

            // The plan closes on the day whose sunrise tithi is Purnima or
            // Amavasya (the paksha's last tithi).
            if (panchang.tithi.isPurnima || panchang.tithi.isAmavasya) break

            date = date.plusDays(1)
            guard++
        }

        return MealPlan(start, days.last().date, days)
    }

    /** First day on/after [from] whose sunrise tithi is a Pratipada. */
    fun nextPratipada(from: LocalDate): LocalDate {
        var date = from
        repeat(32) {
            val tithi = PanchangCalculator.panchangFor(date, location).tithi
            if (tithi.number == 1) return date
            date = date.plusDays(1)
        }
        return from // unreachable in practice
    }

    private fun pickRecipe(
        mealType: MealType,
        recipes: List<Recipe>,
        used: Set<Long>,
        lastUsedAt: Map<Long, Int>,
        slot: Int
    ): Recipe {
        val suitable = recipes.filter { mealType in it.mealTypes }
            .ifEmpty { recipes } // never fail a slot outright
        // Prefer a recipe not used anywhere in this plan.
        val fresh = suitable.filter { it.id !in used }
        if (fresh.isNotEmpty()) return fresh.first()
        // Otherwise reuse the one used longest ago.
        return suitable.minByOrNull { lastUsedAt[it.id] ?: -1 } ?: suitable[slot % suitable.size]
    }
}
