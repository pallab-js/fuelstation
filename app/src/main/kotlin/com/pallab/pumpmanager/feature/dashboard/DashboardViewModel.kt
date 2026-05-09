package com.pallab.pumpmanager.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.feature.sales.SalesRepository
import com.pallab.pumpmanager.feature.shift.ShiftEntity
import com.pallab.pumpmanager.feature.shift.ShiftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DashboardUiState(
    val todayRevenue: Double = 0.0,
    val todaySalesCount: Int = 0,
    val activeShift: ShiftEntity? = null,
    val currentUserId: String? = null,
    val currentUserRole: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val saleRepository: SalesRepository,
    private val shiftRepository: ShiftRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            combine(saleRepository.getTodaySales(startOfDay), shiftRepository.getActiveShift()) { sales, activeShift ->
                DashboardUiState(
                    todayRevenue = sales.sumOf { it.totalAmount },
                    todaySalesCount = sales.size,
                    activeShift = activeShift,
                    currentUserId = sessionManager.currentUserId.value,
                    currentUserRole = sessionManager.currentUserRole.value,
                    isLoading = false
                )
            }.collect { _state.value = it }
        }
    }
}
