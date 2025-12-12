package com.antigravity.businessapp.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import com.antigravity.businessapp.data.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

data class BackupData(
    val parties: List<Party>,
    val items: List<Item>,
    val transactions: List<Transaction>,
    val transactionItems: List<TransactionItem>
)

object BackupHelper {

    suspend fun exportBackup(context: Context, database: AppDatabase, uri: android.net.Uri): String = withContext(Dispatchers.IO) {
        try {
            val backupData = BackupData(
                parties = database.partyDao().getAllPartiesList(),
                items = database.itemDao().getAllItemsList(),
                transactions = database.transactionDao().getAllTransactionsList(),
                transactionItems = database.transactionDao().getAllTransactionItemsList()
            )

            val gson = Gson()
            val jsonString = gson.toJson(backupData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            } ?: return@withContext "Failed to open output stream"
            
            return@withContext "Export Successful"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Export Failed: ${e.message}"
        }
    }

    suspend fun importBackup(context: Context, database: AppDatabase, uri: android.net.Uri): String = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val jsonString = inputStream?.bufferedReader().use { it?.readText() } ?: return@withContext "Read Failed"

            val gson = Gson()
            val data = gson.fromJson(jsonString, BackupData::class.java)

            database.runInTransaction {
                // Clear all tables ideally? Yes, restore usually implies overwrite or we handle conflicts.
                // Simple version: clear all.
                // NOTE: We need clear methods in DAOs. I will run raw queries via database.openHelper if strictly needed but DAO methods are cleaner.
                // Assuming I added deleteAll methods in previous step.
            }
            // DAO methods are suspend, so cannot be called directly inside runInTransaction(Runnable). 
            // `withTransaction` is better but needs KTX.
            
            // Manual clean
            database.transactionDao().deleteAllItems()
            database.transactionDao().deleteAll()
            database.itemDao().deleteAll()
            database.partyDao().deleteAll()

            // Insert
            data.parties.forEach { database.partyDao().insertParty(it) }
            data.items.forEach { database.itemDao().insertItem(it) }
            data.transactions.forEach { database.transactionDao().insertTransaction(it) }
            data.transactionItems.forEach { database.transactionDao().insertTransactionItems(listOf(it)) }

            return@withContext "Restore Successful"
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Import Failed: ${e.message}"
        }
    }
}
