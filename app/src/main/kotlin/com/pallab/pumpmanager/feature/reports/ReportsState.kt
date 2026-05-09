package com.pallab.pumpmanager.feature.reports

enum class Period { TODAY, WEEK, MONTH }

data class ReportsUiState(
    val selectedPeriod: Period = Period.TODAY,
    val totalRevenueToday: Double = 0.0,
    val totalSalesCountToday: Int = 0,
    val fuelTypeBreakdown: Map<String, Double> = emptyMap(),
    val weeklyRevenueTrend: List<Pair<String, Double>> = emptyList(),
    val isExporting: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
