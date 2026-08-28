package io.github.dailytrack.ui.pomodoro

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
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
    
    var isBreak by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }
    var selectedWorkDuration by remember { mutableIntStateOf(25) }
    var selectedBreakDuration by remember { mutableIntStateOf(5) }
    var selectedTodoId by remember { mutableLongStateOf(0L) }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var showFlash by remember { mutableStateOf(false) }
    var flashColor by remember { mutableStateOf(Color(0xFFE94560)) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }
    
    var currentQuote by remember { mutableStateOf("Loading inspiring quote...") }
    var quoteAuthor by remember { mutableStateOf("") }
    var quoteColor by remember { mutableStateOf(quoteColors[0]) }
    var quoteIndex by remember { mutableIntStateOf(0) }
    
    var timerPulse by remember { mutableFloatStateOf(1f) }
    var cardBorderColor by remember { mutableStateOf(Color.Transparent) }

    val isRunning = activePomodoro != null
    val coroutineScope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "border_alpha"
    )

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
            cardBorderColor = if (isBreak) Color(0xFFFFAB40) else Color(0xFF69F0AE)
            while (true) {
                delay(1000)
                elapsedSeconds = (System.currentTimeMillis() - (activePomodoro?.startTime ?: 0L)) / 1000
                
                val totalSeconds = (activePomodoro?.durationMinutes ?: 25) * 60L
                if (elapsedSeconds >= totalSeconds) {
                    showFlash = true
                    flashAlpha = 1f
                    flashColor = if (isBreak) Color(0xFFE94560) else Color(0xFFFFAB40)
                    delay(500)
                    flashAlpha = 0.7f
                    delay(300)
                    flashAlpha = 0f
                    showFlash = false
                    
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
            cardBorderColor = Color.Transparent
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

    val animatedQuoteColor by animateColorAsState(
        targetValue = quoteColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "quote_color"
    )

    val animatedQuoteTextColor by animateColorAsState(
        targetValue = quoteColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "quote_text_color"
    )

    val animatedQuoteAuthorColor by animateColorAsState(
        targetValue = quoteColor.copy(alpha = 0.7f),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "quote_author_color"
    )

    val animatedStartButtonColor by animateColorAsState(
        targetValue = quoteColor,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "start_button_color"
    )

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
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = showFlash,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(flashAlpha)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    flashColor.copy(alpha = 0.6f),
                                    flashColor.copy(alpha = 0.3f)
                                )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isRunning) Modifier.scale(pulseAnim)
                                else Modifier
                            ),
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
                            AnimatedContent(
                                targetState = if (isBreak) "Break Time" else "Focus Time",
                                transitionSpec = {
                                    fadeIn(tween(300)) + slideInVertically(tween(300)) togetherWith 
                                    fadeOut(tween(300)) + slideOutVertically(tween(300))
                                },
                                label = "title_animation"
                            ) { title ->
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isRunning && !isBreak) Color(0xFF69F0AE)
                                    else if (isBreak) Color(0xFFFFAB40)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.size(180.dp),
                                    strokeWidth = 12.dp,
                                    color = if (isBreak) Color(0xFFFFAB40) else Color(0xFFE94560),
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    AnimatedContent(
                                        targetState = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds),
                                        transitionSpec = {
                                            fadeIn(tween(200)) + slideInVertically(tween(200)) togetherWith 
                                            fadeOut(tween(200)) + slideOutVertically(tween(200))
                                        },
                                        label = "timer_animation"
                                    ) { time ->
                                        Text(
                                            text = time,
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isRunning && !isBreak) Color(0xFF69F0AE)
                                            else if (isBreak) Color(0xFFFFAB40)
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
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
                            Text(
                                text = "\"",
                                style = MaterialTheme.typography.headlineLarge,
                                color = animatedQuoteTextColor,
                                fontWeight = FontWeight.Bold
                            )
                            AnimatedContent(
                                targetState = currentQuote,
                                transitionSpec = {
                                    fadeIn(tween(400)) + slideInVertically(tween(400)) togetherWith 
                                    fadeOut(tween(400)) + slideOutVertically(tween(400))
                                },
                                label = "quote_animation"
                            ) { quote ->
                                Text(
                                    text = quote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = animatedQuoteTextColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (quoteAuthor.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AnimatedContent(
                                    targetState = quoteAuthor,
                                    transitionSpec = {
                                        fadeIn(tween(400)) + slideInVertically(tween(400)) togetherWith 
                                        fadeOut(tween(400)) + slideOutVertically(tween(400))
                                    },
                                    label = "author_animation"
                                ) { author ->
                                    Text(
                                        text = "— $author",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = animatedQuoteAuthorColor
                                    )
                                }
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
                                showFlash = true
                                flashAlpha = 1f
                                flashColor = animatedQuoteColor
                                
                                coroutineScope.launch {
                                    delay(500)
                                    flashAlpha = 0.7f
                                    delay(300)
                                    flashAlpha = 0f
                                    showFlash = false
                                }

                                val todoTitle = activeTodos.find { it.id == selectedTodoId }?.title ?: ""
                                val intent = Intent(context, PomodoroForegroundService::class.java).apply {
                                    putExtra(PomodoroForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                                    putExtra(PomodoroForegroundService.EXTRA_DURATION, selectedWorkDuration)
                                    putExtra(PomodoroForegroundService.EXTRA_TODO_TITLE, todoTitle)
                                    putExtra(PomodoroForegroundService.EXTRA_IS_BREAK, false)
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
                                    action = PomodoroForegroundService.ACTION_COMPLETE
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
