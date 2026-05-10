package com.pallab.pumpmanager.feature.shift

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.theme.Green500
import com.pallab.pumpmanager.core.ui.PmCard
import com.pallab.pumpmanager.core.ui.PmDestructiveButton
import com.pallab.pumpmanager.core.ui.PmPrimaryButton
import com.pallab.pumpmanager.core.ui.PmTopBar
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftScreen(
    viewModel: ShiftViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val summaryData = state.summaryData
    var showEndConfirmDialog by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ShiftEvent.DismissError)
        }
    }

    LaunchedEffect(state.isShiftStarted) {
        if (state.isShiftStarted) {
            snackbarHostState.showSnackbar("Shift started successfully")
            viewModel.onEvent(ShiftEvent.DismissStarted)
        }
    }

    if (state.isShiftEnded && summaryData != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(ShiftEvent.DismissSummary) },
            sheetState = sheetState
        ) {
            ShiftSummaryContent(summary = summaryData)
        }
    }

    if (showEndConfirmDialog && state.activeShift != null) {
        AlertDialog(
            onDismissRequest = { showEndConfirmDialog = false },
            title = { Text("End Shift") },
            text = { Text("Are you sure you want to close the current shift? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showEndConfirmDialog = false
                    viewModel.onEvent(ShiftEvent.EndShift)
                }) {
                    Text("Close Shift", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        ShiftTopBar(activeShift = state.activeShift)

        Column(modifier = Modifier.padding(24.dp)) {
            val activeShift = state.activeShift
            if (activeShift == null) {
                StartShiftForm(
                    openingMeter = state.openingMeter,
                    onMeterChange = { viewModel.onEvent(ShiftEvent.OpeningMeterChanged(it)) },
                    onStartClick = { viewModel.onEvent(ShiftEvent.StartShift) },
                    isLoading = state.isLoading
                )
            } else {
                ActiveShiftDashboard(
                    shift = activeShift,
                    closingMeter = state.closingMeter,
                    onMeterChange = { viewModel.onEvent(ShiftEvent.ClosingMeterChanged(it)) },
                    onEndClick = { showEndConfirmDialog = true },
                    isLoading = state.isLoading
                )
            }
        }
    }
}

@Composable
private fun ShiftTopBar(activeShift: com.pallab.pumpmanager.feature.shift.ShiftEntity?) {
    PmTopBar(
        title = "Shift Management",
        actions = {
            if (activeShift != null) {
                val pulseAnim by rememberInfiniteTransition().animateFloat(
                    initialValue = 0.7f, targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
                    label = "pulse"
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Box(Modifier.size(8.dp).scale(pulseAnim).background(Green500, AppShapes.extraSmall))
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
    )
}

@Composable
private fun StartShiftForm(
    openingMeter: String,
    onMeterChange: (String) -> Unit,
    onStartClick: () -> Unit,
    isLoading: Boolean
) {
    PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
        Text("Ready to start?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = openingMeter,
            onValueChange = onMeterChange,
            label = { Text("Opening Meter Reading") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = AppShapes.small
        )
        Spacer(Modifier.height(24.dp))
        PmPrimaryButton("Initialize Shift", onClick = onStartClick, modifier = Modifier.fillMaxWidth(), isLoading = isLoading)
    }
}

@Composable
private fun ActiveShiftDashboard(
    shift: com.pallab.pumpmanager.feature.shift.ShiftEntity,
    closingMeter: String,
    onMeterChange: (String) -> Unit,
    onEndClick: () -> Unit,
    isLoading: Boolean
) {
    var elapsed by remember { mutableStateOf("") }
    LaunchedEffect(shift.startTime) {
        while (true) {
            val duration = Duration.between(
                Instant.ofEpochMilli(shift.startTime).atZone(ZoneId.systemDefault()).toLocalTime(),
                LocalTime.now()
            )
            val absDuration = if (duration.isNegative) duration.negated() else duration
            elapsed = "%dh %02dm".format(absDuration.toHours(), absDuration.toMinutesPart())
            delay(60_000L)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        PmCard(accentColor = Green500, modifier = Modifier.fillMaxWidth()) {
            Text("Active Shift", style = MaterialTheme.typography.labelLarge, color = Green500)
            Text(
                "Started ${formatTime(shift.startTime)}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Duration: $elapsed", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                InfoBlock(label = "Attendant", value = shift.attendantId, modifier = Modifier.weight(1f))
                InfoBlock(label = "Opening Meter", value = shift.openingMeterReading.toInt().toString(), modifier = Modifier.weight(1f))
            }
        }

        PmCard(accentColor = com.pallab.pumpmanager.core.theme.RedError, modifier = Modifier.fillMaxWidth()) {
            Text("Close This Shift", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = closingMeter,
                onValueChange = onMeterChange,
                label = { Text("Closing Meter Reading") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = AppShapes.small
            )
            Spacer(Modifier.height(24.dp))
            PmDestructiveButton("Close Shift", onClick = onEndClick, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ShiftSummaryContent(summary: ShiftSummaryData) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Shift Summary", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Sales", style = MaterialTheme.typography.bodyLarge)
            Text(summary.totalSales.toString(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Revenue", style = MaterialTheme.typography.bodyLarge)
            Text("₹ ${"%.2f".format(summary.totalRevenue)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Meter Difference", style = MaterialTheme.typography.bodyLarge)
            Text("${summary.meterDifference.toInt()} L", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
        if (summary.fuelBreakdown.isNotEmpty()) {
            HorizontalDivider()
            Text("Fuel Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            summary.fuelBreakdown.forEach { (fuel, volume) ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(fuel, style = MaterialTheme.typography.bodyMedium)
                    Text("${volume.toInt()} L", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        PmPrimaryButton("Done", onClick = { }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun InfoBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("hh:mm a"))
}
