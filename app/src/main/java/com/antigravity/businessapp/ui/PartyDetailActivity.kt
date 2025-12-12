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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PartyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPartyDetailBinding
    private lateinit var viewModel: TransactionViewModel
    private var partyId: Long = -1
    private var currentParty: Party? = null

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
    }

    private fun loadPartyInfo(db: com.antigravity.businessapp.data.AppDatabase) {
        lifecycleScope.launch {
            val repo = PartyRepository(db.partyDao())
            currentParty = repo.getPartyById(partyId)
            currentParty?.let {
                binding.tvDetailName.text = it.name
                // Balance calculation is complex, here we just show name for now or sum ledger
            }
        }
    }

    private fun setupLedger() {
        val adapter = LedgerAdapter()
        binding.rvLedger.layoutManager = LinearLayoutManager(this)
        binding.rvLedger.adapter = adapter

        viewModel.getPartyTransactions(partyId).observe(this) { transactions ->
            adapter.submitList(transactions)
            // Calculate Balance Logic could go here: (Sales - Payments)
            var balance = 0.0
            transactions.forEach { t ->
                if (t.type == "SALE") balance += (t.totalAmount - t.paidAmount)
                if (t.type == "PURCHASE") balance -= (t.totalAmount - t.paidAmount) // Owed
                if (t.type == "PAYMENT_IN") balance -= t.paidAmount
            }
            binding.tvDetailBalance.text = "Outstanding Balance: ₹ $balance"
        }
    }
}

class LedgerAdapter : ListAdapter<Transaction, LedgerAdapter.LedgerViewHolder>(LedgerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LedgerViewHolder {
        // Reusing simple layout logic or creating new one?
        // Let's create a simple view programmatically or use a simple layout file if I had one. 
        // I'll create a simple item layout file for ledger item to be safe.
        // Wait, I can reuse 'item_party.xml' but modify text? No, safer to have dedicated.
        // I'll use a standard list item layout built-in? No.
        // I will use a simple inner class layout creation to save file creation or just create file.
        // Create file is better.
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return LedgerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LedgerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class LedgerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val text1 = itemView.findViewById<TextView>(android.R.id.text1)
        val text2 = itemView.findViewById<TextView>(android.R.id.text2)

        fun bind(t: Transaction) {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(t.timestamp))
            text1.text = "${t.type} - ₹ ${t.totalAmount}"
            text2.text = "$date | Paid: ${t.paidAmount}"
        }
    }

    class LedgerDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
    }
}
