package com.yourcompany.pumpmanager.feature.sales

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val shiftId: String,
    val fuelType: String,
    val volumeLiters: Double,
    val pricePerLiter: Double,
    val totalAmount: Double,
    val paymentMode: String,    // "CASH" | "UPI" | "CARD"
    val timestamp: Long
)
