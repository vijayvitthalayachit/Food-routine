package com.foodroutine.core.grocery

import com.foodroutine.core.model.Ingredient
import com.foodroutine.core.model.MealPlan
import com.foodroutine.core.model.MealType
import com.foodroutine.core.model.NutritionFacts
import com.foodroutine.core.model.PlannedDay
import com.foodroutine.core.model.PlannedMeal
import com.foodroutine.core.model.PrepMethod
import com.foodroutine.core.model.Recipe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class GrocerySchedulerTest {

    private val cookDate: LocalDate = LocalDate.of(2025, 8, 10)

    private fun planWith(recipe: Recipe): MealPlan {
        val day = PlannedDay(
            cookDate, "Shukla Dwitiya", null,
            listOf(PlannedMeal(MealType.LUNCH, recipe))
        )
        return MealPlan(cookDate, cookDate, listOf(day))
    }

    @Test
    fun regularIngredientBoughtOneDayBefore() {
        val recipe = Recipe(
            id = 1, name = "Aloo Sabzi",
            ingredients = listOf(Ingredient("Potato", 300.0))
        )
        val tasks = GroceryScheduler.schedule(planWith(recipe))
        assertEquals(1, tasks.size)
        val buy = tasks.single()
        assertEquals(TaskType.BUY, buy.type)
        assertEquals(cookDate.minusDays(1), buy.date)
    }

    @Test
    fun soakedIngredientBoughtTwoDaysBeforeWithSoakReminder() {
        val recipe = Recipe(
            id = 2, name = "Chole",
            ingredients = listOf(
                Ingredient("Kabuli chana (chickpeas)", 200.0, NutritionFacts.ZERO, PrepMethod.SOAK_OVERNIGHT)
            )
        )
        val tasks = GroceryScheduler.schedule(planWith(recipe))
        val buy = tasks.first { it.type == TaskType.BUY }
        val soak = tasks.first { it.type == TaskType.SOAK }
        assertEquals(cookDate.minusDays(2), buy.date)
        assertEquals(cookDate.minusDays(1), soak.date)
        assertEquals(2, tasks.size)
    }

    @Test
    fun sproutedIngredientBoughtThreeDaysBeforeWithSoakAndFerment() {
        val recipe = Recipe(
            id = 3, name = "Moong Sprout Salad",
            ingredients = listOf(
                Ingredient("Whole moong (green gram)", 150.0, NutritionFacts.ZERO, PrepMethod.SPROUT)
            )
        )
        val tasks = GroceryScheduler.schedule(planWith(recipe))
        val buy = tasks.first { it.type == TaskType.BUY }
        val soak = tasks.first { it.type == TaskType.SOAK }
        val ferment = tasks.first { it.type == TaskType.FERMENT }
        assertEquals(cookDate.minusDays(3), buy.date)
        assertEquals(cookDate.minusDays(2), soak.date)
        assertEquals(cookDate.minusDays(1), ferment.date)
        assertEquals(3, tasks.size)
    }

    @Test
    fun shoppingListMergesSameIngredientAcrossRecipes() {
        val r1 = Recipe(id = 1, name = "Sabzi A", ingredients = listOf(Ingredient("Onion", 100.0)))
        val r2 = Recipe(id = 2, name = "Sabzi B", ingredients = listOf(Ingredient("Onion", 150.0)))
        val day = PlannedDay(
            cookDate, "Shukla Dwitiya", null,
            listOf(PlannedMeal(MealType.LUNCH, r1), PlannedMeal(MealType.DINNER, r2))
        )
        val plan = MealPlan(cookDate, cookDate, listOf(day))
        val byDate = GroceryScheduler.shoppingListByDate(plan)
        val list = byDate.getValue(cookDate.minusDays(1))
        assertEquals(1, list.size)
        assertEquals(250.0, list.single().quantityGrams, 0.001)
        assertTrue(list.single().recipeName.contains("Sabzi A"))
        assertTrue(list.single().recipeName.contains("Sabzi B"))
    }

    @Test
    fun fastingDaysProduceNoTasks() {
        val fastingDay = PlannedDay(cookDate, "Krishna Ekadashi", "Ekadashi")
        val plan = MealPlan(cookDate, cookDate, listOf(fastingDay))
        assertTrue(GroceryScheduler.schedule(plan).isEmpty())
    }
}
