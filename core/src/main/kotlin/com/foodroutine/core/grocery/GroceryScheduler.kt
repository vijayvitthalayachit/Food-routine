package com.foodroutine.core.grocery

import com.foodroutine.core.model.MealPlan
import com.foodroutine.core.model.PrepMethod
import java.time.LocalDate

/** Kind of preparation task placed on the grocery/prep timeline. */
enum class TaskType(val displayName: String) {
    BUY("Buy"),
    SOAK("Soak overnight"),
    FERMENT("Drain & leave to sprout/ferment")
}

/**
 * One dated task: buy / soak / ferment a given ingredient for a recipe
 * cooked on [cookDate].
 */
data class GroceryTask(
    val date: LocalDate,
    val type: TaskType,
    val ingredientName: String,
    val quantityGrams: Double,
    val recipeName: String,
    val cookDate: LocalDate
)

/**
 * Turns a [MealPlan] into a dated shopping & prep schedule:
 *
 * - regular ingredients:      BUY one day before cooking;
 * - soak-overnight items:     BUY two days before, SOAK the evening before;
 * - sprouted items:           BUY three days before, SOAK two evenings
 *                             before, FERMENT the evening before.
 */
object GroceryScheduler {

    fun schedule(plan: MealPlan): List<GroceryTask> {
        val tasks = mutableListOf<GroceryTask>()
        for (day in plan.days) {
            if (day.isFasting) continue
            for (meal in day.meals) {
                for (ingredient in meal.recipe.ingredients) {
                    val cookDate = day.date
                    val buyDate = cookDate.minusDays(ingredient.prepMethod.buyDaysBefore)
                    tasks += GroceryTask(
                        buyDate, TaskType.BUY, ingredient.name,
                        ingredient.quantityGrams, meal.recipe.name, cookDate
                    )
                    when (ingredient.prepMethod) {
                        PrepMethod.SOAK_OVERNIGHT -> {
                            tasks += GroceryTask(
                                cookDate.minusDays(1), TaskType.SOAK, ingredient.name,
                                ingredient.quantityGrams, meal.recipe.name, cookDate
                            )
                        }
                        PrepMethod.SPROUT -> {
                            tasks += GroceryTask(
                                cookDate.minusDays(2), TaskType.SOAK, ingredient.name,
                                ingredient.quantityGrams, meal.recipe.name, cookDate
                            )
                            tasks += GroceryTask(
                                cookDate.minusDays(1), TaskType.FERMENT, ingredient.name,
                                ingredient.quantityGrams, meal.recipe.name, cookDate
                            )
                        }
                        PrepMethod.NONE -> Unit
                    }
                }
            }
        }
        return tasks.sortedWith(compareBy({ it.date }, { it.type }, { it.ingredientName }))
    }

    /**
     * Consolidated shopping list per date: identical ingredients bought on
     * the same day are merged with summed quantities.
     */
    fun shoppingListByDate(plan: MealPlan): Map<LocalDate, List<GroceryTask>> =
        schedule(plan)
            .filter { it.type == TaskType.BUY }
            .groupBy { it.date }
            .mapValues { (_, dayTasks) ->
                dayTasks.groupBy { it.ingredientName.lowercase() }
                    .map { (_, same) ->
                        same.first().copy(
                            quantityGrams = same.sumOf { it.quantityGrams },
                            recipeName = same.joinToString(", ") { it.recipeName }
                                .split(", ").distinct().joinToString(", ")
                        )
                    }
                    .sortedBy { it.ingredientName }
            }

    /** Prep (soak/ferment) tasks for a given evening. */
    fun prepTasksOn(plan: MealPlan, date: LocalDate): List<GroceryTask> =
        schedule(plan).filter { it.date == date && it.type != TaskType.BUY }
}
