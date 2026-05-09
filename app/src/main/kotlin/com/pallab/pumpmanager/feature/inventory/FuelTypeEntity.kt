package com.pallab.pumpmanager.feature.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_types")
data class FuelTypeEntity(
    @PrimaryKey val id: String,
    val name: String,           // "Petrol", "Diesel", "CNG"
    val pricePerLiter: Double,
    val isActive: Boolean = true
)
