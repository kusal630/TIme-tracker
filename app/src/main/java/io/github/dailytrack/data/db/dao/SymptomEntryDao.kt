package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.SymptomEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomEntryDao {
    @Query("SELECT * FROM symptom_entries WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    fun getSymptomsInRange(start: Long, end: Long): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entries WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    suspend fun getSymptomsInRangeSync(start: Long, end: Long): List<SymptomEntryEntity>

    @Query("SELECT * FROM symptom_entries WHERE severity = 'SEVERE' AND timestamp >= :start ORDER BY timestamp DESC LIMIT 10")
    suspend fun getSevereSymptoms(start: Long): List<SymptomEntryEntity>

    @Insert
    suspend fun insert(symptom: SymptomEntryEntity): Long

    @Delete
    suspend fun delete(symptom: SymptomEntryEntity)
}
