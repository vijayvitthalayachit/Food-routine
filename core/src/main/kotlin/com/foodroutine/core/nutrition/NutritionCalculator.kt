package com.foodroutine.core.nutrition

import com.foodroutine.core.model.MealPlan
import com.foodroutine.core.model.NutritionFacts
import com.foodroutine.core.model.PlannedDay
import com.foodroutine.core.model.Recipe
import java.time.LocalDate

/** Convenience aggregations over recipes and plans. */
object NutritionCalculator {

    fun recipeTotal(recipe: Recipe): NutritionFacts = recipe.totalNutrition

    fun perServing(recipe: Recipe): NutritionFacts = recipe.nutritionPerServing

    /** Whole-day totals (one serving of each of the day's meals). */
    fun dayTotal(day: PlannedDay): NutritionFacts = day.dayNutrition

    fun dayTotal(plan: MealPlan, date: LocalDate): NutritionFacts =
        plan.days.firstOrNull { it.date == date }?.dayNutrition ?: NutritionFacts.ZERO

    /** Average daily nutrition across the plan's non-fasting days. */
    fun planDailyAverage(plan: MealPlan): NutritionFacts {
        val eatingDays = plan.days.filter { !it.isFasting }
        if (eatingDays.isEmpty()) return NutritionFacts.ZERO
        val total = eatingDays.fold(NutritionFacts.ZERO) { acc, d -> acc + d.dayNutrition }
        return total * (1.0 / eatingDays.size)
    }
}
