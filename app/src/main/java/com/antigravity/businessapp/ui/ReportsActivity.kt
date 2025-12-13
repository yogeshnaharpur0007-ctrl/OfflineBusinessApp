package com.antigravity.businessapp.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.antigravity.businessapp.databinding.ActivityReportsBinding


import androidx.lifecycle.ViewModelProvider
import com.antigravity.businessapp.App
import com.antigravity.businessapp.utils.PdfHelper
import com.antigravity.businessapp.data.*

class ReportsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityReportsBinding
    private lateinit var txViewModel: TransactionViewModel
    private lateinit var stockViewModel: StockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory((application as App).database)
        txViewModel = ViewModelProvider(this, factory).get(TransactionViewModel::class.java)
        stockViewModel = ViewModelProvider(this, factory).get(StockViewModel::class.java)
        
        binding.btnReportSales.setOnClickListener {
            binding.tvReportStatus.text = "Generating Sales Report..."
            txViewModel.allTransactions.observe(this) { list ->
                if (list.isNotEmpty()) {
                     PdfHelper.generateSalesReport(this, list)
                     binding.tvReportStatus.text = "Sales Report Saved to Documents."
                } else {
                     binding.tvReportStatus.text = "No transactions found."
                }
            }
        }

        binding.btnReportStock.setOnClickListener {
             binding.tvReportStatus.text = "Generating Stock Report..."
             stockViewModel.allItems.observe(this) { list ->
                 if (list.isNotEmpty()) {
                     PdfHelper.generateStockReport(this, list)
                     binding.tvReportStatus.text = "Stock Report Saved to Documents."
                 } else {
                     binding.tvReportStatus.text = "No items found."
                 }
             }
        }
    }
}
