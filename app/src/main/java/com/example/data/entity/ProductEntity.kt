package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index(value = ["sku"], unique = true), Index(value = ["barcode"])]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sku: String,
    val barcode: String,
    val name: String,
    val category: String,
    val subcategory: String = "",
    val brand: String = "",
    val unit: String = "pcs",
    val purchasePrice: Double, // Weighted average cost per unit
    val sellingPrice: Double,
    val marketPrice: Double, // Market reference price
    val previousSellingPrice: Double = sellingPrice,
    val minStockLevel: Int = 10,
    val maxStockLevel: Int = 500,
    val supplierName: String = "",
    val defaultLocationId: Long = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val lastPurchaseDate: Long = 0,
    val lastSaleDate: Long = 0,
    val isArchived: Boolean = false
) {
    val priceChangeCurrency: Double
        get() = sellingPrice - previousSellingPrice

    val priceChangePercent: Double
        get() = if (previousSellingPrice > 0) ((sellingPrice - previousSellingPrice) / previousSellingPrice) * 100.0 else 0.0

    val unitProfitMargin: Double
        get() = if (sellingPrice > 0) ((sellingPrice - purchasePrice) / sellingPrice) * 100.0 else 0.0

    val unitGrossProfit: Double
        get() = sellingPrice - purchasePrice
}
