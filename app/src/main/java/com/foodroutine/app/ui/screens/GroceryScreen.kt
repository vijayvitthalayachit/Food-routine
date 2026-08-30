package com.foodroutine.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.foodroutine.app.viewmodel.AppViewModel
import com.foodroutine.core.grocery.TaskType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GroceryScreen(vm: AppViewModel) {
    val plan by vm.activePlan.collectAsState()

    val p = plan
    if (p == null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Text(
                "Grocery planner",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Generate a meal plan first. The shopping list is derived from it: " +
                    "regular items 1 day ahead, soaked items 2 days ahead and " +
                    "sprouted items 3 days ahead.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        return
    }

    val today = LocalDate.now()
    val tasksByDate = remember(p) {
        vm.groceryTasks(p)
            .filter { it.date >= today.minusDays(1) }
            .groupBy { it.date }
            .toSortedMap()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Shopping & prep timeline",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        tasksByDate.forEach { (date, tasks) ->
            item {
                Text(
                    when (date) {
                        today -> "Today · " + date.format(DateTimeFormatter.ofPattern("d MMM"))
                        today.plusDays(1) -> "Tomorrow · " + date.format(DateTimeFormatter.ofPattern("d MMM"))
                        else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            items(tasks.size) { i ->
                val task = tasks[i]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (task.type == TaskType.BUY) {
                        CardDefaults.cardColors()
                    } else {
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                ) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            when (task.type) {
                                TaskType.BUY -> "🛒"
                                TaskType.SOAK -> "💧"
                                TaskType.FERMENT -> "🌱"
                            }
                        )
                        Column {
                            Text(
                                "${task.type.displayName}: ${task.ingredientName} " +
                                    "(${task.quantityGrams.toInt()} g)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "For ${task.recipeName} · cooking on " +
                                    task.cookDate.format(DateTimeFormatter.ofPattern("d MMM")),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
