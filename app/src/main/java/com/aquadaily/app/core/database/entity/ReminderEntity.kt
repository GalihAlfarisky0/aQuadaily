package com.aquadaily.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val hour: Int,
    val minute: Int,
    val day: String,
    val amount: Int,
    val isEnabled: Boolean = true
)
