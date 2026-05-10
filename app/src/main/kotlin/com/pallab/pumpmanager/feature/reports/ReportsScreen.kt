package com.pallab.pumpmanager.feature.reports

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.ui.PmCard
import com.pallab.pumpmanager.core.ui.PmPrimaryButton
import com.pallab.pumpmanager.core.ui.PmTopBar
import com.pallab.pumpmanager.core.ui.ShimmerBox
import kotlinx.coroutines.launch
import java.io.File
import java.io.PrintWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(exportResult) {
        if (exportResult is ExportResult.Ready) {
            val result = exportResult as ExportResult.Ready
            scope.launch { exportToCsv(context, result.sales, result.period) }
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
    ) {
        PmTopBar(
            title = "Analytics",
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Segmented control
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                Period.entries.forEachIndexed { index, period ->
                    SegmentedButton(
                        selected = state.selectedPeriod == period,
                        onClick = { onPeriodChanged(period) },
                        shape = SegmentedButtonDefaults.itemShape(index, Period.entries.size),
                        label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (state.isLoading) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
                    ShimmerBox(modifier = Modifier.weight(1f).height(100.dp))
                }
            } else {
                // KPI row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) {
                        Text("Revenue", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "₹ ${String.format("%.0f", state.totalRevenue)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f)) {
                        Text("Sales", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            state.totalSalesCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Revenue trend chart
                if (state.revenueTrend.isNotEmpty()) {
                    Text("Revenue Trend", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PmCard {
                        RevenueBarChart(data = state.revenueTrend, modifier = Modifier.fillMaxWidth().height(160.dp))
                        Spacer(Modifier.height(8.dp))
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            state.revenueTrend.forEach { (date, _) ->
                                Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Fuel breakdown
                if (state.fuelTypeBreakdown.isNotEmpty()) {
                    Text("Fuel Breakdown", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    PmCard {
                        val maxAmount = state.fuelTypeBreakdown.maxOf { it.value }
                        state.fuelTypeBreakdown.forEach { (fuel, amount) ->
                            val fraction = (amount / maxAmount).toFloat().coerceIn(0f, 1f)
                            val animFraction by animateFloatAsState(
                                targetValue = fraction,
                                animationSpec = tween(600),
                                label = "fuelBar"
                            )
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(fuel, Modifier.width(64.dp), style = MaterialTheme.typography.bodyMedium)
                                Box(Modifier.weight(1f).height(8.dp).clip(AppShapes.extraLarge).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                    Box(Modifier.fillMaxHeight().fillMaxWidth(animFraction).background(MaterialTheme.colorScheme.primary, AppShapes.extraLarge))
                                }
                                Text(
                                    "₹${"%.0f".format(amount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.width(52.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (state.errorMessage != null) {
                Button(
                    onClick = onRefresh,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Retry")
                }
                Spacer(Modifier.height(16.dp))
            }

            PmPrimaryButton(
                text = "Export CSV Report",
                onClick = onExport,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = Icons.Default.Refresh
            )

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun RevenueBarChart(data: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val maxVal = data.maxOfOrNull { it.second } ?: 1.0
    Canvas(modifier = modifier) {
        val barW = size.width / (data.size * 1.5f + 0.5f)
        val gap = barW * 0.5f
        data.forEachIndexed { i, (_, value) ->
            val x = gap + i * (barW + gap)
            val h = (value / maxVal * size.height * 0.85f).toFloat().coerceAtLeast(4f)
            drawRoundRect(
                color = primary,
                topLeft = Offset(x, size.height - h),
                size = Size(barW, h),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
    }
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
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    } catch (e: Exception) {
        Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
