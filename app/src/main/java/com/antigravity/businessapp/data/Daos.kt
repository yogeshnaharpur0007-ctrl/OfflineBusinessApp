package com.antigravity.businessapp.data

import androidx.lifecycle.LiveData
import androix.room.Transaction
import androidx.room.*

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
    
    // For Reporting/Backup
    // Yahan SUM(totalAmount) use kiya hai taki Room easily Double? return kar sake
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

    @Transaction
    @Query("SELECT * FROM transactions WHERE partyId = :partyId")
    suspend fun getPartyLedger(partyId: Long): List<Transaction>
}
