package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val title: String = "",
    val body: String = "",
    val mood: Int = 3,
    val tags: String = "",
    val linkedSessionIds: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
