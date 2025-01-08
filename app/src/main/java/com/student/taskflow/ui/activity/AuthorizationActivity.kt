package com.student.taskflow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityAuthorizationBinding
import com.student.taskflow.model.enums.Role
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import com.student.taskflow.util.NetworkUtils
import com.student.taskflow.util.containsDigit
import kotlinx.coroutines.launch

class AuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthorizationBinding
    private val firestoreRepository = FirebaseFirestoreRepository()

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
            var isAdmin = binding.chipAdmin.isChecked
            var email = binding.textInputEditTextEmail.text.toString()
            var password = binding.textInputEditTextPassword.text.toString()


            var isValidEmail = NetworkUtils.validateEmail(email)
            var isValidPassword = password.length >= 6 && password.containsDigit()
            var isInternetAvailable = NetworkUtils.isInternetAvailable(this@AuthorizationActivity)

            if (!isInternetAvailable) {
                showToast(R.string.check_internet_connection)
                return@setOnClickListener
            }
            if (!isValidEmail) {
                showToast(R.string.enter_valid_email)
                return@setOnClickListener
            }
            if (!isValidPassword) {
                showToast(R.string.password_requirements_error)
                return@setOnClickListener
            }

            signInWithEmailAndPassword(email, password, isAdmin)
        }
    }

    private fun signInWithEmailAndPassword(email: String, password: String, isAdmin: Boolean) {
        lifecycleScope.launch {
            showLoading()
            val resultAuth = FirebaseAuthRepository.signInWithEmailAndPassword(email, password)
            resultAuth.onSuccess { userId ->
                handleSignInResult(userId, isAdmin)
            }.onFailure { error ->
                showToast(error.message.toString())
            }
            hideLoading()
        }
    }

    private suspend fun handleSignInResult(userId: String, isAdmin: Boolean) {
        var result = firestoreRepository.getUserById(userId)
        result.onSuccess { user ->
            if (user.role == Role.ADMIN && isAdmin || user.role == Role.EMPLOYEE && !isAdmin) {
                SharedPreferencesRepository.setUser(user)
                navigateToMain()
            } else {
                showToast(R.string.invalid_role_selected)
            }
        }.onFailure { error ->
            showToast(error.message.toString())
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

    private fun showLoading() {
        binding.dimBackground.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.dimBackground.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@AuthorizationActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@AuthorizationActivity, messageResId, duration).show()
    }
}