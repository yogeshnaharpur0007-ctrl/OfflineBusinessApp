package com.antigravity.businessapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(
    entities = [
        Party::class,
        Item::class,
        Transaction::class,
        TransactionItem::class,
        User::class,
        AuditLog::class,
        StockTx::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun partyDao(): PartyDao
    abstract fun itemDao(): ItemDao
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun auditDao(): AuditDao
    abstract fun stockTxDao(): StockTxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val databaseWriteExecutor: ExecutorService = Executors.newFixedThreadPool(4)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "offline_business_db"
                )
                .fallbackToDestructiveMigration() // Dev only: Wipes data on schema change
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

