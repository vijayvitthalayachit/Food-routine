package com.foodroutine.core.model

import java.time.LocalDate

/** Macro-nutrients. Absolute amounts: kcal and grams. */
data class NutritionFacts(
    val calories: Double = 0.0,
    val proteinG: Double = 0.0,
    val carbsG: Double = 0.0,
    val fiberG: Double = 0.0
) {
    operator fun plus(other: NutritionFacts) = NutritionFacts(
        calories + other.calories,
        proteinG + other.proteinG,
        carbsG + other.carbsG,
        fiberG + other.fiberG
    )

    operator fun times(factor: Double) = NutritionFacts(
        calories * factor, proteinG * factor, carbsG * factor, fiberG * factor
    )

    companion object {
        val ZERO = NutritionFacts()
    }
}

/** How an ingredient must be prepared ahead of cooking day. */
enum class PrepMethod(val displayName: String, val buyDaysBefore: Long) {
    /** Use as-is: buy one day before cooking. */
    NONE("No pre-preparation", 1),

    /** Needs one night of soaking: buy two days before, soak the previous night. */
    SOAK_OVERNIGHT("Soak overnight", 2),

    /**
     * Sprouting: one night soaking plus one night of sprouting/fermenting,
     * so buy three days before cooking.
     */
    SPROUT("Sprout (soak + ferment)", 3)
}

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner")
}

enum class RecipeSource(val displayName: String) {
    CUSTOM("Custom recipe"),
    INSTAGRAM("Instagram"),
    FACEBOOK("Facebook"),
    YOUTUBE("YouTube"),
    WEB("Web");

    companion object {
        /** Classify a shared URL into its source platform. */
        fun fromUrl(url: String?): RecipeSource {
            val u = url?.lowercase() ?: return CUSTOM
            return when {
                "instagram.com" in u -> INSTAGRAM
                "facebook.com" in u || "fb.watch" in u || "fb.com" in u -> FACEBOOK
                "youtube.com" in u || "youtu.be" in u -> YOUTUBE
                u.startsWith("http") -> WEB
                else -> CUSTOM
            }
        }
    }
}

data class Ingredient(
    val name: String,
    /** Quantity used by the recipe, in grams (or ml for liquids). */
    val quantityGrams: Double,
    /** Nutrition of this ingredient per 100 g. */
    val nutritionPer100g: NutritionFacts = NutritionFacts.ZERO,
    val prepMethod: PrepMethod = PrepMethod.NONE
) {
    val nutrition: NutritionFacts get() = nutritionPer100g * (quantityGrams / 100.0)
}

data class Recipe(
    val id: Long = 0,
    val name: String,
    val source: RecipeSource = RecipeSource.CUSTOM,
    val sourceUrl: String? = null,
    val mealTypes: Set<MealType> = MealType.entries.toSet(),
    val servings: Int = 1,
    val ingredients: List<Ingredient> = emptyList(),
    val instructions: String = ""
) {
    /** Total nutrition of the whole prepared recipe. */
    val totalNutrition: NutritionFacts
        get() = ingredients.fold(NutritionFacts.ZERO) { acc, i -> acc + i.nutrition }

    /** Nutrition of a single serving. */
    val nutritionPerServing: NutritionFacts
        get() = totalNutrition * (1.0 / servings.coerceAtLeast(1))
}

/** One meal slot inside a planned day. */
data class PlannedMeal(
    val mealType: MealType,
    val recipe: Recipe
)

/** One civil day of the plan. Fasting days carry no meals. */
data class PlannedDay(
    val date: LocalDate,
    val tithiName: String,
    val fastingReason: String? = null,
    val meals: List<PlannedMeal> = emptyList()
) {
    val isFasting: Boolean get() = fastingReason != null

    /** Whole-day nutrition (sums one serving of each planned meal). */
    val dayNutrition: NutritionFacts
        get() = meals.fold(NutritionFacts.ZERO) { acc, m -> acc + m.recipe.nutritionPerServing }
}

data class MealPlan(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val days: List<PlannedDay>
)
