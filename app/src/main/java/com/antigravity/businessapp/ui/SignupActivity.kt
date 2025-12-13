package com.antigravity.businessapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.antigravity.businessapp.App
import com.antigravity.businessapp.data.User
import com.antigravity.businessapp.data.UserRepository
import com.antigravity.businessapp.databinding.ActivitySignupBinding
import kotlinx.coroutines.launch
import java.security.MessageDigest

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()
        val pin = binding.etPin.text.toString()

        if (username.isBlank() || password.isBlank() || pin.length != 4) {
            Toast.makeText(this, "Please fill all fields. PIN must be 4 digits.", Toast.LENGTH_SHORT).show()
            return
        }

        val db = (application as App).database
        val repo = UserRepository(db.userDao())

        lifecycleScope.launch {
            if (repo.hasUsers()) {
                Toast.makeText(this@SignupActivity, "Admin already exists!", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Simple hash for offline usage
            val passwordHash = hashString(password)
            val user = User(username = username, passwordHash = passwordHash, pin = pin)
            
            repo.registerUser(user)

            // Sync ID/PIN to Prefs for quick login
            val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("user_pin", pin).apply()
            
            Toast.makeText(this@SignupActivity, "Account Created!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@SignupActivity, MainActivity::class.java))
            finish()
        }
    }

    private fun hashString(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}
