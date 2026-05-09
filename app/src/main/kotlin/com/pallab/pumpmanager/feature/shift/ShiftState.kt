package com.pallab.pumpmanager.feature.shift

data class ShiftSummaryData(
    val totalSales: Int = 0,
    val totalRevenue: Double = 0.0,
    val fuelBreakdown: Map<String, Double> = emptyMap(),
    val meterDifference: Double = 0.0
)

data class ShiftUiState(
    val activeShift: ShiftEntity? = null,
    val openingMeter: String = "",
    val closingMeter: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isShiftStarted: Boolean = false,
    val isShiftEnded: Boolean = false,
    val summaryData: ShiftSummaryData? = null
)
