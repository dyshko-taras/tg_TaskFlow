package com.student.taskflow.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.databinding.ActivityAuthorizationBinding
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.util.NetworkUtils
import com.student.taskflow.util.containsDigit
import kotlinx.coroutines.launch

class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAuthorizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun setListener() {
        binding.tvRegister.setOnClickListener {
            navigateToRegistration()
        }
        binding.btnLogin.setOnClickListener {
            var email = binding.textInputEditTextEmail.text.toString()
            var password = binding.textInputEditTextPassword.text.toString()
            var isValidEmail = NetworkUtils.validateEmail(email)
            var isValidPassword = password.length >= 6 && password.containsDigit()

            var isInternetAvailable = NetworkUtils.isInternetAvailable(this@AuthorizationActivity)

            if (!isInternetAvailable) {
                Toast.makeText(
                    this@AuthorizationActivity,
                    "Please check your internet connection.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            if (isValidEmail && isValidPassword) {
                signInWithEmailAndPassword(email, password)
            } else if (!isValidEmail) {
                Toast.makeText(this, "Email is not valid", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Password is not valid", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun navigateToRegistration() {
        val intent = Intent(this@AuthorizationActivity, RegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMain() {
        val intent = Intent(this@AuthorizationActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        showLoading()
        lifecycleScope.launch {
            val result = FirebaseAuthRepository.signInWithEmailAndPassword(email, password)
            hideLoading()
            result.fold(onSuccess = {
                Toast.makeText(this@AuthorizationActivity, it, Toast.LENGTH_LONG).show()
                navigateToMain()
            }, onFailure = {
                Toast.makeText(this@AuthorizationActivity, it, Toast.LENGTH_LONG).show()
            })
        }
    }

    private fun showLoading() {
        binding.dimBackground.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.dimBackground.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }
}