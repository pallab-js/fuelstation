package com.pallab.pumpmanager.feature.sales

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    indices = [Index("shift_id"), Index("timestamp")]
)
data class SaleEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "shift_id") val shiftId: String,
    @ColumnInfo(name = "fuel_type") val fuelType: String,
    @ColumnInfo(name = "volume_liters") val volumeLiters: Double,
    @ColumnInfo(name = "price_per_liter") val pricePerLiter: Double,
    @ColumnInfo(name = "total_amount") val totalAmount: Double,
    @ColumnInfo(name = "payment_mode") val paymentMode: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long
)
