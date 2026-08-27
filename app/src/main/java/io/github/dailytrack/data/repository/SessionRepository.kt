package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.SessionDao
import io.github.dailytrack.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {
    fun getSessionsForDay(dayStart: Long, dayEnd: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsForDay(dayStart, dayEnd)

    suspend fun getSessionsForDaySync(dayStart: Long, dayEnd: Long): List<SessionEntity> =
        sessionDao.getSessionsForDaySync(dayStart, dayEnd)

    fun getActiveSession(): Flow<SessionEntity?> = sessionDao.getActiveSession()
    suspend fun getActiveSessionSync(): SessionEntity? = sessionDao.getActiveSessionSync()
    suspend fun getSessionById(id: Long): SessionEntity? = sessionDao.getSessionById(id)

    fun getSessionsInRange(start: Long, end: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsInRange(start, end)

    suspend fun getSessionsInRangeSync(start: Long, end: Long): List<SessionEntity> =
        sessionDao.getSessionsInRangeSync(start, end)

    suspend fun insert(session: SessionEntity): Long = sessionDao.insert(session)
    suspend fun update(session: SessionEntity) = sessionDao.update(session)
    suspend fun delete(session: SessionEntity) = sessionDao.delete(session)

    suspend fun deactivateAllSessions() = sessionDao.deactivateAllSessions()

    suspend fun startSession(session: SessionEntity): Long {
        sessionDao.deactivateAllSessions()
        return sessionDao.insert(session)
    }

    suspend fun stopActiveSession(endTime: Long = System.currentTimeMillis()) {
        val active = sessionDao.getActiveSessionSync() ?: return
        sessionDao.update(active.copy(endTime = endTime, isActive = false, updatedAt = endTime))
    }
}
