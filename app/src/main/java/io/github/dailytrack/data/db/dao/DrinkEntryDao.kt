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
