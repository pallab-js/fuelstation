package com.pallab.pumpmanager.feature.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.core.session.SessionManager
import com.pallab.pumpmanager.core.util.BusinessConstants
import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import com.pallab.pumpmanager.feature.inventory.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository,
    private val sessionManager: SessionManager,
    private val clock: Clock,
    private val idGenerator: IdGenerator
) : ViewModel() {

    private val _state = MutableStateFlow(SalesUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.getActiveFuelTypes().collect { fuelTypes ->
                _state.update { currentState ->
                    val updatedSelected = currentState.selectedFuel?.let { selected ->
                        fuelTypes.find { it.id == selected.id } ?: fuelTypes.firstOrNull()
                    } ?: fuelTypes.firstOrNull()
                    currentState.copy(
                        fuelTypes = fuelTypes,
                        selectedFuel = updatedSelected,
                        pricePerLiter = updatedSelected?.pricePerLiter ?: 0.0
                    )
                }
            }
        }
    }

    fun onEvent(event: SalesEvent) {
        when (event) {
            is SalesEvent.FuelSelected -> {
                _state.update { it.copy(selectedFuel = event.fuel, pricePerLiter = event.fuel.pricePerLiter) }
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
        val fuel = currentState.selectedFuel

        when {
            vol <= 0.0 -> _state.update { it.copy(errorMessage = "Please enter volume") }
            vol > BusinessConstants.MAX_SALE_VOLUME_LITERS -> _state.update { it.copy(errorMessage = "Volume exceeds maximum (${BusinessConstants.MAX_SALE_VOLUME_LITERS.toInt()} L)") }
            shiftId == null -> _state.update { it.copy(errorMessage = "No active shift. Please start a shift first.") }
            fuel == null -> _state.update { it.copy(errorMessage = "No fuel type selected") }
            else -> viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    salesRepository.insertSale(SaleEntity(
                        id = idGenerator.newId(),
                        shiftId = shiftId,
                        fuelType = fuel.name,
                        volumeLiters = vol,
                        pricePerLiter = currentState.pricePerLiter,
                        totalAmount = currentState.calculatedTotal,
                        paymentMode = currentState.paymentMode.name,
                        timestamp = clock.now()
                    ))
                    val rowsUpdated = inventoryRepository.decrementStock(
                        fuelTypeId = fuel.id,
                        liters = vol
                    )
                    if (rowsUpdated == 0) {
                        _state.update { it.copy(isLoading = false, errorMessage = "Insufficient stock for selected fuel type") }
                        return@launch
                    }
                    _state.update { SalesUiState(isSuccess = true) }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, errorMessage = "Failed to save sale: ${e.message}") }
                }
            }
        }
    }
}
