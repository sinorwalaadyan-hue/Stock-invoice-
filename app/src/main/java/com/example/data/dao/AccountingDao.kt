package com.example.data.dao

import androidx.room.*
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.CustomerEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.SupplierEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountingDao {
    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpensesFlow(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<ExpenseEntity>): List<Long>

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomersFlow(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>): List<Long>

    @Query("UPDATE customers SET totalPurchases = totalPurchases + :addedPurchases, outstandingBalance = outstandingBalance + :addedBalance WHERE id = :customerId")
    suspend fun updateCustomerBalance(customerId: Long, addedPurchases: Double, addedBalance: Double)

    // Suppliers
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliersFlow(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<SupplierEntity>): List<Long>

    @Query("UPDATE suppliers SET totalPurchases = totalPurchases + :addedPurchases, outstandingBalance = outstandingBalance + :addedBalance WHERE id = :supplierId")
    suspend fun updateSupplierBalance(supplierId: Long, addedPurchases: Double, addedBalance: Double)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAllAuditLogsFlow(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLogEntity>): List<Long>
}
