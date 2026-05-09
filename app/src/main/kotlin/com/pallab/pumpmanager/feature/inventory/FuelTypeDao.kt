package com.pallab.pumpmanager.feature.inventory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelTypeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelType(fuelType: FuelTypeEntity)

    @Query("SELECT * FROM fuel_types WHERE isActive = 1")
    fun getActiveFuelTypes(): Flow<List<FuelTypeEntity>>

    @Query("SELECT * FROM fuel_types")
    fun getAllFuelTypes(): Flow<List<FuelTypeEntity>>

    @Query("UPDATE fuel_types SET pricePerLiter = :price WHERE id = :fuelTypeId")
    suspend fun updateFuelTypePrice(fuelTypeId: String, price: Double)
}
