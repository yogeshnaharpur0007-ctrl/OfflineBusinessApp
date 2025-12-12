package com.antigravity.businessapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.antigravity.businessapp.App
import com.antigravity.businessapp.databinding.ActivitySettingsBinding
import com.antigravity.businessapp.utils.BackupHelper
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val PICK_BACKUP_FILE = 1
    private val CREATE_BACKUP_FILE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBackup.setOnClickListener {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "BusinessApp_Backup_${System.currentTimeMillis()}.json")
            }
            startActivityForResult(intent, CREATE_BACKUP_FILE)
        }

        binding.btnRestore.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            startActivityForResult(intent, PICK_BACKUP_FILE)
        }

        binding.btnChangePin.setOnClickListener {
            showChangePinDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data!!
            if (requestCode == PICK_BACKUP_FILE) {
                performRestore(uri)
            } else if (requestCode == CREATE_BACKUP_FILE) {
                performBackup(uri)
            }
        }
    }

    private fun performBackup(uri: android.net.Uri) {
        lifecycleScope.launch {
            binding.tvStatus.text = "Backing up..."
            val result = BackupHelper.exportBackup(this@SettingsActivity, (application as App).database, uri)
            binding.tvStatus.text = result
            Toast.makeText(this@SettingsActivity, result, Toast.LENGTH_LONG).show()
        }
    }

    private fun performRestore(uri: android.net.Uri) {
        lifecycleScope.launch {
            binding.tvStatus.text = "Restoring..."
            val result = BackupHelper.importBackup(this@SettingsActivity, (application as App).database, uri)
            binding.tvStatus.text = result
            Toast.makeText(this@SettingsActivity, result, Toast.LENGTH_LONG).show()
        }
    }

    private fun showChangePinDialog() {
        val input = android.widget.EditText(this)
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        input.filters = arrayOf(android.text.InputFilter.LengthFilter(4))
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Set New PIN")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newPin = input.text.toString()
                if (newPin.length == 4) {
                    val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putString("user_pin", newPin).apply()
                    Toast.makeText(this, "PIN Updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "PIN must be 4 digits", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
