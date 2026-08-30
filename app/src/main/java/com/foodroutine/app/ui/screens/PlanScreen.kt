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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PlanScreen(vm: AppViewModel) {
    val plan by vm.activePlan.collectAsState()
    val generating by vm.generating.collectAsState()
    val error by vm.planError.collectAsState()
    val recipes by vm.recipes.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Paksha meal plan",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Generates 3 meals per day from the next Pratipada up to " +
                            "Purnima/Amavasya (max 15 tithi days), skipping Ekadashi, " +
                            "Janmashtami, Grahana and your custom fasts.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { vm.generatePlan(LocalDate.now()) },
                            enabled = !generating && recipes.isNotEmpty()
                        ) {
                            Text(if (plan == null) "Generate plan" else "Regenerate plan")
                        }
                        if (generating) {
                            Spacer(Modifier.width(12.dp))
                            CircularProgressIndicator(Modifier.width(24.dp).height(24.dp))
                        }
                    }
                    if (recipes.isEmpty()) {
                        Text(
                            "Add recipes first - the plan needs at least one recipe.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    error?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        plan?.let { p ->
            item {
                Text(
                    "${p.startDate.format(DateTimeFormatter.ofPattern("d MMM"))} - " +
                        p.endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(p.days.size) { i ->
                val day = p.days[i]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (day.isFasting) {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    } else {
                        CardDefaults.cardColors()
                    }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            day.date.format(DateTimeFormatter.ofPattern("EEE, d MMM")) +
                                " · " + day.tithiName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (day.isFasting) {
                            Text(
                                "Fasting - ${day.fastingReason}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            day.meals.forEach { meal ->
                                Text(
                                    "${meal.mealType.displayName}: ${meal.recipe.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            NutritionRow(day.dayNutrition)
                        }
                    }
                }
            }
        }
    }
}
