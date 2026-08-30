package com.foodroutine.core.planner

import com.foodroutine.core.model.MealType
import com.foodroutine.core.model.Recipe
import com.foodroutine.core.panchang.FastingCalendar
import com.foodroutine.core.panchang.Location
import com.foodroutine.core.panchang.PanchangCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MealPlanGeneratorTest {

    private fun recipePool(count: Int): List<Recipe> =
        (1..count).map { i ->
            Recipe(
                id = i.toLong(),
                name = "Recipe $i",
                mealTypes = when (i % 3) {
                    0 -> setOf(MealType.BREAKFAST)
                    1 -> setOf(MealType.LUNCH)
                    else -> setOf(MealType.DINNER)
                }
            )
        }

    private val generator = MealPlanGenerator()

    @Test
    fun planStartsOnPratipadaAndEndsOnPurnimaOrAmavasya() {
        val plan = generator.generate(LocalDate.of(2025, 8, 1), recipePool(60))
        val first = PanchangCalculator.panchangFor(plan.startDate).tithi
        val last = PanchangCalculator.panchangFor(plan.endDate).tithi
        assertEquals("start tithi was $first", 1, first.number)
        assertTrue("end tithi was $last", last.isPurnima || last.isAmavasya)
        assertTrue("plan had ${plan.days.size} days", plan.days.size in 12..16)
    }

    @Test
    fun fastingDaysCarryNoMeals() {
        val plan = generator.generate(LocalDate.of(2025, 8, 10), recipePool(60))
        // This paksha (Krishna, Aug 10..23 area) contains Janmashtami (Aug 16)
        // and Krishna Ekadashi (Aug 18/19).
        val fastingDays = plan.days.filter { it.isFasting }
        assertTrue("no fasting days found in $plan", fastingDays.isNotEmpty())
        for (day in fastingDays) {
            assertTrue("fasting day ${day.date} has meals", day.meals.isEmpty())
        }
        for (day in plan.days.filter { !it.isFasting }) {
            assertEquals("day ${day.date}", 3, day.meals.size)
            assertEquals(
                setOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER),
                day.meals.map { it.mealType }.toSet()
            )
        }
    }

    @Test
    fun recipesAreDistinctWhenPoolIsLargeEnough() {
        val plan = generator.generate(LocalDate.of(2025, 8, 1), recipePool(60))
        val used = plan.days.flatMap { d -> d.meals.map { it.recipe.id } }
        assertEquals("recipes repeated: $used", used.size, used.distinct().size)
    }

    @Test
    fun smallPoolFallsBackToLeastRecentlyUsed() {
        val plan = generator.generate(LocalDate.of(2025, 8, 1), recipePool(6))
        // Must still fill every non-fasting day with 3 meals.
        for (day in plan.days.filter { !it.isFasting }) {
            assertEquals(3, day.meals.size)
        }
        // No recipe should appear twice on the same day.
        for (day in plan.days) {
            val ids = day.meals.map { it.recipe.id }
            assertEquals("same recipe twice on ${day.date}", ids.size, ids.distinct().size)
        }
    }

    @Test
    fun customFastingDaysAreExcluded() {
        // Pick a date guaranteed to fall inside the generated plan window.
        val custom = generator.nextPratipada(LocalDate.of(2025, 8, 1)).plusDays(2)
        val gen = MealPlanGenerator(
            fastingCalendar = FastingCalendar(
                location = Location.DEFAULT,
                customFastingDates = setOf(custom)
            )
        )
        val plan = gen.generate(LocalDate.of(2025, 8, 1), recipePool(60))
        val day = plan.days.first { it.date == custom }
        assertTrue(day.isFasting)
        assertTrue(day.meals.isEmpty())
    }
}
