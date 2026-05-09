package com.pallab.pumpmanager.feature.auth

import javax.inject.Inject
import javax.inject.Singleton

interface AuthRepository {
    suspend fun getCurrentUser(): UserEntity?
    suspend fun getUserById(id: String): UserEntity?
    fun getAllUsers(): kotlinx.coroutines.flow.Flow<List<UserEntity>>
    suspend fun insertUser(user: UserEntity)
    suspend fun updatePinHash(userId: String, pinHash: String)
}

@Singleton
class AuthRepositoryImpl @Inject constructor(private val userDao: UserDao) : AuthRepository {
    override suspend fun getCurrentUser() = userDao.getCurrentUser()
    override suspend fun getUserById(id: String) = userDao.getUserById(id)
    override fun getAllUsers() = userDao.getAllUsers()
    override suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    override suspend fun updatePinHash(userId: String, pinHash: String) = userDao.updatePinHash(userId, pinHash)
}
