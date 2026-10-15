package com.example.data.repository

import com.example.data.dao.AccountingDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class InventoryRepository(
    private val productDao: ProductDao,
    private val inventoryDao: InventoryDao,
    private val transactionDao: TransactionDao,
    private val accountingDao: AccountingDao
) {
    // Products
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProductsFlow()
    val activeProducts: Flow<List<ProductEntity>> = productDao.getActiveProductsFlow()
    val allPriceHistory: Flow<List<PriceHistoryEntity>> = productDao.getAllPriceHistoryFlow()

    // Locations & Stocks
    val allLocations: Flow<List<LocationEntity>> = inventoryDao.getAllLocationsFlow()
    val allLocationStocks: Flow<List<LocationStockEntity>> = inventoryDao.getAllLocationStocksFlow()
    val stockMovements: Flow<List<StockMovementEntity>> = inventoryDao.getStockMovementsFlow()
    val stockTransfers: Flow<List<StockTransferEntity>> = inventoryDao.getAllTransfersFlow()

    // Transactions
    val allPurchases: Flow<List<PurchaseEntity>> = transactionDao.getAllPurchasesFlow()
    val allSales: Flow<List<SaleEntity>> = transactionDao.getAllSalesFlow()
    val allSaleItems: Flow<List<SaleItemEntity>> = transactionDao.getAllSaleItemsFlow()

    // Accounting
    val allExpenses: Flow<List<ExpenseEntity>> = accountingDao.getAllExpensesFlow()
    val allCustomers: Flow<List<CustomerEntity>> = accountingDao.getAllCustomersFlow()
    val allSuppliers: Flow<List<SupplierEntity>> = accountingDao.getAllSuppliersFlow()
    val allAuditLogs: Flow<List<AuditLogEntity>> = accountingDao.getAllAuditLogsFlow()

    suspend fun getProductById(id: Long): ProductEntity? = productDao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String): ProductEntity? = productDao.getProductByBarcode(barcode)

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProductsFlow(query)

    suspend fun saveProduct(product: ProductEntity, initialStock: Int = 0, initialLocationId: Long = 1): Long {
        val isNew = product.id == 0L
        val savedId = productDao.insertProduct(product)
        val targetId = if (isNew) savedId else product.id

        if (isNew && initialStock > 0) {
            val stock = LocationStockEntity(
                productId = targetId,
                locationId = initialLocationId,
                availableQty = initialStock
            )
            inventoryDao.insertOrUpdateStock(stock)
            inventoryDao.insertStockMovement(
                StockMovementEntity(
                    timestamp = System.currentTimeMillis(),
                    productId = targetId,
                    productName = product.name,
                    type = "ADJUSTMENT",
                    destLocationId = initialLocationId,
                    quantity = initialStock,
                    unitCost = product.purchasePrice,
                    referenceId = "INIT-STOCK",
                    employeeName = "Admin",
                    notes = "Initial opening stock"
                )
            )
        }

        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = "MANAGER",
                actionType = if (isNew) "PRODUCT_CREATED" else "PRODUCT_UPDATED",
                description = "${if (isNew) "Created" else "Updated"} product ${product.name} (${product.sku})"
            )
        )
        return targetId
    }

    suspend fun archiveProduct(productId: Long, userRole: String = "MANAGER") {
        val prod = productDao.getProductById(productId)
        productDao.archiveProduct(productId)
        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "PRODUCT_ARCHIVED",
                description = "Safely archived product ${prod?.name ?: "ID $productId"}"
            )
        )
    }

    suspend fun unarchiveProduct(productId: Long, userRole: String = "MANAGER") {
        productDao.unarchiveProduct(productId)
        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "PRODUCT_RESTORED",
                description = "Restored archived product ID $productId"
            )
        )
    }

    suspend fun updateProductPrice(
        productId: Long,
        newSellingPrice: Double,
        changeReason: String,
        userRole: String = "MANAGER"
    ) {
        val prod = productDao.getProductById(productId) ?: return
        val oldPrice = prod.sellingPrice

        val updated = prod.copy(
            sellingPrice = newSellingPrice,
            previousSellingPrice = oldPrice
        )
        productDao.updateProduct(updated)

        productDao.insertPriceHistory(
            PriceHistoryEntity(
                productId = productId,
                timestamp = System.currentTimeMillis(),
                oldPrice = oldPrice,
                newPrice = newSellingPrice,
                changeReason = changeReason
            )
        )

        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "PRICE_UPDATE",
                description = "Updated price for ${prod.name}: ₹$oldPrice -> ₹$newSellingPrice ($changeReason)"
            )
        )
    }

    suspend fun recordPurchase(
        purchase: PurchaseEntity,
        items: List<PurchaseItemEntity>,
        userRole: String = "MANAGER"
    ): Long {
        val purchaseId = transactionDao.insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        transactionDao.insertPurchaseItems(itemsWithId)

        for (item in itemsWithId) {
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                // Fetch current stock across all locations to compute weighted average cost
                val currentStocks = inventoryDao.getLocationStocksForProduct(product.id).firstOrNull() ?: emptyList()
                val currentTotalQty = currentStocks.sumOf { it.availableQty }
                val newTotalQty = currentTotalQty + item.quantity

                // Weighted Average Cost Formula:
                val newAvgCost = if (newTotalQty > 0) {
                    ((currentTotalQty * product.purchasePrice) + (item.quantity * item.unitPrice)) / newTotalQty
                } else {
                    item.unitPrice
                }

                productDao.updateProduct(
                    product.copy(
                        purchasePrice = newAvgCost,
                        lastPurchaseDate = purchase.date
                    )
                )

                // Update location stock
                val existingStock = inventoryDao.getLocationStock(item.productId, item.locationId)
                val newAvailable = (existingStock?.availableQty ?: 0) + item.quantity
                inventoryDao.insertOrUpdateStock(
                    LocationStockEntity(
                        id = existingStock?.id ?: 0,
                        productId = item.productId,
                        locationId = item.locationId,
                        availableQty = newAvailable,
                        reservedQty = existingStock?.reservedQty ?: 0,
                        damagedQty = existingStock?.damagedQty ?: 0
                    )
                )

                // Stock Movement
                inventoryDao.insertStockMovement(
                    StockMovementEntity(
                        timestamp = purchase.date,
                        productId = item.productId,
                        productName = item.productName,
                        type = "PURCHASE",
                        destLocationId = item.locationId,
                        quantity = item.quantity,
                        unitCost = item.unitPrice,
                        referenceId = purchase.invoiceNumber,
                        employeeName = userRole,
                        notes = "Purchased from ${purchase.supplierName}"
                    )
                )
            }
        }

        // Update supplier balance if balance due
        val balanceDue = purchase.totalAmount - purchase.amountPaid
        if (balanceDue > 0) {
            accountingDao.updateSupplierBalance(purchase.supplierId, purchase.totalAmount, balanceDue)
        } else {
            accountingDao.updateSupplierBalance(purchase.supplierId, purchase.totalAmount, 0.0)
        }

        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "PURCHASE_CONFIRMED",
                description = "Purchase ${purchase.invoiceNumber} recorded (₹${purchase.totalAmount}) from ${purchase.supplierName}"
            )
        )
        return purchaseId
    }

    suspend fun recordSale(
        sale: SaleEntity,
        items: List<SaleItemEntity>,
        userRole: String = "SALESPERSON"
    ): Result<Long> {
        // Validation: Verify all items have enough stock in this location
        for (item in items) {
            val stock = inventoryDao.getLocationStock(item.productId, sale.locationId)
            val available = stock?.availableQty ?: 0
            if (available < item.quantity) {
                return Result.failure(
                    IllegalStateException("Insufficient stock for ${item.productName}. Available: $available, Requested: ${item.quantity}")
                )
            }
        }

        val saleId = transactionDao.insertSale(sale)
        val itemsWithId = items.map { it.copy(saleId = saleId) }
        transactionDao.insertSaleItems(itemsWithId)

        // Decrement stock and log movements
        for (item in itemsWithId) {
            val stock = inventoryDao.getLocationStock(item.productId, sale.locationId)
            val updatedQty = (stock?.availableQty ?: 0) - item.quantity
            inventoryDao.insertOrUpdateStock(
                LocationStockEntity(
                    id = stock?.id ?: 0,
                    productId = item.productId,
                    locationId = sale.locationId,
                    availableQty = updatedQty.coerceAtLeast(0),
                    reservedQty = stock?.reservedQty ?: 0,
                    damagedQty = stock?.damagedQty ?: 0
                )
            )

            // Update product lastSaleDate
            val prod = productDao.getProductById(item.productId)
            if (prod != null) {
                productDao.updateProduct(prod.copy(lastSaleDate = sale.date))
            }

            // Stock movement
            inventoryDao.insertStockMovement(
                StockMovementEntity(
                    timestamp = sale.date,
                    productId = item.productId,
                    productName = item.productName,
                    type = "SALE",
                    sourceLocationId = sale.locationId,
                    quantity = item.quantity,
                    unitCost = item.unitPurchaseCost,
                    referenceId = sale.invoiceNumber,
                    employeeName = userRole,
                    notes = "Sold to ${sale.customerName}"
                )
            )
        }

        // Customer balance update
        val balanceDue = sale.netAmount - sale.amountPaid
        if (sale.customerId > 0) {
            accountingDao.updateCustomerBalance(sale.customerId, sale.netAmount, balanceDue.coerceAtLeast(0.0))
        }

        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "SALE_COMPLETED",
                description = "Invoice ${sale.invoiceNumber} generated for ${sale.customerName}: ₹${sale.netAmount} (Profit: ₹${sale.grossProfit})"
            )
        )

        return Result.success(saleId)
    }

    suspend fun recordStockTransfer(
        transfer: StockTransferEntity,
        userRole: String = "WAREHOUSE_STAFF"
    ): Result<Long> {
        if (transfer.sourceLocationId == transfer.destLocationId) {
            return Result.failure(IllegalArgumentException("Source and destination locations cannot be identical."))
        }

        val sourceStock = inventoryDao.getLocationStock(transfer.productId, transfer.sourceLocationId)
        val available = sourceStock?.availableQty ?: 0
        if (available < transfer.quantity) {
            return Result.failure(
                IllegalStateException("Insufficient stock at source location. Available: $available, Transfer: ${transfer.quantity}")
            )
        }

        // Decrement source
        inventoryDao.insertOrUpdateStock(
            LocationStockEntity(
                id = sourceStock?.id ?: 0,
                productId = transfer.productId,
                locationId = transfer.sourceLocationId,
                availableQty = available - transfer.quantity,
                reservedQty = sourceStock?.reservedQty ?: 0,
                damagedQty = sourceStock?.damagedQty ?: 0
            )
        )

        // Increment destination
        val destStock = inventoryDao.getLocationStock(transfer.productId, transfer.destLocationId)
        val destAvailable = (destStock?.availableQty ?: 0) + transfer.quantity
        inventoryDao.insertOrUpdateStock(
            LocationStockEntity(
                id = destStock?.id ?: 0,
                productId = transfer.productId,
                locationId = transfer.destLocationId,
                availableQty = destAvailable,
                reservedQty = destStock?.reservedQty ?: 0,
                damagedQty = destStock?.damagedQty ?: 0
            )
        )

        val transferId = inventoryDao.insertStockTransfer(transfer)
        val product = productDao.getProductById(transfer.productId)

        // Movements (Out and In)
        inventoryDao.insertStockMovement(
            StockMovementEntity(
                timestamp = transfer.timestamp,
                productId = transfer.productId,
                productName = transfer.productName,
                type = "TRANSFER_OUT",
                sourceLocationId = transfer.sourceLocationId,
                destLocationId = transfer.destLocationId,
                quantity = transfer.quantity,
                unitCost = product?.purchasePrice ?: 0.0,
                referenceId = transfer.transferNumber,
                employeeName = transfer.responsibleStaff,
                notes = "Dispatched from ${transfer.sourceLocationName} to ${transfer.destLocationName}"
            )
        )

        inventoryDao.insertStockMovement(
            StockMovementEntity(
                timestamp = transfer.timestamp,
                productId = transfer.productId,
                productName = transfer.productName,
                type = "TRANSFER_IN",
                sourceLocationId = transfer.sourceLocationId,
                destLocationId = transfer.destLocationId,
                quantity = transfer.quantity,
                unitCost = product?.purchasePrice ?: 0.0,
                referenceId = transfer.transferNumber,
                employeeName = transfer.responsibleStaff,
                notes = "Received at ${transfer.destLocationName}"
            )
        )

        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "STOCK_TRANSFER",
                description = "Transferred ${transfer.quantity} units of ${transfer.productName} from ${transfer.sourceLocationName} to ${transfer.destLocationName}"
            )
        )

        return Result.success(transferId)
    }

    suspend fun recordExpense(expense: ExpenseEntity, userRole: String = "ACCOUNTANT"): Long {
        val id = accountingDao.insertExpense(expense)
        accountingDao.insertAuditLog(
            AuditLogEntity(
                timestamp = System.currentTimeMillis(),
                userRole = userRole,
                actionType = "EXPENSE_RECORDED",
                description = "Expense ₹${expense.amount} under ${expense.category} (${expense.recipient})"
            )
        )
        return id
    }

    suspend fun saveCustomer(customer: CustomerEntity): Long = accountingDao.insertCustomer(customer)
    suspend fun saveSupplier(supplier: SupplierEntity): Long = accountingDao.insertSupplier(supplier)
}
