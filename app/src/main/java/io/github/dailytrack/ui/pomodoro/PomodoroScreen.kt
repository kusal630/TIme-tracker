package io.github.dailytrack.ui.pomodoro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.data.api.QuotesApi
import io.github.dailytrack.service.PomodoroForegroundService
import io.github.dailytrack.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val quoteColors = listOf(
    Color(0xFFE94560),
    Color(0xFF0F3460),
    Color(0xFF533483),
    Color(0xFF2E7D32),
    Color(0xFFF57C00),
    Color(0xFF00838F),
    Color(0xFF6A1B9A),
    Color(0xFFC62828),
    Color(0xFF1565C0),
    Color(0xFF4E342E)
)

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
    val savedQuotes by viewModel.savedQuotes.collectAsState()
    
    var isBreak by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var selectedWorkDuration by remember { mutableIntStateOf(25) }
    var selectedBreakDuration by remember { mutableIntStateOf(5) }
    var selectedTodoId by remember { mutableLongStateOf(0L) }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    
    var flashProgress by remember { mutableFloatStateOf(0f) }
    var flashColor by remember { mutableStateOf(Color(0xFFE94560)) }
    
    var currentQuote by remember { mutableStateOf("Loading inspiring quote...") }
    var quoteAuthor by remember { mutableStateOf("") }
    var quoteColor by remember { mutableStateOf(quoteColors[0]) }
    var quoteIndex by remember { mutableIntStateOf(0) }

    val isRunning = activePomodoro != null
    val coroutineScope = rememberCoroutineScope()

    val animatedFlashProgress by animateFloatAsState(
        targetValue = flashProgress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "flash_progress"
    )

    val animatedQuoteColor by animateColorAsState(
        targetValue = quoteColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "quote_color"
    )

    val animatedStartButtonColor by animateColorAsState(
        targetValue = quoteColor,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "start_button_color"
    )

    val screenColor = if (isRunning) {
        if (isBreak) {
            val breakColor = Color(0xFFFFAB40)
            val progress = (elapsedSeconds.toFloat() / ((activePomodoro?.durationMinutes ?: selectedBreakDuration) * 60)).coerceIn(0f, 1f)
            val fromColor = Color(0xFFE94560)
            Color(
                red = fromColor.red + (breakColor.red - fromColor.red) * progress,
                green = fromColor.green + (breakColor.green - fromColor.green) * progress,
                blue = fromColor.blue + (breakColor.blue - fromColor.blue) * progress
            )
        } else {
            val workColor = quoteColor
            val breakColor = Color(0xFFFFAB40)
            val progress = (elapsedSeconds.toFloat() / ((activePomodoro?.durationMinutes ?: selectedWorkDuration) * 60)).coerceIn(0f, 1f)
            Color(
                red = workColor.red + (breakColor.red - workColor.red) * progress,
                green = workColor.green + (breakColor.green - workColor.green) * progress,
                blue = workColor.blue + (breakColor.blue - workColor.blue) * progress
            )
        }
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    LaunchedEffect(Unit) {
        val quote = QuotesApi.getRandomQuote()
        currentQuote = quote.text
        quoteAuthor = quote.author
        quoteColor = quoteColors[quoteIndex % quoteColors.size]
        quoteIndex++
    }

    LaunchedEffect(activePomodoro) {
        if (activePomodoro != null) {
            isBreak = activePomodoro?.type == "BREAK"
            elapsedSeconds = (System.currentTimeMillis() - (activePomodoro?.startTime ?: 0L)) / 1000
            while (true) {
                delay(1000)
                elapsedSeconds = (System.currentTimeMillis() - (activePomodoro?.startTime ?: 0L)) / 1000
                
                val totalSeconds = (activePomodoro?.durationMinutes ?: 25) * 60L
                if (elapsedSeconds >= totalSeconds) {
                    flashProgress = 1f
                    flashColor = if (isBreak) Color(0xFFE94560) else Color(0xFFFFAB40)
                    delay(800)
                    flashProgress = 0f
                    
                    val quote = QuotesApi.getRandomQuote()
                    currentQuote = quote.text
                    quoteAuthor = quote.author
                    quoteColor = quoteColors[quoteIndex % quoteColors.size]
                    quoteIndex++
                    
                    if (isBreak) {
                        viewModel.startPomodoro(
                            if (selectedTodoId > 0) selectedTodoId else null,
                            if (selectedCategoryId > 0) selectedCategoryId else null,
                            selectedWorkDuration
                        )
                        isBreak = false
                    } else {
                        viewModel.completePomodoro()
                        viewModel.startPomodoroBreak(selectedBreakDuration)
                        isBreak = true
                    }
                    elapsedSeconds = 0
                }
            }
        } else {
            elapsedSeconds = 0
            flashProgress = 0f
        }
    }

    DisposableEffect(Unit) {
        val workReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.completePomodoro()
            }
        }
        val breakReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.completePomodoro()
            }
        }
        val filter = IntentFilter().apply {
            addAction(PomodoroForegroundService.ACTION_COMPLETE_WORK)
            addAction(PomodoroForegroundService.ACTION_COMPLETE_BREAK)
        }
        ContextCompat.registerReceiver(context, workReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        ContextCompat.registerReceiver(context, breakReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose {
            try {
                context.unregisterReceiver(workReceiver)
                context.unregisterReceiver(breakReceiver)
            } catch (_: Exception) {}
        }
    }

    val remainingSeconds = if (isRunning) {
        val totalSeconds = (activePomodoro?.durationMinutes ?: selectedWorkDuration) * 60L
        (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    } else {
        selectedWorkDuration * 60L
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val progress = if (isRunning) {
        elapsedSeconds.toFloat() / ((activePomodoro?.durationMinutes ?: selectedWorkDuration) * 60)
    } else 0f

    val completedPomodorosToday = todayPomodoros.count { it.type == "WORK" && it.isCompleted }
    val completedBreaksToday = todayPomodoros.count { it.type == "BREAK" && it.isCompleted }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing),
        label = "progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pomodoro Timer") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("saved_quotes") }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Saved Quotes")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (flashProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(animatedFlashProgress)
                        .background(
                            Brush.verticalGradient(
                                0f to flashColor.copy(alpha = 0.8f),
                                0.5f to flashColor.copy(alpha = 0.4f),
                                1f to flashColor.copy(alpha = 0.1f)
                            )
                        )
                )
            }

            if (isRunning) {
                val cardColor = if (isBreak) {
                    val breakColor = Color(0xFFFFAB40)
                    val workStart = Color(0xFFE94560)
                    val p = (elapsedSeconds.toFloat() / ((activePomodoro?.durationMinutes ?: selectedBreakDuration) * 60)).coerceIn(0f, 1f)
                    Color(
                        red = workStart.red + (breakColor.red - workStart.red) * p,
                        green = workStart.green + (breakColor.green - workStart.green) * p,
                        blue = workStart.blue + (breakColor.blue - workStart.blue) * p
                    ).copy(alpha = 0.15f)
                } else {
                    val workColor = quoteColor
                    val breakColor = Color(0xFFFFAB40)
                    val p = (elapsedSeconds.toFloat() / ((activePomodoro?.durationMinutes ?: selectedWorkDuration) * 60)).coerceIn(0f, 1f)
                    Color(
                        red = workColor.red + (breakColor.red - workColor.red) * p,
                        green = workColor.green + (breakColor.green - workColor.green) * p,
                        blue = workColor.blue + (breakColor.blue - workColor.blue) * p
                    ).copy(alpha = 0.15f)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to cardColor,
                                0.5f to cardColor.copy(alpha = 0.08f),
                                1f to Color.Transparent
                            )
                        )
                )
            }

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
                            containerColor = if (isRunning && !isBreak) screenColor.copy(alpha = 0.2f)
                            else if (isBreak) screenColor.copy(alpha = 0.2f)
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
                                color = if (isRunning && !isBreak) screenColor
                                else if (isBreak) screenColor
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.size(180.dp),
                                    strokeWidth = 12.dp,
                                    color = screenColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isRunning && !isBreak) screenColor
                                    else if (isBreak) screenColor
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Focus: $completedPomodorosToday | Break: $completedBreaksToday",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = animatedQuoteColor.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                val isSaved by remember(currentQuote, savedQuotes) {
                                    derivedStateOf { savedQuotes.any { it.text == currentQuote } }
                                }
                                IconButton(
                                    onClick = {
                                        if (isSaved) {
                                            val savedQuote = savedQuotes.find { it.text == currentQuote }
                                            savedQuote?.let { viewModel.deleteSavedQuote(it) }
                                        } else {
                                            viewModel.saveQuote(currentQuote, quoteAuthor)
                                        }
                                    }
                                ) {
                                    Icon(
                                        if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = if (isSaved) "Unsave quote" else "Save quote",
                                        tint = if (isSaved) Color(0xFFE94560) else animatedQuoteColor
                                    )
                                }
                            }
                            Text(
                                text = "\"",
                                style = MaterialTheme.typography.headlineLarge,
                                color = animatedQuoteColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentQuote,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = animatedQuoteColor,
                                fontWeight = FontWeight.Medium
                            )
                            if (quoteAuthor.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "— $quoteAuthor",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = animatedQuoteColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                if (!isRunning) {
                    item {
                        Text(
                            "Focus Duration (minutes)",
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
                                    selected = selectedWorkDuration == duration,
                                    onClick = { selectedWorkDuration = duration },
                                    label = { Text("${duration}m") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            "Break Duration (minutes)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(3, 5, 10, 15, 20).forEach { duration ->
                                FilterChip(
                                    selected = selectedBreakDuration == duration,
                                    onClick = { selectedBreakDuration = duration },
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
                                flashProgress = 1f
                                flashColor = quoteColor
                                
                                coroutineScope.launch {
                                    delay(800)
                                    flashProgress = 0f
                                }

                                val todoTitle = activeTodos.find { it.id == selectedTodoId }?.title ?: ""
                                val colorInt = android.graphics.Color.rgb(
                                    (quoteColor.red * 255).toInt(),
                                    (quoteColor.green * 255).toInt(),
                                    (quoteColor.blue * 255).toInt()
                                )
                                val intent = Intent(context, PomodoroForegroundService::class.java).apply {
                                    putExtra(PomodoroForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                                    putExtra(PomodoroForegroundService.EXTRA_DURATION, selectedWorkDuration)
                                    putExtra(PomodoroForegroundService.EXTRA_TODO_TITLE, todoTitle)
                                    putExtra(PomodoroForegroundService.EXTRA_IS_BREAK, false)
                                    putExtra(PomodoroForegroundService.EXTRA_WORK_COLOR, colorInt)
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                                
                                viewModel.startPomodoro(
                                    if (selectedTodoId > 0) selectedTodoId else null,
                                    if (selectedCategoryId > 0) selectedCategoryId else null,
                                    selectedWorkDuration
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = animatedStartButtonColor
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Focus Session", fontSize = 16.sp)
                        }
                    }
                } else {
                    item {
                        Button(
                            onClick = {
                                val intent = Intent(context, PomodoroForegroundService::class.java).apply {
                                    action = if (isBreak) PomodoroForegroundService.ACTION_COMPLETE_BREAK
                                    else PomodoroForegroundService.ACTION_COMPLETE_WORK
                                }
                                context.startService(intent)
                                viewModel.completePomodoro()
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (isBreak) "Skip Break" else "End Session",
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                val workSessions = todayPomodoros.filter { it.type == "WORK" }
                if (workSessions.isNotEmpty()) {
                    item {
                        Text(
                            "Today's Focus Sessions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(workSessions.reversed(), key = { it.id }) { pomodoro ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Work,
                                    contentDescription = null,
                                    tint = if (pomodoro.isCompleted) Color(0xFFE94560) else Color(0xFF757575)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Focus Session",
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
}
