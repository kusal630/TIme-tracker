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


package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.SavedQuoteDao
import io.github.dailytrack.data.db.entity.SavedQuoteEntity
import kotlinx.coroutines.flow.Flow

class SavedQuoteRepository(private val dao: SavedQuoteDao) {
    fun getAllSavedQuotes(): Flow<List<SavedQuoteEntity>> = dao.getAllSavedQuotes()

    suspend fun getQuoteByText(text: String): SavedQuoteEntity? = dao.getQuoteByText(text)

    suspend fun insert(quote: SavedQuoteEntity): Long = dao.insert(quote)

    suspend fun delete(quote: SavedQuoteEntity) = dao.delete(quote)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun getCount(): Int = dao.getCount()
}
