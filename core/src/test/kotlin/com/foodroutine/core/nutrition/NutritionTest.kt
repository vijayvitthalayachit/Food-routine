package com.foodroutine.core.nutrition

import com.foodroutine.core.model.Ingredient
import com.foodroutine.core.model.MealType
import com.foodroutine.core.model.NutritionFacts
import com.foodroutine.core.model.PlannedDay
import com.foodroutine.core.model.PlannedMeal
import com.foodroutine.core.model.Recipe
import com.foodroutine.core.model.RecipeSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class NutritionTest {

    @Test
    fun recipeNutritionSumsIngredients() {
        val recipe = Recipe(
            id = 1, name = "Dal Rice", servings = 2,
            ingredients = listOf(
                // 100 g rice: 360 kcal, 6.8 P, 78.2 C, 1.3 F
                Ingredient("Rice (raw)", 100.0, NutritionFacts(360.0, 6.8, 78.2, 1.3)),
                // 50 g toor dal: half of 343 kcal, 22.3 P, 62.8 C, 15.0 F
                Ingredient("Toor dal", 50.0, NutritionFacts(343.0, 22.3, 62.8, 15.0))
            )
        )
        val total = NutritionCalculator.recipeTotal(recipe)
        assertEquals(360.0 + 171.5, total.calories, 0.01)
        assertEquals(6.8 + 11.15, total.proteinG, 0.01)
        assertEquals(78.2 + 31.4, total.carbsG, 0.01)
        assertEquals(1.3 + 7.5, total.fiberG, 0.01)

        val serving = NutritionCalculator.perServing(recipe)
        assertEquals(total.calories / 2, serving.calories, 0.01)
    }

    @Test
    fun dayNutritionSumsAllMeals() {
        val breakfast = Recipe(
            id = 1, name = "Poha", ingredients =
            listOf(Ingredient("Poha", 100.0, NutritionFacts(346.0, 6.6, 77.3, 2.0)))
        )
        val lunch = Recipe(
            id = 2, name = "Dal", ingredients =
            listOf(Ingredient("Moong dal", 100.0, NutritionFacts(347.0, 24.5, 59.9, 8.2)))
        )
        val dinner = Recipe(
            id = 3, name = "Khichdi", ingredients =
            listOf(Ingredient("Rice (raw)", 100.0, NutritionFacts(360.0, 6.8, 78.2, 1.3)))
        )
        val day = PlannedDay(
            LocalDate.of(2025, 8, 10), "Shukla Dwitiya", null,
            listOf(
                PlannedMeal(MealType.BREAKFAST, breakfast),
                PlannedMeal(MealType.LUNCH, lunch),
                PlannedMeal(MealType.DINNER, dinner)
            )
        )
        val total = NutritionCalculator.dayTotal(day)
        assertEquals(346.0 + 347.0 + 360.0, total.calories, 0.01)
        assertEquals(6.6 + 24.5 + 6.8, total.proteinG, 0.01)
    }

    @Test
    fun catalogLookupAndSearch() {
        assertNotNull(IngredientCatalog.find("potato"))
        assertNotNull(IngredientCatalog.find("Whole moong (green gram)"))
        val hits = IngredientCatalog.search("dal")
        assert(hits.isNotEmpty())
    }

    @Test
    fun sourceDetectionFromUrl() {
        assertEquals(
            RecipeSource.INSTAGRAM,
            RecipeSource.fromUrl("https://www.instagram.com/reel/xyz/")
        )
        assertEquals(RecipeSource.FACEBOOK, RecipeSource.fromUrl("https://fb.watch/abc"))
        assertEquals(RecipeSource.YOUTUBE, RecipeSource.fromUrl("https://youtu.be/abc"))
        assertEquals(RecipeSource.WEB, RecipeSource.fromUrl("https://example.com/recipe"))
        assertEquals(RecipeSource.CUSTOM, RecipeSource.fromUrl(null))
    }
}
