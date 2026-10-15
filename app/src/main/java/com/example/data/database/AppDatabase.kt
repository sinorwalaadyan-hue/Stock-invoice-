package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AccountingDao
import com.example.data.dao.InventoryDao
import com.example.data.dao.ProductDao
import com.example.data.dao.TransactionDao
import com.example.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        LocationEntity::class,
        LocationStockEntity::class,
        PriceHistoryEntity::class,
        PurchaseEntity::class,
        PurchaseItemEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockMovementEntity::class,
        StockTransferEntity::class,
        ExpenseEntity::class,
        CustomerEntity::class,
        SupplierEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun accountingDao(): AccountingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockvision_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        DatabaseSeeder.seedDatabase(database)
                    }
                }
            }
        }
    }
}
