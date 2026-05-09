package com.pallab.pumpmanager.core.security

import com.pallab.pumpmanager.core.util.IdGenerator
import com.pallab.pumpmanager.feature.auth.UserDao
import com.pallab.pumpmanager.feature.auth.UserEntity
import com.pallab.pumpmanager.feature.inventory.FuelTypeDao
import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity
import com.pallab.pumpmanager.feature.inventory.TankDao
import com.pallab.pumpmanager.feature.inventory.TankEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val userDao: UserDao,
    private val tankDao: TankDao,
    private val fuelTypeDao: FuelTypeDao,
    private val idGenerator: IdGenerator
) {
    suspend fun seedIfEmpty() {
        if (userDao.getCurrentUser() == null) {
            userDao.insertUser(
                UserEntity(
                    id = idGenerator.newId(),
                    name = "Admin",
                    role = "manager",
                    pinHash = ""
                )
            )
        }
        if (fuelTypeDao.getActiveFuelTypes().first().isEmpty()) {
            listOf(
                FuelTypeEntity("petrol", "Petrol", 102.50),
                FuelTypeEntity("diesel", "Diesel", 94.20),
                FuelTypeEntity("cng", "CNG", 85.00)
            ).forEach { fuelTypeDao.insertFuelType(it) }
        }
        if (tankDao.getAllTanks().first().isEmpty()) {
            listOf(
                TankEntity(idGenerator.newId(), "petrol", 10_000.0, 8_000.0),
                TankEntity(idGenerator.newId(), "diesel", 10_000.0, 7_500.0),
                TankEntity(idGenerator.newId(), "cng", 5_000.0, 500.0)
            ).forEach { tankDao.insertTank(it) }
        }
    }
}
