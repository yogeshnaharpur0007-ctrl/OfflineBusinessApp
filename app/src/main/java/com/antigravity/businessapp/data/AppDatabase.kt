package com.antigravity.businessapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Database(entities = [Party::class, Item::class, Transaction::class, TransactionItem::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun partyDao(): PartyDao
    abstract fun itemDao(): ItemDao
    abstract fun transactionDao(): TransactionDao

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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
