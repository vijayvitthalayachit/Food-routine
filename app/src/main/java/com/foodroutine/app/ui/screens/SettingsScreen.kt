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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.foodroutine.app.viewmodel.AppViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val customFasts by vm.customFasts.collectAsState()
    var dateText by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Custom fasting days",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Days added here are treated like Ekadashi: no meals are planned " +
                    "and no groceries are suggested for them.",
                style = MaterialTheme.typography.bodySmall
            )
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dateText,
                        onValueChange = { dateText = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        isError = error != null,
                        supportingText = { error?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Reason (e.g. Karwa Chauth, personal vrat)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(onClick = {
                        try {
                            val date = LocalDate.parse(dateText.trim())
                            vm.addCustomFast(date, label.trim().ifBlank { "Custom fast" })
                            dateText = ""
                            label = ""
                            error = null
                        } catch (e: DateTimeParseException) {
                            error = "Enter the date as YYYY-MM-DD"
                        }
                    }) {
                        Text("Add fasting day")
                    }
                }
            }
        }
        if (customFasts.isNotEmpty()) {
            items(customFasts.size) { i ->
                val fast = customFasts[i]
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                LocalDate.ofEpochDay(fast.epochDay)
                                    .format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy")),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(fast.label, style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { vm.removeCustomFast(fast) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove")
                        }
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "About fasting rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Ekadashi is detected from the tithi at local sunrise. Janmashtami is " +
                    "the Krishna Ashtami of Shravana (amanta) by the midnight rule. " +
                    "Grahana days come from a built-in eclipse table (2025-2028); add " +
                    "other Grahana observances as custom fasts. Tithi timings are " +
                    "computed astronomically and may differ by a day from a printed " +
                    "panchang near tithi boundaries.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
