package com.pallab.pumpmanager.feature.shift

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pallab.pumpmanager.core.theme.PumpManagerTheme
import com.pallab.pumpmanager.core.ui.StatsCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ShiftScreen(
    viewModel: ShiftViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()
    val summaryData = state.summaryData

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ShiftEvent.DismissError)
        }
    }

    if (state.isShiftEnded && summaryData != null) {
        ShiftSummaryDialog(
            summary = summaryData,
            onDismiss = { viewModel.onEvent(ShiftEvent.DismissError) }
        )
    }

    ShiftContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ShiftContent(
    state: ShiftUiState,
    onEvent: (ShiftEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            text = "Shift Management",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (state.activeShift == null) {
            StartShiftForm(
                openingMeter = state.openingMeter,
                onMeterChange = { onEvent(ShiftEvent.OpeningMeterChanged(it)) },
                onStartClick = { onEvent(ShiftEvent.StartShift) },
                isLoading = state.isLoading
            )
        } else {
            ActiveShiftDashboard(
                shift = state.activeShift,
                closingMeter = state.closingMeter,
                onMeterChange = { onEvent(ShiftEvent.ClosingMeterChanged(it)) },
                onEndClick = { onEvent(ShiftEvent.EndShift) },
                isLoading = state.isLoading
            )
        }

        state.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun StartShiftForm(
    openingMeter: String,
    onMeterChange: (String) -> Unit,
    onStartClick: () -> Unit,
    isLoading: Boolean
) {
    StatsCard {
        Text(
            text = "Start New Shift",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = openingMeter,
            onValueChange = onMeterChange,
            label = { Text("Opening Meter Reading") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = MaterialTheme.shapes.small
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onStartClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = MaterialTheme.shapes.small,
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start shift")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Initialize Shift")
            }
        }
    }
}

@Composable
private fun ActiveShiftDashboard(
    shift: ShiftEntity,
    closingMeter: String,
    onMeterChange: (String) -> Unit,
    onEndClick: () -> Unit,
    isLoading: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        StatsCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Active Shift",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Started at ${formatTime(shift.startTime)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "LIVE",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                InfoBlock(label = "Attendant", value = "Pallab", modifier = Modifier.weight(1f))
                InfoBlock(label = "Opening Meter", value = shift.openingMeterReading.toString(), modifier = Modifier.weight(1f))
            }
        }

        StatsCard {
            Text(
                text = "End Shift",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = closingMeter,
                onValueChange = onMeterChange,
                label = { Text("Closing Meter Reading") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.small
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onEndClick,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Icon(Icons.Default.Clear, contentDescription = "Stop shift")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Close Shift")
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern("hh:mm a"))
}

@Composable
private fun ShiftSummaryDialog(summary: ShiftSummaryData, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        title = { Text("Shift Summary", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Total Sales: ${summary.totalSales}", style = MaterialTheme.typography.bodyLarge)
                Text("Total Revenue: ₹ ${"%.2f".format(summary.totalRevenue)}", style = MaterialTheme.typography.bodyLarge)
                Text("Meter Difference: ${summary.meterDifference.toInt()} L", style = MaterialTheme.typography.bodyLarge)
                if (summary.fuelBreakdown.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Volume by Fuel:", fontWeight = FontWeight.SemiBold)
                    summary.fuelBreakdown.forEach { (fuel, volume) ->
                        Text("  $fuel: ${volume.toInt()} L", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ShiftScreenPreview() {
    PumpManagerTheme {
        ShiftContent(
            state = ShiftUiState(
                activeShift = ShiftEntity(
                    id = "1",
                    attendantId = "1",
                    startTime = System.currentTimeMillis(),
                    endTime = null,
                    openingMeterReading = 12500.0,
                    closingMeterReading = null,
                    status = "active"
                )
            ),
            onEvent = {}
        )
    }
}
