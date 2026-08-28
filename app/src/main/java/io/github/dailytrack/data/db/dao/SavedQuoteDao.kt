package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.SavedQuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedQuoteDao {
    @Query("SELECT * FROM saved_quotes ORDER BY savedAt DESC")
    fun getAllSavedQuotes(): Flow<List<SavedQuoteEntity>>

    @Query("SELECT * FROM saved_quotes WHERE text = :text LIMIT 1")
    suspend fun getQuoteByText(text: String): SavedQuoteEntity?

    @Insert
    suspend fun insert(quote: SavedQuoteEntity): Long

    @Delete
    suspend fun delete(quote: SavedQuoteEntity)

    @Query("DELETE FROM saved_quotes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM saved_quotes")
    suspend fun getCount(): Int
}
