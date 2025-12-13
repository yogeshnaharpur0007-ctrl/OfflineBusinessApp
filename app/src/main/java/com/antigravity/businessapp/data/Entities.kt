package com.antigravity.businessapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String, // Store salted hash
    val pin: String? = null // Encrypted or simple storage depending on requirement
)

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String, // "DELETE_PARTY", "EDIT_MAX_STOCK", etc.
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contact: String,
    val type: String, // "CUSTOMER" or "SUPPLIER"
    val openingBalance: Double = 0.0,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String = "pcs",
    val sellingRate: Double,
    val purchaseRate: Double,
    val openingStock: Int = 0,
    val stockQuantity: Int, // Current calculated stock
    val lowStockLimit: Int
)

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(entity = Party::class, parentColumns = ["id"], childColumns = ["partyId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("partyId")]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val partyId: Long,
    val timestamp: Long,
    val type: String, // "SALE", "PURCHASE", "PAYMENT_IN", "PAYMENT_OUT"
    val totalAmount: Double,
    val paidAmount: Double, // Amount received/paid immediately
    val remarks: String
)

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(entity = Transaction::class, parentColumns = ["id"], childColumns = ["transactionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.NO_ACTION)
    ],
    indices = [Index("transactionId"), Index("itemId")]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionId: Long,
    val itemId: Long,
    val quantity: Int,
    val rate: Double,
    val amount: Double
)

@Entity(
    tableName = "stock_tx",
    foreignKeys = [
        ForeignKey(entity = Item::class, parentColumns = ["id"], childColumns = ["itemId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("itemId")]
)
data class StockTx(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: Long,
    val quantity: Int, // Positive for Purchase, Negative for Sale
    val type: String, // "SALE", "PURCHASE", "MANUAL_ADJUST"
    val date: Long = System.currentTimeMillis(),
    val refTransactionId: Long? = null // Link to Transaction if applicable
)

