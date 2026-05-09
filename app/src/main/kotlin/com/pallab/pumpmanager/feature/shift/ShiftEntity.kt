package com.pallab.pumpmanager.feature.shift

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shifts",
    indices = [Index("status")]
)
data class ShiftEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "attendant_id") val attendantId: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
    @ColumnInfo(name = "end_time") val endTime: Long?,
    @ColumnInfo(name = "opening_meter_reading") val openingMeterReading: Double,
    @ColumnInfo(name = "closing_meter_reading") val closingMeterReading: Double?,
    @ColumnInfo(name = "status") val status: String
)
