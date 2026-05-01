package com.yourcompany.pumpmanager.feature.reports

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.compose.style.rememberChartStyle
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.yourcompany.pumpmanager.core.theme.IndigoPrimary
import com.yourcompany.pumpmanager.core.theme.PumpManagerTheme
import com.yourcompany.pumpmanager.core.theme.SuccessGreen
import com.yourcompany.pumpmanager.core.ui.StatsCard

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ReportsEvent.DismissError)
        }
    }

    ReportsContent(
        state = state,
        onRefresh = { viewModel.onEvent(ReportsEvent.RefreshData) },
        onPeriodChanged = { viewModel.onEvent(ReportsEvent.PeriodChanged(it)) }
    )
}

@Composable
private fun ReportsContent(
    state: ReportsUiState,
    onRefresh: () -> Unit,
    onPeriodChanged: (Period) -> Unit = {}
) {
    val chartStyle = rememberChartStyle(
        columnColors = listOf(IndigoPrimary),
        lineColors = listOf(SuccessGreen)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "Analytics",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Period selector
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Period.entries.forEach { period ->
                FilterChip(
                    selected = state.selectedPeriod == period,
                    onClick = { onRefresh(); onPeriodChanged(period) },
                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (state.isLoading) {
            ShimmerKpiRow()
        } else {
            val periodLabel = when (state.selectedPeriod) {
                Period.TODAY -> "Today"
                Period.WEEK -> "This Week"
                Period.MONTH -> "This Month"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KpiCard(
                    label = "$periodLabel Revenue",
                    value = "₹ ${String.format("%.0f", state.totalRevenueToday)}",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    label = "$periodLabel Sales",
                    value = state.totalSalesCountToday.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProvideChartStyle(chartStyle) {
            Text(
                text = "Fuel Breakdown (Today)",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StatsCard {
                if (state.isLoading) {
                    ShimmerBox(modifier = Modifier.height(200.dp).fillMaxWidth())
                } else {
                    val breakdownEntries = state.fuelTypeBreakdown.values.map { it.toFloat() }
                    if (breakdownEntries.isNotEmpty()) {
                        val chartModel = entryModelOf(*breakdownEntries.toTypedArray())
                        Chart(
                            chart = columnChart(),
                            model = chartModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(200.dp).fillMaxWidth()
                        )
                    } else {
                        NoDataView()
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "7-Day Revenue Trend",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StatsCard {
                if (state.isLoading) {
                    ShimmerBox(modifier = Modifier.height(200.dp).fillMaxWidth())
                } else {
                    val trendEntries = state.weeklyRevenueTrend.map { it.second.toFloat() }
                    if (trendEntries.isNotEmpty()) {
                        val trendModel = entryModelOf(*trendEntries.toTypedArray())
                        Chart(
                            chart = lineChart(),
                            model = trendModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.height(200.dp).fillMaxWidth()
                        )
                    } else {
                        NoDataView()
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun ShimmerKpiRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
        ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Box(modifier = modifier.background(brush, shape = MaterialTheme.shapes.large))
}

@Composable
private fun NoDataView() {
    Box(modifier = Modifier.height(200.dp).fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("No data available", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    StatsCard(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    PumpManagerTheme {
        ReportsContent(
            state = ReportsUiState(
                totalRevenueToday = 15450.0,
                totalSalesCountToday = 42,
                fuelTypeBreakdown = mapOf("Petrol" to 8000.0, "Diesel" to 5000.0, "CNG" to 2450.0),
                weeklyRevenueTrend = listOf("23/04" to 12000.0, "24/04" to 14000.0, "25/04" to 11000.0, "26/04" to 15000.0, "27/04" to 13000.0, "28/04" to 16000.0, "29/04" to 15450.0)
            ),
            onRefresh = {}
        )
    }
}
