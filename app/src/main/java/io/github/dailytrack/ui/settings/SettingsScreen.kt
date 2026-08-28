package io.github.dailytrack.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var productiveMinutes by remember { mutableStateOf("60") }
    var learningMinutes by remember { mutableStateOf("30") }
    var exerciseMinutes by remember { mutableStateOf("30") }
    var sleepTarget by remember { mutableStateOf("8") }
    var waterTarget by remember { mutableStateOf("2000") }

    var enableNutrition by remember { mutableStateOf(true) }
    var enableBodyMetrics by remember { mutableStateOf(true) }
    var enableHydration by remember { mutableStateOf(true) }
    var enableExercise by remember { mutableStateOf(true) }
    var maintenanceMode by remember { mutableStateOf(false) }
    var fluidRestriction by remember { mutableStateOf(false) }
    var hideWeight by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SettingsSection(title = "Profile", icon = Icons.Default.Person) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = height,
                            onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            item {
                SettingsSection(title = "Targets", icon = Icons.Default.Flag) {
                    TargetInput("Productive minutes/day", productiveMinutes) { productiveMinutes = it }
                    TargetInput("Learning minutes/day", learningMinutes) { learningMinutes = it }
                    TargetInput("Exercise minutes/day", exerciseMinutes) { exerciseMinutes = it }
                    TargetInput("Sleep target (hours)", sleepTarget) { sleepTarget = it }
                    TargetInput("Water target (ml)", waterTarget) { waterTarget = it }
                }
            }

            item {
                SettingsSection(title = "Modules", icon = Icons.Default.Extension) {
                    Text(
                        "Enable or disable tracking modules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ModuleSwitch("Food & Nutrition", Icons.Default.Restaurant, enableNutrition) { enableNutrition = it }
                    ModuleSwitch("Hydration Tracking", Icons.Default.WaterDrop, enableHydration) { enableHydration = it }
                    ModuleSwitch("Exercise & Movement", Icons.Default.FitnessCenter, enableExercise) { enableExercise = it }
                    ModuleSwitch("Body Metrics", Icons.Default.MonitorHeart, enableBodyMetrics) { enableBodyMetrics = it }
                }
            }

            item {
                SettingsSection(title = "Privacy", icon = Icons.Default.Shield) {
                    PrivacySwitch("Hide weight & calories", Icons.Default.VisibilityOff, hideWeight) { hideWeight = it }
                    PrivacySwitch("Maintenance mode", Icons.Default.Pause, maintenanceMode) { maintenanceMode = it }
                    PrivacySwitch("Fluid restriction", Icons.Default.WaterDrop, fluidRestriction) { fluidRestriction = it }
                }
            }

            item {
                SettingsSection(title = "Notifications", icon = Icons.Default.Notifications) {
                    NotificationSwitch("Timer notification", true)
                    NotificationSwitch("Water reminder", false)
                    NotificationSwitch("Learning reminder", false)
                    NotificationSwitch("Sleep wind-down", false)
                    NotificationSwitch("Insight notifications", false)
                }
            }

            item {
                SettingsSection(title = "About", icon = Icons.Default.Info) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("DailyTrack", fontWeight = FontWeight.Medium)
                            Text(
                                "v0.1.0",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Offline-first, privacy-first",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    AboutButton("View Licenses", Icons.Default.Description)
                    AboutButton("Export Data", Icons.Default.FileDownload)
                    AboutButton("Delete All Data", Icons.Default.Delete, isDestructive = true)
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun TargetInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}

@Composable
fun ModuleSwitch(label: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun PrivacySwitch(label: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun NotificationSwitch(label: String, checked: Boolean) {
    var isChecked by remember { mutableStateOf(checked) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
    }
}

@Composable
fun AboutButton(label: String, icon: ImageVector, isDestructive: Boolean = false) {
    OutlinedButton(
        onClick = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = if (isDestructive) ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ) else ButtonDefaults.outlinedButtonColors()
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label)
    }
}
