package com.pallab.pumpmanager.feature.saleshistory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.pallab.pumpmanager.core.theme.AmberWarning
import com.pallab.pumpmanager.core.theme.AppShapes
import com.pallab.pumpmanager.core.theme.Green500
import com.pallab.pumpmanager.core.ui.PmTopBar
import com.pallab.pumpmanager.feature.sales.SaleEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    viewModel: SalesHistoryViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val salesItems = viewModel.salesPaged.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    val voidState by viewModel.voidState.collectAsState()
    var saleToVoid by remember { mutableStateOf<SaleEntity?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(voidState) {
        when (val state = voidState) {
            is VoidState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.dismissVoidResult()
            }
            is VoidState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.dismissVoidResult()
            }
            else -> {}
        }
    }

    saleToVoid?.let { sale ->
        AlertDialog(
            onDismissRequest = { saleToVoid = null },
            title = { Text("Void Sale") },
            text = { Text("Are you sure you want to void this ${sale.fuelType.replaceFirstChar { it.uppercase() }} sale of ${sale.volumeLiters.toInt()} L (₹${"%.0f".format(sale.totalAmount)})? Stock will be restored.") },
            confirmButton = {
                TextButton(onClick = { viewModel.voidSale(sale); saleToVoid = null }, enabled = voidState !is VoidState.Loading) {
                    Text("Void", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToVoid = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            PmTopBar(
                title = "Sales History",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                salesItems.refresh()
                isRefreshing = false
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (salesItems.itemCount == 0 && salesItems.loadState.refresh is androidx.paging.LoadState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val grouped = (0 until salesItems.itemCount)
                    .mapNotNull { salesItems[it] }
                    .groupBy { formatGroupDate(it.timestamp) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    grouped.forEach { (date, sales) ->
                        item(key = "header_$date") {
                            DateSectionHeader(date)
                        }
                        items(sales, key = { it.id }) { sale ->
                            SaleHistoryCard(
                                sale = sale,
                                onVoidClick = { saleToVoid = sale },
                                isVoiding = voidState is VoidState.Loading
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSectionHeader(date: String) {
    Text(
        date,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun SaleHistoryCard(
    sale: SaleEntity,
    onVoidClick: () -> Unit,
    isVoiding: Boolean
) {
    val isVoided = sale.isVoided
    val fuelDotColor = when {
        sale.fuelType.contains("petrol", ignoreCase = true) -> Green500
        sale.fuelType.contains("diesel", ignoreCase = true) -> Color(0xFF2563EB)
        sale.fuelType.contains("cng", ignoreCase = true) -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(fuelDotColor))
                    Text(
                        sale.fuelType.replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
                    )
                    if (isVoided) {
                        Surface(color = AmberWarning.copy(alpha = 0.2f), shape = AppShapes.small) {
                            Text(
                                "VOIDED",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberWarning,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Text(
                    "${sale.volumeLiters.toInt()} L × ₹ ${"%.2f".format(sale.pricePerLiter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${Instant.ofEpochMilli(sale.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("hh:mm a"))}  ·  ${sale.paymentMode}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "₹ ${"%.0f".format(sale.totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textDecoration = if (isVoided) TextDecoration.LineThrough else TextDecoration.None
            )
            IconButton(onClick = onVoidClick, enabled = !isVoiding) {
                Icon(Icons.Default.Delete, contentDescription = "Void sale", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun formatGroupDate(timestamp: Long): String {
    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = java.time.LocalDate.now()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }
}
