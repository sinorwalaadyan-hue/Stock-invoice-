package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_movements")
data class StockMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val productId: Long,
    val productName: String,
    val type: String, // PURCHASE, SALE, TRANSFER_IN, TRANSFER_OUT, RETURN, DAMAGE, ADJUSTMENT
    val sourceLocationId: Long? = null,
    val destLocationId: Long? = null,
    val quantity: Int,
    val unitCost: Double,
    val referenceId: String,
    val employeeName: String,
    val notes: String = ""
)

@Entity(tableName = "stock_transfers")
data class StockTransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transferNumber: String,
    val timestamp: Long,
    val sourceLocationId: Long,
    val sourceLocationName: String,
    val destLocationId: Long,
    val destLocationName: String,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val responsibleStaff: String,
    val status: String = "COMPLETED", // COMPLETED, IN_TRANSIT
    val notes: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val category: String, // Rent, Salaries, Electricity, Transportation, Packaging, Marketing, Repairs, Other
    val amount: Double,
    val paymentMethod: String = "UPI",
    val recipient: String = "",
    val notes: String = ""
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String = "",
    val address: String = "",
    val totalPurchases: Double = 0.0,
    val outstandingBalance: Double = 0.0
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contactPerson: String = "",
    val phone: String,
    val email: String = "",
    val categoriesSupplied: String = "",
    val totalPurchases: Double = 0.0,
    val outstandingBalance: Double = 0.0
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val userRole: String,
    val actionType: String,
    val description: String
)
