package io.github.dailytrack.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drink_entries")
data class DrinkEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val drinkType: String = "WATER",
    val volumeMl: Double,
    val caffeineMg: Double = 0.0,
    val sugarG: Double = 0.0,
    val alcoholUnits: Double = 0.0,
    val notes: String = "",
    val source: String = "MANUAL",
    val createdAt: Long = System.currentTimeMillis()
)
