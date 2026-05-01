package com.yourcompany.pumpmanager.feature.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.yourcompany.pumpmanager.core.navigation.Routes
import com.yourcompany.pumpmanager.core.theme.PumpManagerTheme
import com.yourcompany.pumpmanager.core.ui.StatsCard
import com.yourcompany.pumpmanager.feature.inventory.InventoryScreen
import com.yourcompany.pumpmanager.feature.sales.SalesScreen
import com.yourcompany.pumpmanager.feature.shift.ShiftScreen
import com.yourcompany.pumpmanager.feature.reports.ReportsScreen
import java.text.SimpleDateFormat
import java.util.*

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
                        icon = { Icon(item.icon, contentDescription = item.label) },                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
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
                0 -> DashboardHome(onNavigateToTab = { selectedItem = it })
                1 -> SalesScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                2 -> ShiftScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                3 -> InventoryScreen(viewModel = hiltViewModel())
                4 -> ReportsScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                else -> PlaceholderScreen(items[selectedItem].label)
            }
        }
    }
}

@Composable
fun DashboardHome(
    onNavigateToTab: (Int) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(modifier = Modifier.weight(1f)) {
                    Text("Today's Revenue", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹ ${String.format("%.0f", state.todayRevenue)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatsCard(modifier = Modifier.weight(1f)) {
                    Text("Sales Today", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        state.todaySalesCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            StatsCard {
                val shift = state.activeShift
                if (shift != null) {
                    Text("Active Shift", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Started ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(shift.startTime))}",
                        style = MaterialTheme.typography.titleMedium
                    )
                } else {
                    Text("No Active Shift", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Start a shift to begin recording sales",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Quick Actions", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { onNavigateToTab(1) }, modifier = Modifier.weight(1f)) {
                    Text("New Sale")
                }
                OutlinedButton(onClick = { onNavigateToTab(2) }, modifier = Modifier.weight(1f)) {
                    Text("Shift")
                }
                OutlinedButton(onClick = { onNavigateToTab(4) }, modifier = Modifier.weight(1f)) {
                    Text("Reports")
                }
            }
        }
    }
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
