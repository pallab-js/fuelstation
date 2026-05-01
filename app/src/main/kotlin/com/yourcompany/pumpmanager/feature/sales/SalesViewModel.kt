package com.yourcompany.pumpmanager.feature.sales

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

private const val MAX_VOLUME = 9999.0

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val saleDao: SaleDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(SalesUiState())
    val state = _state.asStateFlow()

    fun onEvent(event: SalesEvent) {
        when (event) {
            is SalesEvent.FuelSelected -> {
                _state.update { it.copy(selectedFuel = event.fuel, pricePerLiter = event.fuel.price) }
                recalculateTotal()
            }
            is SalesEvent.VolumeDigitEntered -> {
                val current = _state.value.volume
                if (event.digit == "." && current.contains(".")) return
                _state.update { it.copy(volume = current + event.digit) }
                recalculateTotal()
            }
            SalesEvent.VolumeDeleted -> {
                if (_state.value.volume.isNotEmpty()) {
                    _state.update { it.copy(volume = it.volume.dropLast(1)) }
                    recalculateTotal()
                }
            }
            is SalesEvent.PaymentModeChanged -> _state.update { it.copy(paymentMode = event.mode) }
            SalesEvent.SaveSale -> saveSale()
            SalesEvent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun recalculateTotal() {
        val vol = _state.value.volume.toDoubleOrNull() ?: 0.0
        _state.update { it.copy(calculatedTotal = vol * it.pricePerLiter) }
    }

    private fun saveSale() {
        val currentState = _state.value
        val vol = currentState.volume.toDoubleOrNull() ?: 0.0
        val shiftId = sessionManager.currentShiftId.value

        when {
            vol <= 0.0 -> _state.update { it.copy(errorMessage = "Please enter volume") }
            vol > MAX_VOLUME -> _state.update { it.copy(errorMessage = "Volume exceeds maximum (${MAX_VOLUME.toInt()} L)") }
            shiftId == null -> _state.update { it.copy(errorMessage = "No active shift. Please start a shift first.") }
            else -> viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    saleDao.insertSale(SaleEntity(
                        id = UUID.randomUUID().toString(),
                        shiftId = shiftId,
                        fuelType = currentState.selectedFuel.label,
                        volumeLiters = vol,
                        pricePerLiter = currentState.pricePerLiter,
                        totalAmount = currentState.calculatedTotal,
                        paymentMode = currentState.paymentMode.name,
                        timestamp = System.currentTimeMillis()
                    ))
                    _state.update { SalesUiState(isSuccess = true) }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, errorMessage = "Failed to save sale: ${e.message}") }
                }
            }
        }
    }
}
