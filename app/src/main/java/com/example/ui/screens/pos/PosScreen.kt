package com.example.ui.screens.pos

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import com.example.ui.CartItem
import com.example.ui.ProductWithStock
import com.example.ui.StockVisionViewModel
import com.example.ui.components.InvoiceDialog
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: StockVisionViewModel
) {
    val cart by viewModel.cart.collectAsStateWithLifecycle()
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    val locations by viewModel.allLocations.collectAsStateWithLifecycle()
    val posLocationId by viewModel.posLocationId.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val salesHistory by viewModel.allSales.collectAsStateWithLifecycle()
    val saleItemsHistory by viewModel.allSaleItems.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = POS Counter, 1 = Sales Invoices History
    var showCartSheet by remember { mutableStateOf(false) }
    var activeInvoicePreview by remember { mutableStateOf<Pair<SaleEntity, List<SaleItemEntity>>?>(null) }

    val filteredProducts = remember(productsWithStock, searchQuery, posLocationId) {
        productsWithStock.filter { item ->
            searchQuery.isBlank() ||
            item.product.name.contains(searchQuery, ignoreCase = true) ||
            item.product.sku.contains(searchQuery, ignoreCase = true) ||
            item.product.barcode.contains(searchQuery, ignoreCase = true)
        }
    }

    val cartTotalUnits = cart.sumOf { it.quantity }
    val cartSubtotal = cart.sumOf { it.lineTotal }

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
                            text = "Point of Sale & Billing",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Active Counter: " + (locations.find { it.id == posLocationId }?.name ?: "Retail Shop"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Location Selector dropdown for POS source inventory
                    var locationMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { locationMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch Counter", fontSize = 11.sp)
                        }
                        DropdownMenu(
                            expanded = locationMenuExpanded,
                            onDismissRequest = { locationMenuExpanded = false }
                        ) {
                            locations.forEach { loc ->
                                DropdownMenuItem(
                                    text = { Text(loc.name) },
                                    onClick = {
                                        viewModel.posLocationId.value = loc.id
                                        locationMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("POS Terminal") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Sales Records (${salesHistory.size})") }
                    )
                }
            }
        },
        bottomBar = {
            if (selectedTab == 0 && cart.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$cartTotalUnits items in cart",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = Formatters.formatCurrency(cartSubtotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { showCartSheet = true },
                            modifier = Modifier.testTag("view_cart_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                        ) {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Checkout Bill", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (selectedTab == 0) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                // Quick Search in POS
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search product name, SKU or barcode...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pos_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Product items list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { item ->
                        val locQty = item.stockByLocation[posLocationId] ?: 0
                        val prod = item.product
                        val inCart = cart.find { it.product.id == prod.id }?.quantity ?: 0

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "SKU: ${prod.sku} • Stock: $locQty ${prod.unit}",
                                        fontSize = 11.sp,
                                        color = if (locQty <= 5) BrandCrimson else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (locQty <= 5) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(prod.sellingPrice),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (inCart > 0) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        FilledTonalIconButton(
                                            onClick = { viewModel.updateCartQuantity(prod.id, -1) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = inCart.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp)
                                        )

                                        FilledTonalIconButton(
                                            onClick = { viewModel.updateCartQuantity(prod.id, 1) },
                                            modifier = Modifier.size(32.dp),
                                            enabled = inCart < locQty
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.addToCart(prod, posLocationId) },
                                        enabled = locQty > 0,
                                        modifier = Modifier.testTag("add_to_cart_${prod.sku}"),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (locQty > 0) "Add +" else "Out of Stock", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        } else {
            // Sales History Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(salesHistory) { sale ->
                    val relatedItems = saleItemsHistory.filter { it.saleId == sale.id }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        onClick = { activeInvoicePreview = sale to relatedItems }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(sale.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (sale.paymentStatus == "PAID") BrandEmerald.copy(alpha = 0.15f) else BrandGold.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = sale.paymentStatus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (sale.paymentStatus == "PAID") BrandEmerald else BrandGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Customer: ${sale.customerName} • ${Formatters.formatDateTime(sale.date)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(
                                    text = "Revenue: ${Formatters.formatCurrency(sale.netAmount)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Profit: +${Formatters.formatCurrency(sale.grossProfit)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandEmerald
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Cart Checkout Modal Bottom Sheet
    if (showCartSheet) {
        CartCheckoutModal(
            cart = cart,
            customers = customers,
            posLocationId = posLocationId,
            onDismiss = { showCartSheet = false },
            onCheckout = { custId, custName, disc, tax, mode, paid, notes ->
                viewModel.checkoutSale(custId, custName, posLocationId, disc, tax, mode, paid, notes) { completedSale ->
                    showCartSheet = false
                    val completedItems = cart.map {
                        SaleItemEntity(
                            saleId = completedSale.id,
                            productId = it.product.id,
                            productName = it.product.name,
                            quantity = it.quantity,
                            unitSellingPrice = it.unitPrice,
                            unitPurchaseCost = it.product.purchasePrice,
                            lineTotal = it.lineTotal,
                            lineCogs = it.quantity * it.product.purchasePrice,
                            lineProfit = it.lineTotal - (it.quantity * it.product.purchasePrice)
                        )
                    }
                    activeInvoicePreview = completedSale to completedItems
                }
            }
        )
    }

    // Invoice Preview Dialog
    activeInvoicePreview?.let { (sale, items) ->
        InvoiceDialog(
            sale = sale,
            items = items,
            onDismiss = { activeInvoicePreview = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutModal(
    cart: List<CartItem>,
    customers: List<CustomerEntity>,
    posLocationId: Long,
    onDismiss: () -> Unit,
    onCheckout: (Long, String, Double, Double, String, Double, String) -> Unit
) {
    var customerName by remember { mutableStateOf("Walk-in Retail Cash") }
    var selectedCustomerId by remember { mutableStateOf(0L) }
    var discountPercentStr by remember { mutableStateOf("0") }
    var taxPercentStr by remember { mutableStateOf("18") } // GST 18% standard
    var paymentMethod by remember { mutableStateOf("UPI") }
    var amountPaidStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val subtotal = cart.sumOf { it.lineTotal }
    val discPct = discountPercentStr.toDoubleOrNull() ?: 0.0
    val discAmt = subtotal * (discPct / 100.0)
    val taxable = subtotal - discAmt
    val taxPct = taxPercentStr.toDoubleOrNull() ?: 18.0
    val taxAmt = taxable * (taxPct / 100.0)
    val grandTotal = taxable + taxAmt

    LaunchedEffect(grandTotal) {
        if (amountPaidStr.isBlank()) {
            amountPaidStr = grandTotal.toInt().toString()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Checkout & Invoice Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Customer Selector
            item {
                Text("Customer Selection", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCustomerId == 0L,
                        onClick = {
                            selectedCustomerId = 0L
                            customerName = "Walk-in Retail Cash"
                        },
                        label = { Text("Walk-in Retail") }
                    )
                    customers.forEach { cust ->
                        FilterChip(
                            selected = selectedCustomerId == cust.id,
                            onClick = {
                                selectedCustomerId = cust.id
                                customerName = cust.name
                            },
                            label = { Text(cust.name) }
                        )
                    }
                }
            }

            // Calculation Details
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal (${cart.sumOf { it.quantity }} items):", fontSize = 12.sp)
                            Text(Formatters.formatCurrency(subtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount ($discPct%):", fontSize = 12.sp, color = BrandEmerald)
                            Text("- " + Formatters.formatCurrency(discAmt), fontSize = 12.sp, color = BrandEmerald)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GST ($taxPct%):", fontSize = 12.sp)
                            Text(Formatters.formatCurrency(taxAmt), fontSize = 12.sp)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Grand Total Net:", fontWeight = FontWeight.Black, fontSize = 15.sp)
                            Text(Formatters.formatCurrency(grandTotal), fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Discounts & GST
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountPercentStr,
                        onValueChange = { discountPercentStr = it },
                        label = { Text("Discount %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = taxPercentStr,
                        onValueChange = { taxPercentStr = it },
                        label = { Text("GST %") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // Payment Mode
            item {
                Text("Payment Method", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UPI", "CASH", "CARD", "CREDIT").forEach { mode ->
                        FilterChip(
                            selected = paymentMethod == mode,
                            onClick = { paymentMethod = mode },
                            label = { Text(mode) }
                        )
                    }
                }
            }

            // Amount Paid
            item {
                OutlinedTextField(
                    value = amountPaidStr,
                    onValueChange = { amountPaidStr = it },
                    label = { Text("Amount Paid (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Notes
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Invoice Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Complete Order Button
            item {
                Button(
                    onClick = {
                        val paid = amountPaidStr.toDoubleOrNull() ?: grandTotal
                        onCheckout(selectedCustomerId, customerName, discPct, taxPct, paymentMethod, paid, notes)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_checkout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                ) {
                    Text("Confirm Sale & Print Tax Invoice", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
