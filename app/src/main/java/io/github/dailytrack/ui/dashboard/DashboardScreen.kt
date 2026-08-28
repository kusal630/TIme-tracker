package io.github.dailytrack.ui.dashboard

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.ui.Screen
import io.github.dailytrack.ui.components.*
import io.github.dailytrack.ui.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()
    val todayCoverage by viewModel.todayCoverage.collectAsState()
    val todayWater by viewModel.todayWater.collectAsState()
    val todayCalories by viewModel.todayCalories.collectAsState()
    val growthScore by viewModel.growthScore.collectAsState()
    val activeInsights by viewModel.activeInsights.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("DailyTrack")
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                DatePickerCard(
                    selectedDate = selectedDate,
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            if (activeSession != null) {
                item {
                    ActiveSessionCard(activeSession = activeSession!!, onStop = { viewModel.stopSession() })
                }
            }

            item {
                TimeTrackingCard(coverage = todayCoverage)
            }

            item {
                QuickActionsRow(navController, hasActiveSession = activeSession != null)
            }

            item {
                GrowthScoreCard(score = growthScore)
            }

            item {
                HealthOverviewRow(
                    navController = navController,
                    waterMl = todayWater,
                    calories = todayCalories
                )
            }

            if (activeInsights.isNotEmpty()) {
                item {
                    SectionHeader(title = "Insights")
                }
                items(activeInsights.take(3)) { insight ->
                    InsightCard(insight = insight, onDismiss = { viewModel.dismissInsight(insight.id) })
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
fun DatePickerCard(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onDateSelected(selectedDate.minusDays(1)) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
            }
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = {
                val today = LocalDate.now()
                if (selectedDate < today) onDateSelected(selectedDate.plusDays(1))
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
        }
    }
}

@Composable
fun ActiveSessionCard(activeSession: io.github.dailytrack.data.db.entity.SessionEntity, onStop: () -> Unit) {
    var elapsed by remember { mutableLongStateOf(System.currentTimeMillis() - activeSession.startTime) }

    LaunchedEffect(activeSession.startTime) {
        while (true) {
            elapsed = System.currentTimeMillis() - activeSession.startTime
            kotlinx.coroutines.delay(1000)
        }
    }

    val hours = TimeUnit.MILLISECONDS.toHours(elapsed)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsed) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsed) % 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = activeSession.title.ifBlank { "Active Session" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            FilledTonalButton(onClick = onStop) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop")
            }
        }
    }
}

@Composable
fun TimeTrackingCard(coverage: io.github.dailytrack.engine.TimeCoverage?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    "Productive",
                    formatDuration(coverage?.productiveSeconds ?: 0),
                    Color(0xFF2E7D32)
                )
                MetricItem(
                    "Learning",
                    formatDuration(coverage?.learningSeconds ?: 0),
                    Color(0xFF1565C0)
                )
                MetricItem(
                    "Exercise",
                    formatDuration(coverage?.exerciseSeconds ?: 0),
                    Color(0xFFE65100)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    "Sleep",
                    formatDuration(coverage?.sleepSeconds ?: 0),
                    Color(0xFF311B92)
                )
                MetricItem(
                    "Untracked",
                    formatDuration(coverage?.untrackedSeconds ?: 0),
                    Color(0xFF757575)
                )
                MetricItem(
                    "Tracked",
                    "${((coverage?.trackedRatio ?: 0.0) * 100).toInt()}%",
                    MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (coverage?.trackedRatio ?: 0.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickActionsRow(navController: NavController, hasActiveSession: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilledTonalButton(
            onClick = { navController.navigate(Screen.Timer.route) },
            modifier = Modifier.weight(1f),
            colors = if (hasActiveSession) ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) else ButtonDefaults.filledTonalButtonColors()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    if (hasActiveSession) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(if (hasActiveSession) "View Timer" else "Start Timer", fontSize = 11.sp)
            }
        }
        FilledTonalButton(
            onClick = { navController.navigate(Screen.Food.route) },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.Restaurant, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Log Food", fontSize = 11.sp)
            }
        }
        FilledTonalButton(
            onClick = { navController.navigate(Screen.Food.route) },
            modifier = Modifier.weight(1f)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(Icons.Default.WaterDrop, contentDescription = null)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Log Water", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun GrowthScoreCard(score: Double) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Growth Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Today's progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${score.toInt()}",
                style = MaterialTheme.typography.displaySmall,
                color = when {
                    score >= 70 -> Color(0xFF2E7D32)
                    score >= 40 -> Color(0xFFF57C00)
                    else -> Color(0xFFC62828)
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HealthOverviewRow(
    navController: NavController,
    waterMl: Double,
    calories: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HealthCard(
            title = "Water",
            value = "${waterMl.toInt()} ml",
            subtitle = "Target: 2000 ml",
            progress = (waterMl / 2000.0).coerceIn(0.0, 1.0).toFloat(),
            onClick = { navController.navigate(Screen.Food.route) },
            modifier = Modifier.weight(1f)
        )
        HealthCard(
            title = "Calories",
            value = "${calories.toInt()} kcal",
            subtitle = if (calories > 0) "Logged" else "No meals",
            progress = (calories / 2000.0).coerceIn(0.0, 1.0).toFloat(),
            onClick = { navController.navigate(Screen.Food.route) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun HealthCard(
    title: String,
    value: String,
    subtitle: String,
    progress: Float = 0f,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.animateContentSize()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (progress > 0f) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
fun InsightCard(insight: io.github.dailytrack.data.db.entity.InsightEntity, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (insight.severity) {
                "CRITICAL" -> MaterialTheme.colorScheme.errorContainer
                "WARNING" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                "CAUTION" -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodySmall
                )
                if (insight.actionLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = insight.actionLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
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
                text = "DailyTrack provides educational insights only. It is not medical advice. If you have persistent symptoms, please consult a healthcare professional.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
