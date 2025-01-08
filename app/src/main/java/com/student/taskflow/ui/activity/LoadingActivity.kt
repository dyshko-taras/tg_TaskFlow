package com.student.taskflow.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.util.NetworkUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        SharedPreferencesRepository.initialize(this)
        navigateToNextActivity()
    }

    private fun navigateToAuthorization() {
        val intent = Intent(this@LoadingActivity, AuthorizationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoadingActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToNextActivity() {
        lifecycleScope.launch {
            delay(3000)
            if (SharedPreferencesRepository.isLoggedIn() && NetworkUtils.isInternetAvailable(this@LoadingActivity)) {
                navigateToMain()
            } else {
                navigateToAuthorization()
            }
        }
    }
}