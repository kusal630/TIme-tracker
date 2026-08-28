package io.github.dailytrack.ui.todo

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.dailytrack.data.db.entity.TodoEntity
import io.github.dailytrack.ui.viewmodel.MainViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val activeTodos by viewModel.activeTodos.collectAsState()
    val completedTodos by viewModel.completedTodos.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todo List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Todo")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (activeTodos.isNotEmpty()) {
                item {
                    Text(
                        "Active Tasks (${activeTodos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(activeTodos, key = { it.id }) { todo ->
                    TodoItemCard(
                        todo = todo,
                        categories = categories,
                        onComplete = { viewModel.completeTodo(todo.id) },
                        onDelete = { viewModel.deleteTodo(todo) },
                        onStartPomodoro = { viewModel.startPomodoro(todo.id, todo.categoryId, 25) }
                    )
                }
            }

            if (completedTodos.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Completed (${completedTodos.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
                items(completedTodos.take(10), key = { it.id }) { todo ->
                    CompletedTodoItemCard(todo = todo, categories = categories)
                }
            }

            if (activeTodos.isEmpty() && completedTodos.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No tasks yet",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Add your first todo to get started",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onAdd = { title, description, categoryId, deadline, estimatedMinutes ->
                viewModel.addTodo(title, description, categoryId, deadline, estimatedMinutes)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TodoItemCard(
    todo: TodoEntity,
    categories: Map<Long, io.github.dailytrack.data.db.entity.CategoryEntity>,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onStartPomodoro: () -> Unit
) {
    val categoryName = todo.categoryId?.let { categories[it]?.name } ?: ""
    val deadlineStr = todo.deadline?.let {
        java.time.Instant.ofEpochMilli(it)
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = { onComplete() }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (categoryName.isNotBlank() || deadlineStr != null) {
                    Row {
                        if (categoryName.isNotBlank()) {
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (deadlineStr != null) {
                            if (categoryName.isNotBlank()) {
                                Text(" · ", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "Due: $deadlineStr",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (todo.deadline!! < System.currentTimeMillis()) Color(0xFFC62828)
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (todo.estimatedMinutes > 0) {
                    Text(
                        text = "Est: ${todo.estimatedMinutes}min | Done: ${todo.actualMinutes}min | Pomodoros: ${todo.pomodoroCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onStartPomodoro) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = "Start Pomodoro",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CompletedTodoItemCard(
    todo: TodoEntity,
    categories: Map<Long, io.github.dailytrack.data.db.entity.CategoryEntity>
) {
    val categoryName = todo.categoryId?.let { categories[it]?.name } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (categoryName.isNotBlank()) {
                    Text(
                        text = "$categoryName · ${todo.pomodoroCount} pomodoros · ${todo.actualMinutes}min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    categories: Map<Long, io.github.dailytrack.data.db.entity.CategoryEntity>,
    onDismiss: () -> Unit,
    onAdd: (String, String, Long?, Long?, Int) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableLongStateOf(0L) }
    var deadline by remember { mutableLongStateOf(0L) }
    var estimatedMinutes by remember { mutableStateOf("25") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Todo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = categories[selectedCategoryId]?.name ?: "No category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No category") },
                            onClick = {
                                selectedCategoryId = 0L
                                showCategoryDropdown = false
                            }
                        )
                        categories.values.filter { !it.archived }.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = estimatedMinutes,
                    onValueChange = { estimatedMinutes = it },
                    label = { Text("Estimated minutes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedButton(
                    onClick = {
                        val cal = java.util.Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        cal.set(year, month, day, hour, minute)
                                        deadline = cal.timeInMillis
                                    },
                                    cal.get(java.util.Calendar.HOUR_OF_DAY),
                                    cal.get(java.util.Calendar.MINUTE),
                                    true
                                ).show()
                            },
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH),
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (deadline > 0) {
                        "Deadline: ${java.time.Instant.ofEpochMilli(deadline).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))}"
                    } else "Set Deadline")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(
                            title,
                            description,
                            if (selectedCategoryId == 0L) null else selectedCategoryId,
                            if (deadline > 0) deadline else null,
                            estimatedMinutes.toIntOrNull() ?: 25
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
