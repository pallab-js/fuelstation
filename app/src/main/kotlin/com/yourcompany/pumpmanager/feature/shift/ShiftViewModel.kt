package com.yourcompany.pumpmanager.feature.shift

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val shiftDao: ShiftDao
) : ViewModel() {

    private val _state = MutableStateFlow(ShiftUiState())
    val state = _state.asStateFlow()

    init {
        observeActiveShift()
    }

    private fun observeActiveShift() {
        viewModelScope.launch {
            shiftDao.getActiveShift().collect { activeShift ->
                _state.update { it.copy(activeShift = activeShift) }
            }
        }
    }

    fun onEvent(event: ShiftEvent) {
        when (event) {
            is ShiftEvent.OpeningMeterChanged -> {
                _state.update { it.copy(openingMeter = event.value) }
            }
            is ShiftEvent.ClosingMeterChanged -> {
                _state.update { it.copy(closingMeter = event.value) }
            }
            ShiftEvent.StartShift -> {
                startShift()
            }
            ShiftEvent.EndShift -> {
                endShift()
            }
            ShiftEvent.DismissError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun startShift() {
        val meter = _state.value.openingMeter.toDoubleOrNull()
        if (meter == null) {
            _state.update { it.copy(errorMessage = "Invalid opening meter reading") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val newShift = ShiftEntity(
                    id = UUID.randomUUID().toString(),
                    attendantId = "current_user", // TODO: Get from AuthManager
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    openingMeterReading = meter,
                    closingMeterReading = null,
                    status = "active"
                )
                shiftDao.insertShift(newShift)
                _state.update { it.copy(isLoading = false, isShiftStarted = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to start shift: ${e.message}") }
            }
        }
    }

    private fun endShift() {
        val activeShift = _state.value.activeShift ?: return
        val meter = _state.value.closingMeter.toDoubleOrNull()
        
        if (meter == null || meter < activeShift.openingMeterReading) {
            _state.update { it.copy(errorMessage = "Invalid closing meter reading") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val endedShift = activeShift.copy(
                    endTime = System.currentTimeMillis(),
                    closingMeterReading = meter,
                    status = "closed"
                )
                shiftDao.insertShift(endedShift)
                _state.update { it.copy(isLoading = false, isShiftEnded = true, closingMeter = "") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, errorMessage = "Failed to end shift: ${e.message}") }
            }
        }
    }
}
