package com.foodroutine.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** RecipeSource enum name. */
    val source: String,
    val sourceUrl: String?,
    /** Comma-separated MealType enum names. */
    val mealTypes: String,
    val servings: Int,
    val instructions: String
)

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Long,
    val name: String,
    val quantityGrams: Double,
    val kcalPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fiberPer100g: Double,
    /** PrepMethod enum name. */
    val prepMethod: String
)

/** User-defined fasting day. */
@Entity(tableName = "custom_fasts")
data class CustomFastEntity(
    @PrimaryKey val epochDay: Long,
    val label: String
)

/** One row per civil day of the single active meal plan. */
@Entity(tableName = "plan_days")
data class PlanDayEntity(
    @PrimaryKey val epochDay: Long,
    val tithiName: String,
    val fastingReason: String?
)

@Entity(
    tableName = "plan_meals",
    indices = [Index("epochDay"), Index("recipeId")]
)
data class PlanMealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    /** MealType enum name. */
    val mealType: String,
    val recipeId: Long
)
