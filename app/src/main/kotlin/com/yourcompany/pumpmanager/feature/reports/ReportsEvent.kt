package com.yourcompany.pumpmanager.feature.reports

sealed interface ReportsEvent {
    object RefreshData : ReportsEvent
    object DismissError : ReportsEvent
}
