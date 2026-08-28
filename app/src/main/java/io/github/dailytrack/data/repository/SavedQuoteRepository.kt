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
