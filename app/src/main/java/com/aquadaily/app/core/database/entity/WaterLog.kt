package com.aquadaily.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis()
)