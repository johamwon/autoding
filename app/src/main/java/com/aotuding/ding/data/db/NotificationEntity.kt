package com.aotuding.ding.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String = "",
    val title: String = "",
    val message: String = "",
    val postTime: String = ""
)