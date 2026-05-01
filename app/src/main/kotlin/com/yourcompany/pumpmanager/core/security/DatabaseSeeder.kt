package com.yourcompany.pumpmanager.core.security

import com.yourcompany.pumpmanager.feature.auth.UserDao
import com.yourcompany.pumpmanager.feature.auth.UserEntity
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(private val userDao: UserDao) {
    suspend fun seedIfEmpty() {
        if (userDao.getCurrentUser() == null) {
            val salt = UUID.randomUUID().toString()
            userDao.insertUser(
                UserEntity(
                    id = UUID.randomUUID().toString(),
                    name = "Admin",
                    role = "manager",
                    pinHash = PinHasher.hash("1234", salt)
                )
            )
        }
    }
}
