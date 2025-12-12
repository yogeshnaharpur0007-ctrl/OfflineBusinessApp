package com.antigravity.businessapp.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.antigravity.businessapp.App
import com.antigravity.businessapp.data.Item
import com.antigravity.businessapp.data.Party
import com.antigravity.businessapp.data.Transaction
import com.antigravity.businessapp.data.TransactionItem
import com.antigravity.businessapp.databinding.ActivityTransactionEntryBinding

class TransactionEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionEntryBinding
    private lateinit var viewModel: TransactionViewModel
    private lateinit var partyViewModel: PartyViewModel
    private lateinit var stockViewModel: StockViewModel

    private var selectedParty: Party? = null
    private var selectedItem: Item? = null
    private var transactionType: String = "SALE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionType = intent.getStringExtra("TYPE") ?: "SALE"
        binding.tvTransactionHeader.text = "New $transactionType"

        val database = (application as App).database
        val factory = ViewModelFactory(database)
        viewModel = ViewModelProvider(this, factory).get(TransactionViewModel::class.java)
        partyViewModel = ViewModelProvider(this, factory).get(PartyViewModel::class.java)
        stockViewModel = ViewModelProvider(this, factory).get(StockViewModel::class.java)

        setupSpinners()
        setupCalculationListeners()
        
        binding.btnSaveTransaction.setOnClickListener {
            saveTransaction()
        }
    }

    private fun setupSpinners() {
        // Parties
        partyViewModel.allParties.observe(this) { parties ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, parties.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerParty.adapter = adapter
            
            binding.spinnerParty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedParty = parties[position]
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        // Items
        stockViewModel.allItems.observe(this) { items ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerItem.adapter = adapter

            binding.spinnerItem.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedItem = items[position]
                    // Auto-fill rate based on type
                    if (transactionType == "SALE") {
                        binding.etRate.setText(selectedItem?.sellingRate.toString())
                    } else {
                        binding.etRate.setText(selectedItem?.purchaseRate.toString())
                    }
                    calculateTotal()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupCalculationListeners() {
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = calculateTotal()
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.etQuantity.addTextChangedListener(watcher)
        binding.etRate.addTextChangedListener(watcher)
    }

    private fun calculateTotal() {
        val qty = binding.etQuantity.text.toString().toIntOrNull() ?: 0
        val rate = binding.etRate.text.toString().toDoubleOrNull() ?: 0.0
        val total = qty * rate
        binding.tvCalculatedTotal.text = "Total: ₹ $total"
    }

    private fun saveTransaction() {
        if (selectedParty == null || selectedItem == null) return
        
        val qty = binding.etQuantity.text.toString().toIntOrNull() ?: 0
        val rate = binding.etRate.text.toString().toDoubleOrNull() ?: 0.0
        val paid = binding.etPaidAmount.text.toString().toDoubleOrNull() ?: 0.0
        
        if (qty <= 0) {
            Toast.makeText(this, "Quantity must be > 0", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = qty * rate
        
        val transaction = Transaction(
            partyId = selectedParty!!.id,
            timestamp = System.currentTimeMillis(),
            type = transactionType,
            totalAmount = totalAmount,
            paidAmount = paid,
            remarks = "Manual Entry"
        )
        
        val item = TransactionItem(
            transactionId = 0, // Will be set in Repo
            itemId = selectedItem!!.id,
            quantity = qty,
            rate = rate,
            amount = totalAmount
        )

        if (transactionType == "SALE") {
            viewModel.recordSale(transaction, listOf(item))
        } else {
            viewModel.recordPurchase(transaction, listOf(item))
        }
        
        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}
