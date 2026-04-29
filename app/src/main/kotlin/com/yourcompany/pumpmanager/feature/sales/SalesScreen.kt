package com.yourcompany.pumpmanager.feature.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourcompany.pumpmanager.core.theme.PumpManagerTheme

@Composable
fun SalesScreen(
    viewModel: SalesViewModel,
    snackbarHostState: SnackbarHostState
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SalesEvent.DismissError)
        }
    }

    SalesContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun SalesContent(
    state: SalesUiState,
    onEvent: (SalesEvent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
    ) {
        Text(
            text = "New Sale",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Fuel Type Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FuelType.values().forEach { fuel ->
                FuelChip(
                    label = fuel.label,
                    selected = state.selectedFuel == fuel,
                    onClick = { onEvent(SalesEvent.FuelSelected(fuel)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Large Total Display
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "₹ ${String.format("%.2f", state.calculatedTotal)}",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${state.volume.ifEmpty { "0" }} Liters @ ₹${state.pricePerLiter}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Payment Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaymentMode.values().forEach { mode ->
                FilterChip(
                    selected = state.paymentMode == mode,
                    onClick = { onEvent(SalesEvent.PaymentModeChanged(mode)) },
                    label = { Text(mode.name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Numpad and Save Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(modifier = Modifier.weight(1f)) {
                VolumeNumpad(
                    onDigitClick = { onEvent(SalesEvent.VolumeDigitEntered(it)) },
                    onDeleteClick = { onEvent(SalesEvent.VolumeDeleted) }
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Button(
                onClick = { onEvent(SalesEvent.SaveSale) },
                modifier = Modifier
                    .height(64.dp)
                    .weight(0.6f),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Sale", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
private fun FuelChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable { onClick() },
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (!selected) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun VolumeNumpad(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "DEL")
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.width(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(digits) { item ->
            when (item) {
                "DEL" -> NumpadButton(icon = Icons.Default.Backspace, onClick = onDeleteClick)
                else -> NumpadButton(text = item, onClick = { onDigitClick(item) })
            }
        }
    }
}

@Composable
private fun NumpadButton(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (text != null) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        } else if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SalesScreenPreview() {
    PumpManagerTheme {
        SalesContent(
            state = SalesUiState(volume = "15.5", calculatedTotal = 1588.75),
            onEvent = {}
        )
    }
}
