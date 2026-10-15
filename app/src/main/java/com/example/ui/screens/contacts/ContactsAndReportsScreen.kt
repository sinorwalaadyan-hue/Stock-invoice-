package com.example.ui.screens.contacts

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
import com.example.data.entity.CustomerEntity
import com.example.data.entity.SupplierEntity
import com.example.ui.StockVisionViewModel
import com.example.ui.UserRole
import com.example.ui.theme.BrandCrimson
import com.example.ui.theme.BrandCyan
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandGold
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsAndReportsScreen(
    viewModel: StockVisionViewModel
) {
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val suppliers by viewModel.allSuppliers.collectAsStateWithLifecycle()
    val auditLogs by viewModel.allAuditLogs.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Customers, 1 = Suppliers, 2 = Reports & Audits
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showRoleSwitcherDialog by remember { mutableStateOf(false) }

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
                            text = "Commercial Directory & Reports",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Active Role: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.testTag("role_badge")
                            ) {
                                Text(
                                    text = currentRole.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showRoleSwitcherDialog = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("switch_role_button")
                    ) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Switch Role", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Customers (${customers.size})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Suppliers (${suppliers.size})") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Reports & Logs") })
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Customer Balances & Ledgers", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            FilledTonalButton(onClick = { showAddCustomerDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Customer")
                            }
                        }
                    }

                    items(customers) { cust ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val hasBalance = cust.outstandingBalance > 0
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (hasBalance) BrandCrimson.copy(alpha = 0.15f) else BrandEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (hasBalance) "Due: ${Formatters.formatCurrency(cust.outstandingBalance)}" else "Clear",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasBalance) BrandCrimson else BrandEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("Phone: ${cust.phone} • ${cust.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Lifetime Sales: ${Formatters.formatCurrency(cust.totalPurchases)}", fontSize = 11.sp)
                                    Text(if (cust.email.isNotBlank()) cust.email else "No Email", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Supplier Directory & Payables", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            FilledTonalButton(onClick = { showAddSupplierDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Supplier")
                            }
                        }
                    }

                    items(suppliers) { supp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(supp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    val hasPayable = supp.outstandingBalance > 0
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (hasPayable) BrandGold.copy(alpha = 0.15f) else BrandEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = if (hasPayable) "Payable: ${Formatters.formatCurrency(supp.outstandingBalance)}" else "Paid",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasPayable) BrandGold else BrandEmerald,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("Contact: ${supp.contactPerson} • ${supp.phone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Categories: ${supp.categoriesSupplied}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
                2 -> {
                    // Exportable Report Center
                    item {
                        Text("Business Summary & Exportable Reports", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Comprehensive Stock Valuation Audit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Current Inventory Units:", fontSize = 12.sp)
                                    Text("${stats.totalAvailableStockUnits} Units", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Purchase Valuation (Cost):", fontSize = 12.sp)
                                    Text(Formatters.formatCurrency(stats.inventoryPurchaseValuation), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Selling Valuation (Asset):", fontSize = 12.sp)
                                    Text(Formatters.formatCurrency(stats.inventorySellingValuation), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Potential Unrealized Margin:", fontSize = 12.sp)
                                    Text(Formatters.formatCurrency(stats.potentialGrossProfit), fontWeight = FontWeight.Black, fontSize = 13.sp, color = BrandEmerald)
                                }
                            }
                        }
                    }

                    item {
                        Text("Security & Transaction Audit Trail (${auditLogs.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    items(auditLogs.take(20)) { log ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(log.actionType, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Role: ${log.userRole}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(log.description, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                                Text(Formatters.formatDateTime(log.timestamp), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Role Switcher Modal
    if (showRoleSwitcherDialog) {
        Dialog(onDismissRequest = { showRoleSwitcherDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Switch Operations Role", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Simulate permissions across Super Admin, Manager, Accountant, Sales & Warehouse", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    UserRole.values().forEach { role ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (currentRole == role) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            onClick = {
                                viewModel.setUserRole(role)
                                showRoleSwitcherDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(role.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(role.badge, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (currentRole == role) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddCustomerDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Customer Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Optional)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Billing Address") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddCustomerDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    viewModel.saveCustomer(CustomerEntity(name = name, phone = phone, email = email, address = address)) {
                                        showAddCustomerDialog = false
                                    }
                                }
                            },
                            enabled = name.isNotBlank() && phone.isNotBlank()
                        ) {
                            Text("Save Customer")
                        }
                    }
                }
            }
        }
    }

    // Add Supplier Dialog
    if (showAddSupplierDialog) {
        var name by remember { mutableStateOf("") }
        var contactPerson by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var categories by remember { mutableStateOf("Electronics, Hardware") }

        Dialog(onDismissRequest = { showAddSupplierDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Supplier Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company / Vendor Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = contactPerson, onValueChange = { contactPerson = it }, label = { Text("Contact Person") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = categories, onValueChange = { categories = it }, label = { Text("Supplied Categories") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddSupplierDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && phone.isNotBlank()) {
                                    viewModel.saveSupplier(SupplierEntity(name = name, contactPerson = contactPerson, phone = phone, categoriesSupplied = categories)) {
                                        showAddSupplierDialog = false
                                    }
                                }
                            },
                            enabled = name.isNotBlank() && phone.isNotBlank()
                        ) {
                            Text("Save Supplier")
                        }
                    }
                }
            }
        }
    }
}
