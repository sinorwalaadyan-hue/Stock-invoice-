package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // "GODOWN", "WAREHOUSE", "RETAIL_SHOP", "OFFICE"
    val address: String,
    val isDefault: Boolean = false
)

@Entity(
    tableName = "location_stocks",
    indices = [Index(value = ["productId", "locationId"], unique = true)]
)
data class LocationStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val locationId: Long,
    val availableQty: Int = 0,
    val reservedQty: Int = 0,
    val damagedQty: Int = 0
)

@Entity(tableName = "price_history")
data class PriceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val timestamp: Long,
    val oldPrice: Double,
    val newPrice: Double,
    val changeReason: String
)
