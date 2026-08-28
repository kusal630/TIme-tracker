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

import io.github.dailytrack.data.db.dao.SymptomEntryDao
import io.github.dailytrack.data.db.entity.SymptomEntryEntity
import kotlinx.coroutines.flow.Flow

class SymptomEntryRepository(private val symptomEntryDao: SymptomEntryDao) {
    fun getSymptomsInRange(start: Long, end: Long): Flow<List<SymptomEntryEntity>> =
        symptomEntryDao.getSymptomsInRange(start, end)

    suspend fun getSymptomsInRangeSync(start: Long, end: Long): List<SymptomEntryEntity> =
        symptomEntryDao.getSymptomsInRangeSync(start, end)

    suspend fun getSevereSymptoms(start: Long): List<SymptomEntryEntity> =
        symptomEntryDao.getSevereSymptoms(start)

    suspend fun insert(symptom: SymptomEntryEntity): Long = symptomEntryDao.insert(symptom)
    suspend fun delete(symptom: SymptomEntryEntity) = symptomEntryDao.delete(symptom)
}
