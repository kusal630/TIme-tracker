package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "symptom_entries",
    indices = [Index("timestamp")]
)
data class SymptomEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val symptomType: String,
    val severity: String = "MILD",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
