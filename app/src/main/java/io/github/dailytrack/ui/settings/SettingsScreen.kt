package io.github.dailytrack.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var productiveMinutes by remember { mutableStateOf("60") }
    var learningMinutes by remember { mutableStateOf("30") }
    var exerciseMinutes by remember { mutableStateOf("30") }
    var sleepTarget by remember { mutableStateOf("8") }

    var enableNutrition by remember { mutableStateOf(true) }
    var enableExercise by remember { mutableStateOf(true) }
    var maintenanceMode by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    val exportData = """{"app":"DailyTrack","version":"0.1.0","exportDate":"${java.time.Instant.now()}"}"""
                    outputStream.write(exportData.toByteArray())
                }
                Toast.makeText(context, "Data exported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val data = inputStream.readBytes().toString(Charsets.UTF_8)
                    Toast.makeText(context, "Data imported successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
                SettingsSection(title = "Targets", icon = Icons.Default.Flag) {
                    TargetInput("Productive minutes/day", productiveMinutes) { productiveMinutes = it }
                    TargetInput("Learning minutes/day", learningMinutes) { learningMinutes = it }
                    TargetInput("Exercise minutes/day", exerciseMinutes) { exerciseMinutes = it }
                    TargetInput("Sleep target (hours)", sleepTarget) { sleepTarget = it }
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
                    ModuleSwitch("Exercise & Movement", Icons.Default.FitnessCenter, enableExercise) { enableExercise = it }
                }
            }

            item {
                SettingsSection(title = "Privacy", icon = Icons.Default.Shield) {
                    PrivacySwitch("Maintenance mode", Icons.Default.Pause, maintenanceMode) { maintenanceMode = it }
                }
            }

            item {
                SettingsSection(title = "Data", icon = Icons.Default.Storage) {
                    ActionButton("Export Data", Icons.Default.FileDownload) {
                        exportLauncher.launch("dailytrack_export.json")
                    }
                    ActionButton("Import Data", Icons.Default.FileUpload) {
                        importLauncher.launch(arrayOf("application/json"))
                    }
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
                    ActionButton("View Licenses", Icons.Default.Description) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dailytrack/dailytrack/blob/main/LICENSE"))
                        context.startActivity(intent)
                    }
                    ActionButton("GitHub Repository", Icons.Default.Code) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dailytrack/dailytrack"))
                        context.startActivity(intent)
                    }
                    ActionButton("Delete All Data", Icons.Default.Delete, isDestructive = true) {
                        // TODO: Implement data deletion with confirmation dialog
                        Toast.makeText(context, "Coming soon", Toast.LENGTH_SHORT).show()
                    }
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
fun ActionButton(label: String, icon: ImageVector, isDestructive: Boolean = false, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
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
