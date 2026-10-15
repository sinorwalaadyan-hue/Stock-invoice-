package com.example.ui.screens.transfer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.LocationEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.StockTransferEntity
import com.example.ui.ProductWithStock
import com.example.ui.StockVisionViewModel
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransferScreen(
    viewModel: StockVisionViewModel
) {
    val transfers by viewModel.allTransfers.collectAsStateWithLifecycle()
    val locations by viewModel.allLocations.collectAsStateWithLifecycle()
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()

    var showNewTransferDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Inter-Location Transfers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Move stock between Godowns, Warehouses & Retail Shops", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showNewTransferDialog = true },
                        modifier = Modifier.testTag("new_transfer_button")
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Transfer")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "Internal transfers maintain accurate inventory counts across Bhiwandi, Panvel & MG Road without altering sales or accounting revenue.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Transfer Log & Dispatch Slips (${transfers.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (transfers.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No stock transfers recorded yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(transfers) { transfer ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(transfer.transferNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BrandEmerald.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = transfer.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "${transfer.productName} • ${transfer.quantity} Units Moved",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = transfer.sourceLocationName,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.Default.ArrowForward, contentDescription = "To", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = transfer.destLocationName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Staff: ${transfer.responsibleStaff}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(Formatters.formatDateTime(transfer.timestamp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // New Transfer Dialog
    if (showNewTransferDialog) {
        NewTransferDialog(
            productsWithStock = productsWithStock,
            locations = locations,
            onDismiss = { showNewTransferDialog = false },
            onConfirm = { prodId, prodName, srcId, srcName, dstId, dstName, qty, staff, notes ->
                viewModel.executeStockTransfer(prodId, prodName, srcId, srcName, dstId, dstName, qty, staff, notes) {
                    showNewTransferDialog = false
                }
            }
        )
    }
}

@Composable
fun NewTransferDialog(
    productsWithStock: List<ProductWithStock>,
    locations: List<LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Long, String, Long, String, Int, String, String) -> Unit
) {
    var selectedProductId by remember { mutableStateOf(productsWithStock.firstOrNull()?.product?.id ?: 1L) }
    var sourceLocationId by remember { mutableStateOf(locations.firstOrNull()?.id ?: 1L) }
    var destLocationId by remember { mutableStateOf(locations.getOrNull(2)?.id ?: 3L) }
    var quantityStr by remember { mutableStateOf("5") }
    var staffName by remember { mutableStateOf("Warehouse Logistics Staff") }
    var notes by remember { mutableStateOf("Internal floor replenishment") }

    val selectedItem = productsWithStock.find { it.product.id == selectedProductId }
    val srcLocation = locations.find { it.id == sourceLocationId }
    val dstLocation = locations.find { it.id == destLocationId }

    val availableInSource = selectedItem?.stockByLocation?.get(sourceLocationId) ?: 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Execute Stock Transfer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Transfers stock without creating sales or profit transactions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Product Selector
                item {
                    Text("Product to Transfer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var prodExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { prodExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedItem?.product?.name ?: "Select Product")
                        }
                        DropdownMenu(expanded = prodExpanded, onDismissRequest = { prodExpanded = false }) {
                            productsWithStock.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text("${item.product.name} (${item.totalAvailable} total)") },
                                    onClick = {
                                        selectedProductId = item.product.id
                                        prodExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Source Location
                item {
                    Text("Source Warehouse (Dispatched From)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var srcExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { srcExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("${srcLocation?.name ?: "Select Source"} (Available: $availableInSource)")
                        }
                        DropdownMenu(expanded = srcExpanded, onDismissRequest = { srcExpanded = false }) {
                            locations.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text(l.name) },
                                    onClick = {
                                        sourceLocationId = l.id
                                        srcExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Destination Location
                item {
                    Text("Destination Location (Received At)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var dstExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { dstExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(dstLocation?.name ?: "Select Destination")
                        }
                        DropdownMenu(expanded = dstExpanded, onDismissRequest = { dstExpanded = false }) {
                            locations.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text(l.name) },
                                    onClick = {
                                        destLocationId = l.id
                                        dstExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Quantity
                item {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Transfer Quantity (Max: $availableInSource) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Staff
                item {
                    OutlinedTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = { Text("Responsible Staff Member") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Transfer Memo / Dispatch Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Confirm Button
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qty = quantityStr.toIntOrNull() ?: 0
                                if (selectedItem != null && srcLocation != null && dstLocation != null && qty > 0) {
                                    onConfirm(
                                        selectedItem.product.id,
                                        selectedItem.product.name,
                                        sourceLocationId,
                                        srcLocation.name,
                                        destLocationId,
                                        dstLocation.name,
                                        qty,
                                        staffName,
                                        notes
                                    )
                                }
                            },
                            enabled = (quantityStr.toIntOrNull() ?: 0) in 1..availableInSource && sourceLocationId != destLocationId
                        ) {
                            Text("Dispatch Transfer")
                        }
                    }
                }
            }
        }
    }
}
