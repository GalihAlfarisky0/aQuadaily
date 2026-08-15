package com.aquadaily.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aquadaily.app.core.database.dao.HistoryDao
import com.aquadaily.app.core.database.dao.ReminderDao
import com.aquadaily.app.core.database.dao.UserDao
import com.aquadaily.app.core.database.entity.HistoryEntity
import com.aquadaily.app.core.database.entity.ReminderEntity
import com.aquadaily.app.core.database.entity.UserEntity

@Database(
    entities = [
        ReminderEntity::class,
        HistoryEntity::class,
        UserEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    abstract fun historyDao(): HistoryDao

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquadaily_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance

                instance
            }
        }
    }
}