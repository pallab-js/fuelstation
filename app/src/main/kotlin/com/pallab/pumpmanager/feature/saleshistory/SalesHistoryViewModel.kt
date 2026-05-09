package com.pallab.pumpmanager.feature.saleshistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pallab.pumpmanager.feature.sales.SaleEntity
import com.pallab.pumpmanager.feature.sales.SalesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SalesHistoryViewModel @Inject constructor(
    private val salesRepository: SalesRepository
) : ViewModel() {

    val salesPaged: Flow<PagingData<SaleEntity>> = salesRepository.getSalesPaged()
        .cachedIn(viewModelScope)
}
