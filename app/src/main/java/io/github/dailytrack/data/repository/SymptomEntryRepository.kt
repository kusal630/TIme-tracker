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
