package com.yourcompany.pumpmanager.feature.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class InventoryUiState(
    val tanks: List<TankEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class InventoryViewModel @Inject constructor(private val tankDao: TankDao) : ViewModel() {

    private val _state = MutableStateFlow(InventoryUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (tankDao.getAllTanks().first().isEmpty()) {
                listOf(
                    TankEntity(UUID.randomUUID().toString(), "petrol", 10000.0, 8000.0),
                    TankEntity(UUID.randomUUID().toString(), "diesel", 10000.0, 7500.0),
                    TankEntity(UUID.randomUUID().toString(), "cng", 5000.0, 500.0)
                ).forEach { tankDao.insertTank(it) }
            }
            tankDao.getAllTanks().collect { tanks ->
                _state.update { it.copy(tanks = tanks, isLoading = false) }
            }
        }
    }

    /** Returns true if stock is below 10% of capacity. */
    fun isLowStock(tank: TankEntity) = tank.currentStockLiters < tank.capacityLiters * 0.1
}
