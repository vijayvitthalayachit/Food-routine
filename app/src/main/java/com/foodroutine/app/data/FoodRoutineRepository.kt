package com.foodroutine.app.data

import android.content.Context
import com.foodroutine.core.grocery.GroceryScheduler
import com.foodroutine.core.grocery.GroceryTask
import com.foodroutine.core.model.Ingredient
import com.foodroutine.core.model.MealPlan
import com.foodroutine.core.model.MealType
import com.foodroutine.core.model.NutritionFacts
import com.foodroutine.core.model.PlannedDay
import com.foodroutine.core.model.PlannedMeal
import com.foodroutine.core.model.PrepMethod
import com.foodroutine.core.model.Recipe
import com.foodroutine.core.model.RecipeSource
import com.foodroutine.core.panchang.FastingCalendar
import com.foodroutine.core.panchang.Location
import com.foodroutine.core.planner.MealPlanGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Maps Room entities to core-domain models and hosts the app use-cases. */
class FoodRoutineRepository(context: Context) {

    private val db = AppDatabase.get(context)
    private val recipeDao = db.recipeDao()
    private val customFastDao = db.customFastDao()
    private val planDao = db.planDao()

    // ---------- Recipes ----------

    val recipes: Flow<List<Recipe>> =
        recipeDao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun saveRecipe(recipe: Recipe): Long =
        recipeDao.upsertWithIngredients(recipe.toEntity(), recipe.ingredients.map { it.toEntity() })

    suspend fun deleteRecipe(id: Long) = recipeDao.deleteRecipe(id)

    suspend fun recipeById(id: Long): Recipe? = recipeDao.byId(id)?.toDomain()

    // ---------- Custom fasting ----------

    val customFasts: Flow<List<CustomFastEntity>> = customFastDao.observeAll()

    suspend fun addCustomFast(date: LocalDate, label: String) =
        customFastDao.insert(CustomFastEntity(date.toEpochDay(), label))

    suspend fun removeCustomFast(fast: CustomFastEntity) = customFastDao.delete(fast)

    // ---------- Meal plan ----------

    /** The single active plan, reconstructed from Room, as a Flow. */
    val activePlan: Flow<MealPlan?> =
        combine(planDao.observeDays(), planDao.observeMeals(), recipes) { days, meals, recipeList ->
            buildPlan(days, meals, recipeList)
        }

    suspend fun loadActivePlan(): MealPlan? {
        val days = planDao.days()
        val meals = planDao.meals()
        val recipeList = recipeDao.observeAllOnce().map { it.toDomain() }
        return buildPlan(days, meals, recipeList)
    }

    private suspend fun RecipeDao.observeAllOnce(): List<RecipeWithIngredients> {
        // Small helper: byId per meal would be N queries; a single pass is fine.
        val ids = planDao.meals().map { it.recipeId }.distinct()
        return ids.mapNotNull { byId(it) }
    }

    private fun buildPlan(
        days: List<PlanDayEntity>,
        meals: List<PlanMealEntity>,
        recipeList: List<Recipe>
    ): MealPlan? {
        if (days.isEmpty()) return null
        val recipesById = recipeList.associateBy { it.id }
        val mealsByDay = meals.groupBy { it.epochDay }
        val planned = days.map { day ->
            PlannedDay(
                date = LocalDate.ofEpochDay(day.epochDay),
                tithiName = day.tithiName,
                fastingReason = day.fastingReason,
                meals = (mealsByDay[day.epochDay] ?: emptyList()).mapNotNull { m ->
                    recipesById[m.recipeId]?.let {
                        PlannedMeal(MealType.valueOf(m.mealType), it)
                    }
                }
            )
        }
        return MealPlan(planned.first().date, planned.last().date, planned)
    }

    /**
     * Generate a fresh plan starting at the first Pratipada on/after [from]
     * and persist it as the active plan.
     */
    suspend fun generatePlan(
        from: LocalDate,
        location: Location,
        customFastDates: Set<LocalDate>,
        recipePool: List<Recipe>
    ): MealPlan {
        val generator = MealPlanGenerator(
            location = location,
            fastingCalendar = FastingCalendar(location, customFastDates)
        )
        val plan = generator.generate(from, recipePool)
        val dayEntities = plan.days.map {
            PlanDayEntity(it.date.toEpochDay(), it.tithiName, it.fastingReason)
        }
        val mealEntities = plan.days.flatMap { day ->
            day.meals.map {
                PlanMealEntity(
                    epochDay = day.date.toEpochDay(),
                    mealType = it.mealType.name,
                    recipeId = it.recipe.id
                )
            }
        }
        planDao.replacePlan(dayEntities, mealEntities)
        return plan
    }

    // ---------- Grocery ----------

    fun groceryTasks(plan: MealPlan): List<GroceryTask> = GroceryScheduler.schedule(plan)

    fun shoppingListByDate(plan: MealPlan): Map<LocalDate, List<GroceryTask>> =
        GroceryScheduler.shoppingListByDate(plan)
}

// ---------- Entity <-> domain mapping ----------

fun RecipeWithIngredients.toDomain(): Recipe = Recipe(
    id = recipe.id,
    name = recipe.name,
    source = runCatching { RecipeSource.valueOf(recipe.source) }.getOrDefault(RecipeSource.CUSTOM),
    sourceUrl = recipe.sourceUrl,
    mealTypes = recipe.mealTypes.split(",")
        .filter { it.isNotBlank() }
        .mapNotNull { runCatching { MealType.valueOf(it) }.getOrNull() }
        .toSet()
        .ifEmpty { MealType.entries.toSet() },
    servings = recipe.servings,
    ingredients = ingredients.map { it.toDomain() },
    instructions = recipe.instructions
)

fun IngredientEntity.toDomain(): Ingredient = Ingredient(
    name = name,
    quantityGrams = quantityGrams,
    nutritionPer100g = NutritionFacts(kcalPer100g, proteinPer100g, carbsPer100g, fiberPer100g),
    prepMethod = runCatching { PrepMethod.valueOf(prepMethod) }.getOrDefault(PrepMethod.NONE)
)

fun Recipe.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    name = name,
    source = source.name,
    sourceUrl = sourceUrl,
    mealTypes = mealTypes.joinToString(",") { it.name },
    servings = servings,
    instructions = instructions
)

fun Ingredient.toEntity(): IngredientEntity = IngredientEntity(
    recipeId = 0,
    name = name,
    quantityGrams = quantityGrams,
    kcalPer100g = nutritionPer100g.calories,
    proteinPer100g = nutritionPer100g.proteinG,
    carbsPer100g = nutritionPer100g.carbsG,
    fiberPer100g = nutritionPer100g.fiberG,
    prepMethod = prepMethod.name
)
