package com.pallab.pumpmanager.feature.fuelprices

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.ui.PmCard
import com.pallab.pumpmanager.core.ui.PmTopBar
import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPricesScreen(
    viewModel: FuelPricesViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.successMessage) {
        state.successMessage?.let { snackbarHostState.showSnackbar(it); viewModel.dismissMessage() }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it); viewModel.dismissMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PmTopBar(
                title = "Fuel Prices",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.fuelTypes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                if (state.isLoading) CircularProgressIndicator()
                else Text("No fuel types configured", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.fuelTypes, key = { it.id }) { fuelType ->
                    FuelPriceCard(
                        fuelType = fuelType,
                        isEditing = state.editingFuelId == fuelType.id,
                        editingPrice = state.editingPrice,
                        onEdit = { viewModel.startEdit(fuelType) },
                        onPriceChanged = { viewModel.onPriceChanged(it) },
                        onSave = { viewModel.savePrice() },
                        onCancel = { viewModel.cancelEdit() }
                    )
                }
            }

            if (state.isSaving) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun FuelPriceCard(
    fuelType: FuelTypeEntity,
    isEditing: Boolean,
    editingPrice: String,
    onEdit: () -> Unit,
    onPriceChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AnimatedContent(
        targetState = isEditing,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "editToggle"
    ) { editing ->
        if (editing) {
            PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(fuelType.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editingPrice,
                        onValueChange = onPriceChanged,
                        label = { Text("Price per Liter (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(onClick = onSave, modifier = Modifier.weight(1f)) { Text("Save") }
                    }
                }
            }
        } else {
            PmCard(accentColor = MaterialTheme.colorScheme.primary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(fuelType.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            "₹ ${"%.2f".format(fuelType.pricePerLiter)} / L",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit price", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
