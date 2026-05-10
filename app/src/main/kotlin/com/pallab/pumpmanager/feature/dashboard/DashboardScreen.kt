package com.pallab.pumpmanager.feature.dashboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.theme.Green500
import com.pallab.pumpmanager.core.ui.PmCard
import com.pallab.pumpmanager.feature.inventory.InventoryScreen
import com.pallab.pumpmanager.feature.reports.ReportsScreen
import com.pallab.pumpmanager.feature.sales.SalesScreen
import com.pallab.pumpmanager.feature.shift.ShiftScreen
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private data class NavItem(
    val label: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
)

private val navItems = listOf(
    NavItem("Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    NavItem("Sales", Icons.Filled.LocalGasStation, Icons.Outlined.LocalGasStation),
    NavItem("Shift", Icons.Filled.Schedule, Icons.Outlined.Schedule),
    NavItem("Inventory", Icons.Filled.Inventory2, Icons.Outlined.Inventory2),
    NavItem("Reports", Icons.Filled.BarChart, Icons.Outlined.BarChart)
)

@Composable
fun DashboardScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToFuelPrices: () -> Unit = {},
    onNavigateToSalesHistory: () -> Unit = {}
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                modifier = Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = AppShapes.extraSmall
                ).padding(top = 0.dp)
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            Icon(
                                if (selectedItem == index) item.iconFilled else item.iconOutlined,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            AnimatedContent(
                targetState = selectedItem,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * direction } + fadeIn(tween(250)))
                        .togetherWith(slideOutHorizontally { -it * direction } + fadeOut(tween(200)))
                },
                label = "tabContent"
            ) { tab ->
                when (tab) {
                    0 -> DashboardHome(
                        onNavigateToTab = { selectedItem = it },
                        onNavigateToSalesHistory = onNavigateToSalesHistory
                    )
                    1 -> SalesScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                    2 -> ShiftScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                    3 -> InventoryScreen(viewModel = hiltViewModel(), onNavigateToFuelPrices = onNavigateToFuelPrices)
                    4 -> ReportsScreen(viewModel = hiltViewModel(), snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}

@Composable
fun DashboardHome(
    onNavigateToTab: (Int) -> Unit = {},
    onNavigateToSalesHistory: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    val date = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(greeting, style = MaterialTheme.typography.headlineLarge)
        Text(
            date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Shift card
            val shift = state.activeShift
            if (shift != null) {
                PmCard(accentColor = Green500, modifier = Modifier.fillMaxWidth()) {
                    var elapsed by remember { mutableStateOf("") }
                    LaunchedEffect(shift.startTime) {
                        while (true) {
                            val duration = Duration.between(
                                Instant.ofEpochMilli(shift.startTime)
                                    .atZone(ZoneId.systemDefault()).toLocalTime(),
                                LocalTime.now()
                            )
                            val absDuration = if (duration.isNegative) duration.negated() else duration
                            elapsed = "%dh %02dm".format(absDuration.toHours(), absDuration.toMinutesPart())
                            delay(60_000L)
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Active Shift", style = MaterialTheme.typography.labelLarge, color = Green500)
                            Text(
                                "Started ${Instant.ofEpochMilli(shift.startTime).atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a"))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("Duration: $elapsed", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val pulseAnim by rememberInfiniteTransition().animateFloat(
                                initialValue = 0.7f, targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                                label = "pulse"
                            )
                            Box(Modifier.size(8.dp).scale(pulseAnim).background(Green500, MaterialTheme.shapes.extraSmall))
                            Surface(color = Green500, shape = AppShapes.extraSmall) {
                                Text(
                                    "LIVE",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                    Text("No Active Shift", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Start a shift to begin recording sales", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    PmPrimaryButton("Start Shift →", onClick = { onNavigateToTab(2) })
                }
            }

            Spacer(Modifier.height(20.dp))

            // Today at a Glance
            Text("Today at a Glance", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) {
                    Text("Today's Revenue", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹ ${String.format("%.0f", state.todayRevenue)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) {
                    Text("Sales Today", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        state.todaySalesCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Quick Actions
            Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            val quickActions = listOf(
                QuickActionData("Sale", Icons.Default.LocalGasStation, 1),
                QuickActionData("Shift", Icons.Default.Schedule, 2),
                QuickActionData("Inventory", Icons.Default.Inventory2, 3),
                QuickActionData("Reports", Icons.Default.BarChart, 4),
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quickActions) { action ->
                    QuickActionCard(
                        icon = action.icon,
                        label = action.label,
                        onClick = { onNavigateToTab(action.tabIndex) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onNavigateToSalesHistory) {
                Text("View Sales History →")
            }
        }
    }
}

private data class QuickActionData(val label: String, val icon: ImageVector, val tabIndex: Int)

@Composable
private fun QuickActionCard(icon: ImageVector, label: String, onClick: () -> Unit) {
    PmCard(onClick = onClick, modifier = Modifier.aspectRatio(1f)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, AppShapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun PmPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = AppShapes.small
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
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
