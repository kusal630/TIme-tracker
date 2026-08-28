package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks WHERE todoId = :todoId ORDER BY sortOrder ASC, createdAt ASC")
    fun getSubtasksForTodo(todoId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE todoId = :todoId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getSubtasksForTodoSync(todoId: Long): List<SubtaskEntity>

    @Query("SELECT COUNT(*) FROM subtasks WHERE todoId = :todoId AND isCompleted = 1")
    fun getCompletedCountForTodo(todoId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM subtasks WHERE todoId = :todoId")
    fun getTotalCountForTodo(todoId: Long): Flow<Int>

    @Insert
    suspend fun insert(subtask: SubtaskEntity): Long

    @Insert
    suspend fun insertAll(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun update(subtask: SubtaskEntity)

    @Delete
    suspend fun delete(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE todoId = :todoId")
    suspend fun deleteAllForTodo(todoId: Long)

    @Query("UPDATE subtasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean)
}
