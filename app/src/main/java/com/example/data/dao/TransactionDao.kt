package com.example.data.dao

import androidx.room.*
import com.example.data.entity.PurchaseEntity
import com.example.data.entity.PurchaseItemEntity
import com.example.data.entity.SaleEntity
import com.example.data.entity.SaleItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    // Purchases
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchasesFlow(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<PurchaseEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>): List<Long>

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItems(purchaseId: Long): List<PurchaseItemEntity>

    @Query("SELECT * FROM purchase_items")
    fun getAllPurchaseItemsFlow(): Flow<List<PurchaseItemEntity>>

    // Sales
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSalesFlow(): Flow<List<SaleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItemEntity>): List<Long>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Long): List<SaleItemEntity>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItemsFlow(): Flow<List<SaleItemEntity>>

    @Query("UPDATE sales SET isReturned = 1 WHERE id = :saleId")
    suspend fun markSaleReturned(saleId: Long)
}
