package com.example.data.database

import com.example.data.entity.*

object DatabaseSeeder {
    suspend fun seedDatabase(db: AppDatabase) {
        val now = System.currentTimeMillis()
        val dayMillis = 86_400_000L

        // 1. Locations
        val locations = listOf(
            LocationEntity(id = 1, name = "Main Godown (Bhiwandi)", type = "GODOWN", address = "Sector 4 Logistics Park, Bhiwandi", isDefault = true),
            LocationEntity(id = 2, name = "Secondary Warehouse (Panvel)", type = "WAREHOUSE", address = "Plot 12, Industrial Area, Panvel", isDefault = false),
            LocationEntity(id = 3, name = "Retail Shop (MG Road)", type = "RETAIL_SHOP", address = "Shop 14, City Center, MG Road", isDefault = false),
            LocationEntity(id = 4, name = "Head Office & Sample Room", type = "OFFICE", address = "Suite 402, Trade Tower, BKC", isDefault = false)
        )
        db.inventoryDao().insertLocations(locations)

        // 2. Suppliers
        val suppliers = listOf(
            SupplierEntity(id = 1, name = "Apex Electro Tech Ltd", contactPerson = "Rajesh Sharma", phone = "+91 98201 12345", email = "rajesh@apexelectro.in", categoriesSupplied = "Electronics, Laptops", totalPurchases = 425000.0, outstandingBalance = 35000.0),
            SupplierEntity(id = 2, name = "Shree Agro Foods Corp", contactPerson = "Vikram Patel", phone = "+91 98210 54321", email = "sales@shreeagro.in", categoriesSupplied = "Grains, Edible Oils", totalPurchases = 180000.0, outstandingBalance = 0.0),
            SupplierEntity(id = 3, name = "Titan Tool & Hardware Co", contactPerson = "Anil Mehta", phone = "+91 98333 78901", email = "orders@titantools.com", categoriesSupplied = "Hardware, Plumbing", totalPurchases = 210000.0, outstandingBalance = 12000.0),
            SupplierEntity(id = 4, name = "Surat Loomcraft Textiles", contactPerson = "Sanjay Agrawal", phone = "+91 98450 99887", email = "contact@surattextiles.in", categoriesSupplied = "Textiles, Apparel", totalPurchases = 145000.0, outstandingBalance = 8500.0)
        )
        db.accountingDao().insertSuppliers(suppliers)

        // 3. Customers
        val customers = listOf(
            CustomerEntity(id = 1, name = "Metro Superstore", phone = "+91 98700 11223", email = "purchasing@metrosuper.com", address = "Andheri West, Mumbai", totalPurchases = 165000.0, outstandingBalance = 24000.0),
            CustomerEntity(id = 2, name = "Omkar Enterprises", phone = "+91 98690 33445", email = "omkar.ent@gmail.com", address = "Dadar Market, Mumbai", totalPurchases = 92000.0, outstandingBalance = 0.0),
            CustomerEntity(id = 3, name = "Walk-in Retail Cash", phone = "+91 98000 00000", email = "", address = "Store Counter", totalPurchases = 88500.0, outstandingBalance = 0.0),
            CustomerEntity(id = 4, name = "Apex Building Contractors", phone = "+91 98111 66778", email = "procurement@apexbuild.com", address = "Vashi, Navi Mumbai", totalPurchases = 134000.0, outstandingBalance = 18500.0)
        )
        db.accountingDao().insertCustomers(customers)

        // 4. Products
        val products = listOf(
            ProductEntity(
                id = 1, sku = "SV-ELEC-001", barcode = "8901001000101",
                name = "Quantum 4K Smart TV 55\"", category = "Electronics", subcategory = "Smart Displays", brand = "QuantumVision",
                unit = "pcs", purchasePrice = 31500.0, sellingPrice = 43999.0, marketPrice = 44500.0, previousSellingPrice = 42999.0,
                minStockLevel = 10, maxStockLevel = 100, supplierName = "Apex Electro Tech Ltd", defaultLocationId = 1,
                lastPurchaseDate = now - (3 * dayMillis), lastSaleDate = now - (1 * dayMillis)
            ),
            ProductEntity(
                id = 2, sku = "SV-ELEC-002", barcode = "8901001000202",
                name = "AeroBook Pro M2 16GB", category = "Electronics", subcategory = "Laptops", brand = "AeroTech",
                unit = "pcs", purchasePrice = 64000.0, sellingPrice = 79999.0, marketPrice = 81500.0, previousSellingPrice = 78500.0,
                minStockLevel = 5, maxStockLevel = 50, supplierName = "Apex Electro Tech Ltd", defaultLocationId = 1,
                lastPurchaseDate = now - (5 * dayMillis), lastSaleDate = now
            ),
            ProductEntity(
                id = 3, sku = "SV-ACC-003", barcode = "8901001000303",
                name = "Apex Gaming Wireless Mouse", category = "Electronics", subcategory = "Peripherals", brand = "ApexPro",
                unit = "pcs", purchasePrice = 850.0, sellingPrice = 1499.0, marketPrice = 1599.0, previousSellingPrice = 1599.0,
                minStockLevel = 25, maxStockLevel = 300, supplierName = "Apex Electro Tech Ltd", defaultLocationId = 3,
                lastPurchaseDate = now - (8 * dayMillis), lastSaleDate = now
            ),
            ProductEntity(
                id = 4, sku = "SV-FMCG-004", barcode = "8902002000404",
                name = "Basmati Royal Premium Rice 25kg", category = "FMCG", subcategory = "Grains & Rice", brand = "RoyalHeritage",
                unit = "bags", purchasePrice = 1650.0, sellingPrice = 2250.0, marketPrice = 2300.0, previousSellingPrice = 2199.0,
                minStockLevel = 30, maxStockLevel = 400, supplierName = "Shree Agro Foods Corp", defaultLocationId = 1,
                lastPurchaseDate = now - (4 * dayMillis), lastSaleDate = now
            ),
            ProductEntity(
                id = 5, sku = "SV-FMCG-005", barcode = "8902002000505",
                name = "Cold-Pressed Mustard Oil 15L Tin", category = "FMCG", subcategory = "Edible Oils", brand = "PurityGold",
                unit = "tins", purchasePrice = 2100.0, sellingPrice = 2650.0, marketPrice = 2600.0, previousSellingPrice = 2700.0,
                minStockLevel = 20, maxStockLevel = 200, supplierName = "Shree Agro Foods Corp", defaultLocationId = 1,
                lastPurchaseDate = now - (6 * dayMillis), lastSaleDate = now - (1 * dayMillis)
            ),
            ProductEntity(
                id = 6, sku = "SV-TOOL-006", barcode = "8903003000606",
                name = "Industrial Cordless Impact Drill", category = "Hardware", subcategory = "Power Tools", brand = "TitanMax",
                unit = "pcs", purchasePrice = 3800.0, sellingPrice = 5499.0, marketPrice = 5600.0, previousSellingPrice = 5299.0,
                minStockLevel = 8, maxStockLevel = 100, supplierName = "Titan Tool & Hardware Co", defaultLocationId = 2,
                lastPurchaseDate = now - (10 * dayMillis), lastSaleDate = now
            ),
            ProductEntity(
                id = 7, sku = "SV-TOOL-007", barcode = "8903003000707",
                name = "Heavy Duty Brass Ball Valve 1\"", category = "Hardware", subcategory = "Plumbing", brand = "TitanMax",
                unit = "boxes", purchasePrice = 420.0, sellingPrice = 680.0, marketPrice = 690.0, previousSellingPrice = 650.0,
                minStockLevel = 15, maxStockLevel = 250, supplierName = "Titan Tool & Hardware Co", defaultLocationId = 2,
                lastPurchaseDate = now - (12 * dayMillis), lastSaleDate = now - (2 * dayMillis)
            ),
            ProductEntity(
                id = 8, sku = "SV-TEX-008", barcode = "8904004000808",
                name = "Cotton Linen Slim Fit Shirts (Box of 10)", category = "Textiles", subcategory = "Apparel", brand = "SuratClassic",
                unit = "boxes", purchasePrice = 4500.0, sellingPrice = 7200.0, marketPrice = 7500.0, previousSellingPrice = 6999.0,
                minStockLevel = 12, maxStockLevel = 150, supplierName = "Surat Loomcraft Textiles", defaultLocationId = 3,
                lastPurchaseDate = now - (7 * dayMillis), lastSaleDate = now - (1 * dayMillis)
            ),
            ProductEntity(
                id = 9, sku = "SV-POW-009", barcode = "8905005000909",
                name = "Lithium Pro Inverter Battery 150Ah", category = "Hardware", subcategory = "Power Systems", brand = "VoltGuard",
                unit = "pcs", purchasePrice = 11800.0, sellingPrice = 15800.0, marketPrice = 16200.0, previousSellingPrice = 15500.0,
                minStockLevel = 6, maxStockLevel = 60, supplierName = "Apex Electro Tech Ltd", defaultLocationId = 1,
                lastPurchaseDate = now - (9 * dayMillis), lastSaleDate = now
            ),
            ProductEntity(
                id = 10, sku = "SV-SEC-010", barcode = "8906006001010",
                name = "UltraHD Security Dome Camera 4MP", category = "Electronics", subcategory = "Security", brand = "EyeVision",
                unit = "pcs", purchasePrice = 1650.0, sellingPrice = 2499.0, marketPrice = 2550.0, previousSellingPrice = 2599.0,
                minStockLevel = 15, maxStockLevel = 200, supplierName = "Apex Electro Tech Ltd", defaultLocationId = 3,
                lastPurchaseDate = now - (11 * dayMillis), lastSaleDate = now
            )
        )
        db.productDao().insertProducts(products)

        // 5. Stock Balances Across Godowns, Warehouses, and Retail Shop
        val stocks = listOf(
            // Quantum TV
            LocationStockEntity(productId = 1, locationId = 1, availableQty = 34, reservedQty = 2, damagedQty = 0),
            LocationStockEntity(productId = 1, locationId = 2, availableQty = 15, reservedQty = 0, damagedQty = 1),
            LocationStockEntity(productId = 1, locationId = 3, availableQty = 4, reservedQty = 1, damagedQty = 0),

            // AeroBook
            LocationStockEntity(productId = 2, locationId = 1, availableQty = 18, reservedQty = 3, damagedQty = 0),
            LocationStockEntity(productId = 2, locationId = 3, availableQty = 5, reservedQty = 1, damagedQty = 0),

            // Mouse
            LocationStockEntity(productId = 3, locationId = 1, availableQty = 120, reservedQty = 10, damagedQty = 2),
            LocationStockEntity(productId = 3, locationId = 3, availableQty = 45, reservedQty = 0, damagedQty = 0),

            // Rice (Low stock in shop!)
            LocationStockEntity(productId = 4, locationId = 1, availableQty = 160, reservedQty = 20, damagedQty = 0),
            LocationStockEntity(productId = 4, locationId = 3, availableQty = 8, reservedQty = 0, damagedQty = 0),

            // Mustard Oil
            LocationStockEntity(productId = 5, locationId = 1, availableQty = 75, reservedQty = 5, damagedQty = 0),
            LocationStockEntity(productId = 5, locationId = 3, availableQty = 14, reservedQty = 0, damagedQty = 0),

            // Impact Drill (Low stock!)
            LocationStockEntity(productId = 6, locationId = 2, availableQty = 6, reservedQty = 1, damagedQty = 0),
            LocationStockEntity(productId = 6, locationId = 3, availableQty = 3, reservedQty = 0, damagedQty = 0),

            // Brass Valve
            LocationStockEntity(productId = 7, locationId = 2, availableQty = 85, reservedQty = 0, damagedQty = 0),
            LocationStockEntity(productId = 7, locationId = 3, availableQty = 22, reservedQty = 0, damagedQty = 0),

            // Shirts
            LocationStockEntity(productId = 8, locationId = 1, availableQty = 35, reservedQty = 5, damagedQty = 0),
            LocationStockEntity(productId = 8, locationId = 3, availableQty = 12, reservedQty = 0, damagedQty = 0),

            // Battery (Low stock alert!)
            LocationStockEntity(productId = 9, locationId = 1, availableQty = 4, reservedQty = 0, damagedQty = 0),
            LocationStockEntity(productId = 9, locationId = 3, availableQty = 1, reservedQty = 0, damagedQty = 0),

            // Camera
            LocationStockEntity(productId = 10, locationId = 1, availableQty = 55, reservedQty = 0, damagedQty = 0),
            LocationStockEntity(productId = 10, locationId = 3, availableQty = 18, reservedQty = 2, damagedQty = 0)
        )
        db.inventoryDao().insertStocks(stocks)

        // 6. Price History for Demat Charts
        val priceHistories = listOf(
            PriceHistoryEntity(productId = 1, timestamp = now - (60 * dayMillis), oldPrice = 39999.0, newPrice = 41500.0, changeReason = "New Year Revision"),
            PriceHistoryEntity(productId = 1, timestamp = now - (30 * dayMillis), oldPrice = 41500.0, newPrice = 42999.0, changeReason = "Supply Chain Cost Increase"),
            PriceHistoryEntity(productId = 1, timestamp = now - (2 * dayMillis), oldPrice = 42999.0, newPrice = 43999.0, changeReason = "Market Demand Surge"),

            PriceHistoryEntity(productId = 2, timestamp = now - (45 * dayMillis), oldPrice = 74999.0, newPrice = 78500.0, changeReason = "Component Cost Bump"),
            PriceHistoryEntity(productId = 2, timestamp = now - (7 * dayMillis), oldPrice = 78500.0, newPrice = 79999.0, changeReason = "Target Margin Upgrade"),

            PriceHistoryEntity(productId = 4, timestamp = now - (40 * dayMillis), oldPrice = 2050.0, newPrice = 2199.0, changeReason = "Mandi Price Revision"),
            PriceHistoryEntity(productId = 4, timestamp = now - (4 * dayMillis), oldPrice = 2199.0, newPrice = 2250.0, changeReason = "Wholesale Index Adjustment"),

            PriceHistoryEntity(productId = 6, timestamp = now - (50 * dayMillis), oldPrice = 4999.0, newPrice = 5299.0, changeReason = "Import Duty Adjustment"),
            PriceHistoryEntity(productId = 6, timestamp = now - (10 * dayMillis), oldPrice = 5299.0, newPrice = 5499.0, changeReason = "Manufacturer List Update")
        )
        for (ph in priceHistories) {
            db.productDao().insertPriceHistory(ph)
        }

        // 7. Recent Purchases
        val purchases = listOf(
            PurchaseEntity(
                id = 1, invoiceNumber = "PUR-2026-089", supplierId = 1, supplierName = "Apex Electro Tech Ltd",
                date = now - (3 * dayMillis), totalAmount = 378000.0, discount = 5000.0, taxAmount = 61200.0,
                shippingCost = 4500.0, paymentStatus = "PAID", amountPaid = 378000.0, paymentMethod = "BANK", notes = "Regular batch delivery"
            ),
            PurchaseEntity(
                id = 2, invoiceNumber = "PUR-2026-092", supplierId = 2, supplierName = "Shree Agro Foods Corp",
                date = now - (5 * dayMillis), totalAmount = 145000.0, discount = 2000.0, taxAmount = 7250.0,
                shippingCost = 3000.0, paymentStatus = "PAID", amountPaid = 145000.0, paymentMethod = "UPI", notes = "Rice and oil restock"
            ),
            PurchaseEntity(
                id = 3, invoiceNumber = "PUR-2026-098", supplierId = 3, supplierName = "Titan Tool & Hardware Co",
                date = now - (10 * dayMillis), totalAmount = 88000.0, discount = 1500.0, taxAmount = 15840.0,
                shippingCost = 1800.0, paymentStatus = "PARTIAL", amountPaid = 76000.0, paymentMethod = "BANK", notes = "Balance 12k payable in 15 days"
            )
        )
        db.transactionDao().insertPurchases(purchases)

        // 8. Recent Sales & Items (with Weighted COGS & Gross Profit calculated)
        val sales = listOf(
            SaleEntity(
                id = 1, invoiceNumber = "INV-2026-1042", customerId = 1, customerName = "Metro Superstore",
                date = now - (1 * dayMillis), totalAmount = 131997.0, discountAmount = 3997.0, taxAmount = 23040.0,
                netAmount = 151040.0, cogsAmount = 94500.0, grossProfit = 33500.0, paymentMethod = "BANK",
                paymentStatus = "PARTIAL", amountPaid = 127040.0, locationId = 1, notes = "B2B commercial order"
            ),
            SaleEntity(
                id = 2, invoiceNumber = "INV-2026-1043", customerId = 3, customerName = "Walk-in Retail Cash",
                date = now - (4 * 3600_000L), totalAmount = 81498.0, discountAmount = 1498.0, taxAmount = 14400.0,
                netAmount = 94400.0, cogsAmount = 64850.0, grossProfit = 15150.0, paymentMethod = "UPI",
                paymentStatus = "PAID", amountPaid = 94400.0, locationId = 3, notes = "Laptop + Gaming Mouse counter sale"
            ),
            SaleEntity(
                id = 3, invoiceNumber = "INV-2026-1044", customerId = 2, customerName = "Omkar Enterprises",
                date = now - (2 * 3600_000L), totalAmount = 45000.0, discountAmount = 1000.0, taxAmount = 2200.0,
                netAmount = 46200.0, cogsAmount = 33000.0, grossProfit = 11000.0, paymentMethod = "CASH",
                paymentStatus = "PAID", amountPaid = 46200.0, locationId = 3, notes = "20 bags Basmati rice"
            ),
            SaleEntity(
                id = 4, invoiceNumber = "INV-2026-1045", customerId = 4, customerName = "Apex Building Contractors",
                date = now - (1 * 3600_000L), totalAmount = 32994.0, discountAmount = 994.0, taxAmount = 5760.0,
                netAmount = 37760.0, cogsAmount = 22800.0, grossProfit = 9200.0, paymentMethod = "CARD",
                paymentStatus = "PAID", amountPaid = 37760.0, locationId = 2, notes = "6 cordless impact drills"
            )
        )
        db.transactionDao().insertSales(sales)

        val saleItems = listOf(
            SaleItemEntity(saleId = 1, productId = 1, productName = "Quantum 4K Smart TV 55\"", quantity = 3, unitSellingPrice = 43999.0, unitPurchaseCost = 31500.0, lineTotal = 131997.0, lineCogs = 94500.0, lineProfit = 37497.0),
            SaleItemEntity(saleId = 2, productId = 2, productName = "AeroBook Pro M2 16GB", quantity = 1, unitSellingPrice = 79999.0, unitPurchaseCost = 64000.0, lineTotal = 79999.0, lineCogs = 64000.0, lineProfit = 15999.0),
            SaleItemEntity(saleId = 2, productId = 3, productName = "Apex Gaming Wireless Mouse", quantity = 1, unitSellingPrice = 1499.0, unitPurchaseCost = 850.0, lineTotal = 1499.0, lineCogs = 850.0, lineProfit = 649.0),
            SaleItemEntity(saleId = 3, productId = 4, productName = "Basmati Royal Premium Rice 25kg", quantity = 20, unitSellingPrice = 2250.0, unitPurchaseCost = 1650.0, lineTotal = 45000.0, lineCogs = 33000.0, lineProfit = 12000.0),
            SaleItemEntity(saleId = 4, productId = 6, productName = "Industrial Cordless Impact Drill", quantity = 6, unitSellingPrice = 5499.0, unitPurchaseCost = 3800.0, lineTotal = 32994.0, lineCogs = 22800.0, lineProfit = 10194.0)
        )
        db.transactionDao().insertSaleItems(saleItems)

        // 9. Stock Transfers (No profit distortion)
        val transfers = listOf(
            StockTransferEntity(
                id = 1, transferNumber = "TRF-2026-021", timestamp = now - (2 * dayMillis),
                sourceLocationId = 1, sourceLocationName = "Main Godown (Bhiwandi)",
                destLocationId = 3, destLocationName = "Retail Shop (MG Road)",
                productId = 1, productName = "Quantum 4K Smart TV 55\"", quantity = 4,
                responsibleStaff = "Ramesh Kumar (Logistics)", status = "COMPLETED", notes = "Replenished retail floor demo and stock"
            ),
            StockTransferEntity(
                id = 2, transferNumber = "TRF-2026-022", timestamp = now - (1 * dayMillis),
                sourceLocationId = 1, sourceLocationName = "Main Godown (Bhiwandi)",
                destLocationId = 3, destLocationName = "Retail Shop (MG Road)",
                productId = 3, productName = "Apex Gaming Wireless Mouse", quantity = 30,
                responsibleStaff = "Ramesh Kumar (Logistics)", status = "COMPLETED", notes = "Weekend retail display stock"
            ),
            StockTransferEntity(
                id = 3, transferNumber = "TRF-2026-023", timestamp = now - (5 * 3600_000L),
                sourceLocationId = 2, sourceLocationName = "Secondary Warehouse (Panvel)",
                destLocationId = 3, destLocationName = "Retail Shop (MG Road)",
                productId = 6, productName = "Industrial Cordless Impact Drill", quantity = 3,
                responsibleStaff = "Amit Shah (Fleet)", status = "COMPLETED", notes = "Customer pickup booking"
            )
        )
        for (trf in transfers) {
            db.inventoryDao().insertStockTransfer(trf)
        }

        // 10. Expenses
        val expenses = listOf(
            ExpenseEntity(id = 1, date = now - (15 * dayMillis), category = "Rent", amount = 65000.0, paymentMethod = "BANK", recipient = "Bhiwandi Park Realty", notes = "Monthly godown lease"),
            ExpenseEntity(id = 2, date = now - (14 * dayMillis), category = "Salaries", amount = 110000.0, paymentMethod = "BANK", recipient = "Staff Payroll (8 Employees)", notes = "Warehouse and retail staff"),
            ExpenseEntity(id = 3, date = now - (5 * dayMillis), category = "Electricity", amount = 14500.0, paymentMethod = "UPI", recipient = "MSEDCL Power", notes = "Godown & office aircon/lighting"),
            ExpenseEntity(id = 4, date = now - (3 * dayMillis), category = "Transportation", amount = 9800.0, paymentMethod = "UPI", recipient = "SpeedCargo Logistics", notes = "Inter-warehouse transit tempo"),
            ExpenseEntity(id = 5, date = now - (2 * dayMillis), category = "Packaging", amount = 6200.0, paymentMethod = "CASH", recipient = "National Box Co", notes = "Heavy corrugated boxes & bubble wrap"),
            ExpenseEntity(id = 6, date = now - (1 * dayMillis), category = "Marketing", amount = 8500.0, paymentMethod = "UPI", recipient = "Meta Ads / Google Ads", notes = "Local trade campaign")
        )
        db.accountingDao().insertExpenses(expenses)

        // 11. Stock Movement Log
        val movements = listOf(
            StockMovementEntity(id = 1, timestamp = now - (3 * dayMillis), productId = 1, productName = "Quantum 4K Smart TV 55\"", type = "PURCHASE", destLocationId = 1, quantity = 40, unitCost = 31500.0, referenceId = "PUR-2026-089", employeeName = "Ramesh Kumar", notes = "Goods received in Godown"),
            StockMovementEntity(id = 2, timestamp = now - (2 * dayMillis), productId = 1, productName = "Quantum 4K Smart TV 55\"", type = "TRANSFER_OUT", sourceLocationId = 1, destLocationId = 3, quantity = 4, unitCost = 31500.0, referenceId = "TRF-2026-021", employeeName = "Ramesh Kumar", notes = "Dispatched to Retail Shop"),
            StockMovementEntity(id = 3, timestamp = now - (2 * dayMillis), productId = 1, productName = "Quantum 4K Smart TV 55\"", type = "TRANSFER_IN", sourceLocationId = 1, destLocationId = 3, quantity = 4, unitCost = 31500.0, referenceId = "TRF-2026-021", employeeName = "Ramesh Kumar", notes = "Received at Retail Shop"),
            StockMovementEntity(id = 4, timestamp = now - (1 * dayMillis), productId = 1, productName = "Quantum 4K Smart TV 55\"", type = "SALE", sourceLocationId = 1, quantity = 3, unitCost = 31500.0, referenceId = "INV-2026-1042", employeeName = "Sunil Verma", notes = "Shipped to Metro Superstore")
        )
        db.inventoryDao().insertStockMovements(movements)

        // 12. Audit Logs
        val auditLogs = listOf(
            AuditLogEntity(id = 1, timestamp = now - (3 * dayMillis), userRole = "SUPER_ADMIN", actionType = "SYSTEM_INITIALIZATION", description = "StockVision Business database initialized with 4 locations & master SKUs"),
            AuditLogEntity(id = 2, timestamp = now - (3 * dayMillis), userRole = "MANAGER", actionType = "PURCHASE_CONFIRMED", description = "Purchase invoice PUR-2026-089 recorded from Apex Electro Tech Ltd"),
            AuditLogEntity(id = 3, timestamp = now - (2 * dayMillis), userRole = "WAREHOUSE_STAFF", actionType = "STOCK_TRANSFER", description = "Stock transfer TRF-2026-021 executed from Bhiwandi Godown to MG Road Shop"),
            AuditLogEntity(id = 4, timestamp = now - (1 * dayMillis), userRole = "SALESPERSON", actionType = "SALE_COMPLETED", description = "Invoice INV-2026-1042 generated for Metro Superstore (₹1,51,040)")
        )
        db.accountingDao().insertAuditLogs(auditLogs)
    }
}
