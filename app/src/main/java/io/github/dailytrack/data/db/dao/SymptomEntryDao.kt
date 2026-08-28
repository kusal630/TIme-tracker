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
import io.github.dailytrack.data.db.entity.SymptomEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomEntryDao {
    @Query("SELECT * FROM symptom_entries WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    fun getSymptomsInRange(start: Long, end: Long): Flow<List<SymptomEntryEntity>>

    @Query("SELECT * FROM symptom_entries WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    suspend fun getSymptomsInRangeSync(start: Long, end: Long): List<SymptomEntryEntity>

    @Query("SELECT * FROM symptom_entries WHERE severity = 'SEVERE' AND timestamp >= :start ORDER BY timestamp DESC LIMIT 10")
    suspend fun getSevereSymptoms(start: Long): List<SymptomEntryEntity>

    @Insert
    suspend fun insert(symptom: SymptomEntryEntity): Long

    @Delete
    suspend fun delete(symptom: SymptomEntryEntity)
}
