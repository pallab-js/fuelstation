package com.pallab.pumpmanager.feature.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tanks")
data class TankEntity(
    @PrimaryKey val id: String,
    val fuelTypeId: String,
    val capacityLiters: Double,
    val currentStockLiters: Double
)
