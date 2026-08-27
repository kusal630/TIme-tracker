package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.InsightDao
import io.github.dailytrack.data.db.entity.InsightEntity
import kotlinx.coroutines.flow.Flow

class InsightRepository(private val insightDao: InsightDao) {
    fun getAllInsights(): Flow<List<InsightEntity>> = insightDao.getAllInsights()
    fun getActiveInsights(): Flow<List<InsightEntity>> = insightDao.getActiveInsights()
    fun getWarnings(): Flow<List<InsightEntity>> = insightDao.getWarnings()
    fun getInsightsByCategory(category: String): Flow<List<InsightEntity>> =
        insightDao.getInsightsByCategory(category)

    suspend fun getActiveInsightByTitle(title: String, now: Long): InsightEntity? =
        insightDao.getActiveInsightByTitle(title, now)

    suspend fun insert(insight: InsightEntity): Long = insightDao.insert(insight)
    suspend fun update(insight: InsightEntity) = insightDao.update(insight)
    suspend fun dismiss(id: Long) = insightDao.dismiss(id)
    suspend fun deleteDismissed() = insightDao.deleteDismissed()

    suspend fun insertIfNotCoolingDown(insight: InsightEntity): Long {
        val now = System.currentTimeMillis()
        val existing = insightDao.getActiveInsightByTitle(insight.title, now)
        return if (existing == null) {
            insightDao.insert(insight)
        } else {
            existing.id
        }
    }
}
