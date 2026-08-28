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

import io.github.dailytrack.data.db.dao.DrinkEntryDao
import io.github.dailytrack.data.db.entity.DrinkEntryEntity
import kotlinx.coroutines.flow.Flow

class DrinkEntryRepository(private val drinkEntryDao: DrinkEntryDao) {
    fun getDrinkEntriesForDay(dayStart: Long, dayEnd: Long): Flow<List<DrinkEntryEntity>> =
        drinkEntryDao.getDrinkEntriesForDay(dayStart, dayEnd)

    suspend fun getDrinkEntriesForDaySync(dayStart: Long, dayEnd: Long): List<DrinkEntryEntity> =
        drinkEntryDao.getDrinkEntriesForDaySync(dayStart, dayEnd)

    suspend fun getDrinkEntriesInRange(start: Long, end: Long): List<DrinkEntryEntity> =
        drinkEntryDao.getDrinkEntriesInRange(start, end)

    suspend fun insert(entry: DrinkEntryEntity): Long = drinkEntryDao.insert(entry)
    suspend fun update(entry: DrinkEntryEntity) = drinkEntryDao.update(entry)
    suspend fun delete(entry: DrinkEntryEntity) = drinkEntryDao.delete(entry)

    fun getTotalWaterMlForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        drinkEntryDao.getTotalWaterMlForDay(dayStart, dayEnd)

    fun getTotalVolumeMlForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        drinkEntryDao.getTotalVolumeMlForDay(dayStart, dayEnd)

    fun getTotalCaffeineForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        drinkEntryDao.getTotalCaffeineForDay(dayStart, dayEnd)

    fun getTotalSugarForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        drinkEntryDao.getTotalSugarForDay(dayStart, dayEnd)

    fun getTotalAlcoholForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        drinkEntryDao.getTotalAlcoholForDay(dayStart, dayEnd)
}
