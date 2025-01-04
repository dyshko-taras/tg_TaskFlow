package com.student.taskflow.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityRegistrationBinding
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.util.NetworkUtils
import com.student.taskflow.util.Result
import com.student.taskflow.util.containsDigit
import kotlinx.coroutines.launch

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setListener()
    }

    private fun setupUI() {
        configureUIForRole(isAdmin = true)
    }

    private fun setListener() {
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                var checkedId = checkedIds[0] // Get the first checked ID
                when (checkedId) {
                    R.id.chipAdmin -> configureUIForRole(isAdmin = true)
                    R.id.chipEmployee -> configureUIForRole(isAdmin = false)
                }
            }
        }

        binding.textInputLayoutEmail.setEndIconOnClickListener {
            navigateToAuthorization()
        }

        binding.textInputEditTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString()
                val colorGrey = ContextCompat.getColor(binding.root.context, R.color.grey)
                val colorBlack = ContextCompat.getColor(binding.root.context, R.color.black)

                binding.tvPasswordMinimumLength.setTextColor(if (text.length >= 6) colorBlack else colorGrey)
                binding.tvPasswordMinimumLength.setCompoundDrawablesWithIntrinsicBounds(
                    if (text.length >= 6) R.drawable.ic_right else R.drawable.ic_false, 0, 0, 0
                )

                binding.tvPasswordNumbers.setTextColor(if (text.containsDigit()) colorBlack else colorGrey)
                binding.tvPasswordNumbers.setCompoundDrawablesWithIntrinsicBounds(
                    if (text.containsDigit()) R.drawable.ic_right else R.drawable.ic_false,
                    0,
                    0,
                    0
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnSave.setOnClickListener {
            var email = binding.textInputEditTextEmail.text.toString()
            var password = binding.textInputEditTextPassword.text.toString()
            var isValidEmail = NetworkUtils.validateEmail(email)
            var isValidPassword = password.length >= 6 && password.containsDigit()
            var isInternetAvailable =
                NetworkUtils.isInternetAvailable(this@RegistrationActivity)

            if (!isInternetAvailable) {
                Toast.makeText(
                    this@RegistrationActivity,
                    "Please check your internet connection.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            if (isValidEmail && isValidPassword) {
                registerWithEmailAndPassword(email, password)
            } else if (!isValidEmail) {
                Toast.makeText(
                    this,
                    "Please enter a valid email address.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    "Your password must contain at least 6 characters and a number.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun navigateToAuthorization() {
        val intent = Intent(this@RegistrationActivity, AuthorizationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerWithEmailAndPassword(email: String, password: String) {
        showLoading()
        lifecycleScope.launch {
            val result = FirebaseAuthRepository.registerWithEmailAndPassword(email, password)
            hideLoading()
            when (result) {
                is Result.Success -> {
                    Toast.makeText(
                        this@RegistrationActivity,
                        result.message,
                        Toast.LENGTH_LONG
                    ).show()
                    navigateToAuthorization()
                }

                is Result.Failure -> Toast.makeText(
                    this@RegistrationActivity,
                    result.message,
                    Toast.LENGTH_LONG
                ).show()
            }
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

    private fun configureUIForRole(isAdmin: Boolean) {
        binding.tvGroupId.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.textInputLayoutGroupId.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.textInputEditTextGroupId.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.textInputLayoutGroupName.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.textInputEditTextGroupName.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }
}