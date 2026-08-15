package com.aquadaily.app.core.repository

import com.aquadaily.app.core.database.dao.UserDao
import com.aquadaily.app.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun deleteUser() = userDao.deleteUser()
}
