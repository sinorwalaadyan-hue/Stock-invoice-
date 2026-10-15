package com.example.ui.screens.purchase

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
import com.example.data.entity.ProductEntity
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.SupplierEntity
import com.example.ui.StockVisionViewModel
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    viewModel: StockVisionViewModel
) {
    val purchases by viewModel.allPurchases.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val products by viewModel.activeProducts.collectAsStateWithLifecycle()
    val locations by viewModel.allLocations.collectAsStateWithLifecycle()

    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    var selectedPurchaseDetail by remember { mutableStateOf<PurchaseEntity?>(null) }

    val totalPurchasesVal = purchases.sumOf { it.totalAmount }
    val totalPaidVal = purchases.sumOf { it.amountPaid }
    val totalOutstandingPayable = totalPurchasesVal - totalPaidVal

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Purchase Inwards & Suppliers", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Receiving Stock into Godown with Weighted Costing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showNewPurchaseDialog = true },
                        modifier = Modifier.testTag("new_purchase_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Inward PO")
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
                // Supplier Payables Summary Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Purchases", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(Formatters.formatCurrency(totalPurchasesVal), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Outstanding Payables", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = Formatters.formatCurrency(totalOutstandingPayable),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalOutstandingPayable > 0) BrandCrimson else BrandEmerald
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Purchase Invoices & Inward Slips",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (purchases.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No purchase invoices recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(purchases) { purchase ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = CardDefaults.outlinedCardBorder(),
                        onClick = { selectedPurchaseDetail = purchase }
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(purchase.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (purchase.paymentStatus == "PAID") BrandEmerald.copy(alpha = 0.15f) else BrandGold.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = purchase.paymentStatus,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (purchase.paymentStatus == "PAID") BrandEmerald else BrandGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = Formatters.formatCurrency(purchase.totalAmount),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "Supplier: ${purchase.supplierName} • ${Formatters.formatDateTime(purchase.date)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Payment: ${purchase.paymentMethod}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                val balance = purchase.totalAmount - purchase.amountPaid
                                if (balance > 0) {
                                    Text("Due: ${Formatters.formatCurrency(balance)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BrandCrimson)
                                } else {
                                    Text("Settled in Full", fontSize = 11.sp, color = BrandEmerald)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // New Inward Purchase Dialog
    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            suppliers = suppliers,
            products = products,
            locations = locations,
            onDismiss = { showNewPurchaseDialog = false },
            onConfirm = { suppId, suppName, locId, prodId, prodName, qty, unitCost, disc, taxPct, shipping, mode, paid, notes ->
                viewModel.recordPurchase(suppId, suppName, locId, prodId, prodName, qty, unitCost, disc, taxPct, shipping, mode, paid, notes) {
                    showNewPurchaseDialog = false
                }
            }
        )
    }

    // Purchase Detail Modal
    selectedPurchaseDetail?.let { purchase ->
        Dialog(onDismissRequest = { selectedPurchaseDetail = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Purchase Inward Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        IconButton(onClick = { selectedPurchaseDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text("Invoice #: ${purchase.invoiceNumber}", fontWeight = FontWeight.Bold)
                    Text("Supplier: ${purchase.supplierName}", fontSize = 12.sp)
                    Text("Date: ${Formatters.formatDateTime(purchase.date)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Bill Amount:", fontSize = 12.sp)
                        Text(Formatters.formatCurrency(purchase.totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Amount Paid:", fontSize = 12.sp)
                        Text(Formatters.formatCurrency(purchase.amountPaid), fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Status:", fontSize = 12.sp)
                        Text(purchase.paymentStatus, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (purchase.paymentStatus == "PAID") BrandEmerald else BrandGold)
                    }
                    if (purchase.notes.isNotBlank()) {
                        Text("Notes: ${purchase.notes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { selectedPurchaseDetail = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun NewPurchaseDialog(
    suppliers: List<SupplierEntity>,
    products: List<ProductEntity>,
    locations: List<com.example.data.entity.LocationEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, Long, Long, String, Int, Double, Double, Double, Double, String, Double, String) -> Unit
) {
    var selectedSupplierId by remember { mutableStateOf(suppliers.firstOrNull()?.id ?: 1L) }
    var selectedLocationId by remember { mutableStateOf(locations.firstOrNull()?.id ?: 1L) }
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id ?: 1L) }

    var quantityStr by remember { mutableStateOf("20") }
    var unitCostStr by remember { mutableStateOf(products.firstOrNull()?.purchasePrice?.toString() ?: "1000") }
    var discountStr by remember { mutableStateOf("0") }
    var taxPercentStr by remember { mutableStateOf("18") }
    var shippingCostStr by remember { mutableStateOf("500") }
    var paymentMethod by remember { mutableStateOf("BANK") }
    var amountPaidStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val selectedProd = products.find { it.id == selectedProductId }
    val selectedSupp = suppliers.find { it.id == selectedSupplierId }

    // Auto update cost when product changed
    LaunchedEffect(selectedProductId) {
        selectedProd?.let {
            unitCostStr = it.purchasePrice.toString()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Inward Purchase Order (PO)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Increases warehouse stock & adjusts weighted purchase cost", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Select Supplier
                item {
                    Text("Supplier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var suppExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { suppExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedSupp?.name ?: "Select Supplier")
                        }
                        DropdownMenu(expanded = suppExpanded, onDismissRequest = { suppExpanded = false }) {
                            suppliers.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name) },
                                    onClick = {
                                        selectedSupplierId = s.id
                                        suppExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Select Receiving Warehouse
                item {
                    Text("Receiving Destination Godown", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var locExpanded by remember { mutableStateOf(false) }
                    val currentLoc = locations.find { it.id == selectedLocationId }
                    Box {
                        OutlinedButton(onClick = { locExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(currentLoc?.name ?: "Select Location")
                        }
                        DropdownMenu(expanded = locExpanded, onDismissRequest = { locExpanded = false }) {
                            locations.forEach { l ->
                                DropdownMenuItem(
                                    text = { Text(l.name) },
                                    onClick = {
                                        selectedLocationId = l.id
                                        locExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Select Product
                item {
                    Text("Product to Restock", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var prodExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { prodExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedProd?.name ?: "Select Product")
                        }
                        DropdownMenu(expanded = prodExpanded, onDismissRequest = { prodExpanded = false }) {
                            products.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text("${p.name} (${p.sku})") },
                                    onClick = {
                                        selectedProductId = p.id
                                        prodExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Quantity and Unit Cost
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Quantity *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = unitCostStr,
                            onValueChange = { unitCostStr = it },
                            label = { Text("Unit Cost (₹) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                // Discount, Tax and Freight
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = discountStr, onValueChange = { discountStr = it }, label = { Text("Disc (₹)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = taxPercentStr, onValueChange = { taxPercentStr = it }, label = { Text("Tax %") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = shippingCostStr, onValueChange = { shippingCostStr = it }, label = { Text("Freight (₹)") }, modifier = Modifier.weight(1f))
                    }
                }

                // Payment mode
                item {
                    Text("Payment Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("BANK", "UPI", "CASH", "CREDIT").forEach { mode ->
                            FilterChip(
                                selected = paymentMethod == mode,
                                onClick = { paymentMethod = mode },
                                label = { Text(mode, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountPaidStr,
                        onValueChange = { amountPaidStr = it },
                        label = { Text("Amount Paid Now (₹) (Leave blank for full)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qty = quantityStr.toIntOrNull() ?: 1
                                val cost = unitCostStr.toDoubleOrNull() ?: 0.0
                                val disc = discountStr.toDoubleOrNull() ?: 0.0
                                val taxPct = taxPercentStr.toDoubleOrNull() ?: 18.0
                                val freight = shippingCostStr.toDoubleOrNull() ?: 0.0

                                val sub = qty * cost - disc
                                val totalEstimated = sub + (sub * taxPct / 100.0) + freight
                                val paid = amountPaidStr.toDoubleOrNull() ?: totalEstimated

                                if (selectedProd != null && selectedSupp != null && qty > 0 && cost > 0) {
                                    onConfirm(
                                        selectedSupp.id,
                                        selectedSupp.name,
                                        selectedLocationId,
                                        selectedProd.id,
                                        selectedProd.name,
                                        qty,
                                        cost,
                                        disc,
                                        taxPct,
                                        freight,
                                        paymentMethod,
                                        paid,
                                        notes
                                    )
                                }
                            }
                        ) {
                            Text("Receive into Stock")
                        }
                    }
                }
            }
        }
    }
}
