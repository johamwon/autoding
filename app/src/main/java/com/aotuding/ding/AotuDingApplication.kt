package com.aotuding.ding

import android.app.Application
import androidx.room.Room
import com.aotuding.ding.data.db.AppDatabase

class AotuDingApplication : Application() {

    companion object {
        lateinit var instance: AotuDingApplication
            private set
    }

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "aotuding.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}