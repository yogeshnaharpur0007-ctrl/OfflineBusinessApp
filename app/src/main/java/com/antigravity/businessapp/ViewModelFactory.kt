package com.antigravity.businessapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.antigravity.businessapp.data.AppDatabase

class ViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    private val db = AppDatabase.getDatabase(application)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(PartyViewModel::class.java) -> {
                PartyViewModel(
                    db.partyDao(),
                    db.transactionDao()
                ) as T
            }

            modelClass.isAssignableFrom(ItemViewModel::class.java) -> {
                ItemViewModel(db.itemDao()) as T
            }

            modelClass.isAssignableFrom(StockViewModel::class.java) -> {
                StockViewModel(
                    db.itemDao(),
                    db.stockTxDao()
                ) as T
            }

            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                TransactionViewModel(
                    db.transactionDao(),
                    db.itemDao()
                ) as T
            }

            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(db.userDao()) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
