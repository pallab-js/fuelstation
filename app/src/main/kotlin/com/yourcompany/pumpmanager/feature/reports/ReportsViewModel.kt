package com.yourcompany.pumpmanager.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(private val saleDao: SaleDao) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState())
    val state = _state.asStateFlow()

    init { loadReportData() }

    fun onEvent(event: ReportsEvent) {
        when (event) {
            ReportsEvent.RefreshData -> loadReportData()
            ReportsEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
            is ReportsEvent.PeriodChanged -> {
                _state.update { it.copy(selectedPeriod = event.period) }
                loadReportData()
            }
        }
    }

    private fun loadReportData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                saleDao.getAllSales().collect { allSales ->
                    val period = _state.value.selectedPeriod
                    val days = when (period) { Period.TODAY -> 0; Period.WEEK -> 6; Period.MONTH -> 29 }
                    val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

                    val windowStart = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, -days)
                        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                    }.timeInMillis

                    val periodSales = allSales.filter { it.timestamp >= windowStart }

                    val trend = (0..days).reversed().map { daysAgo ->
                        val cal = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_YEAR, -daysAgo)
                            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                        }
                        val dayStart = cal.timeInMillis
                        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                        val dayEnd = cal.timeInMillis
                        val rev = allSales.filter { it.timestamp in dayStart..dayEnd }.sumOf { it.totalAmount }
                        dateFormat.format(cal.time) to rev
                    }

                    _state.update {
                        it.copy(
                            totalRevenueToday = periodSales.sumOf { s -> s.totalAmount },
                            totalSalesCountToday = periodSales.size,
                            fuelTypeBreakdown = periodSales.groupBy { s -> s.fuelType }
                                .mapValues { e -> e.value.sumOf { s -> s.totalAmount } },
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
