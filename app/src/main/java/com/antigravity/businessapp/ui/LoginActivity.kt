package com.antigravity.businessapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.antigravity.businessapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if PIN is set, if not, maybe let them in or ask to set?
        // Requirement: "Simple offline login PIN (4-digit)"
        // Default PIN: 1234
        
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val enteredPin = binding.etPin.text.toString()
            if (checkPin(enteredPin)) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }

    private fun checkPin(pin: String): Boolean {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedPin = prefs.getString("user_pin", "1234") // Default 1234
        return pin == savedPin
    }
}
