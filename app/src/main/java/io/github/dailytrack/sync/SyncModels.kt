package io.github.dailytrack.sync

import java.util.UUID

data class SyncManifest(
    val schemaVersion: Int = 1,
    val appSyncVersion: Int = 1,
    val lastUpdatedByDeviceId: String,
    val lastUpdatedAt: String
)

data class SyncTodo(
    val id: String,
    val localId: Long,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val deadline: String? = null,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val estimatedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val pomodoroCount: Int = 0,
    val priority: Int = 0,
    val deleted: Boolean = false,
    val revision: Long = 1,
    val createdAt: String,
    val updatedAt: String
)

data class SyncSubtask(
    val id: String,
    val localId: Long,
    val todoId: String,
    val title: String,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0,
    val deleted: Boolean = false,
    val revision: Long = 1,
    val createdAt: String
)

data class SyncPomodoroSession(
    val id: String,
    val localId: Long,
    val todoId: String? = null,
    val categoryId: Long? = null,
    val startedAt: String,
    val endedAt: String? = null,
    val plannedMinutes: Int = 25,
    val actualSeconds: Long = 0,
    val type: String = "WORK",
    val isCompleted: Boolean = false,
    val deleted: Boolean = false,
    val revision: Long = 1,
    val createdAt: String
)

data class SyncSession(
    val id: String,
    val localId: Long,
    val title: String,
    val categoryId: Long? = null,
    val type: String,
    val startedAt: String,
    val endedAt: String? = null,
    val notes: String = "",
    val source: String = "TIMER",
    val isActive: Boolean = false,
    val deleted: Boolean = false,
    val revision: Long = 1,
    val createdAt: String,
    val updatedAt: String
)

data class SyncActivePomodoro(
    val taskId: String? = null,
    val status: String = "idle",
    val startedAt: String? = null,
    val plannedMinutes: Int = 25,
    val accumulatedSeconds: Long = 0,
    val pausedAt: String? = null,
    val ownerDeviceId: String,
    val revision: Long = 1,
    val updatedAt: String
)

data class SyncSettings(
    val workMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val revision: Long = 1,
    val updatedAt: String
)

data class SyncTodoFile(val todos: List<SyncTodo> = emptyList())
data class SyncSubtaskFile(val subtasks: List<SyncSubtask> = emptyList())
data class SyncSessionFile(val sessions: List<SyncSession> = emptyList())
data class SyncPomodoroFile(val pomodoros: List<SyncPomodoroSession> = emptyList())
