package com.pallab.pumpmanager.feature.saleshistory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
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
            text = {
                Text("Are you sure you want to void this ${sale.fuelType.replaceFirstChar { it.uppercase() }} sale of ${sale.volumeLiters.toInt()} L (₹${"%.0f".format(sale.totalAmount)})? Stock will be restored.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.voidSale(sale)
                        saleToVoid = null
                    },
                    enabled = voidState !is VoidState.Loading
                ) {
                    Text("Void", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { saleToVoid = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(count = salesItems.itemCount) { index ->
                        val sale = salesItems[index]
                        if (sale != null) {
                            SaleHistoryCard(
                                sale = sale,
                                onVoidClick = { saleToVoid = sale },
                                isVoiding = voidState is VoidState.Loading
                            )
                        }
                    }
                    salesItems.apply {
                        when {
                            loadState.append is androidx.paging.LoadState.Loading -> {
                                item { Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                            }
                            loadState.append is androidx.paging.LoadState.Error -> {
                                val e = loadState.append as androidx.paging.LoadState.Error
                                item {
                                    TextButton(onClick = { retry() }) {
                                        Text("Error: ${e.error.message}. Tap to retry", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaleHistoryCard(
    sale: SaleEntity,
    onVoidClick: () -> Unit,
    isVoiding: Boolean
) {
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
                Text(sale.fuelType.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                Text(
                    "${sale.volumeLiters.toInt()} L × ₹ ${"%.2f".format(sale.pricePerLiter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    Instant.ofEpochMilli(sale.timestamp).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM hh:mm a")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    sale.paymentMode,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "₹ ${"%.0f".format(sale.totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onVoidClick,
                enabled = !isVoiding
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Void sale",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
