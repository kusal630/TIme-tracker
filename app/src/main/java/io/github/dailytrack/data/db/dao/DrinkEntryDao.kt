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
import io.github.dailytrack.data.db.entity.DrinkEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkEntryDao {
    @Query("SELECT * FROM drink_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp")
    fun getDrinkEntriesForDay(dayStart: Long, dayEnd: Long): Flow<List<DrinkEntryEntity>>

    @Query("SELECT * FROM drink_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp")
    suspend fun getDrinkEntriesForDaySync(dayStart: Long, dayEnd: Long): List<DrinkEntryEntity>

    @Query("SELECT * FROM drink_entries WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    suspend fun getDrinkEntriesInRange(start: Long, end: Long): List<DrinkEntryEntity>

    @Insert
    suspend fun insert(entry: DrinkEntryEntity): Long

    @Update
    suspend fun update(entry: DrinkEntryEntity)

    @Delete
    suspend fun delete(entry: DrinkEntryEntity)

    @Query("SELECT SUM(volumeMl) FROM drink_entries WHERE drinkType = 'WATER' AND timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalWaterMlForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(volumeMl) FROM drink_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalVolumeMlForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(caffeineMg) FROM drink_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalCaffeineForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(sugarG) FROM drink_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalSugarForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(alcoholUnits) FROM drink_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalAlcoholForDay(dayStart: Long, dayEnd: Long): Flow<Double?>
}
