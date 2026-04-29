package com.yourcompany.pumpmanager.feature.sales

sealed interface SalesEvent {
    data class FuelSelected(val fuel: FuelType) : SalesEvent
    data class VolumeDigitEntered(val digit: String) : SalesEvent
    object VolumeDeleted : SalesEvent
    data class PaymentModeChanged(val mode: PaymentMode) : SalesEvent
    object SaveSale : SalesEvent
    object DismissError : SalesEvent
}
