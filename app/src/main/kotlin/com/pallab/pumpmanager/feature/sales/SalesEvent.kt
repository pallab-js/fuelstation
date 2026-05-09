package com.pallab.pumpmanager.feature.sales

import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity

sealed interface SalesEvent {
    data class FuelSelected(val fuel: FuelTypeEntity) : SalesEvent
    data class VolumeDigitEntered(val digit: String) : SalesEvent
    object VolumeDeleted : SalesEvent
    data class PaymentModeChanged(val mode: PaymentMode) : SalesEvent
    object SaveSale : SalesEvent
    object DismissError : SalesEvent
}
