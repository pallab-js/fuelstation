package com.yourcompany.pumpmanager.feature.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourcompany.pumpmanager.core.navigation.Routes
import com.yourcompany.pumpmanager.core.theme.PumpManagerTheme
import com.yourcompany.pumpmanager.feature.sales.SalesScreen
import com.yourcompany.pumpmanager.feature.shift.ShiftScreen
import com.yourcompany.pumpmanager.feature.reports.ReportsScreen

@Composable
fun DashboardScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateTo: (String) -> Unit
) {
    var selectedItem by remember { mutableIntStateOf(0) }
    val items = listOf(
        NavigationItem("Dashboard", Icons.Default.Dashboard, Routes.Dashboard.route),
        NavigationItem("Sales", Icons.Default.LocalGasStation, Routes.Sales.route),
        NavigationItem("Shift", Icons.Default.Schedule, Routes.Shift.route),
        NavigationItem("Inventory", Icons.Default.Inventory, Routes.Inventory.route),
        NavigationItem("Reports", Icons.Default.BarChart, Routes.Reports.route)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // Current tab content
        Surface(
            modifier = Modifier.padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when (selectedItem) {
                0 -> DashboardHome()
                1 -> SalesScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                2 -> ShiftScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                4 -> ReportsScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                else -> PlaceholderScreen(items[selectedItem].label)
            }
        }
    }
}

@Composable
fun DashboardHome() {
    // Basic dashboard home content
    Text(
        text = "Dashboard Overview",
        style = MaterialTheme.typography.displaySmall,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun PlaceholderScreen(name: String) {
    Text(
        text = "$name Screen (Coming Soon)",
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(16.dp)
    )
}

data class NavigationItem(val label: String, val icon: ImageVector, val route: String)

@Preview
@Composable
fun DashboardScreenPreview() {
    PumpManagerTheme {
        DashboardScreen(
            snackbarHostState = remember { SnackbarHostState() },
            onNavigateTo = {}
        )
    }
}
