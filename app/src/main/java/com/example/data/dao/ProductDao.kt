package com.example.data.dao

import androidx.room.*
import com.example.data.entity.PriceHistoryEntity
import com.example.data.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isArchived = 0 ORDER BY name ASC")
    fun getActiveProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND isArchived = 0 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>): List<Long>

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET isArchived = 1 WHERE id = :productId")
    suspend fun archiveProduct(productId: Long)

    @Query("UPDATE products SET isArchived = 0 WHERE id = :productId")
    suspend fun unarchiveProduct(productId: Long)

    @Query("""
        SELECT * FROM products 
        WHERE (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%')
        AND isArchived = 0
        ORDER BY name ASC
    """)
    fun searchProductsFlow(query: String): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPriceHistory(history: PriceHistoryEntity): Long

    @Query("SELECT * FROM price_history WHERE productId = :productId ORDER BY timestamp DESC")
    fun getPriceHistoryFlow(productId: Long): Flow<List<PriceHistoryEntity>>

    @Query("SELECT * FROM price_history ORDER BY timestamp DESC LIMIT 100")
    fun getAllPriceHistoryFlow(): Flow<List<PriceHistoryEntity>>
}
