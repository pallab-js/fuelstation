package com.pallab.pumpmanager.feature.reports

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.pallab.pumpmanager.core.theme.PumpManagerTheme
import com.pallab.pumpmanager.core.ui.StatsCard

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(exportResult) {
        if (exportResult is ExportResult.Ready) {
            val result = exportResult as ExportResult.Ready
            scope.launch {
                exportToCsv(context, result.sales, result.period)
            }
            viewModel.clearExportResult()
        }
    }

    ReportsContent(
        state = state,
        onRefresh = { viewModel.onEvent(ReportsEvent.RefreshData) },
        onPeriodChanged = { viewModel.onEvent(ReportsEvent.PeriodChanged(it)) },
        onExport = { viewModel.onEvent(ReportsEvent.ExportCsv) }
    )
}

private suspend fun exportToCsv(context: Context, sales: List<com.pallab.pumpmanager.feature.sales.SaleEntity>, period: Period) {
    if (sales.isEmpty()) {
        Toast.makeText(context, "No data to export", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val now = LocalDateTime.now()
        val reportsDir = File(context.getExternalFilesDir(null), "reports")
        reportsDir.mkdirs()
        val file = File(reportsDir, "report_${now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"))}.csv")
        PrintWriter(file).use { writer ->
            writer.println("Date,Fuel Type,Volume (L),Price/L,Total,Payment Mode")
            sales.forEach { sale ->
                val date = java.time.Instant.ofEpochMilli(sale.timestamp)
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                writer.println("$date,${sale.fuelType},${sale.volumeLiters},${sale.pricePerLiter},${sale.totalAmount},${sale.paymentMode}")
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share Report")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(chooser)
        } else {
            Toast.makeText(context, "No app available to share the report", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@Composable
private fun ReportsContent(
    state: ReportsUiState,
    onRefresh: () -> Unit,
    onPeriodChanged: (Period) -> Unit = {},
    onExport: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytics",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Period.entries.forEach { period ->
                FilterChip(
                    selected = state.selectedPeriod == period,
                    onClick = { onPeriodChanged(period) },
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
                    value = "₹ ${String.format("%.0f", state.totalRevenue)}",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    label = "$periodLabel Sales",
                    value = state.totalSalesCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!state.isLoading) {
            val breakdownLabel = when (state.selectedPeriod) {
                Period.TODAY -> "Fuel Breakdown (Today)"
                Period.WEEK -> "Fuel Breakdown (This Week)"
                Period.MONTH -> "Fuel Breakdown (This Month)"
            }
            Text(
                text = breakdownLabel,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StatsCard {
                if (state.fuelTypeBreakdown.isEmpty()) {
                    NoDataView()
                } else {
                    state.fuelTypeBreakdown.forEach { (fuel, amount) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(fuel, style = MaterialTheme.typography.bodyMedium)
                            Text("₹ ${"%.0f".format(amount)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val trendLabel = when (state.selectedPeriod) {
                Period.TODAY -> "Today's Revenue"
                Period.WEEK -> "7-Day Revenue Trend"
                Period.MONTH -> "30-Day Revenue Trend"
            }
            Text(
                text = trendLabel,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            StatsCard {
                if (state.revenueTrend.isEmpty()) {
                    NoDataView()
                } else {
                    state.revenueTrend.forEach { (date, revenue) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(date, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("₹ ${"%.0f".format(revenue)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Retry")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Report")
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
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
                totalRevenue = 15450.0,
                totalSalesCount = 42,
                fuelTypeBreakdown = mapOf("Petrol" to 8000.0, "Diesel" to 5000.0, "CNG" to 2450.0),
                revenueTrend = listOf("23/04" to 12000.0, "24/04" to 14000.0, "25/04" to 11000.0, "26/04" to 15000.0, "27/04" to 13000.0, "28/04" to 16000.0, "29/04" to 15450.0)
            ),
            onRefresh = {}
        )
    }
}
