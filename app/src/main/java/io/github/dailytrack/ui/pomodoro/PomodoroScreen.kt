package io.github.dailytrack.ui.pomodoro

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.service.TimerForegroundService
import io.github.dailytrack.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val activePomodoro by viewModel.activePomodoro.collectAsState()
    val todayPomodoros by viewModel.todayPomodoros.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val activeTodos by viewModel.activeTodos.collectAsState()
    var isBreak by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var selectedDuration by remember { mutableIntStateOf(25) }
    var selectedTodoId by remember { mutableLongStateOf(0L) }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }

    val isRunning = activePomodoro != null

    LaunchedEffect(activePomodoro) {
        if (activePomodoro != null) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - (activePomodoro?.startTime ?: 0L)) / 1000
                kotlinx.coroutines.delay(1000)
                
                val totalSeconds = (activePomodoro?.durationMinutes ?: 25) * 60L
                if (elapsedSeconds >= totalSeconds) {
                    viewModel.completePomodoro()
                    elapsedSeconds = 0
                    isBreak = !isBreak
                    if (isBreak) {
                        viewModel.startPomodoroBreak(5)
                    }
                }
            }
        } else {
            elapsedSeconds = 0
        }
    }

    val minutes = (selectedDuration * 60 - elapsedSeconds) / 60
    val seconds = (selectedDuration * 60 - elapsedSeconds) % 60
    val progress = if (isRunning) elapsedSeconds.toFloat() / (selectedDuration * 60) else 0f

    val completedPomodorosToday = todayPomodoros.count { it.type == "WORK" && it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRunning && !isBreak) Color(0xFF1A3D1A)
                        else if (isBreak) Color(0xFF3D2A1A)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isBreak) "Break Time" else "Focus Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isRunning && !isBreak) Color(0xFF69F0AE)
                            else if (isBreak) Color(0xFFFFAB40)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(180.dp),
                                strokeWidth = 12.dp,
                                color = if (isBreak) Color(0xFFFFAB40) else Color(0xFFE94560),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRunning && !isBreak) Color(0xFF69F0AE)
                                    else if (isBreak) Color(0xFFFFAB40)
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Completed: $completedPomodorosToday pomodoros today",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!isRunning) {
                item {
                    Text(
                        "Duration (minutes)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 25, 30, 45, 60).forEach { duration ->
                            FilterChip(
                                selected = selectedDuration == duration,
                                onClick = { selectedDuration = duration },
                                label = { Text("${duration}m") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Link to Todo (optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    var showTodoDropdown by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = showTodoDropdown,
                        onExpandedChange = { showTodoDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = activeTodos.find { it.id == selectedTodoId }?.title ?: "No todo",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Todo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTodoDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showTodoDropdown,
                            onDismissRequest = { showTodoDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("No todo") },
                                onClick = {
                                    selectedTodoId = 0L
                                    showTodoDropdown = false
                                }
                            )
                            activeTodos.forEach { todo ->
                                DropdownMenuItem(
                                    text = { Text(todo.title) },
                                    onClick = {
                                        selectedTodoId = todo.id
                                        selectedCategoryId = todo.categoryId ?: 0L
                                        showTodoDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.startPomodoro(
                                if (selectedTodoId > 0) selectedTodoId else null,
                                if (selectedCategoryId > 0) selectedCategoryId else null,
                                selectedDuration
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE94560)
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Focus Session", fontSize = 16.sp)
                    }
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.completePomodoro()
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Session", fontSize = 16.sp)
                        }
                    }
                }
            }

            if (todayPomodoros.isNotEmpty()) {
                item {
                    Text(
                        "Today's Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(todayPomodoros.reversed(), key = { it.id }) { pomodoro ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (pomodoro.type == "WORK") Icons.Default.Work else Icons.Default.Coffee,
                                contentDescription = null,
                                tint = if (pomodoro.type == "WORK") Color(0xFFE94560) else Color(0xFFFFAB40)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (pomodoro.type == "WORK") "Focus Session" else "Break",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                val todoName = pomodoro.todoId?.let { todoId ->
                                    activeTodos.find { it.id == todoId }?.title
                                }
                                if (todoName != null) {
                                    Text(
                                        text = todoName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "${pomodoro.durationMinutes}min",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (pomodoro.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
