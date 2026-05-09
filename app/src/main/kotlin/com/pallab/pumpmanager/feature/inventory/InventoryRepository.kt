package com.pallab.pumpmanager.feature.inventory

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface InventoryRepository {
    fun getAllTanks(): Flow<List<TankEntity>>
    suspend fun getTankById(id: String): TankEntity?
    fun getLowStockTanks(): Flow<List<TankEntity>>
    suspend fun decrementStock(fuelTypeId: String, liters: Double): Int
    suspend fun addStock(tankId: String, liters: Double): Int
    fun getActiveFuelTypes(): Flow<List<FuelTypeEntity>>
    fun getAllFuelTypes(): Flow<List<FuelTypeEntity>>
    suspend fun updateFuelTypePrice(fuelTypeId: String, price: Double)
    suspend fun insertRefillLog(log: RefillLogEntity)
}

@Singleton
class InventoryRepositoryImpl @Inject constructor(
    private val tankDao: TankDao,
    private val fuelTypeDao: FuelTypeDao,
    private val refillLogDao: RefillLogDao
) : InventoryRepository {
    override fun getAllTanks() = tankDao.getAllTanks()
    override suspend fun getTankById(id: String) = tankDao.getTankById(id)
    override fun getLowStockTanks() = tankDao.getLowStockTanks()
    override suspend fun decrementStock(fuelTypeId: String, liters: Double) = tankDao.decrementStock(fuelTypeId, liters)
    override suspend fun addStock(tankId: String, liters: Double) = tankDao.addStock(tankId, liters)
    override fun getActiveFuelTypes() = fuelTypeDao.getActiveFuelTypes()
    override fun getAllFuelTypes() = fuelTypeDao.getAllFuelTypes()
    override suspend fun updateFuelTypePrice(fuelTypeId: String, price: Double) = fuelTypeDao.updateFuelTypePrice(fuelTypeId, price)
    override suspend fun insertRefillLog(log: RefillLogEntity) = refillLogDao.insertRefillLog(log)
}
