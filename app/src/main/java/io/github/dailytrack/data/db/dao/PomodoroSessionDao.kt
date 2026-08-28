package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PomodoroSessionDao {
    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    fun getAllPomodoros(): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    suspend fun getAllPomodorosSync(): List<PomodoroSessionEntity>

    @Query("SELECT * FROM pomodoro_sessions WHERE startTime >= :start AND startTime < :end ORDER BY startTime")
    fun getPomodorosForDay(start: Long, end: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE startTime >= :start AND startTime < :end ORDER BY startTime")
    suspend fun getPomodorosForDaySync(start: Long, end: Long): List<PomodoroSessionEntity>

    @Query("SELECT * FROM pomodoro_sessions WHERE todoId = :todoId ORDER BY startTime DESC")
    fun getPomodorosForTodo(todoId: Long): Flow<List<PomodoroSessionEntity>>

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :id")
    suspend fun getPomodoroById(id: Long): PomodoroSessionEntity?

    @Insert
    suspend fun insert(pomodoro: PomodoroSessionEntity): Long

    @Update
    suspend fun update(pomodoro: PomodoroSessionEntity)

    @Delete
    suspend fun delete(pomodoro: PomodoroSessionEntity)

    @Query("UPDATE pomodoro_sessions SET endTime = :endTime, isCompleted = :isCompleted WHERE id = :id")
    suspend fun completePomodoro(id: Long, endTime: Long, isCompleted: Boolean = true)

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1 AND startTime >= :start AND startTime < :end")
    suspend fun getTotalWorkMinutesInRange(start: Long, end: Long): Int?

    @Query("SELECT SUM(durationMinutes) FROM pomodoro_sessions WHERE type = 'BREAK' AND isCompleted = 1 AND startTime >= :start AND startTime < :end")
    suspend fun getTotalBreakMinutesInRange(start: Long, end: Long): Int?

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'WORK' AND isCompleted = 1 AND startTime >= :start AND startTime < :end")
    suspend fun getCompletedPomodoroCountInRange(start: Long, end: Long): Int
}
