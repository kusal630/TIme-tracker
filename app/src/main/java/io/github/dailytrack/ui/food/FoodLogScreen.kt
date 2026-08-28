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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.ui.components.*
import io.github.dailytrack.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val todayWater by viewModel.todayWater.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val todayProtein by viewModel.todayProtein.collectAsState()
    val todayFiber by viewModel.todayFiber.collectAsState()

    var selectedMealType by remember { mutableStateOf("BREAKFAST") }
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var waterAmount by remember { mutableStateOf("") }

    val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Food & Drink") },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Today: ${todayWater.toInt()} ml",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Target: 2000 ml",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(250, 500, 750, 1000).forEach { amount ->
                            FilledTonalButton(
                                onClick = { viewModel.logWater(amount.toDouble()) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("${amount}ml")
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = waterAmount,
                            onValueChange = { waterAmount = it },
                            label = { Text("Custom (ml)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                val ml = waterAmount.toDoubleOrNull() ?: 0.0
                                if (ml > 0) {
                                    viewModel.logWater(ml)
                                    waterAmount = ""
                                }
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Log Food")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            mealTypes.forEach { type ->
                                FilterChip(
                                    selected = selectedMealType == type,
                                    onClick = { selectedMealType = type },
                                    label = {
                                        Text(
                                            type.lowercase().replaceFirstChar { it.uppercase() },
                                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Food name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = calories,
                                onValueChange = { calories = it },
                                label = { Text("Calories") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = protein,
                                onValueChange = { protein = it },
                                label = { Text("Protein (g)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fiber,
                                onValueChange = { fiber = it },
                                label = { Text("Fiber (g)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (foodName.isNotBlank()) {
                                    viewModel.logFood(
                                        name = foodName,
                                        mealType = selectedMealType,
                                        calories = calories.toDoubleOrNull() ?: 0.0,
                                        protein = protein.toDoubleOrNull() ?: 0.0,
                                        fiber = fiber.toDoubleOrNull() ?: 0.0
                                    )
                                    foodName = ""
                                    calories = ""
                                    protein = ""
                                    fiber = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Food")
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Today's Summary")
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SummaryRow("Water", "${todayWater.toInt()} ml", "Target: 2000 ml")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRow("Calories", "${todayCalories.toInt()} kcal", "No target set")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRow("Protein", "${todayProtein.toInt()}g", "Target: 50g")
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        SummaryRow("Fiber", "${todayFiber.toInt()}g", "Target: 25g")
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
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
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

@Composable
fun MedicalDisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Medical Disclaimer",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Text(
                text = "Nutrition information is for educational purposes only and is not medical advice. If you have dietary concerns, please consult a healthcare professional.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
