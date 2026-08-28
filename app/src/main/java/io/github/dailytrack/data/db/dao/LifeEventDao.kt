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
