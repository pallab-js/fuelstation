package com.pallab.pumpmanager.feature.shift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.util.BusinessConstants
import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import com.pallab.pumpmanager.feature.sales.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val shiftRepository: ShiftRepository,
    private val salesRepository: SalesRepository,
    private val sessionManager: SessionManager,
    private val clock: Clock,
    private val idGenerator: IdGenerator
) : ViewModel() {

    private val _state = MutableStateFlow(ShiftUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            shiftRepository.getActiveShift().collect { activeShift ->
                _state.update { it.copy(activeShift = activeShift) }
            }
        }
    }

    fun onEvent(event: ShiftEvent) {
        when (event) {
            is ShiftEvent.OpeningMeterChanged -> _state.update { it.copy(openingMeter = event.value) }
            is ShiftEvent.ClosingMeterChanged -> _state.update { it.copy(closingMeter = event.value) }
            ShiftEvent.StartShift -> startShift()
            ShiftEvent.EndShift -> endShift()
            ShiftEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun startShift() {
        if (_state.value.activeShift != null) {
            _state.update { it.copy(errorMessage = "A shift is already active") }
            return
        }
        val meter = _state.value.openingMeter.toDoubleOrNull()
        if (meter == null) {
            _state.update { it.copy(errorMessage = "Invalid opening meter reading") }
            return
        }
        val userId = sessionManager.currentUserId.value ?: run {
            _state.update { it.copy(errorMessage = "No active session") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val newShift = ShiftEntity(
                    id = idGenerator.newId(),
                    attendantId = userId,
                    startTime = clock.now(),
                    endTime = null,
                    openingMeterReading = meter,
                    closingMeterReading = null,
                    status = "active"
                )
                shiftRepository.insertShift(newShift)
                sessionManager.setShift(newShift.id)
                _state.update { it.copy(isLoading = false, isShiftStarted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to start shift: ${e.message}") }
            }
        }
    }

    private fun endShift() {
        val activeShift = _state.value.activeShift ?: return
        val meter = _state.value.closingMeter.toDoubleOrNull()
        when {
            meter == null || meter < activeShift.openingMeterReading ->
                _state.update { it.copy(errorMessage = "Closing meter must be ≥ opening meter") }
            meter - activeShift.openingMeterReading > BusinessConstants.MAX_SHIFT_METER_DIFFERENCE_LITERS ->
                _state.update { it.copy(errorMessage = "Meter difference exceeds maximum (${BusinessConstants.MAX_SHIFT_METER_DIFFERENCE_LITERS.toInt()} L)") }
            else -> viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    shiftRepository.updateShift(activeShift.copy(
                        endTime = clock.now(),
                        closingMeterReading = meter,
                        status = "closed"
                    ))
                    sessionManager.clearShift()
                    val revenue = salesRepository.getTotalRevenueForShift(activeShift.id) ?: 0.0
                    val salesByShift = salesRepository.getSalesByShiftId(activeShift.id)
                    var salesList: List<com.pallab.pumpmanager.feature.sales.SaleEntity>? = null
                    salesByShift.collect { salesList = it; return@collect }
                    val breakdown = salesList?.groupBy { it.fuelType }?.mapValues { (_, v) -> v.sumOf { it.volumeLiters } } ?: emptyMap()
                    _state.update { it.copy(
                        isLoading = false,
                        isShiftEnded = true,
                        closingMeter = "",
                        summaryData = ShiftSummaryData(
                            totalSales = salesList?.size ?: 0,
                            totalRevenue = revenue,
                            fuelBreakdown = breakdown,
                            meterDifference = meter - activeShift.openingMeterReading
                        )
                    ) }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, errorMessage = "Failed to end shift: ${e.message}") }
                }
            }
        }
    }
}
