package com.foodroutine.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.foodroutine.core.model.NutritionFacts
import kotlin.math.roundToInt

@Composable
fun NutritionRow(n: NutritionFacts, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("${n.calories.roundToInt()} kcal", style = MaterialTheme.typography.bodySmall)
        Text("P ${n.proteinG.roundToInt()} g", style = MaterialTheme.typography.bodySmall)
        Text("C ${n.carbsG.roundToInt()} g", style = MaterialTheme.typography.bodySmall)
        Text("Fib ${n.fiberG.roundToInt()} g", style = MaterialTheme.typography.bodySmall)
    }
}
