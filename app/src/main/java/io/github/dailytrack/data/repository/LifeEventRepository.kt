package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.LifeEventDao
import io.github.dailytrack.data.db.entity.LifeEventEntity
import kotlinx.coroutines.flow.Flow

class LifeEventRepository(private val lifeEventDao: LifeEventDao) {
    fun getAllLifeEvents(): Flow<List<LifeEventEntity>> = lifeEventDao.getAllLifeEvents()
    fun getLifeEventsInRange(start: Long, end: Long): Flow<List<LifeEventEntity>> =
        lifeEventDao.getLifeEventsInRange(start, end)

    suspend fun insert(event: LifeEventEntity): Long = lifeEventDao.insert(event)
    suspend fun update(event: LifeEventEntity) = lifeEventDao.update(event)
    suspend fun delete(event: LifeEventEntity) = lifeEventDao.delete(event)
}
