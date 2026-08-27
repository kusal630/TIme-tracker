package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "life_events")
data class LifeEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val endTime: Long? = null,
    val title: String,
    val type: String = "CUSTOM",
    val description: String = "",
    val peopleTags: String = "",
    val emotionalImpact: Int = 3,
    val attachmentUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
