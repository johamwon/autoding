package com.aotuding.ding.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY time ASC")
    fun getAllTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: TaskEntity): Long

    @Update
    fun update(task: TaskEntity)

    @Delete
    fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks")
    fun deleteAll()

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getById(id: Int): TaskEntity?
}