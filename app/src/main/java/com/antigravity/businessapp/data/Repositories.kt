package com.antigravity.businessapp.data

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {
    suspend fun getUser(username: String) = userDao.getUserByUsername(username)
    suspend fun hasUsers() = userDao.getUserCount() > 0
    suspend fun registerUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
}

class AuditRepository(private val auditDao: AuditDao) {
    val allLogs: LiveData<List<AuditLog>> = auditDao.getAllLogs()
    suspend fun logAction(action: String, details: String) {
        auditDao.insertLog(AuditLog(action = action, details = details))
    }
}

class PartyRepository(private val partyDao: PartyDao) {
    val allParties: LiveData<List<Party>> = partyDao.getAllParties()

    suspend fun insert(party: Party) = partyDao.insertParty(party)
    suspend fun update(party: Party) = partyDao.updateParty(party)
    suspend fun delete(party: Party) = partyDao.deleteParty(party)
    suspend fun archive(party: Party) = partyDao.archiveParty(party.id)
    suspend fun getPartyById(id: Long) = partyDao.getPartyById(id)
}

class StockRepository(private val itemDao: ItemDao, private val stockTxDao: StockTxDao) {
    val allItems: LiveData<List<Item>> = itemDao.getAllItems()
    val lowStockItems: LiveData<List<Item>> = itemDao.getLowStockItems()

    suspend fun insert(item: Item) = itemDao.insertItem(item)
    suspend fun update(item: Item) = itemDao.updateItem(item)
    suspend fun updateStock(itemId: Long, change: Int, type: String, refId: Long?) {
        itemDao.updateStock(itemId, change)
        stockTxDao.insertStockTx(StockTx(itemId = itemId, quantity = change, type = type, refTransactionId = refId))
    }
    suspend fun getItemById(id: Long) = itemDao.getItemById(id)
    fun getStockHistory(itemId: Long) = stockTxDao.getStockHistory(itemId)
}

class TransactionRepository(private val transactionDao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAllTransactions()
    val recentTransactions: LiveData<List<Transaction>> = transactionDao.getRecentTransactions()

    suspend fun insertTransaction(transaction: Transaction, items: List<TransactionItem>) {
        val transactionId = transactionDao.insertTransaction(transaction)
        val itemsWithId = items.map { it.copy(transactionId = transactionId) }
        transactionDao.insertTransactionItems(itemsWithId)
        transactionDao.insertTransactionItems(itemsWithId)
    }
    
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }
    
    fun getTransactionsForParty(partyId: Long) = transactionDao.getCreateTransactionsForParty(partyId)
    
    fun getSalesTotal(start: Long, end: Long) = transactionDao.getSalesTotal(start, end)
    fun getPurchasesTotal(start: Long, end: Long) = transactionDao.getPurchasesTotal(start, end)
}
