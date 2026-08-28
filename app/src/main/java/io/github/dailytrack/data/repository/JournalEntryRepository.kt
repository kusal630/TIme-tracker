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

import io.github.dailytrack.data.db.dao.JournalEntryDao
import io.github.dailytrack.data.db.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

class JournalEntryRepository(private val journalEntryDao: JournalEntryDao) {
    fun getAllJournalEntries(): Flow<List<JournalEntryEntity>> =
        journalEntryDao.getAllJournalEntries()

    fun getJournalEntriesInRange(start: Long, end: Long): Flow<List<JournalEntryEntity>> =
        journalEntryDao.getJournalEntriesInRange(start, end)

    suspend fun getJournalEntryById(id: Long): JournalEntryEntity? =
        journalEntryDao.getJournalEntryById(id)

    suspend fun insert(entry: JournalEntryEntity): Long = journalEntryDao.insert(entry)
    suspend fun update(entry: JournalEntryEntity) = journalEntryDao.update(entry)
    suspend fun delete(entry: JournalEntryEntity) = journalEntryDao.delete(entry)
}
