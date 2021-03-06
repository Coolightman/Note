package com.coolightman.note.domain.repository

import androidx.lifecycle.LiveData
import com.coolightman.note.domain.entity.Task

interface TaskRepository {

    suspend fun insertTask(task: Task)
    fun getAllTasks(): LiveData<List<Task>>
    suspend fun deleteTask(taskId: Long)
    suspend fun switchActive(taskId: Long)
    suspend fun getTask(taskId: Long): Task
    suspend fun deleteAllInactive()
    suspend fun setTaskIsDeleted(taskId: Long, isDeleted: Boolean)
    suspend fun exportTasks()
    suspend fun importTasks()
}