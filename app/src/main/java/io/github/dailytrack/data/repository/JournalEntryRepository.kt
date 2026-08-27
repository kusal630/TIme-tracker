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
