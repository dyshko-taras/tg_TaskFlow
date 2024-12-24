package com.student.taskflow.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.student.taskflow.databinding.ActivityAuthorizationBinding

class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun navigateToRegistration() {
        val intent = Intent(this@AuthorizationActivity, RegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setListener() {
        binding.tvRegister.setOnClickListener {
            navigateToRegistration()
        }
    }

}