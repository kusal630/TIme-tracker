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

import io.github.dailytrack.data.db.dao.SubtaskDao
import io.github.dailytrack.data.db.dao.TodoDao
import io.github.dailytrack.data.db.entity.SubtaskEntity
import io.github.dailytrack.data.db.entity.TodoEntity
import kotlinx.coroutines.flow.Flow

class TodoRepository(
    private val todoDao: TodoDao,
    private val subtaskDao: SubtaskDao
) {
    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllTodos()
    fun getActiveTodos(): Flow<List<TodoEntity>> = todoDao.getActiveTodos()
    fun getCompletedTodos(): Flow<List<TodoEntity>> = todoDao.getCompletedTodos()
    fun getActiveTodosByCategory(categoryId: Long): Flow<List<TodoEntity>> = todoDao.getActiveTodosByCategory(categoryId)
    fun getActiveTodosByMinPriority(minPriority: Int): Flow<List<TodoEntity>> = todoDao.getActiveTodosByMinPriority(minPriority)
    fun getPendingTodosWithDeadline(now: Long): Flow<List<TodoEntity>> = todoDao.getPendingTodosWithDeadline(now)
    fun getActiveTodoCount(): Flow<Int> = todoDao.getActiveTodoCount()
    fun getCompletedTodoCount(): Flow<Int> = todoDao.getCompletedTodoCount()
    fun getTodayActiveTodoCount(dayStart: Long, dayEnd: Long): Flow<Int> = todoDao.getTodayActiveTodoCount(dayStart, dayEnd)
    fun getTodayCompletedTodoCount(dayStart: Long, dayEnd: Long): Flow<Int> = todoDao.getTodayCompletedTodoCount(dayStart, dayEnd)
    fun getHighPriorityCount(): Flow<Int> = todoDao.getHighPriorityCount()
    fun getOverdueCount(now: Long): Flow<Int> = todoDao.getOverdueCount(now)

    suspend fun getTodoById(id: Long): TodoEntity? = todoDao.getTodoById(id)
    suspend fun insert(todo: TodoEntity): Long = todoDao.insert(todo)
    suspend fun update(todo: TodoEntity) = todoDao.update(todo)
    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
    suspend fun completeTodo(id: Long) = todoDao.completeTodo(id)
    suspend fun getTodosWithDeadlineApproaching(now: Long, deadline: Long): List<TodoEntity> = todoDao.getTodosWithDeadlineApproaching(now, deadline)
    suspend fun getTotalCompletedMinutesInRange(start: Long, end: Long): Int = todoDao.getTotalCompletedMinutesInRange(start, end) ?: 0
    suspend fun getCompletedCountInRange(start: Long, end: Long): Int = todoDao.getCompletedCountInRange(start, end)
    suspend fun getHighPriorityCompletedInRange(start: Long, end: Long): Int = todoDao.getHighPriorityCompletedInRange(start, end)
    suspend fun getMediumPriorityCompletedInRange(start: Long, end: Long): Int = todoDao.getMediumPriorityCompletedInRange(start, end)
    suspend fun getLowPriorityCompletedInRange(start: Long, end: Long): Int = todoDao.getLowPriorityCompletedInRange(start, end)
    suspend fun getActiveCountByCategory(categoryId: Long): Int = todoDao.getActiveCountByCategory(categoryId)
    suspend fun getCompletedCountByCategory(categoryId: Long): Int = todoDao.getCompletedCountByCategory(categoryId)

    fun getSubtasksForTodo(todoId: Long): Flow<List<SubtaskEntity>> = subtaskDao.getSubtasksForTodo(todoId)
    suspend fun getSubtasksForTodoSync(todoId: Long): List<SubtaskEntity> = subtaskDao.getSubtasksForTodoSync(todoId)
    fun getCompletedSubtaskCount(todoId: Long): Flow<Int> = subtaskDao.getCompletedCountForTodo(todoId)
    fun getTotalSubtaskCount(todoId: Long): Flow<Int> = subtaskDao.getTotalCountForTodo(todoId)
    suspend fun insertSubtask(subtask: SubtaskEntity): Long = subtaskDao.insert(subtask)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>) = subtaskDao.insertAll(subtasks)
    suspend fun updateSubtask(subtask: SubtaskEntity) = subtaskDao.update(subtask)
    suspend fun deleteSubtask(subtask: SubtaskEntity) = subtaskDao.delete(subtask)
    suspend fun deleteAllSubtasksForTodo(todoId: Long) = subtaskDao.deleteAllForTodo(todoId)
    suspend fun setSubtaskCompleted(id: Long, completed: Boolean) = subtaskDao.setCompleted(id, completed)
}
