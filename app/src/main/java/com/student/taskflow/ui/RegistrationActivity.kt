package com.student.taskflow.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.student.taskflow.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun navigateToAuthorization() {
        val intent = Intent(this@RegistrationActivity, AuthorizationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setListener() {

    }
}