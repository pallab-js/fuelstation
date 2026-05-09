package com.pallab.pumpmanager.feature.fuelprices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity
import com.pallab.pumpmanager.feature.inventory.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FuelPricesUiState(
    val fuelTypes: List<FuelTypeEntity> = emptyList(),
    val editingFuelId: String? = null,
    val editingPrice: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class FuelPricesViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FuelPricesUiState())
    val state: StateFlow<FuelPricesUiState> = _state

    init {
        viewModelScope.launch {
            inventoryRepository.getAllFuelTypes().collect { fuelTypes ->
                _state.value = _state.value.copy(fuelTypes = fuelTypes, isLoading = false)
            }
        }
    }

    fun startEdit(fuelType: FuelTypeEntity) {
        _state.value = _state.value.copy(
            editingFuelId = fuelType.id,
            editingPrice = fuelType.pricePerLiter.toBigDecimal().toPlainString(),
            successMessage = null,
            errorMessage = null
        )
    }

    fun onPriceChanged(price: String) {
        _state.value = _state.value.copy(editingPrice = price)
    }

    fun cancelEdit() {
        _state.value = _state.value.copy(editingFuelId = null, editingPrice = "", errorMessage = null)
    }

    fun savePrice() {
        val s = _state.value
        val fuelId = s.editingFuelId ?: return
        val price = s.editingPrice.toDoubleOrNull()
        if (price == null || price <= 0) {
            _state.value = s.copy(errorMessage = "Please enter a valid price")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            inventoryRepository.updateFuelTypePrice(fuelId, price)
            _state.value = _state.value.copy(
                isSaving = false,
                editingFuelId = null,
                editingPrice = "",
                successMessage = "Price updated successfully"
            )
        }
    }

    fun dismissMessage() {
        _state.value = _state.value.copy(successMessage = null, errorMessage = null)
    }
}
