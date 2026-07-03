package com.aotuding.ding.data.repository

import com.aotuding.ding.AotuDingApplication
import com.aotuding.ding.data.db.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TaskRepository {

    private val dao by lazy { AotuDingApplication.instance.database.taskDao() }

    suspend fun getAllTasks(): List<TaskEntity> = withContext(Dispatchers.IO) {
        dao.getAllTasks()
    }

    suspend fun addTask(time: String) = withContext(Dispatchers.IO) {
        dao.insert(TaskEntity(time = time))
    }

    suspend fun updateTask(id: Int, newTime: String) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        dao.update(existing.copy(time = newTime))
    }

    suspend fun deleteTask(id: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        dao.delete(existing)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.deleteAll()
    }
}