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
