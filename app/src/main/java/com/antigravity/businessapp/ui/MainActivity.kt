package com.antigravity.businessapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.antigravity.businessapp.App
import com.antigravity.businessapp.databinding.ActivityMainBinding

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
    }

    private fun setupObservers() {
        viewModel.getTodaySales().observe(this) { total ->
            binding.tvTodaySales.text = "₹ ${total ?: 0.0}"
        }
        viewModel.getTodayPurchases().observe(this) { total ->
            binding.tvTodayPurchases.text = "₹ ${total ?: 0.0}"
        }
    }
}
