package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.BodyMetricDao
import io.github.dailytrack.data.db.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

class BodyMetricRepository(private val bodyMetricDao: BodyMetricDao) {
    fun getMetricsInRange(start: Long, end: Long): Flow<List<BodyMetricEntity>> =
        bodyMetricDao.getMetricsInRange(start, end)

    fun getRecentMetricsByType(type: String, limit: Int = 10): Flow<List<BodyMetricEntity>> =
        bodyMetricDao.getRecentMetricsByType(type, limit)

    suspend fun getLatestMetric(type: String): BodyMetricEntity? =
        bodyMetricDao.getLatestMetric(type)

    suspend fun insert(metric: BodyMetricEntity): Long = bodyMetricDao.insert(metric)
    suspend fun update(metric: BodyMetricEntity) = bodyMetricDao.update(metric)
    suspend fun delete(metric: BodyMetricEntity) = bodyMetricDao.delete(metric)
}
