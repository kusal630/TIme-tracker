package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.MoodCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodCheckInDao {
    @Query("SELECT * FROM mood_check_ins WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp")
    fun getMoodCheckInsForDay(dayStart: Long, dayEnd: Long): Flow<List<MoodCheckInEntity>>

    @Query("SELECT * FROM mood_check_ins WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    fun getMoodCheckInsInRange(start: Long, end: Long): Flow<List<MoodCheckInEntity>>

    @Insert
    suspend fun insert(checkIn: MoodCheckInEntity): Long

    @Delete
    suspend fun delete(checkIn: MoodCheckInEntity)
}
