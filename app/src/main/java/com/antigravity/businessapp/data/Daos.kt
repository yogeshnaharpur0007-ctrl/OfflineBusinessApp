package com.antigravity.businessapp.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction as RoomTransaction

// ================== PARTY DAO ==================
@Dao
interface PartyDao {

    @Query("SELECT * FROM parties ORDER BY name ASC")
    fun getAllParties(): LiveData<List<Party>>

    @Query("SELECT * FROM parties WHERE id = :id")
    suspend fun getPartyById(id: Long): Party?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParty(party: Party): Long

    @Update
    suspend fun updateParty(party: Party)

    @Query("SELECT * FROM parties")
    suspend fun getAllPartiesList(): List<Party>

    @Query("DELETE FROM parties")
    suspend fun deleteAll()

    // Single party delete (Repositories.kt ke deleteParty call ke liye)
    @Delete
    suspend fun deleteParty(party: Party)
}

// ================== ITEM DAO ==================
@Dao
interface ItemDao {

    @Query("SELECT * FROM items ORDER BY name ASC")
    fun getAllItems(): LiveData<List<Item>>

    @Query("SELECT * FROM items WHERE stockQuantity <= lowStockLimit")
    fun getLowStockItems(): LiveData<List<Item>>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getItemById(id: Long): Item?

    @Query("SELECT * FROM items")
    suspend fun getAllItemsList(): List<Item>

    @Query("DELETE FROM items")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: Item): Long

    @Update
    suspend fun updateItem(item: Item)

    @Query("UPDATE items SET stockQuantity = stockQuantity + :change WHERE id = :itemId")
    suspend fun updateStock(itemId: Long, change: Int)
}

// ================== TRANSACTION DAO ==================
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>)

    @Query("SELECT * FROM transactions WHERE partyId = :partyId ORDER BY timestamp DESC")
    fun getCreateTransactionsForParty(partyId: Long): LiveData<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): LiveData<List<Transaction>>
    
    // For Reporting/Backup – totalAmount ka SUM
    @Query(
        "SELECT SUM(totalAmount) FROM transactions " +
        "WHERE type = 'SALE' AND timestamp >= :startTime AND timestamp <= :endTime"
    )
    fun getSalesTotal(startTime: Long, endTime: Long): LiveData<Double?>

    @Query(
        "SELECT SUM(totalAmount) FROM transactions " +
        "WHERE type = 'PURCHASE' AND timestamp >= :startTime AND timestamp <= :endTime"
    )
    fun getPurchasesTotal(startTime: Long, endTime: Long): LiveData<Double?>

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactionsList(): List<Transaction>

    @Query("SELECT * FROM transaction_items")
    suspend fun getAllTransactionItemsList(): List<TransactionItem>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
    
    @Query("DELETE FROM transaction_items")
    suspend fun deleteAllItems()

    // Ledger ke liye; annotation ko alias (RoomTransaction) se use kiya hai
    @RoomTransaction
    @Query("SELECT * FROM transactions WHERE partyId = :partyId")
    suspend fun getPartyLedger(partyId: Long): List<Transaction>
}

