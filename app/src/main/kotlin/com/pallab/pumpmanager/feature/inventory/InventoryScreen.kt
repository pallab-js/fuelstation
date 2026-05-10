package com.pallab.pumpmanager.feature.inventory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pallab.pumpmanager.core.theme.AmberWarning
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.ui.PmCard
import com.pallab.pumpmanager.core.ui.PmTopBar

@OptIn(ExperimentalMaterial3Api::class)
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
                .padding(scaffoldPadding)
        ) {
            PmTopBar(
                title = "Inventory",
                actions = {
                    TextButton(onClick = onNavigateToFuelPrices) {
                        Text("Fuel Prices")
                    }
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

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
                        items(state.tanks, key = { it.id }) { tank ->
                            TankCard(
                                tank = tank,
                                fuelTypeName = viewModel.getFuelTypeName(tank.fuelTypeId),
                                isLowStock = viewModel.isLowStock(tank),
                                onRefill = { viewModel.showRefillDialog(tank.id) }
                            )
                        }
                    }
                }
            }
        }
    }

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
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Refill Tank", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (tank != null) {
                val stockFraction = if (tank.capacityLiters > 0)
                    (tank.currentStockLiters / tank.capacityLiters).toFloat().coerceIn(0f, 1f)
                else 0f
                Box(Modifier.fillMaxWidth().height(8.dp).clip(AppShapes.extraLarge).background(MaterialTheme.colorScheme.surfaceVariant)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(stockFraction).background(MaterialTheme.colorScheme.primary, AppShapes.extraLarge))
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${state.fuelTypeNames[tank.fuelTypeId] ?: tank.fuelTypeId.replaceFirstChar { it.uppercase() }} — ${tank.currentStockLiters.toInt()} / ${tank.capacityLiters.toInt()} L",
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { viewModel.cancelRefill() }, modifier = Modifier.weight(1f)) {
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
    fuelTypeName: String,
    isLowStock: Boolean,
    onRefill: () -> Unit = {}
) {
    val stockFraction = if (tank.capacityLiters > 0)
        (tank.currentStockLiters / tank.capacityLiters).toFloat().coerceIn(0f, 1f)
    else 0f

    val animatedFraction by animateFloatAsState(
        targetValue = stockFraction,
        animationSpec = tween(800),
        label = "stockBar"
    )

    PmCard(
        accentColor = if (isLowStock) AmberWarning else MaterialTheme.colorScheme.primary,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(fuelTypeName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isLowStock) {
                    Icon(Icons.Default.Warning, contentDescription = "Low stock", tint = AmberWarning, modifier = Modifier.size(20.dp))
                }
                OutlinedButton(
                    onClick = onRefill,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Refill", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(10.dp).clip(AppShapes.extraLarge).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(Modifier.fillMaxHeight().fillMaxWidth(animatedFraction).background(
                if (isLowStock) AmberWarning else MaterialTheme.colorScheme.primary,
                AppShapes.extraLarge
            ))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "${tank.currentStockLiters.toInt()} / ${tank.capacityLiters.toInt()} L",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isLowStock) {
                Text(
                    "Low stock",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberWarning,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
