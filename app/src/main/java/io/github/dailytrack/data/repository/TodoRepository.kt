package io.github.dailytrack.data.repository

import io.github.dailytrack.data.db.dao.TodoDao
import io.github.dailytrack.data.db.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    fun getActiveTodos(): Flow<List<TodoEntity>> = todoDao.getActiveTodos()
    fun getCompletedTodos(): Flow<List<TodoEntity>> = todoDao.getCompletedTodos()
    fun getPendingTodosWithDeadline(now: Long): Flow<List<TodoEntity>> = todoDao.getPendingTodosWithDeadline(now)
    fun getActiveTodoCount(): Flow<Int> = todoDao.getActiveTodoCount()
    fun getCompletedTodoCount(): Flow<Int> = todoDao.getCompletedTodoCount()

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)
    suspend fun insert(todo: TodoEntity): Long = todoDao.insert(todo)
    suspend fun update(todo: TodoEntity) = todoDao.update(todo)
    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
    suspend fun completeTodo(id: Long) = todoDao.completeTodo(id)
    suspend fun getTodosWithDeadlineApproaching(now: Long, deadline: Long): List<TodoEntity> = todoDao.getTodosWithDeadlineApproaching(now, deadline)
    suspend fun getTotalCompletedMinutesInRange(start: Long, end: Long): Int = todoDao.getTotalCompletedMinutesInRange(start, end) ?: 0
    suspend fun getCompletedCountInRange(start: Long, end: Long): Int = todoDao.getCompletedCountInRange(start, end)
}
