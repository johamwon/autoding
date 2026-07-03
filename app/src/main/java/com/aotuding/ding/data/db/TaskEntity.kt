package com.aotuding.ding.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: String, // HH:mm:ss
    val enabled: Boolean = true
)