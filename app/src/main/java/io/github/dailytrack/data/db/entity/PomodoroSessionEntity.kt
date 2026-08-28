package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val todoId: Long? = null,
    val categoryId: Long? = null,
    val startTime: Long,
    val endTime: Long? = null,
    val durationMinutes: Int = 25,
    val type: String = "WORK",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
