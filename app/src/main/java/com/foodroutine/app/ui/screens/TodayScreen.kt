package com.foodroutine.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodroutine.app.ui.components.NutritionRow
import com.foodroutine.app.viewmodel.AppViewModel
import com.foodroutine.core.grocery.TaskType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(vm: AppViewModel) {
    val plan by vm.activePlan.collectAsState()
    val customFasts by vm.customFasts.collectAsState()
    val today = LocalDate.now()
    val panchang = remember(customFasts) { vm.panchangToday() }
    val fastingReason = remember(customFasts) { vm.fastingReasonToday() }

    val todayPlanned = plan?.days?.firstOrNull { it.date == today }
    val tasksToday = plan?.let { p -> vm.groceryTasks(p).filter { it.date == today } } ?: emptyList()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${panchang.tithi.displayName} · ${panchang.lunarMonth.displayName} (amanta)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "Sunrise ${panchang.sunriseTime}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        if (fastingReason != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Fasting day: ${fastingReason.displayName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "No meal plan today. Wishing you a peaceful fast.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else if (todayPlanned != null && todayPlanned.meals.isNotEmpty()) {
            item {
                Text(
                    "Today's meals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(todayPlanned.meals.size) { i ->
                val meal = todayPlanned.meals[i]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(meal.mealType.displayName, style = MaterialTheme.typography.labelLarge)
                        Text(
                            meal.recipe.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        NutritionRow(meal.recipe.nutritionPerServing)
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Whole day nutrition",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        NutritionRow(todayPlanned.dayNutrition)
                    }
                }
            }
        } else {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "No meal plan for today. Generate one from the Plan tab.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (tasksToday.isNotEmpty()) {
            item {
                Text(
                    "Today's grocery & prep tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(tasksToday.size) { i ->
                val task = tasksToday[i]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            when (task.type) {
                                TaskType.BUY -> "🛒"
                                TaskType.SOAK -> "💧"
                                TaskType.FERMENT -> "🌱"
                            }
                        )
                        Column {
                            Text(
                                "${task.type.displayName}: ${task.ingredientName} (${task.quantityGrams.toInt()} g)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "For ${task.recipeName} on ${task.cookDate}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
