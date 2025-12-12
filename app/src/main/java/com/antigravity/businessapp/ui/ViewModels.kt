package com.antigravity.businessapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antigravity.businessapp.data.*
import kotlinx.coroutines.launch
import java.util.Calendar

class PartyViewModel(private val repository: PartyRepository) : ViewModel() {
    val allParties: LiveData<List<Party>> = repository.allParties
    
    fun insert(party: Party) = viewModelScope.launch { repository.insert(party) }
    fun update(party: Party) = viewModelScope.launch { repository.update(party) }
    fun delete(party: Party) = viewModelScope.launch { repository.delete(party) }
}

class StockViewModel(private val repository: StockRepository) : ViewModel() {
    val allItems: LiveData<List<Item>> = repository.allItems
    val lowStockItems: LiveData<List<Item>> = repository.lowStockItems
    
    fun insert(item: Item) = viewModelScope.launch { repository.insert(item) }
    fun update(item: Item) = viewModelScope.launch { repository.update(item) }
}

class TransactionViewModel(
    private val transactionRepo: TransactionRepository,
    private val stockRepo: StockRepository // Needed to update stock on sale/purchase
) : ViewModel() {
    
    val allTransactions = transactionRepo.allTransactions
    
    fun recordSale(transaction: Transaction, items: List<TransactionItem>) = viewModelScope.launch {
        transactionRepo.insertTransaction(transaction, items)
        // Reduce stock
        items.forEach { 
            stockRepo.updateStock(it.itemId, -1 * it.quantity)
        }
    }
    
    fun recordPurchase(transaction: Transaction, items: List<TransactionItem>) = viewModelScope.launch {
        transactionRepo.insertTransaction(transaction, items)
        // Increase stock
        items.forEach {
            stockRepo.updateStock(it.itemId, it.quantity)
        }
    }
    
    fun getPartyTransactions(partyId: Long) = transactionRepo.getTransactionsForParty(partyId)

    fun getTodaySales(): LiveData<Double?> {
        val (start, end) = getTodayRange()
        return transactionRepo.getSalesTotal(start, end)
    }

    fun getTodayPurchases(): LiveData<Double?> {
        val (start, end) = getTodayRange()
        return transactionRepo.getPurchasesTotal(start, end)
    }
    
    private fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val start = calendar.timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val end = calendar.timeInMillis
        return Pair(start, end)
    }
}

class ViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartyViewModel::class.java)) {
            return PartyViewModel(PartyRepository(database.partyDao())) as T
        }
        if (modelClass.isAssignableFrom(StockViewModel::class.java)) {
            return StockViewModel(StockRepository(database.itemDao())) as T
        }
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(
                TransactionRepository(database.transactionDao()),
                StockRepository(database.itemDao())
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
