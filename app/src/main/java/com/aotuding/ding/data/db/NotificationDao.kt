package com.aotuding.ding.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE postTime LIKE :today || '%' ORDER BY postTime DESC")
    suspend fun getTodayNotifications(today: String): List<NotificationEntity>

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}