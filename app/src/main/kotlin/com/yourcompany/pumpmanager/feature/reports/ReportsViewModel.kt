package com.yourcompany.pumpmanager.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val saleDao: SaleDao
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state = _state.asStateFlow()

    init {
        loadReportData()
    }

    fun onEvent(event: ReportsEvent) {
        when (event) {
            ReportsEvent.RefreshData -> loadReportData()
            ReportsEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.timeInMillis

                saleDao.getAllSales().collect { allSales ->
                    val todaySales = allSales.filter { it.timestamp >= startOfDay }
                    
                    val totalRevenueToday = todaySales.sumOf { it.totalAmount }
                    val totalSalesCountToday = todaySales.size
                    
                    val breakdown = todaySales.groupBy { it.fuelType }
                        .mapValues { entry -> entry.value.sumOf { it.totalAmount } }

                    // Weekly Trend (last 7 days)
                    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                    val trend = (0..6).reversed().map { daysAgo ->
                        val dayCal = Calendar.getInstance()
                        dayCal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                        dayCal.set(Calendar.HOUR_OF_DAY, 0)
                        val dayStart = dayCal.timeInMillis
                        dayCal.set(Calendar.HOUR_OF_DAY, 23)
                        dayCal.set(Calendar.MINUTE, 59)
                        val dayEnd = dayCal.timeInMillis
                        
                        val dayRevenue = allSales.filter { it.timestamp in dayStart..dayEnd }
                            .sumOf { it.totalAmount }
                        
                        dateFormat.format(dayCal.time) to dayRevenue
                    }

                    _state.update {
                        it.copy(
                            totalRevenueToday = totalRevenueToday,
                            totalSalesCountToday = totalSalesCountToday,
                            fuelTypeBreakdown = breakdown,
                            weeklyRevenueTrend = trend,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
}
