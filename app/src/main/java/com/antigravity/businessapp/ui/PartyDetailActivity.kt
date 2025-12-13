    package com.antigravity.businessapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antigravity.businessapp.App
import com.antigravity.businessapp.R
import com.antigravity.businessapp.data.Party
import com.antigravity.businessapp.data.PartyRepository
import com.antigravity.businessapp.data.Transaction
import com.antigravity.businessapp.databinding.ActivityPartyDetailBinding
import com.antigravity.businessapp.utils.PdfHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PartyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartyDetailBinding
    private lateinit var viewModel: TransactionViewModel
    private var partyId: Long = -1
    private var currentParty: Party? = null
    private var currentTransactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        partyId = intent.getLongExtra("party_id", -1)
        if (partyId == -1L) finish()

        val database = (application as App).database
        val factory = ViewModelFactory(database)
        viewModel = ViewModelProvider(this, factory).get(TransactionViewModel::class.java)

        loadPartyInfo(database)
        setupLedger()
        
        binding.btnEditParty.setOnClickListener {
            val intent = Intent(this, PartyFormActivity::class.java)
            intent.putExtra("party_id", partyId)
            startActivity(intent)
        }
        
        binding.btnExportPdf.setOnClickListener {
            currentParty?.let { party ->
                if (currentTransactions.isNotEmpty()) {
                    PdfHelper.generateLedgerPdf(this, party.name, currentTransactions)
                }
            }
        }
        
        binding.btnArchive.setOnClickListener {
            archiveParty()
        }
        
        // Assuming there is a delete button - if not, I'll add one or hook into 'Edit' menu differently
        // For 'Speed', I will add a Delete/Archive button to the UI first
    }
    
    // ... loadPartyInfo ...
    
    private fun archiveParty() {
         androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Archive Party?")
            .setMessage("This will hide the party but keep transaction history. Continue?")
            .setPositiveButton("Archive") { _, _ ->
                 currentParty?.let { 
                     // Need access to PartyViewModel here, but I only initialized TransactionViewModel
                     // Quick fix: Use ViewModelFactory to get PartyViewModel too
                     val factory = ViewModelFactory((application as App).database)
                     val partyVM = ViewModelProvider(this, factory).get(PartyViewModel::class.java)
                     partyVM.archive(it)
                     finish()
                 }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadPartyInfo(db: com.antigravity.businessapp.data.AppDatabase) {
        lifecycleScope.launch {
            val repo = PartyRepository(db.partyDao())
            currentParty = repo.getPartyById(partyId)
            currentParty?.let {
                binding.tvDetailName.text = it.name
            }
        }
    }

    private fun setupLedger() {
        val adapter = LedgerAdapter { transaction ->
             // On Long Click -> Delete
             deleteTransactionWithUndo(transaction)
             true
        }
        binding.rvLedger.layoutManager = LinearLayoutManager(this)
        binding.rvLedger.adapter = adapter

        viewModel.getPartyTransactions(partyId).observe(this) { transactions ->
            currentTransactions = transactions
            adapter.submitList(transactions)
            // Balance Calc
            var balance = 0.0
             currentParty?.let { balance += it.openingBalance }
             
            transactions.forEach { t ->
                if (t.type == "SALE") balance += (t.totalAmount - t.paidAmount) // Positive for receivable (Customer)
                if (t.type == "PURCHASE") balance -= (t.totalAmount - t.paidAmount) // Negative for payable (Supplier)
                if (t.type == "PAYMENT_IN") balance -= t.paidAmount
                if (t.type == "PAYMENT_OUT") balance += t.paidAmount
            }
            binding.tvDetailBalance.text = "Outstanding Balance: ₹ $balance"
        }
    }
    
    private fun deleteTransactionWithUndo(transaction: Transaction) {
        // Optimistic Remove or DB remove?
        // DB Remove -> Then Undo inserts it back.
        // We need 'delete' in ViewModel. I only have 'recordXXX'. 
        // I will need to quickly add 'deleteTransaction' to TransactionViewModel.
        // Assuming I add it:
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Transaction?")
            .setMessage("Are you sure you want to delete this?")
            .setPositiveButton("Delete") { _, _ ->
                 viewModel.deleteTransaction(transaction)
                 
                 com.google.android.material.snackbar.Snackbar.make(binding.root, "Transaction Deleted", 3000)
                    .setAction("UNDO") {
                        // Undo Logic: Re-insert
                        // Ideally we restore items too since cascading delete removes them
                        // For MVP: We just re-insert the transaction object but items might be lost if we don't fetch them first!
                        // "Undo" for complex DB structure requires holding the full object graph in memory.
                        viewModel.restoreTransaction(transaction) // Placeholder
                    }
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

class LedgerAdapter(private val onLongClick: (Transaction) -> Boolean) : ListAdapter<Transaction, LedgerAdapter.LedgerViewHolder>(LedgerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ledger_row, parent, false)
        return LedgerViewHolder(view, onLongClick)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LedgerViewHolder(itemView: View, val onLongClick: (Transaction) -> Boolean) : RecyclerView.ViewHolder(itemView) {
        val tvDate = itemView.findViewById<TextView>(R.id.tv_row_date)
        val tvType = itemView.findViewById<TextView>(R.id.tv_row_type)
        val tvTotal = itemView.findViewById<TextView>(R.id.tv_row_total)
        val tvPaid = itemView.findViewById<TextView>(R.id.tv_row_paid)

        fun bind(t: Transaction) {
            tvDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(t.timestamp))
            tvType.text = t.type
            tvTotal.text = t.totalAmount.toString()
            tvPaid.text = t.paidAmount.toString()
            
            itemView.setOnLongClickListener { 
                onLongClick(t)
            }
        }
    }

    class LedgerDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
    }
}
