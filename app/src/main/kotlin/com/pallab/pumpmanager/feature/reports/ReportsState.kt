package com.pallab.pumpmanager.feature.reports

enum class Period { TODAY, WEEK, MONTH }

data class ReportsUiState(
    val selectedPeriod: Period = Period.TODAY,
    val totalRevenue: Double = 0.0,
    val totalSalesCount: Int = 0,
    val fuelTypeBreakdown: Map<String, Double> = emptyMap(),
    val revenueTrend: List<Pair<String, Double>> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
