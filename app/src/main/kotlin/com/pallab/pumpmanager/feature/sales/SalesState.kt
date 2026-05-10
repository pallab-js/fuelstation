package com.pallab.pumpmanager.feature.sales

import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity

enum class PaymentMode {
    CASH, UPI, CARD, CHEQUE, FUEL_CARD, CREDIT
}

data class SalesUiState(
    val fuelTypes: List<FuelTypeEntity> = emptyList(),
    val selectedFuel: FuelTypeEntity? = null,
    val volume: String = "",
    val pricePerLiter: Double = 0.0,
    val calculatedTotal: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
