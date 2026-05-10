package com.pallab.pumpmanager.feature.sales

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.ui.PmCard
import com.pallab.pumpmanager.core.ui.PmPrimaryButton
import com.pallab.pumpmanager.core.ui.PmSelectableChip
import com.pallab.pumpmanager.core.ui.PmTopBar

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

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            snackbarHostState.showSnackbar("Sale saved successfully")
            viewModel.onEvent(SalesEvent.DismissSuccess)
        }
    }

    SalesContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
internal fun SalesContent(
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
            "New Sale",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        // Fuel Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.fuelTypes, key = { it.id }) { fuel ->
                PmSelectableChip(
                    label = fuel.name,
                    selected = state.selectedFuel?.id == fuel.id,
                    onClick = { onEvent(SalesEvent.FuelSelected(fuel)) }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Total display with animation
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedContent(
                targetState = state.calculatedTotal,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn())
                        .togetherWith(slideOutVertically { it } + fadeOut())
                },
                label = "totalAmount"
            ) { total ->
                Text(
                    "₹ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                "${state.volume.ifEmpty { "0" }} L @ ₹${"%.2f".format(state.pricePerLiter)}/L",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.weight(1f))

        // Payment mode
        Text("Payment Mode", style = MaterialTheme.typography.labelMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(PaymentMode.entries) { mode ->
                PmSelectableChip(
                    label = mode.name,
                    selected = state.paymentMode == mode,
                    onClick = { onEvent(SalesEvent.PaymentModeChanged(mode)) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Numpad
        NumpadGrid(
            onDigitClick = { onEvent(SalesEvent.VolumeDigitEntered(it)) },
            onDeleteClick = { onEvent(SalesEvent.VolumeDeleted) }
        )

        Spacer(Modifier.height(16.dp))

        // Save button
        PmPrimaryButton(
            text = if (state.isLoading) "Saving..." else "Save Sale",
            onClick = { onEvent(SalesEvent.SaveSale) },
            modifier = Modifier.fillMaxWidth(),
            isLoading = state.isLoading
        )
    }
}

@Composable
private fun NumpadGrid(
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf(".", "0", "DEL")).forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { key ->
                    NumpadButton(key = key, onClick = {
                        when (key) {
                            "DEL" -> onDeleteClick()
                            else -> onDigitClick(key)
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun NumpadButton(key: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(60.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (key == "DEL") {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardBackspace,
                    contentDescription = "Delete",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(key, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Medium)
            }
        }
    }
}


