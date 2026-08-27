package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodEntryDao {
    @Query("SELECT * FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp")
    fun getFoodEntriesForDay(dayStart: Long, dayEnd: Long): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp")
    suspend fun getFoodEntriesForDaySync(dayStart: Long, dayEnd: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp")
    suspend fun getFoodEntriesInRange(start: Long, end: Long): List<FoodEntryEntity>

    @Query("SELECT * FROM food_entries WHERE id = :id")
    suspend fun getFoodEntryById(id: Long): FoodEntryEntity?

    @Insert
    suspend fun insert(entry: FoodEntryEntity): Long

    @Update
    suspend fun update(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query("SELECT SUM(caloriesKcal) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalCaloriesForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(proteinG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalProteinForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(carbohydrateG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalCarbsForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(fatG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalFatForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(fiberG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalFiberForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(sugarG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalSugarForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(addedSugarG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalAddedSugarForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(sodiumMg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalSodiumForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(caffeineMg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalCaffeineForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(ironMg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalIronForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(calciumMg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalCalciumForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(vitaminDMcg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalVitaminDForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(vitaminB12Mcg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalVitaminB12ForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(folateMcg) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalFolateForDay(dayStart: Long, dayEnd: Long): Flow<Double?>

    @Query("SELECT SUM(saturatedFatG) FROM food_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd")
    fun getTotalSaturatedFatForDay(dayStart: Long, dayEnd: Long): Flow<Double?>
}
