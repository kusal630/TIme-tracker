package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "body_metrics")
data class BodyMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val metricType: String,
    val value: Double,
    val unit: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
