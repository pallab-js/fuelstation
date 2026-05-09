package com.pallab.pumpmanager.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pallab.pumpmanager.core.util.Clock
import com.pallab.pumpmanager.core.util.IdGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryUiState(
    val tanks: List<TankEntity> = emptyList(),
    val refillingTankId: String? = null,
    val refillVolume: String = "",
    val isRefilling: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val idGenerator: IdGenerator,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            inventoryRepository.getAllTanks().collect { tanks ->
                _state.update { it.copy(tanks = tanks, isLoading = false) }
            }
        }
    }

    fun isLowStock(tank: TankEntity) = tank.currentStockLiters < tank.capacityLiters * 0.1

    fun showRefillDialog(tankId: String) {
        _state.update { it.copy(refillingTankId = tankId, refillVolume = "", errorMessage = null) }
    }

    fun onRefillVolumeChanged(volume: String) {
        _state.update { it.copy(refillVolume = volume) }
    }

    fun cancelRefill() {
        _state.update { it.copy(refillingTankId = null, refillVolume = "") }
    }

    fun confirmRefill() {
        val s = _state.value
        val tankId = s.refillingTankId ?: return
        val liters = s.refillVolume.toDoubleOrNull()
        if (liters == null || liters <= 0) {
            _state.update { it.copy(errorMessage = "Please enter a valid volume") }
            return
        }
        val tank = s.tanks.find { it.id == tankId } ?: return
        viewModelScope.launch {
            _state.update { it.copy(isRefilling = true) }
            inventoryRepository.addStock(tankId, liters)
            inventoryRepository.insertRefillLog(
                RefillLogEntity(
                    id = idGenerator.newId(),
                    tankId = tankId,
                    fuelTypeId = tank.fuelTypeId,
                    litersAdded = liters,
                    timestamp = clock.now()
                )
            )
            _state.update { it.copy(
                isRefilling = false,
                refillingTankId = null,
                refillVolume = "",
                successMessage = "Refill completed: ${liters.toInt()} L added"
            ) }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(successMessage = null, errorMessage = null) }
    }
}
