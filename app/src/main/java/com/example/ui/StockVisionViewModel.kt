package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.*
import com.example.data.repository.InventoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class UserRole(val label: String, val badge: String) {
    SUPER_ADMIN("Super Admin / Owner", "All Access"),
    MANAGER("Operations Manager", "Inventory & Orders"),
    ACCOUNTANT("Finance & Accounts", "P&L & Invoicing"),
    SALESPERSON("Store & Counter POS", "Sales Only"),
    WAREHOUSE_STAFF("Godown & Logistics", "Transfers & Stock")
}

data class ProductWithStock(
    val product: ProductEntity,
    val totalAvailable: Int,
    val totalReserved: Int,
    val totalDamaged: Int,
    val stockByLocation: Map<Long, Int>
) {
    val isLowStock: Boolean
        get() = totalAvailable <= product.minStockLevel && totalAvailable > 0

    val isOutOfStock: Boolean
        get() = totalAvailable == 0

    val estimatedPurchaseValuation: Double
        get() = totalAvailable * product.purchasePrice

    val estimatedSellingValuation: Double
        get() = totalAvailable * product.sellingPrice

    val potentialProfit: Double
        get() = estimatedSellingValuation - estimatedPurchaseValuation
}

data class DashboardStats(
    val totalProductsCount: Int = 0,
    val totalAvailableStockUnits: Int = 0,
    val inventoryPurchaseValuation: Double = 0.0,
    val inventorySellingValuation: Double = 0.0,
    val potentialGrossProfit: Double = 0.0,
    val todaySalesAmount: Double = 0.0,
    val todayGrossProfit: Double = 0.0,
    val monthlySalesAmount: Double = 0.0,
    val monthlyGrossProfit: Double = 0.0,
    val monthlyExpensesAmount: Double = 0.0,
    val monthlyNetProfit: Double = 0.0,
    val lowStockCount: Int = 0,
    val outOfStockCount: Int = 0
)

data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val unitPrice: Double,
    val availableInLocation: Int
) {
    val lineTotal: Double
        get() = quantity * unitPrice
}

class StockVisionViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = InventoryRepository(
        database.productDao(),
        database.inventoryDao(),
        database.transactionDao(),
        database.accountingDao()
    )

    // User Role State
    private val _currentUserRole = MutableStateFlow(UserRole.SUPER_ADMIN)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    // Filters
    val selectedLocationId = MutableStateFlow<Long?>(null)
    val selectedCategory = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")

    // Raw Flows from Repository
    val allLocations = repository.allLocations.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val activeProducts = repository.activeProducts.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allLocationStocks = repository.allLocationStocks.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSales = repository.allSales.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allPurchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allTransfers = repository.stockTransfers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allStockMovements = repository.stockMovements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allCustomers = repository.allCustomers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSuppliers = repository.allSuppliers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allPriceHistory = repository.allPriceHistory.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allAuditLogs = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val allSaleItems = repository.allSaleItems.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Cart for POS
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    // POS selected location (defaults to Retail Shop if present, or 1)
    val posLocationId = MutableStateFlow(3L)

    // User Feedback UI Event / Alert
    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    fun clearSnackMessage() {
        _snackMessage.value = null
    }

    fun setUserRole(role: UserRole) {
        _currentUserRole.value = role
        _snackMessage.value = "Switched to ${role.label}"
    }

    // Aggregated Products with Location Stocks
    val productsWithStock: StateFlow<List<ProductWithStock>> = combine(
        activeProducts,
        allLocationStocks,
        selectedLocationId,
        selectedCategory,
        searchQuery
    ) { products, stocks, locId, cat, query ->
        products
            .filter { p ->
                (cat == null || p.category.equals(cat, ignoreCase = true)) &&
                (query.isBlank() || p.name.contains(query, ignoreCase = true) ||
                 p.sku.contains(query, ignoreCase = true) ||
                 p.barcode.contains(query, ignoreCase = true))
            }
            .map { product ->
                val productStocks = stocks.filter { it.productId == product.id }
                val relevantStocks = if (locId != null) {
                    productStocks.filter { it.locationId == locId }
                } else {
                    productStocks
                }

                val available = relevantStocks.sumOf { it.availableQty }
                val reserved = relevantStocks.sumOf { it.reservedQty }
                val damaged = relevantStocks.sumOf { it.damagedQty }
                val byLoc = productStocks.associate { it.locationId to it.availableQty }

                ProductWithStock(
                    product = product,
                    totalAvailable = available,
                    totalReserved = reserved,
                    totalDamaged = damaged,
                    stockByLocation = byLoc
                )
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Dashboard Financial Stats
    val dashboardStats: StateFlow<DashboardStats> = combine(
        productsWithStock,
        allSales,
        allExpenses
    ) { products, sales, expenses ->
        val now = Calendar.getInstance()
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val totalUnits = products.sumOf { it.totalAvailable }
        val purchaseVal = products.sumOf { it.estimatedPurchaseValuation }
        val sellingVal = products.sumOf { it.estimatedSellingValuation }

        val todaySales = sales.filter { it.date >= startOfToday && !it.isReturned }
        val monthSales = sales.filter { it.date >= startOfMonth && !it.isReturned }
        val monthExpenses = expenses.filter { it.date >= startOfMonth }

        val todaySalesTotal = todaySales.sumOf { it.netAmount }
        val todayProfitTotal = todaySales.sumOf { it.grossProfit }

        val monthSalesTotal = monthSales.sumOf { it.netAmount }
        val monthProfitTotal = monthSales.sumOf { it.grossProfit }
        val monthExpTotal = monthExpenses.sumOf { it.amount }
        val monthNetProfit = monthProfitTotal - monthExpTotal

        val lowStock = products.count { it.isLowStock }
        val outOfStock = products.count { it.isOutOfStock }

        DashboardStats(
            totalProductsCount = products.size,
            totalAvailableStockUnits = totalUnits,
            inventoryPurchaseValuation = purchaseVal,
            inventorySellingValuation = sellingVal,
            potentialGrossProfit = (sellingVal - purchaseVal).coerceAtLeast(0.0),
            todaySalesAmount = todaySalesTotal,
            todayGrossProfit = todayProfitTotal,
            monthlySalesAmount = monthSalesTotal,
            monthlyGrossProfit = monthProfitTotal,
            monthlyExpensesAmount = monthExpTotal,
            monthlyNetProfit = monthNetProfit,
            lowStockCount = lowStock,
            outOfStockCount = outOfStock
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, DashboardStats())

    // Cart Management
    fun addToCart(product: ProductEntity, locationId: Long) {
        val current = _cart.value.toMutableList()
        val stocks = allLocationStocks.value.filter { it.productId == product.id && it.locationId == locationId }
        val available = stocks.sumOf { it.availableQty }

        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            if (existing.quantity < available) {
                current[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                _cart.value = current
            } else {
                _snackMessage.value = "Cannot exceed available location stock ($available units)"
            }
        } else {
            if (available > 0) {
                current.add(
                    CartItem(
                        product = product,
                        quantity = 1,
                        unitPrice = product.sellingPrice,
                        availableInLocation = available
                    )
                )
                _cart.value = current
            } else {
                _snackMessage.value = "Stock not available at this location!"
            }
        }
    }

    fun updateCartQuantity(productId: Long, delta: Int) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = current[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                current.removeAt(index)
            } else if (newQty <= item.availableInLocation) {
                current[index] = item.copy(quantity = newQty)
            } else {
                _snackMessage.value = "Cannot exceed available stock (${item.availableInLocation})"
            }
            _cart.value = current
        }
    }

    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    // POS Checkout
    fun checkoutSale(
        customerId: Long,
        customerName: String,
        locationId: Long,
        discountPercent: Double,
        taxPercent: Double,
        paymentMethod: String,
        amountPaid: Double,
        notes: String,
        onSuccess: (SaleEntity) -> Unit
    ) {
        val items = _cart.value
        if (items.isEmpty()) {
            _snackMessage.value = "Cart is empty"
            return
        }

        viewModelScope.launch {
            val subtotal = items.sumOf { it.lineTotal }
            val discountAmount = subtotal * (discountPercent / 100.0)
            val taxableAmount = subtotal - discountAmount
            val taxAmount = taxableAmount * (taxPercent / 100.0)
            val netAmount = taxableAmount + taxAmount

            val cogsAmount = items.sumOf { it.quantity * it.product.purchasePrice }
            val grossProfit = taxableAmount - cogsAmount // gross profit based on pre-tax net

            val invoiceNumber = "INV-" + System.currentTimeMillis().toString().takeLast(6)

            val saleEntity = SaleEntity(
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName.ifBlank { "Walk-in Retail Cash" },
                date = System.currentTimeMillis(),
                totalAmount = subtotal,
                discountAmount = discountAmount,
                taxAmount = taxAmount,
                netAmount = netAmount,
                cogsAmount = cogsAmount,
                grossProfit = grossProfit,
                paymentMethod = paymentMethod,
                paymentStatus = if (amountPaid >= netAmount) "PAID" else if (amountPaid > 0) "PARTIAL" else "UNPAID",
                amountPaid = amountPaid,
                locationId = locationId,
                notes = notes
            )

            val saleItems = items.map { item ->
                val lineTotal = item.lineTotal
                val lineCogs = item.quantity * item.product.purchasePrice
                SaleItemEntity(
                    saleId = 0,
                    productId = item.product.id,
                    productName = item.product.name,
                    quantity = item.quantity,
                    unitSellingPrice = item.unitPrice,
                    unitPurchaseCost = item.product.purchasePrice,
                    lineTotal = lineTotal,
                    lineCogs = lineCogs,
                    lineProfit = lineTotal - lineCogs
                )
            }

            val result = repository.recordSale(saleEntity, saleItems, _currentUserRole.value.name)
            result.onSuccess { saleId ->
                _cart.value = emptyList()
                _snackMessage.value = "Invoice #$invoiceNumber created successfully!"
                onSuccess(saleEntity.copy(id = saleId))
            }.onFailure { ex ->
                _snackMessage.value = ex.message ?: "Failed to record sale"
            }
        }
    }

    // Purchase Order Creation
    fun recordPurchase(
        supplierId: Long,
        supplierName: String,
        locationId: Long,
        productId: Long,
        productName: String,
        quantity: Int,
        unitPurchasePrice: Double,
        discount: Double,
        taxPercent: Double,
        shippingCost: Double,
        paymentMethod: String,
        amountPaid: Double,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val subtotal = quantity * unitPurchasePrice
            val discounted = subtotal - discount
            val taxAmount = discounted * (taxPercent / 100.0)
            val totalAmount = discounted + taxAmount + shippingCost

            val invoiceNumber = "PUR-" + System.currentTimeMillis().toString().takeLast(6)
            val purchase = PurchaseEntity(
                invoiceNumber = invoiceNumber,
                supplierId = supplierId,
                supplierName = supplierName,
                date = System.currentTimeMillis(),
                totalAmount = totalAmount,
                discount = discount,
                taxAmount = taxAmount,
                shippingCost = shippingCost,
                paymentStatus = if (amountPaid >= totalAmount) "PAID" else if (amountPaid > 0) "PARTIAL" else "UNPAID",
                amountPaid = amountPaid,
                paymentMethod = paymentMethod,
                notes = notes
            )

            val item = PurchaseItemEntity(
                purchaseId = 0,
                productId = productId,
                productName = productName,
                quantity = quantity,
                unitPrice = unitPurchasePrice,
                locationId = locationId
            )

            repository.recordPurchase(purchase, listOf(item), _currentUserRole.value.name)
            _snackMessage.value = "Purchase $invoiceNumber added to inventory!"
            onSuccess()
        }
    }

    // Stock Transfer
    fun executeStockTransfer(
        productId: Long,
        productName: String,
        sourceLocationId: Long,
        sourceLocationName: String,
        destLocationId: Long,
        destLocationName: String,
        quantity: Int,
        responsibleStaff: String,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val transferNumber = "TRF-" + System.currentTimeMillis().toString().takeLast(6)
            val transfer = StockTransferEntity(
                transferNumber = transferNumber,
                timestamp = System.currentTimeMillis(),
                sourceLocationId = sourceLocationId,
                sourceLocationName = sourceLocationName,
                destLocationId = destLocationId,
                destLocationName = destLocationName,
                productId = productId,
                productName = productName,
                quantity = quantity,
                responsibleStaff = responsibleStaff.ifBlank { "Warehouse Logistics" },
                status = "COMPLETED",
                notes = notes
            )

            val result = repository.recordStockTransfer(transfer, _currentUserRole.value.name)
            result.onSuccess {
                _snackMessage.value = "Transfer #$transferNumber completed ($quantity units moved)"
                onSuccess()
            }.onFailure { ex ->
                _snackMessage.value = ex.message ?: "Transfer failed"
            }
        }
    }

    // Price Update
    fun updatePrice(productId: Long, newSellingPrice: Double, changeReason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateProductPrice(productId, newSellingPrice, changeReason, _currentUserRole.value.name)
            _snackMessage.value = "Selling price updated successfully!"
            onSuccess()
        }
    }

    // Product Add/Edit
    fun saveProduct(product: ProductEntity, initialStock: Int, initialLocationId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveProduct(product, initialStock, initialLocationId)
            _snackMessage.value = "Product ${product.name} saved!"
            onSuccess()
        }
    }

    fun archiveProduct(productId: Long) {
        viewModelScope.launch {
            repository.archiveProduct(productId, _currentUserRole.value.name)
            _snackMessage.value = "Product archived safely."
        }
    }

    // Expense
    fun recordExpense(category: String, amount: Double, paymentMethod: String, recipient: String, notes: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val expense = ExpenseEntity(
                date = System.currentTimeMillis(),
                category = category,
                amount = amount,
                paymentMethod = paymentMethod,
                recipient = recipient,
                notes = notes
            )
            repository.recordExpense(expense, _currentUserRole.value.name)
            _snackMessage.value = "Expense ₹$amount recorded under $category"
            onSuccess()
        }
    }

    // Partner Contacts
    fun saveCustomer(customer: CustomerEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _snackMessage.value = "Customer ${customer.name} saved"
            onSuccess()
        }
    }

    fun saveSupplier(supplier: SupplierEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveSupplier(supplier)
            _snackMessage.value = "Supplier ${supplier.name} saved"
            onSuccess()
        }
    }
}
