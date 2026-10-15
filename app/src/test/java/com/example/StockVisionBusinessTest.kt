package com.example

import com.example.data.entity.*
import org.junit.Assert.*
import org.junit.Test

class StockVisionBusinessTest {

    @Test
    fun testProductMarginAndGrossProfitCalculations() {
        val product = ProductEntity(
            id = 1,
            sku = "SV-TEST-001",
            barcode = "8901234567890",
            name = "Test 4K Monitor",
            category = "Electronics",
            unit = "pcs",
            purchasePrice = 10000.0,
            sellingPrice = 15000.0,
            marketPrice = 15500.0,
            previousSellingPrice = 14000.0
        )

        // Price changes
        assertEquals(1000.0, product.priceChangeCurrency, 0.01)
        val expectedPercent = ((15000.0 - 14000.0) / 14000.0) * 100.0
        assertEquals(expectedPercent, product.priceChangePercent, 0.01)

        // Profit margins
        assertEquals(5000.0, product.unitGrossProfit, 0.01)
        val expectedMargin = (5000.0 / 15000.0) * 100.0
        assertEquals(expectedMargin, product.unitProfitMargin, 0.01)
    }

    @Test
    fun testWeightedAverageInventoryCostingFormula() {
        // Initial inventory: 10 units at ₹500 = ₹5,000
        val currentUnits = 10
        val currentAvgCost = 500.0

        // Inward purchase: 20 units at ₹650 = ₹13,000
        val newUnits = 20
        val newBatchCost = 650.0

        // Weighted Average Cost = (10*500 + 20*650) / (10 + 20) = (5000 + 13000) / 30 = 18000 / 30 = ₹600
        val totalUnits = currentUnits + newUnits
        val calculatedWeightedCost = ((currentUnits * currentAvgCost) + (newUnits * newBatchCost)) / totalUnits

        assertEquals(600.0, calculatedWeightedCost, 0.01)
    }

    @Test
    fun testCogsAndGrossProfitOnSale() {
        // Selling 5 units at ₹1,000 each with weighted cost of ₹600
        val soldQty = 5
        val unitSellingPrice = 1000.0
        val unitWeightedCost = 600.0
        val discount = 200.0 // Discount applied to total bill

        val grossRevenue = soldQty * unitSellingPrice // 5,000
        val netRevenue = grossRevenue - discount // 4,800
        val cogs = soldQty * unitWeightedCost // 3,000
        val grossProfit = netRevenue - cogs // 1,800

        assertEquals(5000.0, grossRevenue, 0.01)
        assertEquals(4800.0, netRevenue, 0.01)
        assertEquals(3000.0, cogs, 0.01)
        assertEquals(1800.0, grossProfit, 0.01)
    }

    @Test
    fun testNetOperatingProfitAfterExpenses() {
        val grossProfit = 50000.0
        val expenses = listOf(
            ExpenseEntity(date = 1000L, category = "Rent", amount = 15000.0, recipient = "Landlord"),
            ExpenseEntity(date = 2000L, category = "Electricity", amount = 3500.0, recipient = "Power Co"),
            ExpenseEntity(date = 3000L, category = "Salaries", amount = 18000.0, recipient = "Staff")
        )

        val totalExpenses = expenses.sumOf { it.amount } // 36,500
        val netOperatingProfit = grossProfit - totalExpenses // 13,500

        assertEquals(36500.0, totalExpenses, 0.01)
        assertEquals(13500.0, netOperatingProfit, 0.01)
    }
}
