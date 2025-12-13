package com.antigravity.businessapp.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.antigravity.businessapp.App
import com.antigravity.businessapp.R
import com.antigravity.businessapp.data.Transaction
import com.antigravity.businessapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TransactionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory((application as App).database)
        viewModel = ViewModelProvider(this, factory).get(TransactionViewModel::class.java)

        setupObservers()
        setupListeners()
        setupRecentTransactions()
    }

    private fun setupListeners() {
        binding.btnParties.setOnClickListener {
            startActivity(Intent(this, PartyListActivity::class.java))
        }
        binding.btnStock.setOnClickListener {
            startActivity(Intent(this, StockListActivity::class.java))
        }
        binding.btnNewSale.setOnClickListener {
            val intent = Intent(this, TransactionEntryActivity::class.java)
            intent.putExtra("TYPE", "SALE")
            startActivity(intent)
        }
        binding.btnNewPurchase.setOnClickListener {
            val intent = Intent(this, TransactionEntryActivity::class.java)
            intent.putExtra("TYPE", "PURCHASE")
            startActivity(intent)
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.btnReports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.getTodaySales().observe(this) { total ->
            binding.tvTodaySales.text = "₹ ${total ?: 0.0}"
        }
        viewModel.getTodayPurchases().observe(this) { total ->
            binding.tvTodayPurchases.text = "₹ ${total ?: 0.0}"
        }
    }
    
    private fun setupRecentTransactions() {
        val adapter = RecentTxAdapter()
        binding.rvRecentTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvRecentTransactions.adapter = adapter
        
        viewModel.recentTransactions.observe(this) { list ->
            adapter.submitList(list)
        }
    }
}

class RecentTxAdapter : ListAdapter<Transaction, RecentTxAdapter.TxViewHolder>(TxDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction_row, parent, false)
        return TxViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TxViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class TxViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvType = itemView.findViewById<TextView>(R.id.tv_tx_type)
        val tvAmount = itemView.findViewById<TextView>(R.id.tv_tx_amount)
        val tvDate = itemView.findViewById<TextView>(R.id.tv_tx_date)
        
        fun bind(t: Transaction) {
            tvType.text = t.type
            tvAmount.text = "₹ ${t.totalAmount}"
            tvDate.text = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(t.timestamp))
            
            if (t.type == "SALE") {
                tvType.setTextColor(Color.parseColor("#43A047")) // Green
            } else if (t.type == "PURCHASE") {
                tvType.setTextColor(Color.parseColor("#E53935")) // Red
            }
        }
    }
    
    class TxDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean = oldItem == newItem
    }
}
