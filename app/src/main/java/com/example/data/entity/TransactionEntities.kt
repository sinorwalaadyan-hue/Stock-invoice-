package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val supplierId: Long,
    val supplierName: String,
    val date: Long,
    val totalAmount: Double,
    val discount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val shippingCost: Double = 0.0,
    val paymentStatus: String = "PAID", // PAID, PARTIAL, UNPAID
    val amountPaid: Double = totalAmount,
    val paymentMethod: String = "BANK", // BANK, UPI, CASH, CREDIT
    val notes: String = ""
)

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val locationId: Long
)

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long,
    val customerName: String,
    val date: Long,
    val totalAmount: Double,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val netAmount: Double, // total - discount + tax
    val cogsAmount: Double, // Cost of Goods Sold
    val grossProfit: Double, // netAmount - cogsAmount
    val paymentMethod: String = "CASH", // CASH, UPI, CARD, CREDIT
    val paymentStatus: String = "PAID", // PAID, PARTIAL, UNPAID
    val amountPaid: Double = netAmount,
    val locationId: Long,
    val notes: String = "",
    val isReturned: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitSellingPrice: Double,
    val unitPurchaseCost: Double,
    val lineTotal: Double,
    val lineCogs: Double,
    val lineProfit: Double
)
