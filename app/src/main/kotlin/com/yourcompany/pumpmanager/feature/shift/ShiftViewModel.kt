package com.yourcompany.pumpmanager.feature.shift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yourcompany.pumpmanager.core.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

private const val MAX_METER_DIFFERENCE = 5000.0

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val shiftDao: ShiftDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ShiftUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            shiftDao.getActiveShift().collect { activeShift ->
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
                    id = UUID.randomUUID().toString(),
                    attendantId = userId,
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    openingMeterReading = meter,
                    closingMeterReading = null,
                    status = "active"
                )
                shiftDao.insertShift(newShift)
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
            meter - activeShift.openingMeterReading > MAX_METER_DIFFERENCE ->
                _state.update { it.copy(errorMessage = "Meter difference exceeds maximum (${MAX_METER_DIFFERENCE.toInt()} L)") }
            else -> viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    shiftDao.insertShift(activeShift.copy(
                        endTime = System.currentTimeMillis(),
                        closingMeterReading = meter,
                        status = "closed"
                    ))
                    sessionManager.clearShift()
                    _state.update { it.copy(isLoading = false, isShiftEnded = true, closingMeter = "") }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, errorMessage = "Failed to end shift: ${e.message}") }
                }
            }
        }
    }
}
