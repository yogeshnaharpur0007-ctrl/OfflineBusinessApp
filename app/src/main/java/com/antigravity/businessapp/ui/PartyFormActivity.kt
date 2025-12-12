package com.antigravity.businessapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.antigravity.businessapp.App
import com.antigravity.businessapp.data.Party
import com.antigravity.businessapp.data.PartyRepository // Import Repository 
import com.antigravity.businessapp.databinding.ActivityPartyFormBinding
import kotlinx.coroutines.launch

class PartyFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPartyFormBinding
    private lateinit var viewModel: PartyViewModel
    private var editingPartyId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartyFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = (application as App).database
        val factory = ViewModelFactory(database)
        viewModel = ViewModelProvider(this, factory).get(PartyViewModel::class.java)

        // Check if editing
        if (intent.hasExtra("party_id")) {
            editingPartyId = intent.getLongExtra("party_id", -1)
            loadPartyData(editingPartyId, database)
        }

        binding.btnSaveParty.setOnClickListener {
            saveParty()
        }
    }

    private fun loadPartyData(id: Long, db: com.antigravity.businessapp.data.AppDatabase) {
        lifecycleScope.launch {
            val repo = com.antigravity.businessapp.data.PartyRepository(db.partyDao())
            val party = repo.getPartyById(id)
            party?.let {
                binding.etPartyName.setText(it.name)
                binding.etPartyContact.setText(it.contact)
                if (it.type == "SUPPLIER") {
                    binding.rbSupplier.isChecked = true
                }
            }
        }
    }

    private fun saveParty() {
        val name = binding.etPartyName.text.toString()
        val contact = binding.etPartyContact.text.toString()
        val type = if (binding.rbSupplier.isChecked) "SUPPLIER" else "CUSTOMER"

        if (name.isBlank()) return

        val party = Party(
            id = if (editingPartyId != -1L) editingPartyId else 0,
            name = name,
            contact = contact,
            type = type
        )

        if (editingPartyId != -1L) {
            viewModel.update(party)
        } else {
            viewModel.insert(party)
        }
        finish()
    }
}
