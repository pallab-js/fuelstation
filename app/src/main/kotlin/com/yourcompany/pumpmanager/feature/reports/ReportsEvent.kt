package com.yourcompany.pumpmanager.feature.reports

sealed interface ReportsEvent {
    object RefreshData : ReportsEvent
    object DismissError : ReportsEvent
    data class PeriodChanged(val period: Period) : ReportsEvent
}
