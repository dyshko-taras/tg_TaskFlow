package com.student.taskflow.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityMainBinding
import com.student.taskflow.model.Task
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import com.student.taskflow.ui.adapter.TaskAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val firestoreRepository = FirebaseFirestoreRepository()
    private val user = SharedPreferencesRepository.getUser()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        setListener()
        getTasks()
    }

    private fun setupUI() {
        var hello = getString(R.string.hello)
        binding.appBar.title = "$hello ${SharedPreferencesRepository.getUser()?.name}"
    }


    private fun setListener() {
        binding.btnAddTask.setOnClickListener {
            navigateToAddTask()
        }

        binding.btnMore.setOnClickListener {
            showPopupMenu(binding.btnMore)
        }
    }

    private fun getTasks() {
        lifecycleScope.launch {
            val result = firestoreRepository.getListOfTasks(user.groupId)
            result.onSuccess { tasks ->
                initRecyclerViewTasks(tasks)
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun initRecyclerViewTasks(tasks: List<Task>) {
        val taskAdapter = TaskAdapter(tasks) { task ->
            println("Clicked on task: ${task.title}")
        }
        binding.rvTasks.adapter = taskAdapter
    }

    fun showPopupMenu(view: View) {
        var popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popupmenu_admin, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_copy_id -> {
                    val groupId = SharedPreferencesRepository.getUser()?.groupId

                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("text", groupId)
                    clipboard.setPrimaryClip(clip)

                    showToast(R.string.id_copied)
                    true
                }

                R.id.menu_sign_out -> {
                    var result = FirebaseAuthRepository.signOut()
                    result.onSuccess {
                        SharedPreferencesRepository.clearUser()
                        navigateToAuthorization()
                        showToast(R.string.log_out_successful)
                    }.onFailure {
                        showToast(it.message.toString())
                    }
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    private fun navigateToAddTask() {
        val intent = Intent(this@MainActivity, AddTaskActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToAuthorization() {
        val intent = Intent(this@MainActivity, AuthorizationActivity::class.java)
        startActivity(intent)
        finish()
    }
//
//    private fun showLoading() {
//        binding.dimBackground.visibility = View.VISIBLE
//        binding.progressBar.visibility = View.VISIBLE
//    }
//
//    private fun hideLoading() {
//        binding.dimBackground.visibility = View.GONE
//        binding.progressBar.visibility = View.GONE
//    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MainActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MainActivity, messageResId, duration).show()
    }
}