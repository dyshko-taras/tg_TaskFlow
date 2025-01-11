package com.student.taskflow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityMyGroupBinding
import com.student.taskflow.model.User
import com.student.taskflow.model.enums.Role
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import com.student.taskflow.ui.adapter.UserAdapter
import kotlinx.coroutines.launch

class MyGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyGroupBinding
    private lateinit var userAdapter: UserAdapter
    private var isFirstGetUsers = true
    private val firestoreRepository = FirebaseFirestoreRepository()
    private val user = SharedPreferencesRepository.getUser()!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMyGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setListener()
        getUsers()
    }

    private fun setListener() {
        binding.btnHome.setOnClickListener {
            navigateToMain()
        }

        binding.btnPolls.setOnClickListener {
            navigateToPolls()
        }
    }

    private fun getUsers() {
        lifecycleScope.launch {
            var listOfUsers = mutableListOf<User>()
            var resultAdmin = firestoreRepository.getAdmin(user.groupId)
            resultAdmin.onSuccess { admin ->
                listOfUsers.add(admin)
                var result = firestoreRepository.getListOfEmployees(user.groupId)
                result.onSuccess { employees ->
                    listOfUsers.addAll(employees)
                    if (isFirstGetUsers) {
                        initRecyclerViewUsers(listOfUsers)
                        isFirstGetUsers = false
                    } else {
                        userAdapter.updateUsers(listOfUsers)
                    }
                }.onFailure { error ->
                    showToast(error.message.toString())
                }
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun initRecyclerViewUsers(users: List<User>) {
        var isAdmin = user.role == Role.ADMIN

        var onBtnDelete: ((User) -> Unit) = { user ->
            lifecycleScope.launch {
                firestoreRepository.deleteUser(user).onSuccess {
                    getUsers()
                }.onFailure { error ->
                    showToast(error.message.toString())
                }
            }
        }

        userAdapter = UserAdapter(
            users,
            isAdmin,
            onBtnDelete
        )
        binding.rvUsers.adapter = userAdapter

        hideLoading()
    }

    private fun navigateToMain() {
        val intent = Intent(this@MyGroupActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToPolls() {
        val intent = Intent(this@MyGroupActivity, PollsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }


    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MyGroupActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MyGroupActivity, messageResId, duration).show()
    }
}