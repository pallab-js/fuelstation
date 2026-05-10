package com.pallab.pumpmanager.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.feature.sales.SaleEntity
import com.pallab.pumpmanager.feature.sales.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val saleRepository: SalesRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(Period.TODAY)
    private val _refreshTrigger = MutableStateFlow(0)

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
            ReportsEvent.RefreshData -> _refreshTrigger.value = _refreshTrigger.value + 1
            ReportsEvent.DismissError -> { /* stateIn handles error lifecycle on next emission */ }
            ReportsEvent.ExportCsv -> {
                viewModelScope.launch {
                    val period = _selectedPeriod.value
                    val days = when (period) { Period.TODAY -> 0; Period.WEEK -> 6; Period.MONTH -> 29 }
                    val windowStart = LocalDate.now().minusDays(days.toLong())
                        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val sales = getSalesForExport(windowStart)
                    _exportResult.value = ExportResult.Ready(sales, period)
                }
            }
        }
    }

    private val _exportResult = MutableStateFlow<ExportResult>(ExportResult.None)
    val exportResult: StateFlow<ExportResult> = _exportResult

    private suspend fun getSalesForExport(windowStart: Long): List<SaleEntity> {
        return try {
            saleRepository.getAllSales().first().filter { it.timestamp >= windowStart }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearExportResult() {
        _exportResult.value = ExportResult.None
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

        val trend = try { saleRepository.getRevenueTrendSince(windowStart) } catch (_: Exception) { emptyList() }
        val revenueTrend = trend.map { it.day to it.revenue }

        return ReportsUiState(
            selectedPeriod = period,
            totalRevenue = periodSales.sumOf { s -> s.totalAmount },
            totalSalesCount = periodSales.size,
            fuelTypeBreakdown = periodSales.groupBy { s -> s.fuelType }
                .mapValues { e -> e.value.sumOf { s -> s.totalAmount } },
            revenueTrend = revenueTrend,
            isLoading = false
        )
    }
}
