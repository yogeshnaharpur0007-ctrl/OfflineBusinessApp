package com.antigravity.businessapp.data

import androidx.lifecycle.LiveData

class PartyRepository(private val partyDao: PartyDao) {
    val allParties: LiveData<List<Party>> = partyDao.getAllParties()

    suspend fun insert(party: Party) = partyDao.insertParty(party)
    suspend fun update(party: Party) = partyDao.updateParty(party)
    suspend fun delete(party: Party) = partyDao.deleteParty(party)
    suspend fun getPartyById(id: Long) = partyDao.getPartyById(id)
}

class StockRepository(private val itemDao: ItemDao) {
    val allItems: LiveData<List<Item>> = itemDao.getAllItems()
    val lowStockItems: LiveData<List<Item>> = itemDao.getLowStockItems()

    suspend fun insert(item: Item) = itemDao.insertItem(item)
    suspend fun update(item: Item) = itemDao.updateItem(item)
    suspend fun updateStock(itemId: Long, change: Int) = itemDao.updateStock(itemId, change)
    suspend fun getItemById(id: Long) = itemDao.getItemById(id)
}

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun insertTransaction(transaction: Transaction, items: List<TransactionItem>) {
        // This should transactionally ideally, but for now simple
        val transactionId = transactionDao.insertTransaction(transaction)
        val itemsWithId = items.map { it.copy(transactionId = transactionId) }
        transactionDao.insertTransactionItems(itemsWithId)
    }
    
    fun getTransactionsForParty(partyId: Long) = transactionDao.getCreateTransactionsForParty(partyId)
    
    fun getSalesTotal(start: Long, end: Long) = transactionDao.getSalesTotal(start, end)
    fun getPurchasesTotal(start: Long, end: Long) = transactionDao.getPurchasesTotal(start, end)
}
