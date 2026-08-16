package com.aquadaily.app.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao
    abstract fun historyDao(): HistoryDao
    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN passwordHash TEXT NOT NULL DEFAULT ''"
                )
                database.execSQL(
                    "ALTER TABLE history ADD COLUMN userId INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE reminder ADD COLUMN userId INTEGER NOT NULL DEFAULT 0"
                )

                database.execSQL(
                    "UPDATE history SET userId = (SELECT id FROM users ORDER BY id ASC LIMIT 1) WHERE userId = 0"
                )
                database.execSQL(
                    "UPDATE reminder SET userId = (SELECT id FROM users ORDER BY id ASC LIMIT 1) WHERE userId = 0"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aquadaily_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
