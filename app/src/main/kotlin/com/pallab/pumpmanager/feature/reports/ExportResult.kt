package com.pallab.pumpmanager.feature.reports

import com.pallab.pumpmanager.feature.sales.SaleEntity

sealed interface ExportResult {
    data object None : ExportResult
    data class Ready(val sales: List<SaleEntity>, val period: Period) : ExportResult
}
