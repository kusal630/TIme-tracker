package io.github.dailytrack.sync

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.documentfile.provider.DocumentFile
import io.github.dailytrack.data.db.AppDatabase
import io.github.dailytrack.data.db.entity.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SyncRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val prefs by lazy {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
    }

    private val _syncStatus = MutableStateFlow(SyncStatus.DISABLED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime: StateFlow<String?> = _lastSyncTime

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError

    private var syncJob: Job? = null
    private var pendingWrites = 0
    private val debounceDelay = 2000L

    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    enum class SyncStatus {
        DISABLED, ENABLED, FOLDER_MISSING, PERMISSION_MISSING, SYNCING, ERROR
    }

    val deviceId: String by lazy {
        var id = prefs.getString("device_id", null)
        if (id == null) {
            id = "device-${UUID.randomUUID().toString().take(8)}"
            prefs.edit().putString("device_id", id).apply()
        }
        id
    }

    fun isSyncEnabled(): Boolean = prefs.getBoolean("sync_enabled", false)

    fun getSyncFolderUri(): String? = prefs.getString("sync_folder_uri", null)

    fun enableSync(folderUri: Uri) {
        prefs.edit()
            .putBoolean("sync_enabled", true)
            .putString("sync_folder_uri", folderUri.toString())
            .apply()
        _syncStatus.value = SyncStatus.ENABLED
        startPeriodicSync()
    }

    fun disableSync() {
        prefs.edit()
            .putBoolean("sync_enabled", false)
            .remove("sync_folder_uri")
            .apply()
        _syncStatus.value = SyncStatus.DISABLED
        syncJob?.cancel()
    }

    fun startPeriodicSync() {
        syncJob?.cancel()
        syncJob = scope.launch {
            while (isActive) {
                try {
                    performSync()
                } catch (e: Exception) {
                    _lastError.value = e.message
                    _syncStatus.value = SyncStatus.ERROR
                }
                delay(30_000)
            }
        }
    }

    suspend fun performSync() {
        val folderUri = getSyncFolderUri()?.let { Uri.parse(it) } ?: run {
            _syncStatus.value = SyncStatus.FOLDER_MISSING
            return
        }

        try {
            _syncStatus.value = SyncStatus.SYNCING

            exportToLocalFolder(folderUri)
            importFromLocalFolder(folderUri)

            _lastSyncTime.value = isoFormat.format(Date())
            _lastError.value = null
            _syncStatus.value = SyncStatus.ENABLED
        } catch (e: Exception) {
            _lastError.value = e.message
            _syncStatus.value = SyncStatus.ERROR
        }
    }

    fun triggerExport() {
        scope.launch {
            try {
                val folderUri = getSyncFolderUri()?.let { Uri.parse(it) } ?: return@launch
                exportToLocalFolder(folderUri)
                _lastSyncTime.value = isoFormat.format(Date())
            } catch (e: Exception) {
                _lastError.value = e.message
            }
        }
    }

    private suspend fun exportToLocalFolder(folderUri: Uri) {
        val docFile = DocumentFile.fromTreeUri(context, folderUri) ?: return

        val manifest = SyncManifest(
            lastUpdatedByDeviceId = deviceId,
            lastUpdatedAt = isoFormat.format(Date())
        )
        writeFile(docFile, "manifest.json", manifest.toJson())

        val todos = exportTodos()
        writeFile(docFile, "tasks.json", JSONObject().apply {
            put("todos", JSONArray(todos.map { it.toJson() }))
        }.toString())

        val subtasks = exportSubtasks()
        writeFile(docFile, "subtasks.json", JSONObject().apply {
            put("subtasks", JSONArray(subtasks.map { it.toJson() }))
        }.toString())

        val sessions = exportSessions()
        writeFile(docFile, "sessions.json", JSONObject().apply {
            put("sessions", JSONArray(sessions.map { it.toJson() }))
        }.toString())

        val pomodoros = exportPomodoros()
        writeFile(docFile, "pomodoro_sessions.json", JSONObject().apply {
            put("pomodoros", JSONArray(pomodoros.map { it.toJson() }))
        }.toString())

        val settings = exportSettings()
        writeFile(docFile, "settings.json", settings.toJson())
    }

    private suspend fun importFromLocalFolder(folderUri: Uri) {
        val docFile = DocumentFile.fromTreeUri(context, folderUri) ?: return

        importTodos(docFile)
        importSubtasks(docFile)
        importSessions(docFile)
        importPomodoros(docFile)
        importSettings(docFile)
    }

    private fun exportTodos(): List<SyncTodo> {
        val todoDao = database.todoDao()
        return runBlocking {
            val allTodos = todoDao.getAllTodosSync()
            allTodos.map { todo ->
                val syncId = getOrCreateSyncId("todo_${todo.id}")
                storeSyncMapping(syncId, todo.id, "todo")
                SyncTodo(
                    id = syncId,
                    localId = todo.id,
                    title = todo.title,
                    description = todo.description,
                    categoryId = todo.categoryId,
                    deadline = todo.deadline?.let { isoFormat.format(Date(it)) },
                    isCompleted = todo.isCompleted,
                    completedAt = todo.completedAt?.let { isoFormat.format(Date(it)) },
                    estimatedMinutes = todo.estimatedMinutes,
                    actualMinutes = todo.actualMinutes,
                    pomodoroCount = todo.pomodoroCount,
                    priority = todo.priority,
                    deleted = false,
                    revision = prefs.getLong("todo_rev_${todo.id}", 1),
                    createdAt = isoFormat.format(Date(todo.createdAt)),
                    updatedAt = isoFormat.format(Date(todo.updatedAt))
                )
            }
        }
    }

    private fun exportSubtasks(): List<SyncSubtask> {
        val subtaskDao = database.subtaskDao()
        val todoDao = database.todoDao()
        return runBlocking {
            val allTodos = todoDao.getAllTodosSync()
            val subtasks = mutableListOf<SyncSubtask>()
            for (todo in allTodos) {
                val todoSubtasks = subtaskDao.getSubtasksForTodoSync(todo.id)
                for (st in todoSubtasks) {
                    val syncId = getOrCreateSyncId("subtask_${st.id}")
                    storeSyncMapping(syncId, st.id, "subtask")
                    subtasks.add(SyncSubtask(
                        id = syncId,
                        localId = st.id,
                        todoId = getOrCreateSyncId("todo_${todo.id}"),
                        title = st.title,
                        isCompleted = st.isCompleted,
                        sortOrder = st.sortOrder,
                        deleted = false,
                        revision = 1,
                        createdAt = isoFormat.format(Date(st.createdAt))
                    ))
                }
            }
            subtasks
        }
    }

    private fun exportSessions(): List<SyncSession> {
        val sessionDao = database.sessionDao()
        return runBlocking {
            val sessions = sessionDao.getAllSessionsSync()
            sessions.map { session ->
                val syncId = getOrCreateSyncId("session_${session.id}")
                storeSyncMapping(syncId, session.id, "session")
                SyncSession(
                    id = syncId,
                    localId = session.id,
                    title = session.title,
                    categoryId = session.categoryId,
                    type = session.type,
                    startedAt = isoFormat.format(Date(session.startTime)),
                    endedAt = session.endTime?.let { isoFormat.format(Date(it)) },
                    notes = session.notes,
                    source = session.source,
                    isActive = session.isActive,
                    deleted = false,
                    revision = prefs.getLong("session_rev_${session.id}", 1),
                    createdAt = isoFormat.format(Date(session.createdAt)),
                    updatedAt = isoFormat.format(Date(session.updatedAt))
                )
            }
        }
    }

    private fun exportPomodoros(): List<SyncPomodoroSession> {
        val pomodoroDao = database.pomodoroSessionDao()
        return runBlocking {
            val pomodoros = pomodoroDao.getAllPomodorosSync()
            pomodoros.map { pom ->
                val syncId = getOrCreateSyncId("pomodoro_${pom.id}")
                storeSyncMapping(syncId, pom.id, "pomodoro")
                SyncPomodoroSession(
                    id = syncId,
                    localId = pom.id,
                    todoId = pom.todoId?.let { getOrCreateSyncId("todo_$it") },
                    categoryId = pom.categoryId,
                    startedAt = isoFormat.format(Date(pom.startTime)),
                    endedAt = pom.endTime?.let { isoFormat.format(Date(it)) },
                    plannedMinutes = pom.durationMinutes,
                    actualSeconds = pom.endTime?.let { (it - pom.startTime) / 1000 } ?: 0,
                    type = pom.type,
                    isCompleted = pom.isCompleted,
                    deleted = false,
                    revision = prefs.getLong("pomodoro_rev_${pom.id}", 1),
                    createdAt = isoFormat.format(Date(pom.createdAt))
                )
            }
        }
    }

    private fun exportSettings(): SyncSettings {
        val todoDao = database.todoDao()
        return runBlocking {
            SyncSettings(
                workMinutes = prefs.getInt("work_minutes", 25),
                shortBreakMinutes = prefs.getInt("short_break_minutes", 5),
                longBreakMinutes = prefs.getInt("long_break_minutes", 15),
                soundEnabled = prefs.getBoolean("sound_enabled", true),
                vibrationEnabled = prefs.getBoolean("vibration_enabled", true),
                revision = prefs.getLong("settings_rev", 1),
                updatedAt = isoFormat.format(Date())
            )
        }
    }

    private fun importTodos(docFile: DocumentFile) {
        val content = readFile(docFile, "tasks.json") ?: return
        try {
            val json = JSONObject(content)
            val todosArray = json.getJSONArray("todos")
            val todoDao = database.todoDao()

            for (i in 0 until todosArray.length()) {
                val todoJson = todosArray.getJSONObject(i)
                val syncTodo = parseSyncTodo(todoJson)
                importSingleTodo(syncTodo, todoDao)
            }
        } catch (e: Exception) {
            _lastError.value = "Error importing todos: ${e.message}"
        }
    }

    private fun importSingleTodo(syncTodo: SyncTodo, todoDao: io.github.dailytrack.data.db.dao.TodoDao) {
        val existingLocalId = prefs.getLong("sync_todo_${syncTodo.id}", 0)
        val existingRevision = prefs.getLong("todo_rev_${existingLocalId}", 0)

        if (existingLocalId > 0) {
            if (syncTodo.revision > existingRevision) {
                runBlocking {
                    val existing = todoDao.getTodoById(existingLocalId)
                    if (existing != null) {
                        todoDao.update(existing.copy(
                            title = syncTodo.title,
                            description = syncTodo.description,
                            isCompleted = syncTodo.isCompleted,
                            completedAt = syncTodo.completedAt?.let { parseDate(it) },
                            estimatedMinutes = syncTodo.estimatedMinutes,
                            actualMinutes = syncTodo.actualMinutes,
                            pomodoroCount = syncTodo.pomodoroCount,
                            priority = syncTodo.priority,
                            deadline = syncTodo.deadline?.let { parseDate(it) },
                            updatedAt = System.currentTimeMillis()
                        ))
                        prefs.edit().putLong("todo_rev_${existingLocalId}", syncTodo.revision).apply()
                    }
                }
            }
        } else if (!syncTodo.deleted) {
            val duplicate = runBlocking {
                todoDao.getAllTodosSync().any {
                    it.title == syncTodo.title && it.description == syncTodo.description
                }
            }
            if (duplicate) return

            val newId = runBlocking {
                todoDao.insert(TodoEntity(
                    title = syncTodo.title,
                    description = syncTodo.description,
                    categoryId = syncTodo.categoryId,
                    deadline = syncTodo.deadline?.let { parseDate(it) },
                    isCompleted = syncTodo.isCompleted,
                    completedAt = syncTodo.completedAt?.let { parseDate(it) },
                    estimatedMinutes = syncTodo.estimatedMinutes,
                    actualMinutes = syncTodo.actualMinutes,
                    pomodoroCount = syncTodo.pomodoroCount,
                    priority = syncTodo.priority,
                    createdAt = parseDate(syncTodo.createdAt),
                    updatedAt = parseDate(syncTodo.updatedAt)
                ))
            }
            prefs.edit()
                .putLong("sync_todo_${syncTodo.id}", newId)
                .putLong("todo_rev_$newId", syncTodo.revision)
                .apply()
        }
    }

    private fun importSubtasks(docFile: DocumentFile) {
        val content = readFile(docFile, "subtasks.json") ?: return
        try {
            val json = JSONObject(content)
            val subtasksArray = json.getJSONArray("subtasks")
            val subtaskDao = database.subtaskDao()

            for (i in 0 until subtasksArray.length()) {
                val subtaskJson = subtasksArray.getJSONObject(i)
                val syncSubtask = parseSyncSubtask(subtaskJson)
                val localTodoId = prefs.getLong("sync_todo_${syncSubtask.todoId}", 0)
                if (localTodoId == 0L) continue

                val existingLocalId = prefs.getLong("sync_subtask_${syncSubtask.id}", 0)
                if (existingLocalId == 0L && !syncSubtask.deleted) {
                    val newId = runBlocking {
                        subtaskDao.insert(SubtaskEntity(
                            todoId = localTodoId,
                            title = syncSubtask.title,
                            isCompleted = syncSubtask.isCompleted,
                            sortOrder = syncSubtask.sortOrder,
                            createdAt = parseDate(syncSubtask.createdAt)
                        ))
                    }
                    prefs.edit().putLong("sync_subtask_${syncSubtask.id}", newId).apply()
                }
            }
        } catch (e: Exception) {
            _lastError.value = "Error importing subtasks: ${e.message}"
        }
    }

    private fun importSessions(docFile: DocumentFile) {
        val content = readFile(docFile, "sessions.json") ?: return
        try {
            val json = JSONObject(content)
            val sessionsArray = json.getJSONArray("sessions")
            val sessionDao = database.sessionDao()

            for (i in 0 until sessionsArray.length()) {
                val sessionJson = sessionsArray.getJSONObject(i)
                val syncSession = parseSyncSession(sessionJson)
                val existingLocalId = prefs.getLong("sync_session_${syncSession.id}", 0)
                val existingRevision = prefs.getLong("session_rev_${existingLocalId}", 0)

                if (existingLocalId > 0) {
                    if (syncSession.revision > existingRevision) {
                        runBlocking {
                            val existing = sessionDao.getSessionById(existingLocalId)
                            if (existing != null) {
                                sessionDao.update(existing.copy(
                                    title = syncSession.title,
                                    type = syncSession.type,
                                    endTime = syncSession.endedAt?.let { parseDate(it) },
                                    isActive = syncSession.isActive,
                                    notes = syncSession.notes,
                                    updatedAt = System.currentTimeMillis()
                                ))
                                prefs.edit().putLong("session_rev_${existingLocalId}", syncSession.revision).apply()
                            }
                        }
                    }
                } else if (!syncSession.deleted) {
                    val newId = runBlocking {
                        sessionDao.insert(SessionEntity(
                            title = syncSession.title,
                            categoryId = syncSession.categoryId,
                            type = syncSession.type,
                            startTime = parseDate(syncSession.startedAt),
                            endTime = syncSession.endedAt?.let { parseDate(it) },
                            notes = syncSession.notes,
                            source = syncSession.source,
                            isActive = syncSession.isActive,
                            timezoneId = TimeZone.getDefault().id,
                            createdAt = parseDate(syncSession.createdAt),
                            updatedAt = parseDate(syncSession.updatedAt)
                        ))
                    }
                    prefs.edit()
                        .putLong("sync_session_${syncSession.id}", newId)
                        .putLong("session_rev_$newId", syncSession.revision)
                        .apply()
                }
            }
        } catch (e: Exception) {
            _lastError.value = "Error importing sessions: ${e.message}"
        }
    }

    private fun importPomodoros(docFile: DocumentFile) {
        val content = readFile(docFile, "pomodoro_sessions.json") ?: return
        try {
            val json = JSONObject(content)
            val pomodorosArray = json.getJSONArray("pomodoros")
            val pomodoroDao = database.pomodoroSessionDao()

            for (i in 0 until pomodorosArray.length()) {
                val pomJson = pomodorosArray.getJSONObject(i)
                val syncPom = parseSyncPomodoro(pomJson)
                val existingLocalId = prefs.getLong("sync_pomodoro_${syncPom.id}", 0)
                val existingRevision = prefs.getLong("pomodoro_rev_${existingLocalId}", 0)

                if (existingLocalId > 0) {
                    if (syncPom.revision > existingRevision) {
                        runBlocking {
                            val existing = pomodoroDao.getPomodoroById(existingLocalId)
                            if (existing != null) {
                                pomodoroDao.update(existing.copy(
                                    endTime = syncPom.endedAt?.let { parseDate(it) },
                                    isCompleted = syncPom.isCompleted,
                                    durationMinutes = syncPom.plannedMinutes
                                ))
                                prefs.edit().putLong("pomodoro_rev_${existingLocalId}", syncPom.revision).apply()
                            }
                        }
                    }
                } else if (!syncPom.deleted) {
                    val localTodoId = syncPom.todoId?.let { prefs.getLong("sync_todo_$it", 0) }
                    val newId = runBlocking {
                        pomodoroDao.insert(PomodoroSessionEntity(
                            todoId = if (localTodoId != null && localTodoId > 0) localTodoId else null,
                            categoryId = syncPom.categoryId,
                            startTime = parseDate(syncPom.startedAt),
                            endTime = syncPom.endedAt?.let { parseDate(it) },
                            durationMinutes = syncPom.plannedMinutes,
                            type = syncPom.type,
                            isCompleted = syncPom.isCompleted,
                            createdAt = parseDate(syncPom.createdAt)
                        ))
                    }
                    prefs.edit()
                        .putLong("sync_pomodoro_${syncPom.id}", newId)
                        .putLong("pomodoro_rev_$newId", syncPom.revision)
                        .apply()
                }
            }
        } catch (e: Exception) {
            _lastError.value = "Error importing pomodoros: ${e.message}"
        }
    }

    private fun importSettings(docFile: DocumentFile) {
        val content = readFile(docFile, "settings.json") ?: return
        try {
            val json = JSONObject(content)
            val remoteRevision = json.optLong("revision", 0)
            val localRevision = prefs.getLong("settings_rev", 0)

            if (remoteRevision > localRevision) {
                prefs.edit()
                    .putInt("work_minutes", json.optInt("workMinutes", 25))
                    .putInt("short_break_minutes", json.optInt("shortBreakMinutes", 5))
                    .putInt("long_break_minutes", json.optInt("longBreakMinutes", 15))
                    .putBoolean("sound_enabled", json.optBoolean("soundEnabled", true))
                    .putBoolean("vibration_enabled", json.optBoolean("vibrationEnabled", true))
                    .putLong("settings_rev", remoteRevision)
                    .apply()
            }
        } catch (e: Exception) {
            _lastError.value = "Error importing settings: ${e.message}"
        }
    }

    private fun writeFile(docFile: DocumentFile, fileName: String, content: String) {
        try {
            var file = docFile.findFile(fileName)
            if (file == null) {
                file = docFile.createFile("application/json", fileName)
            }
            if (file != null) {
                context.contentResolver.openOutputStream(file.uri, "wt")?.use { outputStream ->
                    outputStream.write(content.toByteArray(Charsets.UTF_8))
                }
            }
        } catch (e: Exception) {
            _lastError.value = "Error writing $fileName: ${e.message}"
        }
    }

    private fun readFile(docFile: DocumentFile, fileName: String): String? {
        return try {
            val file = docFile.findFile(fileName) ?: return null
            context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            _lastError.value = "Error reading $fileName: ${e.message}"
            null
        }
    }

    private fun getOrCreateSyncId(key: String): String {
        var id = prefs.getString("sync_id_$key", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("sync_id_$key", id).apply()
        }
        return id
    }

    private fun storeSyncMapping(syncId: String, localId: Long, entityType: String) {
        prefs.edit().putLong("sync_${entityType}_${syncId}", localId).apply()
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            isoFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseSyncTodo(json: JSONObject): SyncTodo {
        return SyncTodo(
            id = json.getString("id"),
            localId = json.optLong("localId", 0),
            title = json.getString("title"),
            description = json.optString("description", ""),
            categoryId = if (json.has("categoryId") && !json.isNull("categoryId")) json.getLong("categoryId") else null,
            deadline = json.optString("deadline", null),
            isCompleted = json.optBoolean("completed", false),
            completedAt = json.optString("completedAt", null),
            estimatedMinutes = json.optInt("estimatedMinutes", 0),
            actualMinutes = json.optInt("actualMinutes", 0),
            pomodoroCount = json.optInt("pomodoroCount", 0),
            priority = json.optInt("priority", 0),
            deleted = json.optBoolean("deleted", false),
            revision = json.optLong("revision", 1),
            createdAt = json.getString("createdAt"),
            updatedAt = json.getString("updatedAt")
        )
    }

    private fun parseSyncSubtask(json: JSONObject): SyncSubtask {
        return SyncSubtask(
            id = json.getString("id"),
            localId = json.optLong("localId", 0),
            todoId = json.getString("todoId"),
            title = json.getString("title"),
            isCompleted = json.optBoolean("completed", false),
            sortOrder = json.optInt("sortOrder", 0),
            deleted = json.optBoolean("deleted", false),
            revision = json.optLong("revision", 1),
            createdAt = json.getString("createdAt")
        )
    }

    private fun parseSyncSession(json: JSONObject): SyncSession {
        return SyncSession(
            id = json.getString("id"),
            localId = json.optLong("localId", 0),
            title = json.getString("title"),
            categoryId = if (json.has("categoryId") && !json.isNull("categoryId")) json.getLong("categoryId") else null,
            type = json.getString("type"),
            startedAt = json.getString("startedAt"),
            endedAt = json.optString("endedAt", null),
            notes = json.optString("notes", ""),
            source = json.optString("source", "TIMER"),
            isActive = json.optBoolean("isActive", false),
            deleted = json.optBoolean("deleted", false),
            revision = json.optLong("revision", 1),
            createdAt = json.getString("createdAt"),
            updatedAt = json.getString("updatedAt")
        )
    }

    private fun parseSyncPomodoro(json: JSONObject): SyncPomodoroSession {
        return SyncPomodoroSession(
            id = json.getString("id"),
            localId = json.optLong("localId", 0),
            todoId = json.optString("taskId", null),
            categoryId = if (json.has("categoryId") && !json.isNull("categoryId")) json.getLong("categoryId") else null,
            startedAt = json.getString("startedAt"),
            endedAt = json.optString("endedAt", null),
            plannedMinutes = json.optInt("plannedMinutes", 25),
            actualSeconds = json.optLong("actualSeconds", 0),
            type = json.optString("type", "WORK"),
            isCompleted = json.optBoolean("isCompleted", false),
            deleted = json.optBoolean("deleted", false),
            revision = json.optLong("revision", 1),
            createdAt = json.getString("createdAt")
        )
    }

    fun incrementRevision(entityType: String, entityId: Long) {
        val key = "${entityType}_rev_$entityId"
        val current = prefs.getLong(key, 1)
        prefs.edit().putLong(key, current + 1).apply()
    }

    fun onDestroy() {
        syncJob?.cancel()
        scope.cancel()
    }
}

private fun SyncManifest.toJson(): String {
    return JSONObject().apply {
        put("schemaVersion", schemaVersion)
        put("appSyncVersion", appSyncVersion)
        put("lastUpdatedByDeviceId", lastUpdatedByDeviceId)
        put("lastUpdatedAt", lastUpdatedAt)
    }.toString(2)
}

private fun SyncTodo.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("localId", localId)
        put("title", title)
        put("description", description)
        put("categoryId", categoryId ?: JSONObject.NULL)
        put("deadline", deadline ?: JSONObject.NULL)
        put("completed", isCompleted)
        put("completedAt", completedAt ?: JSONObject.NULL)
        put("estimatedMinutes", estimatedMinutes)
        put("actualMinutes", actualMinutes)
        put("pomodoroCount", pomodoroCount)
        put("priority", priority)
        put("deleted", deleted)
        put("revision", revision)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
}

