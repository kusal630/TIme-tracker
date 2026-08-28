package io.github.dailytrack.ui.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(navController: NavController) {
    var waterAmount by remember { mutableStateOf("") }
    var foodName by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("BREAKFAST") }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Quick Water Log",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(250, 500, 750, 1000).forEach { amount ->
                                FilledTonalButton(
                                    onClick = { waterAmount = "" },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("${amount}ml")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                            FilledTonalButton(onClick = { waterAmount = "" }) {
                                Text("Add")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Log Food",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
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
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = foodName,
                            onValueChange = { foodName = it },
                            label = { Text("Food name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { foodName = "" },
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
                Text(
                    "Food logging is an optional module. Enable it in Settings to track nutrition.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
