package com.aquadaily.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String = "",
    val gender: String,
    val age: Int,
    val weight: Double,
    val profileImage: String? = null,
    val streak: Int = 0
)
