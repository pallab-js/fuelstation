package com.pallab.pumpmanager.feature.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pallab.pumpmanager.core.ui.StatsCard

private val WarningAmber = androidx.compose.ui.graphics.Color(0xFFFFC107)

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateToFuelPrices: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(scaffoldPadding)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToFuelPrices) {
                    Text("Fuel Prices")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.tanks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tanks found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(state.tanks) { tank ->
                        TankCard(
                            tank = tank,
                            isLowStock = viewModel.isLowStock(tank),
                            onRefill = { viewModel.showRefillDialog(tank.id) }
                        )
                    }
                }
            }
        }
    }

    // Refill Bottom Sheet
    if (state.refillingTankId != null) {
        RefillBottomSheet(viewModel = viewModel, state = state)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefillBottomSheet(viewModel: InventoryViewModel, state: InventoryUiState) {
    val tank = state.tanks.find { it.id == state.refillingTankId }
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.cancelRefill() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Refill Tank",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (tank != null) {
                Text(
                    text = "${tank.fuelTypeId.replaceFirstChar { it.uppercase() }} — ${tank.currentStockLiters.toInt()} / ${tank.capacityLiters.toInt()} L",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedTextField(
                value = state.refillVolume,
                onValueChange = { viewModel.onRefillVolumeChanged(it) },
                label = { Text("Volume to add (L)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.cancelRefill() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.confirmRefill() },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isRefilling
                ) {
                    if (state.isRefilling) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Confirm Refill")
                    }
                }
            }
        }
    }
}

@Composable
private fun TankCard(
    tank: TankEntity,
    isLowStock: Boolean,
    onRefill: () -> Unit = {}
) {
    val stockFraction = (tank.currentStockLiters / tank.capacityLiters).toFloat().coerceIn(0f, 1f)
    val fuelLabel = tank.fuelTypeId.replaceFirstChar { it.uppercase() }

    StatsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(fuelLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (isLowStock) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Low stock warning",
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onRefill, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Refill", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { stockFraction },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (isLowStock) WarningAmber else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${tank.currentStockLiters.toInt()} / ${tank.capacityLiters.toInt()} L",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
