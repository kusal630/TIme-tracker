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
