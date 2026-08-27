package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "insights",
    indices = [Index("createdAt"), Index("category")]
)
data class InsightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val severity: String = "INFO",
    val category: String,
    val title: String,
    val message: String,
    val dataEvidence: String = "",
    val actionLabel: String = "",
    val dismissed: Boolean = false,
    val cooldownUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
