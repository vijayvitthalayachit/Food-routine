package com.foodroutine.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodroutine.app.ui.components.NutritionRow
import com.foodroutine.app.viewmodel.AppViewModel
import com.foodroutine.core.model.Ingredient
import com.foodroutine.core.model.MealType
import com.foodroutine.core.model.NutritionFacts
import com.foodroutine.core.model.PrepMethod
import com.foodroutine.core.model.Recipe
import com.foodroutine.core.model.RecipeSource
import com.foodroutine.core.nutrition.IngredientCatalog

/** Editable working copy of an ingredient row. */
private data class IngredientDraft(
    var name: String = "",
    var quantity: String = "",
    var kcal: String = "0",
    var protein: String = "0",
    var carbs: String = "0",
    var fiber: String = "0",
    var prep: PrepMethod = PrepMethod.NONE
)

@Composable
fun RecipeEditScreen(vm: AppViewModel, recipeId: Long?, onDone: () -> Unit) {
    val sharedDraft by vm.sharedDraft.collectAsState()

    var name by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("") }
    var source by remember { mutableStateOf(RecipeSource.CUSTOM) }
    var servings by remember { mutableStateOf("2") }
    var instructions by remember { mutableStateOf("") }
    var mealTypes by remember { mutableStateOf(setOf<MealType>(MealType.LUNCH)) }
    val ingredients = remember { mutableListOf<IngredientDraft>().toMutableStateList() }
    var loaded by remember { mutableStateOf(false) }

    // Prefill from a share intent (Instagram reel / Facebook video link).
    LaunchedEffect(sharedDraft) {
        sharedDraft?.let {
            sourceUrl = it.url
            source = it.source
            vm.consumeSharedDraft()
        }
    }

    // Load an existing recipe for editing.
    LaunchedEffect(recipeId) {
        if (recipeId != null && !loaded) {
            loaded = true
            val existing = vm.recipes.value.firstOrNull { it.id == recipeId }
            existing?.let { r ->
                name = r.name
                sourceUrl = r.sourceUrl ?: ""
                source = r.source
                servings = r.servings.toString()
                instructions = r.instructions
                mealTypes = r.mealTypes
                ingredients.clear()
                ingredients.addAll(
                    r.ingredients.map {
                        IngredientDraft(
                            it.name,
                            it.quantityGrams.toString(),
                            it.nutritionPer100g.calories.toString(),
                            it.nutritionPer100g.proteinG.toString(),
                            it.nutritionPer100g.carbsG.toString(),
                            it.nutritionPer100g.fiberG.toString(),
                            it.prepMethod
                        )
                    }
                )
            }
        }
    }

    fun buildRecipe(): Recipe = Recipe(
        id = recipeId ?: 0,
        name = name.trim().ifBlank { "Untitled recipe" },
        source = if (sourceUrl.isBlank()) RecipeSource.CUSTOM else RecipeSource.fromUrl(sourceUrl),
        sourceUrl = sourceUrl.trim().ifBlank { null },
        mealTypes = mealTypes.ifEmpty { MealType.entries.toSet() },
        servings = servings.toIntOrNull()?.coerceAtLeast(1) ?: 1,
        instructions = instructions,
        ingredients = ingredients.filter { it.name.isNotBlank() }.map {
            Ingredient(
                name = it.name.trim(),
                quantityGrams = it.quantity.toDoubleOrNull() ?: 0.0,
                nutritionPer100g = NutritionFacts(
                    it.kcal.toDoubleOrNull() ?: 0.0,
                    it.protein.toDoubleOrNull() ?: 0.0,
                    it.carbs.toDoubleOrNull() ?: 0.0,
                    it.fiber.toDoubleOrNull() ?: 0.0
                ),
                prepMethod = it.prep
            )
        }
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (recipeId == null) "New recipe" else "Edit recipe",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Recipe name") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = sourceUrl, onValueChange = { sourceUrl = it },
                label = { Text("Source link (Instagram / Facebook / YouTube / web)") },
                supportingText = {
                    val detected = RecipeSource.fromUrl(sourceUrl.ifBlank { null })
                    Text("Detected source: ${detected.displayName}")
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MealType.entries.forEach { mt ->
                    FilterChip(
                        selected = mt in mealTypes,
                        onClick = {
                            mealTypes = if (mt in mealTypes) mealTypes - mt else mealTypes + mt
                        },
                        label = { Text(mt.displayName) }
                    )
                }
            }
        }
        item {
            OutlinedTextField(
                value = servings, onValueChange = { servings = it },
                label = { Text("Servings") },
                modifier = Modifier.width(140.dp)
            )
        }

        item {
            Text(
                "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(ingredients.size) { index ->
            IngredientEditor(
                draft = ingredients[index],
                onChange = { ingredients[index] = it },
                onDelete = { ingredients.removeAt(index) }
            )
        }
        item {
            OutlinedButton(onClick = { ingredients.add(IngredientDraft()) }) {
                Text("+ Add ingredient")
            }
        }

        item {
            OutlinedTextField(
                value = instructions, onValueChange = { instructions = it },
                label = { Text("Instructions") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            val preview = buildRecipe()
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Nutrition per serving (auto-calculated)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    NutritionRow(preview.nutritionPerServing)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Whole recipe: ${preview.totalNutrition.calories.toInt()} kcal",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { vm.saveRecipe(buildRecipe()) { onDone() } }) {
                    Text("Save recipe")
                }
                OutlinedButton(onClick = onDone) { Text("Cancel") }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun IngredientNameField(
    value: String,
    onPick: (String, NutritionFacts?, PrepMethod?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val suggestions = remember(value) { IngredientCatalog.search(value).take(6) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onPick(it, null, null)
                expanded = true
            },
            label = { Text("Ingredient") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            suggestions.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.name) },
                    onClick = {
                        onPick(item.name, item.per100g, item.defaultPrep)
                        expanded = false
                    }
                )
            }
        }
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun PrepMethodField(value: PrepMethod, onPick: (PrepMethod) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = value.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Preparation") },
            supportingText = {
                Text(
                    when (value) {
                        PrepMethod.NONE -> "Buy 1 day before cooking"
                        PrepMethod.SOAK_OVERNIGHT -> "Buy 2 days before; soak the previous night"
                        PrepMethod.SPROUT -> "Buy 3 days before; soak, then sprout/ferment overnight"
                    }
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            PrepMethod.entries.forEach { pm ->
                DropdownMenuItem(
                    text = { Text(pm.displayName) },
                    onClick = {
                        onPick(pm)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun IngredientEditor(
    draft: IngredientDraft,
    onChange: (IngredientDraft) -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    IngredientNameField(draft.name) { newName, per100g, prep ->
                        onChange(
                            if (per100g != null) {
                                draft.copy(
                                    name = newName,
                                    kcal = per100g.calories.toString(),
                                    protein = per100g.proteinG.toString(),
                                    carbs = per100g.carbsG.toString(),
                                    fiber = per100g.fiberG.toString(),
                                    prep = prep ?: draft.prep
                                )
                            } else {
                                draft.copy(name = newName)
                            }
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Remove ingredient")
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.quantity,
                    onValueChange = { onChange(draft.copy(quantity = it)) },
                    label = { Text("Grams") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = draft.kcal,
                    onValueChange = { onChange(draft.copy(kcal = it)) },
                    label = { Text("kcal/100g") },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = draft.protein,
                    onValueChange = { onChange(draft.copy(protein = it)) },
                    label = { Text("Protein/100g") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = draft.carbs,
                    onValueChange = { onChange(draft.copy(carbs = it)) },
                    label = { Text("Carbs/100g") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = draft.fiber,
                    onValueChange = { onChange(draft.copy(fiber = it)) },
                    label = { Text("Fiber/100g") },
                    modifier = Modifier.weight(1f)
                )
            }
            PrepMethodField(draft.prep) { onChange(draft.copy(prep = it)) }
        }
    }
}
