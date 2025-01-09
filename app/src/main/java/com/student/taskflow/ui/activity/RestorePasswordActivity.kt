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
import com.student.taskflow.databinding.ActivityRestorePasswordBinding
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.util.NetworkUtils
import kotlinx.coroutines.launch

class RestorePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestorePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRestorePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListener()
    }

    private fun setListener() {
        binding.btnRetrieve.setOnClickListener {
            var email = binding.textInputEditTextEmail.text.toString()
            var isValidEmail = NetworkUtils.validateEmail(email)

            if (!isValidEmail) {
                showToast(R.string.enter_valid_email)
                return@setOnClickListener
            }

            lifecycleScope.launch {
                showLoading()
                var result = FirebaseAuthRepository.resetPassword(email)
                result.onSuccess {
                    showToast(R.string.password_reset_email_sent)
                }.onFailure { error ->
                    showToast(error.message.toString())
                }
                hideLoading()
                navigateToAuthorization()
            }
        }
    }

    private fun navigateToAuthorization() {
        val intent = Intent(this@RestorePasswordActivity, AuthorizationActivity::class.java)
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
        Toast.makeText(this@RestorePasswordActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@RestorePasswordActivity, messageResId, duration).show()
    }

}