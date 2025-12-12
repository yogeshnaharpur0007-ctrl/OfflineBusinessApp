package com.antigravity.businessapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.antigravity.businessapp.App
import com.antigravity.businessapp.data.Item
import com.antigravity.businessapp.data.StockRepository
import com.antigravity.businessapp.databinding.ActivityStockFormBinding
import kotlinx.coroutines.launch

class StockFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStockFormBinding
    private lateinit var viewModel: StockViewModel
    private var editingItemId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStockFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = (application as App).database
        val factory = ViewModelFactory(database)
        viewModel = ViewModelProvider(this, factory).get(StockViewModel::class.java)

        if (intent.hasExtra("item_id")) {
            editingItemId = intent.getLongExtra("item_id", -1)
            loadItemData(editingItemId, database)
        }

        binding.btnSaveItem.setOnClickListener {
            saveItem()
        }
    }

    private fun loadItemData(id: Long, db: com.antigravity.businessapp.data.AppDatabase) {
        lifecycleScope.launch {
            val repo = StockRepository(db.itemDao())
            val item = repo.getItemById(id)
            item?.let {
                binding.etItemName.setText(it.name)
                binding.etItemRate.setText(it.sellingRate.toString())
                binding.etItemPurchaseRate.setText(it.purchaseRate.toString())
                binding.etItemStock.setText(it.stockQuantity.toString())
                binding.etItemLowLimit.setText(it.lowStockLimit.toString())
            }
        }
    }

    private fun saveItem() {
        val name = binding.etItemName.text.toString()
        val rate = binding.etItemRate.text.toString().toDoubleOrNull() ?: 0.0
        val pRate = binding.etItemPurchaseRate.text.toString().toDoubleOrNull() ?: 0.0
        val stock = binding.etItemStock.text.toString().toIntOrNull() ?: 0
        val limit = binding.etItemLowLimit.text.toString().toIntOrNull() ?: 5

        if (name.isBlank()) return

        val item = Item(
            id = if (editingItemId != -1L) editingItemId else 0,
            name = name,
            sellingRate = rate,
            purchaseRate = pRate,
            stockQuantity = stock,
            lowStockLimit = limit
        )

        if (editingItemId != -1L) {
            viewModel.update(item)
        } else {
            viewModel.insert(item)
        }
        finish()
    }
}
