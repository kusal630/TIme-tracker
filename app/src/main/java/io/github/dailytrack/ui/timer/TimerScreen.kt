package io.github.dailytrack.ui.timer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.service.TimerForegroundService
import io.github.dailytrack.ui.viewmodel.MainViewModel

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
    var showCategoryError by remember { mutableStateOf(false) }
    var showTitleError by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val isRunning = activeSession != null
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val catName = categories[selectedCategoryId]?.name ?: ""
            viewModel.startSession(sessionTitle, selectedCategoryId)
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                putExtra(TimerForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                putExtra(TimerForegroundService.EXTRA_TITLE, sessionTitle.ifBlank { catName })
                putExtra(TimerForegroundService.EXTRA_CATEGORY, catName)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

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

    val selectedCategory = categories[selectedCategoryId]
    val isNoCategory = selectedCategoryId == 0L
    val isWastedCategory = selectedCategory?.type == "WASTED"
    val isProductiveCategory = selectedCategory?.type in listOf("PRODUCTIVE", "LEARNING", "EXERCISE")

    val categoryBorderColor = when {
        selectedCategoryId == 0L -> MaterialTheme.colorScheme.outline
        isWastedCategory -> Color(0xFFC62828)
        isProductiveCategory -> Color(0xFF2E7D32)
        else -> MaterialTheme.colorScheme.outline
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
                        containerColor = when {
                            isRunning && isWastedCategory -> Color(0xFF3D1F1F)
                            isRunning && isProductiveCategory -> Color(0xFF1A3D1A)
                            isRunning -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
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
                            color = when {
                                isRunning && isWastedCategory -> Color(0xFFFF6B6B)
                                isRunning && isProductiveCategory -> Color(0xFF69F0AE)
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isRunning) {
                                activeSession?.title?.ifBlank { 
                                    categories[activeSession?.categoryId]?.name ?: "Active Session" 
                                } ?: "Active Session"
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
                Text(
                    text = "Category *",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = if (showCategoryError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (selectedCategoryId != 0L) {
                                Modifier.border(
                                    width = 2.dp,
                                    color = categoryBorderColor,
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else Modifier
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        categories.values
                            .filter { !it.archived }
                            .sortedBy { it.name }
                            .forEach { category ->
                                val isSelected = selectedCategoryId == category.id
                                val categoryColor = when (category.type) {
                                    "WASTED" -> Color(0xFFC62828)
                                    "PRODUCTIVE" -> Color(0xFF2E7D32)
                                    "LEARNING" -> Color(0xFF1565C0)
                                    "EXERCISE" -> Color(0xFFE65100)
                                    "SOCIAL" -> Color(0xFF6A1B9A)
                                    "SLEEP" -> Color(0xFF311B92)
                                    "RECOVERY" -> Color(0xFF4CAF50)
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedCategoryId = category.id
                                            showCategoryError = false
                                        }
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = categoryColor,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                            } else Modifier
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .then(
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = categoryColor,
                                                    shape = RoundedCornerShape(6.dp)
                                                )
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = categoryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                    }
                }
                
                if (showCategoryError) {
                    Text(
                        text = "Please select a category",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = sessionTitle,
                    onValueChange = { 
                        sessionTitle = it
                        showTitleError = false
                    },
                    label = { 
                        Text(if (isNoCategory) "Session name * (required for No category)" else "Session name (optional)") 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isRunning,
                    isError = showTitleError,
                    supportingText = if (showTitleError) {
                        { Text("Please enter a session name") }
                    } else null
                )
            }

            item {
                Button(
                    onClick = {
                        if (isRunning) {
                            viewModel.stopSession()
                            context.stopService(Intent(context, TimerForegroundService::class.java))
                            sessionTitle = ""
                            selectedCategoryId = 0L
                        } else {
                            if (selectedCategoryId == 0L) {
                                showCategoryError = true
                                return@Button
                            }
                            
                            if (isNoCategory && sessionTitle.isBlank()) {
                                showTitleError = true
                                return@Button
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                
                                if (!hasPermission) {
                                    showPermissionDialog = true
                                    return@Button
                                }
                            }

                            val catName = categories[selectedCategoryId]?.name ?: ""
                            viewModel.startSession(sessionTitle, selectedCategoryId)
                            val intent = Intent(context, TimerForegroundService::class.java).apply {
                                putExtra(TimerForegroundService.EXTRA_START_TIME, System.currentTimeMillis())
                                putExtra(TimerForegroundService.EXTRA_TITLE, sessionTitle.ifBlank { catName })
                                putExtra(TimerForegroundService.EXTRA_CATEGORY, catName)
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(intent)
                            } else {
                                context.startService(intent)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
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
                    Text(if (isRunning) "Stop" else "Start Timer", fontSize = 16.sp)
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
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Notification Permission") },
            text = { Text("Soul Track needs notification permission to show the running timer. Please allow notifications.") },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionDialog = false
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
