package com.aquadaily.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val date: String,

    val time: String,

    val amount: Int,

    val note: String = ""
)