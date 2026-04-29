package com.yourcompany.pumpmanager.feature.reports

data class ReportsUiState(
    val totalRevenueToday: Double = 0.0,
    val totalSalesCountToday: Int = 0,
    val fuelTypeBreakdown: Map<String, Double> = emptyMap(),
    val weeklyRevenueTrend: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
