package com.student.taskflow.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityMainBinding
import com.student.taskflow.model.Task
import com.student.taskflow.model.enums.Role
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
        binding.txUserName.text = user.name.first().toString()

        var name = SharedPreferencesRepository.getUser()?.name
        binding.txTitle.text = getString(R.string.hello, name)

        binding.btnAddTask.visibility =
            if (user.role == Role.ADMIN) View.VISIBLE else View.INVISIBLE
    }


    private fun setListener() {
        binding.btnAddTask.setOnClickListener {
            navigateToAddTask()
        }

        binding.btnSettings.setOnClickListener {
            showSettingsPopupMenu(binding.btnSettings)
        }
    }

    private fun getTasks() {
        lifecycleScope.launch {
            val result =
                if (user.role == Role.ADMIN) {
                    firestoreRepository.getListOfTasks(user.id)
                } else {
                    firestoreRepository.getListOfTasksCurrentUser(user.groupId)
                }
            result.onSuccess { tasks ->
                initRecyclerViewTasks(tasks)
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun initRecyclerViewTasks(tasks: List<Task>) {
        var isAdmin = user.role == Role.ADMIN

        var onCheckBoxClick: ((Task) -> Unit) = { task ->
            showToast("1: ${task.title}")
        }
        var onBtnMoreClick: ((anchor: View, Task) -> Unit) = { anchor, task ->
            showTaskPopupMenu(anchor, task)
            showToast("click more")
        }
        var onBtnInfoClick: ((Task) -> Unit) = { task ->
            showToast("3: ${task.title}")
        }

        val taskAdapter =
            TaskAdapter(
                tasks,
                isAdmin,
                onCheckBoxClick,
                onBtnMoreClick,
                onBtnInfoClick
            )
        binding.rvTasks.adapter = taskAdapter
    }

    fun showSettingsPopupMenu(anchor: View) {
        val inflater = LayoutInflater.from(anchor.context)
        val popupView = inflater.inflate(R.layout.popup_settings, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val copyItem = popupView.findViewById<AppCompatTextView>(R.id.menu_item_copy)
        val signOutItem = popupView.findViewById<AppCompatTextView>(R.id.menu_item_sign_out)

        copyItem.setOnClickListener {
            val groupId = SharedPreferencesRepository.getUser()?.groupId

            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("text", groupId)
            clipboard.setPrimaryClip(clip)

            showToast(R.string.id_copied)
            popupWindow.dismiss()
        }

        signOutItem.setOnClickListener {
            var result = FirebaseAuthRepository.signOut()
            result.onSuccess {
                SharedPreferencesRepository.clearUser()
                navigateToAuthorization()
                showToast(R.string.log_out_successful)
            }.onFailure {
                showToast(it.message.toString())
            }
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor)
    }

    fun showTaskPopupMenu(anchor: View, task: Task) {
        val inflater = LayoutInflater.from(anchor.context)
        val popupView = inflater.inflate(R.layout.popup_task, null)

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        val editItem = popupView.findViewById<AppCompatTextView>(R.id.menu_item_edit)
        val deleteItem = popupView.findViewById<AppCompatTextView>(R.id.menu_item_delete)

        editItem.setOnClickListener {
            var gson = Gson()
            var json = gson.toJson(task)
            val intent = Intent(this@MainActivity, AddTaskActivity::class.java)
            intent.putExtra("task", json)
            startActivity(intent)
            popupWindow.dismiss()
        }

        deleteItem.setOnClickListener {
            lifecycleScope.launch {
                var result = firestoreRepository.deleteTask(task)
                result.onSuccess {
                    showToast(R.string.task_deleted_successfully)
                    getTasks()
                }.onFailure {
                    showToast(it.message.toString())
                }
            }
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor)
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

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MainActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MainActivity, messageResId, duration).show()
    }
}