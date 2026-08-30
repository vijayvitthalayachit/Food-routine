package com.foodroutine.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class RecipeWithIngredients(
    @Embedded val recipe: RecipeEntity,
    @Relation(parentColumn = "id", entityColumn = "recipeId")
    val ingredients: List<IngredientEntity>
)

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name")
    fun observeAll(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun byId(id: Long): RecipeWithIngredients?

    @Insert
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Insert
    suspend fun insertIngredients(ingredients: List<IngredientEntity>)

    @Query("DELETE FROM ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsOf(recipeId: Long)

    @Transaction
    suspend fun upsertWithIngredients(recipe: RecipeEntity, ingredients: List<IngredientEntity>): Long {
        val id = if (recipe.id == 0L) {
            insertRecipe(recipe)
        } else {
            updateRecipe(recipe)
            deleteIngredientsOf(recipe.id)
            recipe.id
        }
        insertIngredients(ingredients.map { it.copy(recipeId = id) })
        return id
    }
}

@Dao
interface CustomFastDao {
    @Query("SELECT * FROM custom_fasts ORDER BY epochDay")
    fun observeAll(): Flow<List<CustomFastEntity>>

    @Insert
    suspend fun insert(fast: CustomFastEntity)

    @Delete
    suspend fun delete(fast: CustomFastEntity)
}

@Dao
interface PlanDao {
    @Query("SELECT * FROM plan_days ORDER BY epochDay")
    fun observeDays(): Flow<List<PlanDayEntity>>

    @Query("SELECT * FROM plan_meals ORDER BY epochDay")
    fun observeMeals(): Flow<List<PlanMealEntity>>

    @Query("SELECT * FROM plan_days ORDER BY epochDay")
    suspend fun days(): List<PlanDayEntity>

    @Query("SELECT * FROM plan_meals ORDER BY epochDay")
    suspend fun meals(): List<PlanMealEntity>

    @Insert
    suspend fun insertDays(days: List<PlanDayEntity>)

    @Insert
    suspend fun insertMeals(meals: List<PlanMealEntity>)

    @Query("DELETE FROM plan_days")
    suspend fun clearDays()

    @Query("DELETE FROM plan_meals")
    suspend fun clearMeals()

    @Transaction
    suspend fun replacePlan(days: List<PlanDayEntity>, meals: List<PlanMealEntity>) {
        clearMeals()
        clearDays()
        insertDays(days)
        insertMeals(meals)
    }
}
