package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("startTime"),
        Index("endTime"),
        Index("categoryId"),
        Index("isActive")
    ]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val categoryId: Long? = null,
    val type: String = "ACTIVITY",
    val startTime: Long,
    val endTime: Long? = null,
    val notes: String = "",
    val tags: String = "",
    val source: String = "TIMER",
    val timezoneId: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
