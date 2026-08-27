package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.MoodCheckInDao
import io.github.dailytrack.data.db.entity.MoodCheckInEntity
import kotlinx.coroutines.flow.Flow

class MoodCheckInRepository(private val moodCheckInDao: MoodCheckInDao) {
    fun getMoodCheckInsForDay(dayStart: Long, dayEnd: Long): Flow<List<MoodCheckInEntity>> =
        moodCheckInDao.getMoodCheckInsForDay(dayStart, dayEnd)

    fun getMoodCheckInsInRange(start: Long, end: Long): Flow<List<MoodCheckInEntity>> =
        moodCheckInDao.getMoodCheckInsInRange(start, end)

    suspend fun insert(checkIn: MoodCheckInEntity): Long = moodCheckInDao.insert(checkIn)
    suspend fun delete(checkIn: MoodCheckInEntity) = moodCheckInDao.delete(checkIn)
}
