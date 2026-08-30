package com.foodroutine.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodroutine.app.ui.components.NutritionRow
import com.foodroutine.app.viewmodel.AppViewModel

@Composable
fun RecipesScreen(vm: AppViewModel, onAdd: () -> Unit, onEdit: (Long) -> Unit) {
    val recipes by vm.recipes.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add recipe")
            }
        }
    ) { padding ->
        if (recipes.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
                Text(
                    "No recipes yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Create a custom recipe with the + button, or share a reel/video " +
                        "from Instagram, Facebook or YouTube into Food Routine to save it here.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recipes.size) { i ->
                    val recipe = recipes[i]
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onEdit(recipe.id) }
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    recipe.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    recipe.source.displayName +
                                        (recipe.sourceUrl?.let { " · $it" } ?: "") +
                                        " · " + recipe.mealTypes.joinToString { it.displayName },
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2
                                )
                                NutritionRow(recipe.nutritionPerServing)
                            }
                            IconButton(onClick = { vm.deleteRecipe(recipe.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
