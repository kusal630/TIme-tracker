package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.InsightEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("SELECT * FROM insights ORDER BY createdAt DESC")
    fun getAllInsights(): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights WHERE dismissed = 0 ORDER BY createdAt DESC")
    fun getActiveInsights(): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights WHERE dismissed = 0 AND severity IN ('WARNING', 'CRITICAL') ORDER BY createdAt DESC")
    fun getWarnings(): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights WHERE category = :category AND dismissed = 0 ORDER BY createdAt DESC")
    fun getInsightsByCategory(category: String): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights WHERE title = :title AND cooldownUntil > :now AND dismissed = 0")
    suspend fun getActiveInsightByTitle(title: String, now: Long): InsightEntity?

    @Insert
    suspend fun insert(insight: InsightEntity): Long

    @Update
    suspend fun update(insight: InsightEntity)

    @Query("UPDATE insights SET dismissed = 1 WHERE id = :id")
    suspend fun dismiss(id: Long)

    @Query("DELETE FROM insights WHERE dismissed = 1")
    suspend fun deleteDismissed()
}
