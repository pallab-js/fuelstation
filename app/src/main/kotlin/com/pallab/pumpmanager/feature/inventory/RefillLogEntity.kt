package com.pallab.pumpmanager.feature.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "refill_log")
data class RefillLogEntity(
    @PrimaryKey val id: String,
    val tankId: String,
    val fuelTypeId: String,
    val litersAdded: Double,
    val timestamp: Long
)
