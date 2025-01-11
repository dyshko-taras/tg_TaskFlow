package com.student.taskflow.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.skydoves.powerspinner.PowerSpinnerView
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityMainBinding
import com.student.taskflow.model.Task
import com.student.taskflow.model.enums.Role
import com.student.taskflow.model.enums.Status
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseAuthRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import com.student.taskflow.ui.adapter.TaskAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter
    private var isFirstGetTasks = true
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
        binding.txUserName.text = user.name.first().toString().uppercase()

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

        binding.btnMyGroup.setOnClickListener {
            navigateToMyGroup()
        }

        binding.btnPolls.setOnClickListener {
            navigateToPolls()
        }
    }

    private fun getTasks() {
        lifecycleScope.launch {
            val result =
                if (user.role == Role.ADMIN) {
                    firestoreRepository.getListOfTasks(user.groupId)
                } else {
                    firestoreRepository.getListOfTasksCurrentUser(user.id)
                }
            result.onSuccess { tasks ->
                if (isFirstGetTasks) {
                    initRecyclerViewTasks(tasks)
                    isFirstGetTasks = false
                } else {
                    taskAdapter.updateTasks(tasks)
                }
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun initRecyclerViewTasks(tasks: List<Task>) {
        var isAdmin = user.role == Role.ADMIN

        var onCheckBoxClick: ((Task) -> Unit) = { task -> updateTask(task, null) }
        var onBtnMoreClick: ((anchor: View, Task) -> Unit) =
            { anchor, task -> showTaskPopupMenu(anchor, task) }
        var onBtnInfoClick: ((Task) -> Unit) = { task -> showAlertDialogTask(task) }

        taskAdapter =
            TaskAdapter(
                tasks,
                isAdmin,
                onCheckBoxClick,
                onBtnMoreClick,
                onBtnInfoClick
            )
        binding.rvTasks.adapter = taskAdapter

        hideLoading()
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

    private fun showAlertDialogTask(selectedTask: Task) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_task_info, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var tvTitle = view.findViewById<AppCompatTextView>(R.id.tvTitle)
        var btnClose = view.findViewById<AppCompatImageView>(R.id.btnClose)
        var spinnerStatus = view.findViewById<PowerSpinnerView>(R.id.spinnerStatus)
        var tvDescription = view.findViewById<AppCompatTextView>(R.id.tvDescription)
        var txDateline = view.findViewById<AppCompatTextView>(R.id.txDateline)
        var btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        var btnSave = view.findViewById<AppCompatButton>(R.id.btnSave)

        tvTitle.text = selectedTask.title
        tvDescription.text = selectedTask.description
        txDateline.text = binding.root.context.getString(R.string.due_date, selectedTask.deadline)

        var itemsStatus = arrayListOf(
            IconSpinnerItem(
                iconRes = R.drawable.shape_1,
                text = ContextCompat.getString(this, R.string.not_started)
            ),
            IconSpinnerItem(
                iconRes = R.drawable.shape_2,
                text = ContextCompat.getString(this, R.string.in_progress)
            ),
            IconSpinnerItem(
                iconRes = R.drawable.shape_3,
                text = ContextCompat.getString(this, R.string.completed)
            ),
            IconSpinnerItem(
                iconRes = R.drawable.shape_4,
                text = ContextCompat.getString(this, R.string.under_review)
            )
        )
        spinnerStatus.setSpinnerAdapter(IconSpinnerAdapter(spinnerStatus))
        spinnerStatus.setItems(itemsStatus)

        var selectIndexStatus = when (selectedTask.status) {
            Status.NOT_STARTED -> 0
            Status.IN_PROGRESS -> 1
            Status.COMPLETED -> 2
            Status.UNDER_REVIEW -> 3
        }
        spinnerStatus.selectItemByIndex(selectIndexStatus)

        var selectedStatus = itemsStatus[selectIndexStatus].text.toString()
        spinnerStatus.setOnSpinnerItemSelectedListener<IconSpinnerItem> { oldIndex, oldItem, newIndex, newItem ->
            selectedStatus = newItem.text.toString()
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            selectedTask.status = Status.fromString(this, selectedStatus)
            updateTask(selectedTask, dialog)
        }

        dialog.show()
    }

    private fun updateTask(task: Task, dialog: AlertDialog?) {
        lifecycleScope.launch {
            var result = firestoreRepository.updateTask(task)
            result.onSuccess { message ->
                showToast(R.string.task_updated_successfully)
                getTasks()
                dialog?.dismiss()
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
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

    private fun navigateToMyGroup() {
        val intent = Intent(this@MainActivity, MyGroupActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToPolls() {
        val intent = Intent(this@MainActivity, PollsActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MainActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@MainActivity, messageResId, duration).show()
    }
}