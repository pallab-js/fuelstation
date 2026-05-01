package com.yourcompany.pumpmanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.pumpmanager.core.session.SessionManager
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import com.yourcompany.pumpmanager.feature.shift.ShiftDao
import com.yourcompany.pumpmanager.feature.shift.ShiftEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class DashboardUiState(
    val todayRevenue: Double = 0.0,
    val todaySalesCount: Int = 0,
    val activeShift: ShiftEntity? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val saleDao: SaleDao,
    private val shiftDao: ShiftDao,
    val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
            }.timeInMillis

            combine(saleDao.getAllSales(), shiftDao.getActiveShift()) { sales, activeShift ->
                val todaySales = sales.filter { it.timestamp >= startOfDay }
                DashboardUiState(
                    todayRevenue = todaySales.sumOf { it.totalAmount },
                    todaySalesCount = todaySales.size,
                    activeShift = activeShift,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }
}
