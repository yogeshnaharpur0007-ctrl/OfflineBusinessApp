package com.antigravity.businessapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.antigravity.businessapp.App
import com.antigravity.businessapp.databinding.ActivityStockListBinding

class StockListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStockListBinding
    private lateinit var viewModel: StockViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory((application as App).database)
        viewModel = ViewModelProvider(this, factory).get(StockViewModel::class.java)

        val adapter = StockAdapter { item ->
            val intent = Intent(this, StockFormActivity::class.java)
            intent.putExtra("item_id", item.id)
            startActivity(intent)
        }
        binding.rvStock.layoutManager = LinearLayoutManager(this)
        binding.rvStock.adapter = adapter

        viewModel.allItems.observe(this) { items ->
            adapter.submitList(items)
        }

        binding.fabAddItem.setOnClickListener {
            startActivity(Intent(this, StockFormActivity::class.java))
        }
    }
}
