package com.yourcompany.pumpmanager.feature.sales

enum class FuelType(val label: String, val price: Double) {
    PETROL("Petrol", 102.50),
    DIESEL("Diesel", 94.20),
    CNG("CNG", 85.00)
}

enum class PaymentMode {
    CASH, UPI, CARD
}

data class SalesUiState(
    val selectedFuel: FuelType = FuelType.PETROL,
    val volume: String = "",
    val pricePerLiter: Double = FuelType.PETROL.price,
    val calculatedTotal: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
