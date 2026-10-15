package com.example.data.dao

import androidx.room.*
import com.example.data.entity.LocationEntity
import com.example.data.entity.LocationStockEntity
import com.example.data.entity.StockMovementEntity
import com.example.data.entity.StockTransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM locations ORDER BY id ASC")
    fun getAllLocationsFlow(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>): List<Long>

    @Query("SELECT * FROM location_stocks")
    fun getAllLocationStocksFlow(): Flow<List<LocationStockEntity>>

    @Query("SELECT * FROM location_stocks WHERE productId = :productId")
    fun getLocationStocksForProduct(productId: Long): Flow<List<LocationStockEntity>>

    @Query("SELECT * FROM location_stocks WHERE productId = :productId AND locationId = :locationId LIMIT 1")
    suspend fun getLocationStock(productId: Long, locationId: Long): LocationStockEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStock(stock: LocationStockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<LocationStockEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovementEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovements(movements: List<StockMovementEntity>): List<Long>

    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT 200")
    fun getStockMovementsFlow(): Flow<List<StockMovementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockTransfer(transfer: StockTransferEntity): Long

    @Query("SELECT * FROM stock_transfers ORDER BY timestamp DESC")
    fun getAllTransfersFlow(): Flow<List<StockTransferEntity>>

    @Query("UPDATE stock_transfers SET status = :status WHERE id = :transferId")
    suspend fun updateTransferStatus(transferId: Long, status: String)
}
