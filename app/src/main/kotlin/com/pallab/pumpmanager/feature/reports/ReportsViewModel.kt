package com.pallab.pumpmanager.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.feature.sales.SaleEntity
import com.pallab.pumpmanager.feature.sales.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val saleRepository: SalesRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(Period.TODAY)

    val state: StateFlow<ReportsUiState> = _selectedPeriod
        .flatMapLatest { period ->
            saleRepository.getAllSales().mapLatest { allSales ->
                buildReportsState(period, allSales)
            }
        }
        .catch { e -> emit(ReportsUiState(errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ReportsUiState(isLoading = true)
        )

    fun onEvent(event: ReportsEvent) {
        when (event) {
            is ReportsEvent.PeriodChanged -> _selectedPeriod.value = event.period
            ReportsEvent.RefreshData -> _selectedPeriod.value = _selectedPeriod.value
            ReportsEvent.DismissError -> { }
            ReportsEvent.ExportCsv -> { }
        }
    }

    suspend fun getSalesForExport(startOfDay: Long): List<SaleEntity> {
        return saleRepository.getAllSales().let { flow ->
            var list: List<SaleEntity>? = null
            flow.collect { list = it; return@collect }
            list ?: emptyList()
        }.filter { it.timestamp >= startOfDay }
    }

    private suspend fun buildReportsState(period: Period, allSales: List<SaleEntity>): ReportsUiState {
        val now = LocalDate.now()
        val zone = ZoneId.systemDefault()

        val days = when (period) { Period.TODAY -> 0; Period.WEEK -> 6; Period.MONTH -> 29 }

        val windowStart = now.minusDays(days.toLong())
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

        val periodSales = allSales.filter { it.timestamp >= windowStart }

        val trend = saleRepository.getRevenueTrendSince(windowStart)
        val weeklyRevenueTrend = trend.map { it.day to it.revenue }

        return ReportsUiState(
            selectedPeriod = period,
            totalRevenueToday = periodSales.sumOf { s -> s.totalAmount },
            totalSalesCountToday = periodSales.size,
            fuelTypeBreakdown = periodSales.groupBy { s -> s.fuelType }
                .mapValues { e -> e.value.sumOf { s -> s.totalAmount } },
            weeklyRevenueTrend = weeklyRevenueTrend,
            isLoading = false
        )
    }
}
