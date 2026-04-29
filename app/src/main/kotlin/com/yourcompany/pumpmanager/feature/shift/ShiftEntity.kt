package com.yourcompany.pumpmanager.feature.shift

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val id: String,
    val attendantId: String,
    val startTime: Long,        // Unix epoch ms
    val endTime: Long?,
    val openingMeterReading: Double,
    val closingMeterReading: Double?,
    val status: String          // "active" | "closed"
)
