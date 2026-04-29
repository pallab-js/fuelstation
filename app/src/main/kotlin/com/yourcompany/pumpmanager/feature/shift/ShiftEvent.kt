package com.yourcompany.pumpmanager.feature.shift

sealed interface ShiftEvent {
    data class OpeningMeterChanged(val value: String) : ShiftEvent
    data class ClosingMeterChanged(val value: String) : ShiftEvent
    object StartShift : ShiftEvent
    object EndShift : ShiftEvent
    object DismissError : ShiftEvent
}
