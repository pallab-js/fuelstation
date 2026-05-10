package com.pallab.pumpmanager.feature.saleshistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pallab.pumpmanager.feature.inventory.InventoryRepository
import com.pallab.pumpmanager.feature.sales.SaleEntity
import com.pallab.pumpmanager.feature.sales.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface VoidState {
    data object Idle : VoidState
    data object Loading : VoidState
    data class Success(val message: String) : VoidState
    data class Error(val message: String) : VoidState
}

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val salesRepository: SalesRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    val salesPaged: Flow<PagingData<SaleEntity>> = salesRepository.getSalesPaged()
        .cachedIn(viewModelScope)

    private val _voidState = MutableStateFlow<VoidState>(VoidState.Idle)
    val voidState = _voidState.asStateFlow()

    fun voidSale(sale: SaleEntity) {
        viewModelScope.launch {
            _voidState.value = VoidState.Loading
            try {
                val rowsUpdated = inventoryRepository.incrementStockByFuelTypeId(
                    fuelTypeId = sale.fuelType,
                    liters = sale.volumeLiters
                )
                if (rowsUpdated == 0) {
                    _voidState.value = VoidState.Error("Could not restore stock for fuel type: ${sale.fuelType}")
                    return@launch
                }
                salesRepository.deleteSale(sale.id)
                _voidState.value = VoidState.Success("Sale voided successfully")
            } catch (e: Exception) {
                _voidState.value = VoidState.Error("Failed to void sale: ${e.message}")
            }
        }
    }

    fun dismissVoidResult() {
        _voidState.value = VoidState.Idle
    }
}
