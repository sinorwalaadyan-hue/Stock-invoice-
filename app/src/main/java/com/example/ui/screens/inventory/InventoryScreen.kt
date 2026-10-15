package com.example.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.data.entity.ProductEntity
import com.example.data.entity.StockMovementEntity
import com.example.ui.ProductWithStock
import com.example.ui.StockVisionViewModel
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: StockVisionViewModel,
    onProductClick: (ProductEntity) -> Unit
) {
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    val locations by viewModel.allLocations.collectAsStateWithLifecycle()
    val stockMovements by viewModel.allStockMovements.collectAsStateWithLifecycle()
    val selectedLocId by viewModel.selectedLocationId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var showMovementsDialog by remember { mutableStateOf(false) }
    var selectedProductForDetail by remember { mutableStateOf<ProductWithStock?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Stock List, 1 = Stock Movements History

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Inventory & Godowns",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${productsWithStock.size} Active Products Tracked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = { showMovementsDialog = true }) {
                            Icon(imageVector = Icons.Default.History, contentDescription = "Movements History")
                        }
                        FilledTonalButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("add_product_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New SKU")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by SKU, Name or Barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inventory_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location selector tabs
                ScrollableTabRow(
                    selectedTabIndex = if (selectedLocId == null) 0 else (locations.indexOfFirst { it.id == selectedLocId } + 1).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent
                ) {
                    Tab(
                        selected = selectedLocId == null,
                        onClick = { viewModel.selectedLocationId.value = null },
                        text = { Text("All Warehouses", fontSize = 12.sp) }
                    )
                    locations.forEach { loc ->
                        Tab(
                            selected = selectedLocId == loc.id,
                            onClick = { viewModel.selectedLocationId.value = loc.id },
                            text = { Text(loc.name.take(16), fontSize = 12.sp) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (productsWithStock.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No inventory matches your filters", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(productsWithStock) { item ->
                    InventoryItemCard(
                        item = item,
                        locations = locations,
                        onClick = { selectedProductForDetail = item },
                        onArchive = { viewModel.archiveProduct(item.product.id) }
                    )
                }
            }
        }
    }

    // Detail Dialog
    selectedProductForDetail?.let { item ->
        ProductDetailModal(
            item = item,
            locations = locations,
            onDismiss = { selectedProductForDetail = null },
            onEdit = {
                selectedProductForDetail = null
                onProductClick(item.product)
            }
        )
    }

    // Add Product Dialog
    if (showAddDialog) {
        AddProductDialog(
            locations = locations,
            onDismiss = { showAddDialog = false },
            onSave = { product, stock, locId ->
                viewModel.saveProduct(product, stock, locId) {
                    showAddDialog = false
                }
            }
        )
    }

    // Stock Movements Dialog
    if (showMovementsDialog) {
        StockMovementsModal(
            movements = stockMovements,
            onDismiss = { showMovementsDialog = false }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: ProductWithStock,
    locations: List<com.example.data.entity.LocationEntity>,
    onClick: () -> Unit,
    onArchive: () -> Unit
) {
    val prod = item.product
    val isBullish = prod.priceChangeCurrency >= 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${prod.sku}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = prod.sku,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = prod.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = prod.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Barcode: ${prod.barcode} • Brand: ${prod.brand}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status Chip (Low / In Stock / Out of Stock)
                val statusText = if (item.isOutOfStock) "Out of Stock" else if (item.isLowStock) "Low Stock" else "In Stock"
                val statusColor = if (item.isOutOfStock) BrandCrimson else if (item.isLowStock) BrandGold else BrandEmerald

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Pricing & Quantity Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Avg Purchase Cost", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(prod.purchasePrice), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Selling Price", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(prod.sellingPrice), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text("Profit Margin", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%.1f", prod.unitProfitMargin)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandEmerald)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Available Stock", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${item.totalAvailable} ${prod.unit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (item.isLowStock) BrandCrimson else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun ProductDetailModal(
    item: ProductWithStock,
    locations: List<com.example.data.entity.LocationEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val prod = item.product

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Inventory Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(text = prod.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(text = "SKU: ${prod.sku} | Barcode: ${prod.barcode}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(14.dp))

                Text(text = "Location Distribution:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))

                locations.forEach { loc ->
                    val qty = item.stockByLocation[loc.id] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(loc.name, fontSize = 12.sp)
                        Text("$qty ${prod.unit}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Market Reference Price:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(prod.marketPrice), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Holding Stock Valuation:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Formatters.formatCurrency(item.estimatedSellingValuation), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Update Price / Edit SKU")
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    locations: List<com.example.data.entity.LocationEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity, Int, Long) -> Unit
) {
    var sku by remember { mutableStateOf("SV-" + System.currentTimeMillis().toString().takeLast(5)) }
    var name by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("890" + System.currentTimeMillis().toString().takeLast(10)) }
    var category by remember { mutableStateOf("Electronics") }
    var brand by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pcs") }
    var purchasePriceStr by remember { mutableStateOf("") }
    var sellingPriceStr by remember { mutableStateOf("") }
    var marketPriceStr by remember { mutableStateOf("") }
    var initialStockStr by remember { mutableStateOf("10") }
    var minStockStr by remember { mutableStateOf("5") }
    var selectedLocationId by remember { mutableStateOf(locations.firstOrNull()?.id ?: 1L) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Add Master Product", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("Barcode") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (pcs/boxes)") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = purchasePriceStr, onValueChange = { purchasePriceStr = it }, label = { Text("Cost (₹) *") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = sellingPriceStr, onValueChange = { sellingPriceStr = it }, label = { Text("Selling (₹) *") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = initialStockStr, onValueChange = { initialStockStr = it }, label = { Text("Opening Stock") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = minStockStr, onValueChange = { minStockStr = it }, label = { Text("Min Stock Alert") }, modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cost = purchasePriceStr.toDoubleOrNull() ?: 0.0
                                val sell = sellingPriceStr.toDoubleOrNull() ?: 0.0
                                val mkt = marketPriceStr.toDoubleOrNull() ?: sell
                                val stock = initialStockStr.toIntOrNull() ?: 0
                                val minStock = minStockStr.toIntOrNull() ?: 5

                                if (name.isNotBlank() && sell > 0) {
                                    val newProduct = ProductEntity(
                                        sku = sku,
                                        barcode = barcode,
                                        name = name,
                                        category = category,
                                        brand = brand,
                                        unit = unit,
                                        purchasePrice = cost,
                                        sellingPrice = sell,
                                        marketPrice = mkt,
                                        previousSellingPrice = sell,
                                        minStockLevel = minStock,
                                        defaultLocationId = selectedLocationId
                                    )
                                    onSave(newProduct, stock, selectedLocationId)
                                }
                            },
                            enabled = name.isNotBlank() && sellingPriceStr.isNotBlank()
                        ) {
                            Text("Save SKU")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockMovementsModal(
    movements: List<StockMovementEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stock Movement Audit Trail", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(movements) { mov ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mov.productName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${mov.type} • Ref: ${mov.referenceId}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(Formatters.formatDateTime(mov.timestamp), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "${if (mov.type == "PURCHASE" || mov.type == "TRANSFER_IN") "+" else "-"}${mov.quantity}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = if (mov.type == "PURCHASE" || mov.type == "TRANSFER_IN") BrandEmerald else BrandCrimson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
