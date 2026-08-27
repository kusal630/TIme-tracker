package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE startTime >= :dayStart AND startTime < :dayEnd ORDER BY startTime")
    fun getSessionsForDay(dayStart: Long, dayEnd: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE startTime >= :dayStart AND startTime < :dayEnd ORDER BY startTime")
    suspend fun getSessionsForDaySync(dayStart: Long, dayEnd: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): SessionEntity?

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE startTime >= :start AND startTime < :end ORDER BY startTime")
    fun getSessionsInRange(start: Long, end: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE startTime >= :start AND startTime < :end ORDER BY startTime")
    suspend fun getSessionsInRangeSync(start: Long, end: Long): List<SessionEntity>

    @Insert
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("UPDATE sessions SET isActive = 0, updatedAt = :now")
    suspend fun deactivateAllSessions(now: Long = System.currentTimeMillis())

    @Query("SELECT * FROM sessions WHERE categoryId = :categoryId AND startTime >= :start AND startTime < :end ORDER BY startTime")
    fun getSessionsByCategoryForDay(categoryId: Long, start: Long, end: Long): Flow<List<SessionEntity>>
}
