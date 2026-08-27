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
