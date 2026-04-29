package com.yourcompany.pumpmanager.core.navigation

sealed class Routes(val route: String) {
    object Splash : Routes("splash")
    object Auth : Routes("auth")
    object Dashboard : Routes("dashboard")
    object Sales : Routes("sales")
    object Shift : Routes("shift")
    object Inventory : Routes("inventory")
    object Reports : Routes("reports")
}
