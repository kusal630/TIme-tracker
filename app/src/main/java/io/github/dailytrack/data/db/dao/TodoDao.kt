package io.github.dailytrack.data.db.dao

import androidx.room.*
import io.github.dailytrack.data.db.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY deadline ASC, createdAt DESC")
    fun getAllTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isCompleted = 0 ORDER BY deadline ASC")
    fun getActiveTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTodos(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE deadline IS NOT NULL AND deadline > :now AND isCompleted = 0 ORDER BY deadline ASC")
    fun getPendingTodosWithDeadline(now: Long): Flow<List<TodoEntity>>

    @Query("SELECT * FROM todos WHERE deadline IS NOT NULL AND deadline <= :deadline AND deadline > :now AND isCompleted = 0")
    suspend fun getTodosWithDeadlineApproaching(now: Long, deadline: Long): List<TodoEntity>

    @Query("SELECT * FROM todos WHERE id = :id")
    suspend fun getTodoById(id: Long): TodoEntity?

    @Insert
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("UPDATE todos SET isCompleted = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun completeTodo(id: Long, completedAt: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 0")
    fun getActiveTodoCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1")
    fun getCompletedTodoCount(): Flow<Int>

    @Query("SELECT SUM(actualMinutes) FROM todos WHERE isCompleted = 1 AND completedAt >= :start AND completedAt < :end")
    suspend fun getTotalCompletedMinutesInRange(start: Long, end: Long): Int?

    @Query("SELECT COUNT(*) FROM todos WHERE isCompleted = 1 AND completedAt >= :start AND completedAt < :end")
    suspend fun getCompletedCountInRange(start: Long, end: Long): Int
}
