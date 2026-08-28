package io.github.dailytrack.ui.timer

import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.service.TimerForegroundService
import io.github.dailytrack.ui.components.*
import io.github.dailytrack.ui.viewmodel.MainViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var sessionTitle by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }

    val isRunning = activeSession != null
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(activeSession) {
        if (activeSession != null) {
            while (true) {
                elapsedSeconds = (System.currentTimeMillis() - (activeSession?.startTime ?: 0L)) / 1000
                kotlinx.coroutines.delay(1000)
            }
        } else {
            elapsedSeconds = 0
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60

    val categoryList = remember(categories) {
        listOf(0L to "No category") + categories.values
            .filter { !it.archived }
            .sortedBy { it.name }
            .map { it.id to it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timer") },
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRunning)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRunning) {
                                activeSession?.title?.ifBlank { "Active Session" } ?: "Active Session"
                            } else "Ready to start",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        if (isRunning) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Started at ${java.time.Instant.ofEpochMilli(activeSession?.startTime ?: 0L)
                                    .atZone(java.time.ZoneId.systemDefault())
                                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = sessionTitle,
                    onValueChange = { sessionTitle = it },
                    label = { Text("Session title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isRunning
                )
            }

            item {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategoryDropdown(
                    categories = categoryList,
                    selectedId = selectedCategoryId,
                    onSelected = { selectedCategoryId = it ?: 0L }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (isRunning) {
                                viewModel.stopSession()
                                context.stopService(Intent(context, TimerForegroundService::class.java))
                                sessionTitle = ""
                                selectedCategoryId = 0L
                            } else {
                                val catName = categories[selectedCategoryId]?.name ?: ""
                                viewModel.startSession(sessionTitle, selectedCategoryId)
                                val intent = Intent(context, TimerForegroundService::class.java).apply {
                                    putExtra(TimerForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                                    putExtra(TimerForegroundService.EXTRA_TITLE, sessionTitle.ifBlank { "Active Session" })
                                    putExtra(TimerForegroundService.EXTRA_CATEGORY, catName)
                                }
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRunning) "Stop" else "Start", fontSize = 16.sp)
                    }
                }
            }

            if (isRunning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Active Session Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duration", style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if ((activeSession?.title ?: "").isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Title", style = MaterialTheme.typography.bodyMedium)
                                    Text(activeSession?.title ?: "", fontWeight = FontWeight.Bold)
                                }
                            }
                            val activeCategoryName = activeSession?.categoryId?.let { categories[it]?.name } ?: ""
                            if (activeCategoryName.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Category", style = MaterialTheme.typography.bodyMedium)
                                    Text(activeCategoryName, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                MedicalDisclaimerCard()
            }
        }
    }
}
