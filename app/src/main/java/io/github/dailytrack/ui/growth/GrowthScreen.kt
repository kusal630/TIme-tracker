package io.github.dailytrack.ui.growth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.engine.GrowthScoreResult
import io.github.dailytrack.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val growthScore by viewModel.growthScore.collectAsState()
    val growthResult by viewModel.growthResult.collectAsState()
    val loopStatus by viewModel.loopStatus.collectAsState()
    val todayCoverage by viewModel.todayCoverage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Growth & Routine") },
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
                GrowthScoreDetailCard(score = growthScore, result = growthResult)
            }

            item {
                GrowthComponentsCard(result = growthResult, coverage = todayCoverage)
            }

            item {
                RoutineLoopCard(loopStatus = loopStatus)
            }

            item {
                ComfortZoneCard(loopStatus = loopStatus)
            }
        }
    }
}

@Composable
fun GrowthScoreDetailCard(score: Double, result: GrowthScoreResult?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Growth Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${score.toInt()}",
                style = MaterialTheme.typography.displaySmall,
                color = when {
                    score >= 70 -> Color(0xFF2E7D32)
                    score >= 40 -> Color(0xFFF57C00)
                    else -> Color(0xFFC62828)
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                "Today's progress",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GrowthComponentsCard(result: GrowthScoreResult?, coverage: io.github.dailytrack.engine.TimeCoverage?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Growth Components",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            GrowthComponentRow(
                "Learning",
                formatMinutes(coverage?.learningSeconds ?: 0),
                result?.learningComponent ?: 0.0,
                Color(0xFF1565C0)
            )
            GrowthComponentRow(
                "Productive",
                formatMinutes(coverage?.productiveSeconds ?: 0),
                result?.productiveComponent ?: 0.0,
                Color(0xFF2E7D32)
            )
            GrowthComponentRow(
                "Exercise",
                formatMinutes(coverage?.exerciseSeconds ?: 0),
                result?.exerciseComponent ?: 0.0,
                Color(0xFFE65100)
            )
            GrowthComponentRow(
                "Sleep",
                formatHoursMinutes(coverage?.sleepSeconds ?: 0),
                result?.sleepComponent ?: 0.0,
                Color(0xFF311B92)
            )
            GrowthComponentRow(
                "Social",
                formatMinutes(coverage?.socialSeconds ?: 0),
                result?.socialComponent ?: 0.0,
                Color(0xFF6A1B9A)
            )
            GrowthComponentRow(
                "Novelty",
                "--",
                result?.noveltyComponent ?: 0.0,
                Color(0xFF00838F)
            )
            GrowthComponentRow(
                "Consistency",
                "${((result?.consistencyComponent ?: 0.0) / 5.0 * 7).toInt()}/7 days",
                result?.consistencyComponent ?: 0.0,
                Color(0xFF4E342E)
            )
        }
    }
}

@Composable
fun GrowthComponentRow(label: String, value: String, score: Double, color: Color, totalScore: Double = 100.0) {
    val percentage = if (totalScore > 0) (score / totalScore * 100).toInt() else 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            "${score.toInt()} pts",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.weight(0.5f)
        )
    }
}

@Composable
fun RoutineLoopCard(loopStatus: io.github.dailytrack.engine.LoopDetectionResult?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Routine Loop Detection",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (loopStatus?.isLoopDetected == true) {
                Text(
                    "Loop detected for ${loopStatus.consecutiveDays} consecutive days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "Consider adding new activities to break the pattern.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "No routine loop detected. Your routine has enough variety.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ComfortZoneCard(loopStatus: io.github.dailytrack.engine.LoopDetectionResult?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Comfort Zone Warnings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (loopStatus?.isLoopDetected == true && loopStatus.consecutiveDays >= 5) {
                Text(
                    "You've been in your comfort zone for ${loopStatus.consecutiveDays} days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "Try something new today to stimulate growth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "No comfort zone warnings. Keep pushing your boundaries!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatMinutes(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return if (minutes > 0) "${minutes}m ${secs}s" else "${secs}s"
}

fun formatHoursMinutes(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m ${secs}s"
        minutes > 0 -> "${minutes}m ${secs}s"
        else -> "${secs}s"
    }
}
