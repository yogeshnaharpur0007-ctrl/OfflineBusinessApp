package com.antigravity.businessapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.antigravity.businessapp.App
import com.antigravity.businessapp.databinding.ActivityPartyListBinding

class PartyListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPartyListBinding
    private lateinit var viewModel: PartyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPartyListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = ViewModelFactory((application as App).database)
        viewModel = ViewModelProvider(this, factory).get(PartyViewModel::class.java)

        val adapter = PartyAdapter { party ->
            // Open Details
            val intent = Intent(this, PartyDetailActivity::class.java)
            intent.putExtra("party_id", party.id)
            startActivity(intent)
        }
        binding.rvParties.layoutManager = LinearLayoutManager(this)
        binding.rvParties.adapter = adapter

        viewModel.allParties.observe(this) { parties ->
            adapter.submitList(parties)
        }

        binding.fabAddParty.setOnClickListener {
            startActivity(Intent(this, PartyFormActivity::class.java))
        }
    }
}