private fun SyncSubtask.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("localId", localId)
        put("todoId", todoId)
        put("title", title)
        put("completed", isCompleted)
        put("sortOrder", sortOrder)
        put("deleted", deleted)
        put("revision", revision)
        put("createdAt", createdAt)
    }
}

private fun SyncSession.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("localId", localId)
        put("title", title)
        put("categoryId", categoryId ?: JSONObject.NULL)
        put("type", type)
        put("startedAt", startedAt)
        put("endedAt", endedAt ?: JSONObject.NULL)
        put("notes", notes)
        put("source", source)
        put("isActive", isActive)
        put("deleted", deleted)
        put("revision", revision)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }
}

private fun SyncPomodoroSession.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("localId", localId)
        put("taskId", todoId ?: JSONObject.NULL)
        put("categoryId", categoryId ?: JSONObject.NULL)
        put("startedAt", startedAt)
        put("endedAt", endedAt ?: JSONObject.NULL)
        put("plannedMinutes", plannedMinutes)
        put("actualSeconds", actualSeconds)
        put("type", type)
        put("isCompleted", isCompleted)
        put("deleted", deleted)
        put("revision", revision)
        put("createdAt", createdAt)
    }
}

private fun SyncSettings.toJson(): String {
    return JSONObject().apply {
        put("workMinutes", workMinutes)
        put("shortBreakMinutes", shortBreakMinutes)
        put("longBreakMinutes", longBreakMinutes)
        put("soundEnabled", soundEnabled)
        put("vibrationEnabled", vibrationEnabled)
        put("revision", revision)
        put("updatedAt", updatedAt)
    }.toString(2)
}
