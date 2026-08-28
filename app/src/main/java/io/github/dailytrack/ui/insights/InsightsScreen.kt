package io.github.dailytrack.ui.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.data.db.entity.InsightEntity
import io.github.dailytrack.ui.viewmodel.MainViewModel
import io.github.dailytrack.ui.viewmodel.WeeklyStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val activeInsights by viewModel.activeInsights.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val todayCoverage by viewModel.todayCoverage.collectAsState()
    val growthScore by viewModel.growthScore.collectAsState()
    val activeTodoCount by viewModel.activeTodoCount.collectAsState()
    val completedTodoCount by viewModel.completedTodoCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") },
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
                StatsOverviewCard(
                    weeklyStats = weeklyStats,
                    todayCoverage = todayCoverage,
                    growthScore = growthScore,
                    activeTodoCount = activeTodoCount,
                    completedTodoCount = completedTodoCount
                )
            }

            item {
                WeeklyChartCard(weeklyStats = weeklyStats)
            }

            item {
                TodoProgressCard(
                    activeCount = activeTodoCount,
                    completedCount = completedTodoCount
                )
            }

            if (activeInsights.isNotEmpty()) {
                item {
                    Text(
                        "Smart Insights (${activeInsights.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(activeInsights, key = { it.id }) { insight ->
                    InsightDetailCard(insight = insight, onDismiss = { viewModel.dismissInsight(insight.id) })
                }
            }
        }
    }
}

@Composable
fun StatsOverviewCard(
    weeklyStats: WeeklyStats,
    todayCoverage: io.github.dailytrack.engine.TimeCoverage?,
    growthScore: Double,
    activeTodoCount: Int,
    completedTodoCount: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Weekly Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Productive", "${weeklyStats.totalProductiveMinutes / 60}h", Color(0xFF2E7D32))
                StatItem("Wasted", "${weeklyStats.totalWastedMinutes / 60}h", Color(0xFFC62828))
                StatItem("Free", "${weeklyStats.totalFreeMinutes / 60}h", Color(0xFF757575))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Growth", "${growthScore.toInt()}%", MaterialTheme.colorScheme.primary)
                StatItem("Todos Done", "$completedTodoCount", Color(0xFF2E7D32))
                StatItem("Todos Pending", "$activeTodoCount", Color(0xFFF57C00))
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
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
fun WeeklyChartCard(weeklyStats: WeeklyStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Weekly Time Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val total = (weeklyStats.totalProductiveMinutes + weeklyStats.totalWastedMinutes + weeklyStats.totalFreeMinutes).toFloat()
            
            if (total > 0) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val productiveWidth = (weeklyStats.totalProductiveMinutes / total) * size.width
                    val wastedWidth = (weeklyStats.totalWastedMinutes / total) * size.width
                    val freeWidth = (weeklyStats.totalFreeMinutes / total) * size.width
                    
                    drawRect(
                        color = Color(0xFF2E7D32),
                        topLeft = Offset.Zero,
                        size = Size(productiveWidth, size.height)
                    )
                    drawRect(
                        color = Color(0xFFC62828),
                        topLeft = Offset(productiveWidth, 0f),
                        size = Size(wastedWidth, size.height)
                    )
                    drawRect(
                        color = Color(0xFF757575),
                        topLeft = Offset(productiveWidth + wastedWidth, 0f),
                        size = Size(freeWidth, size.height)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LegendItem(Color(0xFF2E7D32), "Productive")
                    LegendItem(Color(0xFFC62828), "Wasted")
                    LegendItem(Color(0xFF757575), "Free")
                }
            } else {
                Text(
                    "No data available yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TodoProgressCard(activeCount: Int, completedCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Todo Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            val total = activeCount + completedCount
            val progress = if (total > 0) completedCount.toFloat() / total else 0f
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = Color(0xFF2E7D32),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$completedCount completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    "$activeCount pending",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF57C00)
                )
            }
        }
    }
}

@Composable
fun InsightDetailCard(insight: InsightEntity, onDismiss: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (insight.severity) {
                            "CRITICAL" -> Icons.Default.Warning
                            "WARNING" -> Icons.Default.Info
                            "CAUTION" -> Icons.Default.Lightbulb
                            else -> Icons.Default.Lightbulb
                        },
                        contentDescription = null,
                        tint = when (insight.severity) {
                            "CRITICAL" -> Color(0xFFC62828)
                            "WARNING" -> Color(0xFFF57C00)
                            "CAUTION" -> Color(0xFF1565C0)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = insight.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
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
