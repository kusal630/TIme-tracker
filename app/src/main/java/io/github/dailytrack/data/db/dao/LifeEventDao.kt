package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.LifeEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeEventDao {
    @Query("SELECT * FROM life_events ORDER BY timestamp DESC")
    fun getAllLifeEvents(): Flow<List<LifeEventEntity>>

    @Query("SELECT * FROM life_events WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    fun getLifeEventsInRange(start: Long, end: Long): Flow<List<LifeEventEntity>>

    @Insert
    suspend fun insert(event: LifeEventEntity): Long

    @Update
    suspend fun update(event: LifeEventEntity)

    @Delete
    suspend fun delete(event: LifeEventEntity)
}
