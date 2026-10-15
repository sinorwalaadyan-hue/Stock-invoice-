package com.example.ui.screens.profit

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.SaleEntity
import com.example.ui.StockVisionViewModel
import com.example.ui.components.SalesAreaChart
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfitLossScreen(
    viewModel: StockVisionViewModel
) {
    val sales by viewModel.allSales.collectAsStateWithLifecycle()
    val saleItems by viewModel.allSaleItems.collectAsStateWithLifecycle()
    val expenses by viewModel.allExpenses.collectAsStateWithLifecycle()

    var selectedPeriod by remember { mutableStateOf("THIS_MONTH") } // TODAY, THIS_WEEK, THIS_MONTH, THIS_YEAR, ALL
    var showExpenseDialog by remember { mutableStateOf(false) }
    var showDrilldownType by remember { mutableStateOf<String?>(null) } // "SALES", "COGS", "EXPENSES"

    // Filter sales and expenses based on period
    val now = Calendar.getInstance().timeInMillis
    val filterThreshold = remember(selectedPeriod) {
        val cal = Calendar.getInstance()
        when (selectedPeriod) {
            "TODAY" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "THIS_WEEK" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "THIS_MONTH" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            "THIS_YEAR" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.timeInMillis
            }
            else -> 0L
        }
    }

    val periodSales = remember(sales, filterThreshold) {
        sales.filter { it.date >= filterThreshold && !it.isReturned }
    }
    val periodExpenses = remember(expenses, filterThreshold) {
        expenses.filter { it.date >= filterThreshold }
    }

    val grossSalesRevenue = periodSales.sumOf { it.totalAmount }
    val totalDiscounts = periodSales.sumOf { it.discountAmount }
    val netSalesRevenue = grossSalesRevenue - totalDiscounts
    val cogsAmount = periodSales.sumOf { it.cogsAmount }
    val grossProfit = netSalesRevenue - cogsAmount
    val totalOperatingExpenses = periodExpenses.sumOf { it.amount }
    val netOperatingProfit = grossProfit - totalOperatingExpenses
    val netProfitMarginPercent = if (netSalesRevenue > 0) (netOperatingProfit / netSalesRevenue) * 100.0 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Business Profit & Loss Ledger", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Weighted-Average Costing & P&L Statement", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { showExpenseDialog = true },
                        modifier = Modifier.testTag("add_expense_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Expense")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Period Selector Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "TODAY" to "Today",
                        "THIS_WEEK" to "This Week",
                        "THIS_MONTH" to "This Month",
                        "THIS_YEAR" to "This Year",
                        "ALL" to "All-Time"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedPeriod == key,
                            onClick = { selectedPeriod = key },
                            label = { Text(label) }
                        )
                    }
                }
            }

            // High-Impact Net Operating Profit Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netOperatingProfit >= 0) BrandEmerald.copy(alpha = 0.12f) else BrandCrimson.copy(alpha = 0.12f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (netOperatingProfit >= 0) BrandEmerald else BrandCrimson)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "NET OPERATING PROFIT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = Formatters.formatCurrency(netOperatingProfit),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = if (netOperatingProfit >= 0) BrandEmerald else BrandCrimson
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (netOperatingProfit >= 0) BrandEmerald else BrandCrimson
                            ) {
                                Text(
                                    text = "${String.format("%.1f", netProfitMarginPercent)}% Margin",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Calculated after deducting weighted inventory COGS & operating overheads",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Structured Accounting P&L Statement Table
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Formal P&L Breakdown (Inspect Underlyings)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Gross Revenue
                        AccountingRow(
                            label = "1. Gross Sales Revenue",
                            value = Formatters.formatCurrency(grossSalesRevenue),
                            actionLabel = "Inspect (${periodSales.size} Invoices)",
                            onClick = { showDrilldownType = "SALES" }
                        )

                        // Sales Discounts
                        if (totalDiscounts > 0) {
                            AccountingRow(
                                label = "   Less: Sales Discounts",
                                value = "- " + Formatters.formatCurrency(totalDiscounts),
                                isNegative = true
                            )
                        }

                        // Net Revenue
                        AccountingRow(
                            label = "2. Net Sales Revenue",
                            value = Formatters.formatCurrency(netSalesRevenue),
                            isBold = true
                        )

                        // COGS
                        AccountingRow(
                            label = "3. Cost of Goods Sold (COGS)",
                            value = "- " + Formatters.formatCurrency(cogsAmount),
                            isNegative = true,
                            actionLabel = "Inspect Cost Batches",
                            onClick = { showDrilldownType = "COGS" }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Gross Profit
                        AccountingRow(
                            label = "4. Gross Profit",
                            value = Formatters.formatCurrency(grossProfit),
                            isBold = true,
                            customColor = BrandEmerald
                        )

                        // Operating Expenses
                        AccountingRow(
                            label = "5. Operating Expenses (Rent, Salary, Bills)",
                            value = "- " + Formatters.formatCurrency(totalOperatingExpenses),
                            isNegative = true,
                            actionLabel = "Inspect (${periodExpenses.size} Expenses)",
                            onClick = { showDrilldownType = "EXPENSES" }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Net Profit
                        AccountingRow(
                            label = "6. Net Business Profit",
                            value = Formatters.formatCurrency(netOperatingProfit),
                            isBold = true,
                            customColor = if (netOperatingProfit >= 0) BrandEmerald else BrandCrimson
                        )
                    }
                }
            }

            // Recent Expenses List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Operating Expenses (${periodExpenses.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    TextButton(onClick = { showExpenseDialog = true }) {
                        Text("Record Bill", fontSize = 12.sp)
                    }
                }
            }

            items(periodExpenses) { expense ->
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
                        Column {
                            Text(expense.category, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = "${expense.recipient} • ${Formatters.formatDateTime(expense.date)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (expense.notes.isNotBlank()) {
                                Text(expense.notes, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = "- " + Formatters.formatCurrency(expense.amount),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BrandCrimson
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Expense Entry Modal
    if (showExpenseDialog) {
        ExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onConfirm = { cat, amt, mode, rec, notes ->
                viewModel.recordExpense(cat, amt, mode, rec, notes) {
                    showExpenseDialog = false
                }
            }
        )
    }

    // Drilldown Inspection Dialog
    showDrilldownType?.let { type ->
        Dialog(onDismissRequest = { showDrilldownType = null }) {
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
                        Text(
                            text = when (type) {
                                "SALES" -> "Underlying Sales Invoices"
                                "COGS" -> "Underlying COGS by Product"
                                else -> "Expense Details"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(onClick = { showDrilldownType = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        when (type) {
                            "SALES" -> {
                                items(periodSales) { s ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(s.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(s.customerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(Formatters.formatCurrency(s.netAmount), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                            "COGS" -> {
                                val cogsByProduct = saleItems.groupBy { it.productName }.map { (name, items) ->
                                    Triple(name, items.sumOf { it.quantity }, items.sumOf { it.lineCogs })
                                }
                                items(cogsByProduct) { (name, totalQty, totalCogs) ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text("$totalQty units sold", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(Formatters.formatCurrency(totalCogs), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandCyan)
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                }
                            }
                            "EXPENSES" -> {
                                items(periodExpenses) { exp ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(exp.category, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(exp.recipient, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(Formatters.formatCurrency(exp.amount), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = BrandCrimson)
                                    }
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
fun AccountingRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    isNegative: Boolean = false,
    customColor: Color? = null,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = if (isBold) 13.sp else 12.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = value,
                fontSize = if (isBold) 14.sp else 12.sp,
                fontWeight = if (isBold) FontWeight.Black else FontWeight.SemiBold,
                color = customColor ?: if (isNegative) BrandCrimson else MaterialTheme.colorScheme.onSurface
            )
        }
        if (actionLabel != null && onClick != null) {
            TextButton(
                onClick = onClick,
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.height(24.dp)
            ) {
                Text(actionLabel, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String, String, String) -> Unit
) {
    val categories = listOf("Rent", "Salaries", "Electricity", "Transportation", "Packaging", "Marketing", "Repairs", "Software & Tools", "Other")
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var amountStr by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    var recipient by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Record Business Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                item {
                    Text("Expense Category", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    var catExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { catExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(selectedCategory)
                        }
                        DropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            categories.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c) },
                                    onClick = {
                                        selectedCategory = c
                                        catExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Expense Amount (₹) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = recipient,
                        onValueChange = { recipient = it },
                        label = { Text("Paid To / Vendor Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("Payment Mode", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("UPI", "BANK", "CASH", "CARD").forEach { mode ->
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
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Description / Bill Memo") },
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
                                val amt = amountStr.toDoubleOrNull() ?: 0.0
                                if (amt > 0 && recipient.isNotBlank()) {
                                    onConfirm(selectedCategory, amt, paymentMethod, recipient, notes)
                                }
                            },
                            enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0 && recipient.isNotBlank()
                        ) {
                            Text("Record Bill")
                        }
                    }
                }
            }
        }
    }
}
