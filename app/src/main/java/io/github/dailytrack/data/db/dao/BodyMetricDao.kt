/*
 * Copyright 2024 Soul Track Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


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
