package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.BodyMetricEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMetricDao {
    @Query("SELECT * FROM body_metrics WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    fun getMetricsInRange(start: Long, end: Long): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics WHERE metricType = :type ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMetricsByType(type: String, limit: Int): Flow<List<BodyMetricEntity>>

    @Query("SELECT * FROM body_metrics WHERE metricType = :type ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMetric(type: String): BodyMetricEntity?

    @Insert
    suspend fun insert(metric: BodyMetricEntity): Long

    @Update
    suspend fun update(metric: BodyMetricEntity)

    @Delete
    suspend fun delete(metric: BodyMetricEntity)
}
