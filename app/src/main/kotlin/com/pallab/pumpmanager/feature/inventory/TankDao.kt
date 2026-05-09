package com.pallab.pumpmanager.feature.inventory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TankDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTank(tank: TankEntity)

    @Query("SELECT * FROM tanks")
    fun getAllTanks(): Flow<List<TankEntity>>

    @Query("SELECT * FROM tanks WHERE id = :id")
    suspend fun getTankById(id: String): TankEntity?

    @Query("SELECT * FROM tanks WHERE currentStockLiters < (capacityLiters * 0.1)")
    fun getLowStockTanks(): Flow<List<TankEntity>>

    @Query("UPDATE tanks SET currentStockLiters = currentStockLiters - :liters WHERE fuelTypeId = :fuelTypeId AND currentStockLiters >= :liters")
    suspend fun decrementStock(fuelTypeId: String, liters: Double): Int

    @Query("UPDATE tanks SET currentStockLiters = currentStockLiters + :liters WHERE id = :tankId")
    suspend fun addStock(tankId: String, liters: Double): Int
}
