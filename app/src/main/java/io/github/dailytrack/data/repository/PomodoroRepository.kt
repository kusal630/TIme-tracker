package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.PomodoroSessionDao
import io.github.dailytrack.data.db.entity.PomodoroSessionEntity
import kotlinx.coroutines.flow.Flow

class PomodoroRepository(private val pomodoroDao: PomodoroSessionDao) {
    fun getAllPomodoros(): Flow<List<PomodoroSessionEntity>> = pomodoroDao.getAllPomodoros()
    fun getPomodorosForDay(start: Long, end: Long): Flow<List<PomodoroSessionEntity>> = pomodoroDao.getPomodorosForDay(start, end)
    fun getPomodorosForTodo(todoId: Long): Flow<List<PomodoroSessionEntity>> = pomodoroDao.getPomodorosForTodo(todoId)

    suspend fun getPomodoroById(id: Long): PomodoroSessionEntity? = pomodoroDao.getPomodoroById(id)
    suspend fun insert(pomodoro: PomodoroSessionEntity): Long = pomodoroDao.insert(pomodoro)
    suspend fun update(pomodoro: PomodoroSessionEntity) = pomodoroDao.update(pomodoro)
    suspend fun delete(pomodoro: PomodoroSessionEntity) = pomodoroDao.delete(pomodoro)
    suspend fun completePomodoro(id: Long, endTime: Long) = pomodoroDao.completePomodoro(id, endTime)
    suspend fun getPomodorosForDaySync(start: Long, end: Long): List<PomodoroSessionEntity> = pomodoroDao.getPomodorosForDaySync(start, end)
    suspend fun getTotalWorkMinutesInRange(start: Long, end: Long): Int = pomodoroDao.getTotalWorkMinutesInRange(start, end) ?: 0
    suspend fun getTotalBreakMinutesInRange(start: Long, end: Long): Int = pomodoroDao.getTotalBreakMinutesInRange(start, end) ?: 0
    suspend fun getCompletedPomodoroCountInRange(start: Long, end: Long): Int = pomodoroDao.getCompletedPomodoroCountInRange(start, end)
}
