package io.github.dailytrack.ui.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.dailytrack.ui.components.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(navController: NavController) {
    var selectedMealType by remember { mutableStateOf("BREAKFAST") }
    var foodName by remember { mutableStateOf("") }
    var portionQuantity by remember { mutableStateOf("") }
    var portionUnit by remember { mutableStateOf("serving") }
    var notes by remember { mutableStateOf("") }
    var waterMl by remember { mutableStateOf("") }

    val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
    val drinkTypes = listOf("WATER", "TEA", "COFFEE", "MILK", "JUICE", "SODA", "OTHER")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food & Drink Log") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(title = "Quick Water Log")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        OutlinedTextField(
                            value = waterMl,
                            onValueChange = { waterMl = it },
                            label = { Text("Water (ml)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                val ml = waterMl.toDoubleOrNull() ?: 0.0
                                if (ml > 0) {
                                    waterMl = ""
                                }
                            }
                        ) {
                            Text("Log")
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Log Food")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mealTypes.forEach { type ->
                                FilterChip(
                                    selected = selectedMealType == type,
                                    onClick = { selectedMealType = type },
                                    label = { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Food name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = portionQuantity,
                                onValueChange = { portionQuantity = it },
                                label = { Text("Quantity") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = portionUnit,
                                onValueChange = { portionUnit = it },
                                label = { Text("Unit") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        NotesField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = "Notes"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (foodName.isNotBlank()) {
                                        foodName = ""
                                        portionQuantity = ""
                                        notes = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Food")
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Log Drink")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        var drinkType by remember { mutableStateOf("WATER") }
                        var drinkVolume by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            drinkTypes.forEach { type ->
                                FilterChip(
                                    selected = drinkType == type,
                                    onClick = { drinkType = type },
                                    label = { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = drinkVolume,
                            onValueChange = { drinkVolume = it },
                            label = { Text("Volume (ml)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val ml = drinkVolume.toDoubleOrNull() ?: 0.0
                                    if (ml > 0) {
                                        drinkVolume = ""
                                    }
                                }
                            ) {
                                Text("Add Drink")
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Today's Summary")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        SummaryRow("Water", "0 ml", "Target: 2000 ml")
                        SummaryRow("Calories", "0 kcal", "No meals logged")
                        SummaryRow("Protein", "0g", "--")
                        SummaryRow("Fiber", "0g", "--")
                    }
                }
            }

            item {
                MedicalDisclaimerCard()
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
