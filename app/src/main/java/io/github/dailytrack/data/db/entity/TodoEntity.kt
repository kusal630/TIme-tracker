package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val deadline: Long? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val estimatedMinutes: Int = 0,
    val actualMinutes: Int = 0,
    val pomodoroCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
