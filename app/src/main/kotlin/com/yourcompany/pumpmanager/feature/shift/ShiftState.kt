package com.yourcompany.pumpmanager.feature.shift

data class ShiftUiState(
    val activeShift: ShiftEntity? = null,
    val openingMeter: String = "",
    val closingMeter: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isShiftStarted: Boolean = false,
    val isShiftEnded: Boolean = false
)
