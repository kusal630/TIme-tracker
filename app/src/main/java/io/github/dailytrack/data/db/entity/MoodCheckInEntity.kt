package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_check_ins")
data class MoodCheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val mood: Int = 3,
    val energy: Int = 3,
    val stress: Int = 3,
    val painLevel: Int = 0,
    val note: String = "",
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
