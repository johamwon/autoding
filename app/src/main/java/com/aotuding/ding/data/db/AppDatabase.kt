package com.aotuding.ding.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskEntity::class, NotificationEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun notificationDao(): NotificationDao
}