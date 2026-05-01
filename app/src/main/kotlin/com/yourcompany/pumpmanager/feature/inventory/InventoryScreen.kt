package com.yourcompany.pumpmanager.feature.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourcompany.pumpmanager.core.ui.StatsCard

private val WarningAmber = androidx.compose.ui.graphics.Color(0xFFFFC107)

@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            text = "Inventory",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
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
                    TankCard(tank = tank, isLowStock = viewModel.isLowStock(tank))
                }
            }
        }
    }
}

@Composable
private fun TankCard(tank: TankEntity, isLowStock: Boolean) {
    val stockFraction = (tank.currentStockLiters / tank.capacityLiters).toFloat().coerceIn(0f, 1f)
    val fuelLabel = tank.fuelTypeId.replaceFirstChar { it.uppercase() }

    StatsCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(fuelLabel, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (isLowStock) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Low stock warning",
                    tint = WarningAmber,
                    modifier = Modifier.size(20.dp)
                )
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
