package com.aquadaily.app.core.repository

import com.aquadaily.app.core.database.dao.UserDao
import com.aquadaily.app.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    fun getUserById(userId: Int): Flow<UserEntity?> =
        userDao.getUserById(userId)

    suspend fun getUserByEmail(email: String): UserEntity? =
        userDao.getUserByEmail(email)

    suspend fun insertUser(user: UserEntity): Long =
        userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) =
        userDao.updateUser(user)

    suspend fun deleteUserById(userId: Int) =
        userDao.deleteUserById(userId)

    suspend fun deleteUser() =
        userDao.deleteUser()
}
