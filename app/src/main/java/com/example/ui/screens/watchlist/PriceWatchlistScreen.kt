package com.example.ui.screens.watchlist

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
import com.example.ui.ProductWithStock
import com.example.ui.StockVisionViewModel
import com.example.ui.components.CandlestickChart
import com.example.ui.components.CandlestickData
import com.example.ui.components.MiniSparkline
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceWatchlistScreen(
    viewModel: StockVisionViewModel,
    onProductClick: (ProductEntity) -> Unit = {}
) {
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    var selectedProductForPriceUpdate by remember { mutableStateOf<ProductEntity?>(null) }
    var selectedCandleProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var timeFilter by remember { mutableStateOf("1M") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Demat Price Terminal", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Real-time Margins, Valuation & Price Revisions", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Refresh */ }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
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
                // Info Banner clarifying that internal inventory valuation is tracked against optional market benchmarks
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = BrandCyan, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Prices reflect internal weighted-average cost & live store catalog. Market benchmarks are tracked for valuation.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(productsWithStock) { item ->
                DematProductCard(
                    item = item,
                    onUpdatePrice = { selectedProductForPriceUpdate = item.product },
                    onViewChart = { selectedCandleProduct = item.product }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Price Update Modal
    selectedProductForPriceUpdate?.let { product ->
        PriceUpdateDialog(
            product = product,
            onDismiss = { selectedProductForPriceUpdate = null },
            onConfirm = { newPrice, reason ->
                viewModel.updatePrice(product.id, newPrice, reason) {
                    selectedProductForPriceUpdate = null
                }
            }
        )
    }

    // Product Candlestick Modal
    selectedCandleProduct?.let { product ->
        val candlePoints = remember(product) {
            val base = product.sellingPrice.toFloat()
            listOf(
                CandlestickData(open = base * 0.94f, high = base * 0.98f, low = base * 0.92f, close = base * 0.97f, volume = 50f, label = "D1"),
                CandlestickData(open = base * 0.97f, high = base * 1.01f, low = base * 0.96f, close = base * 0.99f, volume = 85f, label = "D2"),
                CandlestickData(open = base * 0.99f, high = base * 1.03f, low = base * 0.97f, close = base * 1.02f, volume = 120f, label = "D3"),
                CandlestickData(open = base * 1.02f, high = base * 1.05f, low = base * 1.00f, close = base, volume = 160f, label = "D4")
            )
        }

        Dialog(onDismissRequest = { selectedCandleProduct = null }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { selectedCandleProduct = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    CandlestickChart(
                        data = candlePoints,
                        title = "${product.sku} History",
                        timeFilter = timeFilter,
                        onTimeFilterSelected = { timeFilter = it }
                    )
                }
            }
        }
    }
}

@Composable
fun DematProductCard(
    item: ProductWithStock,
    onUpdatePrice: () -> Unit,
    onViewChart: () -> Unit
) {
    val prod = item.product
    val isPositive = prod.priceChangeCurrency >= 0
    val deltaColor = if (isPositive) BrandEmerald else BrandCrimson

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("demat_card_${prod.sku}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.4f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = prod.sku,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                }

                // Current Price & Delta
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatCurrency(prod.sellingPrice),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = deltaColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (isPositive) "+" else ""}${Formatters.formatCurrency(prod.priceChangeCurrency)} (${String.format("%.2f", prod.priceChangePercent)}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = deltaColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Demat Statistics Matrix
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Purchase Cost (W-Avg):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(prod.purchasePrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Market Reference Price:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(prod.marketPrice), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = BrandCyan)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Holding Quantity:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${item.totalAvailable} ${prod.unit}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Est. Holding Purchase Cost:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(item.estimatedPurchaseValuation), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Est. Holding Selling Value:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(Formatters.formatCurrency(item.estimatedSellingValuation), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Potential Gross Profit:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = Formatters.formatCurrency(item.potentialProfit) + " (${String.format("%.1f", prod.unitProfitMargin)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandEmerald
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Update Price & Inspect Chart)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewChart,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chart", fontSize = 12.sp)
                }

                Button(
                    onClick = onUpdatePrice,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.PriceChange, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set Price", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PriceUpdateDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var priceStr by remember { mutableStateOf(product.sellingPrice.toString()) }
    var reason by remember { mutableStateOf("Market Revision") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Update Selling Price",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Current: ${Formatters.formatCurrency(product.sellingPrice)} (Cost: ${Formatters.formatCurrency(product.purchasePrice)})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("New Selling Price (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Revision Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val newPrice = priceStr.toDoubleOrNull() ?: 0.0
                            if (newPrice > 0) {
                                onConfirm(newPrice, reason)
                            }
                        },
                        enabled = (priceStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Apply Update")
                    }
                }
            }
        }
    }
}
