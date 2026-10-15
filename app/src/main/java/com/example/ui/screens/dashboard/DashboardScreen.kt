package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ProductEntity
import com.example.ui.ProductWithStock
import com.example.ui.StockVisionViewModel
import com.example.ui.components.*
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: StockVisionViewModel,
    onNavigateToPos: () -> Unit,
    onNavigateToPurchases: () -> Unit,
    onNavigateToTransfers: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToProfitLoss: () -> Unit,
    onSelectProduct: (ProductEntity) -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val productsWithStock by viewModel.productsWithStock.collectAsStateWithLifecycle()
    val locations by viewModel.allLocations.collectAsStateWithLifecycle()
    val selectedLocId by viewModel.selectedLocationId.collectAsStateWithLifecycle()
    val selectedCat by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val activeProducts by viewModel.activeProducts.collectAsStateWithLifecycle()

    var candleTimeFilter by remember { mutableStateOf("1M") }

    // Prepare demo candlestick points
    val candleData = remember(candleTimeFilter, activeProducts) {
        listOf(
            CandlestickData(open = 41200f, high = 42800f, low = 40900f, close = 42500f, volume = 140f, label = "W1"),
            CandlestickData(open = 42500f, high = 43100f, low = 41800f, close = 42200f, volume = 95f, label = "W2"),
            CandlestickData(open = 42200f, high = 43900f, low = 42000f, close = 43700f, volume = 210f, label = "W3"),
            CandlestickData(open = 43700f, high = 44500f, low = 43100f, close = 43999f, volume = 180f, label = "W4"),
            CandlestickData(open = 43999f, high = 45200f, low = 43600f, close = 44850f, volume = 240f, label = "W5")
        )
    }

    val salesChartData = remember {
        listOf(
            "Mon" to 42000.0,
            "Tue" to 78000.0,
            "Wed" to 54000.0,
            "Thu" to 95000.0,
            "Fri" to 112000.0,
            "Sat" to 164000.0,
            "Sun" to 88000.0
        )
    }
    val costChartData = remember {
        listOf(
            "Mon" to 31000.0,
            "Tue" to 58000.0,
            "Wed" to 40000.0,
            "Thu" to 71000.0,
            "Fri" to 82000.0,
            "Sat" to 118000.0,
            "Sun" to 65000.0
        )
    }

    Scaffold(
        topBar = {
            Column {
                DematTicker(
                    products = activeProducts,
                    onProductClick = onSelectProduct
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Quick Location & Category Filter Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedLocId == null,
                        onClick = { viewModel.selectedLocationId.value = null },
                        label = { Text("All Godowns & Shops") },
                        leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    locations.forEach { loc ->
                        FilterChip(
                            selected = selectedLocId == loc.id,
                            onClick = { viewModel.selectedLocationId.value = loc.id },
                            label = { Text(loc.name.take(18)) }
                        )
                    }
                }
            }

            item {
                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf("All", "Electronics", "FMCG", "Hardware", "Textiles")
                    categories.forEach { cat ->
                        val isAll = cat == "All"
                        val isSelected = (isAll && selectedCat == null) || (selectedCat == cat)
                        SuggestionChip(
                            onClick = { viewModel.selectedCategory.value = if (isAll) null else cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            // Quick Actions Hub
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        ActionIconButton(icon = Icons.Default.ShoppingCart, label = "New Sale / POS", color = BrandEmerald, onClick = onNavigateToPos)
                        ActionIconButton(icon = Icons.Default.AddBusiness, label = "Purchase", color = BrandCyan, onClick = onNavigateToPurchases)
                        ActionIconButton(icon = Icons.Default.SwapHoriz, label = "Stock Transfer", color = BrandGold, onClick = onNavigateToTransfers)
                        ActionIconButton(icon = Icons.Default.TrendingUp, label = "P&L Ledger", color = MaterialTheme.colorScheme.primary, onClick = onNavigateToProfitLoss)
                    }
                }
            }

            // Primary Financial Metric Cards Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Today's Gross Sales",
                        value = Formatters.formatCurrency(stats.todaySalesAmount),
                        trendText = "+₹${Formatters.formatNumber(stats.todayGrossProfit.toInt())} Profit",
                        isTrendPositive = true,
                        icon = Icons.Default.TrendingUp,
                        accentColor = BrandEmerald,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Monthly Net Profit",
                        value = Formatters.formatCurrency(stats.monthlyNetProfit),
                        trendText = "After Expenses",
                        isTrendPositive = stats.monthlyNetProfit >= 0,
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = if (stats.monthlyNetProfit >= 0) BrandEmerald else BrandCrimson,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Holding Inventory Value",
                        value = Formatters.formatCurrency(stats.inventorySellingValuation),
                        subtitle = "Cost: ${Formatters.formatCurrency(stats.inventoryPurchaseValuation)}",
                        icon = Icons.Default.Inventory2,
                        accentColor = BrandCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Potential Gross Margin",
                        value = Formatters.formatCurrency(stats.potentialGrossProfit),
                        trendText = "${stats.totalAvailableStockUnits} Total Units",
                        isTrendPositive = true,
                        icon = Icons.Default.MonetizationOn,
                        accentColor = BrandGold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Alerts Banner if any low stock exists
            if (stats.lowStockCount > 0 || stats.outOfStockCount > 0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandCrimson.copy(alpha = 0.12f),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BrandCrimson.copy(alpha = 0.4f))),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = "Alert", tint = BrandCrimson)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Stock Alert: ${stats.lowStockCount} Items Low, ${stats.outOfStockCount} Out of Stock",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BrandCrimson
                                )
                                Text(
                                    text = "Restock orders recommended for Bhiwandi and MG Road counters.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onNavigateToInventory) {
                                Text("Inspect", color = BrandCrimson, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Demat Candlestick Valuation Chart
            item {
                CandlestickChart(
                    data = candleData,
                    timeFilter = candleTimeFilter,
                    onTimeFilterSelected = { candleTimeFilter = it }
                )
            }

            // Sales vs Cost Area Chart
            item {
                SalesAreaChart(
                    salesData = salesChartData,
                    costData = costChartData
                )
            }

            // Top Demat Inventory Tickers
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "High-Volume Inventory Watchlist",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onNavigateToInventory) {
                        Text("View All (${productsWithStock.size})", fontSize = 12.sp)
                    }
                }
            }

            items(productsWithStock.take(5)) { item ->
                ProductWatchlistCard(item = item, onClick = { onSelectProduct(item.product) })
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = color.copy(alpha = 0.15f))
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
fun ProductWatchlistCard(
    item: ProductWithStock,
    onClick: () -> Unit
) {
    val prod = item.product
    val isPositive = prod.priceChangeCurrency >= 0
    val deltaColor = if (isPositive) BrandEmerald else BrandCrimson

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                Text(
                    text = prod.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = prod.sku,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${item.totalAvailable} ${prod.unit} left",
                        fontSize = 10.sp,
                        fontWeight = if (item.isLowStock) FontWeight.Bold else FontWeight.Normal,
                        color = if (item.isLowStock) BrandCrimson else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatCurrency(prod.sellingPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = deltaColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "${if (isPositive) "+" else ""}${String.format("%.1f", prod.priceChangePercent)}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = deltaColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
