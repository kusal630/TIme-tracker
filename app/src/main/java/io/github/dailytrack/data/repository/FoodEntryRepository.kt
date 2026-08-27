package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.FoodEntryDao
import io.github.dailytrack.data.db.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

class FoodEntryRepository(private val foodEntryDao: FoodEntryDao) {
    fun getFoodEntriesForDay(dayStart: Long, dayEnd: Long): Flow<List<FoodEntryEntity>> =
        foodEntryDao.getFoodEntriesForDay(dayStart, dayEnd)

    suspend fun getFoodEntriesForDaySync(dayStart: Long, dayEnd: Long): List<FoodEntryEntity> =
        foodEntryDao.getFoodEntriesForDaySync(dayStart, dayEnd)

    suspend fun getFoodEntriesInRange(start: Long, end: Long): List<FoodEntryEntity> =
        foodEntryDao.getFoodEntriesInRange(start, end)

    suspend fun insert(entry: FoodEntryEntity): Long = foodEntryDao.insert(entry)
    suspend fun update(entry: FoodEntryEntity) = foodEntryDao.update(entry)
    suspend fun delete(entry: FoodEntryEntity) = foodEntryDao.delete(entry)

    fun getTotalCaloriesForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalCaloriesForDay(dayStart, dayEnd)

    fun getTotalProteinForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalProteinForDay(dayStart, dayEnd)

    fun getTotalCarbsForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalCarbsForDay(dayStart, dayEnd)

    fun getTotalFatForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalFatForDay(dayStart, dayEnd)

    fun getTotalFiberForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalFiberForDay(dayStart, dayEnd)

    fun getTotalSugarForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalSugarForDay(dayStart, dayEnd)

    fun getTotalAddedSugarForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalAddedSugarForDay(dayStart, dayEnd)

    fun getTotalSodiumForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalSodiumForDay(dayStart, dayEnd)

    fun getTotalIronForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalIronForDay(dayStart, dayEnd)

    fun getTotalCalciumForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalCalciumForDay(dayStart, dayEnd)

    fun getTotalVitaminDForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalVitaminDForDay(dayStart, dayEnd)

    fun getTotalVitaminB12ForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalVitaminB12ForDay(dayStart, dayEnd)

    fun getTotalFolateForDay(dayStart: Long, dayEnd: Long): Flow<Double?> =
        foodEntryDao.getTotalFolateForDay(dayStart, dayEnd)
}
