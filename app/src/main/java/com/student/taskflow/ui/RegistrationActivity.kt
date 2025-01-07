package com.student.taskflow.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityRegistrationBinding
import com.student.taskflow.model.Group
import com.student.taskflow.model.User
import com.student.taskflow.model.enums.Role
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import com.student.taskflow.util.NetworkUtils
import com.student.taskflow.util.containsDigit
import kotlinx.coroutines.launch
import java.util.UUID

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private val firestoreRepository = FirebaseFirestoreRepository()

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
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

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
                    if (text.containsDigit()) R.drawable.ic_right else R.drawable.ic_false, 0, 0, 0
                )
            }
        })

        binding.btnSave.setOnClickListener {
            var isAdmin = binding.chipAdmin.isChecked
            var email = binding.textInputEditTextEmail.text.toString()
            var yourName = binding.textInputEditTextYourName.text.toString()
            var groupName = binding.textInputEditTextGroupName.text.toString()
            var groupId = binding.textInputEditTextGroupId.text.toString()
            var password = binding.textInputEditTextPassword.text.toString()


            var isValidEmail = NetworkUtils.validateEmail(email)
            var isValidPassword = password.length >= 6 && password.containsDigit()
            var isInternetAvailable = NetworkUtils.isInternetAvailable(this@RegistrationActivity)
            var isFieldsEmptyForAdmin =
                isAdmin && (yourName.isEmpty() || email.isEmpty() || password.isEmpty() || groupName.isEmpty())
            var isFieldsEmptyForEmployee =
                !isAdmin && (yourName.isEmpty() || email.isEmpty() || password.isEmpty() || groupId.isEmpty())

            if (!isInternetAvailable) {
                showToast(R.string.check_internet_connection)
                return@setOnClickListener
            }
            if (isFieldsEmptyForAdmin || isFieldsEmptyForEmployee) {
                showToast(R.string.fill_all_fields)
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

            var newGroupForAdmin = Group(
                id = UUID.randomUUID().toString(), name = groupName
            )
            var user = User(
                id = "",
                groupId = if (isAdmin) "" else groupId,
                role = if (isAdmin) Role.ADMIN else Role.EMPLOYEE,
                name = yourName,
                email = email
            )

            registerWithEmailAndPassword(email, password, user, newGroupForAdmin)
        }
    }

    private fun registerWithEmailAndPassword(
        email: String, password: String, user: User, group: Group
    ) {
        lifecycleScope.launch {
            showLoading()
            val authResult = FirebaseAuthRepository.registerWithEmailAndPassword(email, password)
            authResult.onSuccess { userId ->
                user.id = userId
                handleUserRole(user, group)
            }.onFailure { error ->
                showToast(error.message.toString())
            }
            hideLoading()
        }
    }

    private suspend fun handleUserRole(user: User, group: Group) {
        if (user.role == Role.ADMIN) handleAdminRole(user, group) else handleEmployeeRole(user)
    }

    private suspend fun handleAdminRole(user: User, group: Group) {
        user.groupId = group.id
        firestoreRepository.addGroup(group).onSuccess {
            firestoreRepository.addUser(user).onSuccess {
                navigateToAuthorization()
            }.onFailure {
                showToast(it.message.toString())
            }
        }.onFailure {
            showToast(it.message.toString())
        }
    }

    private suspend fun handleEmployeeRole(user: User) {
        var result = firestoreRepository.isGroupExists(user.groupId)
        result.onSuccess { isGroupExists ->
            if (isGroupExists) {
                firestoreRepository.addUser(user).onSuccess {
                    navigateToAuthorization()
                }.onFailure {
                    showToast(it.message.toString())
                }
            } else {
                showToast(R.string.group_not_exist)
            }
        }.onFailure {
            showToast(it.message.toString())
        }
    }

    private fun navigateToAuthorization() {
        val intent = Intent(this@RegistrationActivity, AuthorizationActivity::class.java)
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

    private fun configureUIForRole(isAdmin: Boolean) {
        binding.tvGroupId.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.textInputLayoutGroupId.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.textInputEditTextGroupId.visibility = if (isAdmin) View.GONE else View.VISIBLE
        binding.textInputLayoutGroupName.visibility = if (isAdmin) View.VISIBLE else View.GONE
        binding.textInputEditTextGroupName.visibility = if (isAdmin) View.VISIBLE else View.GONE
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@RegistrationActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@RegistrationActivity, messageResId, duration).show()
    }
}