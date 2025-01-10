package com.student.taskflow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityAddTaskBinding
import com.student.taskflow.model.Task
import com.student.taskflow.model.User
import com.student.taskflow.model.enums.Priority
import com.student.taskflow.model.enums.Status
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private var firestoreRepository = FirebaseFirestoreRepository()
    private var user = SharedPreferencesRepository.getUser()!!
    private var selectedDate: String = LocalDate.now().toString()
    private var selectedPriority: String = ""
    private var selectedStatus: String = ""
    private var selectedEmployeeIndex: Int = -1
    private var editTask: Task? = null
    private var isVerified: Boolean = false
    private var listOfEmployees: MutableList<User> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, systemBars.bottom)
            insets
        }

        setupUI()
        setItemsForSpinnerEmployee()
        setListener()
        setEditTask()
    }

    private fun setupUI() {
        binding.spinnerStatus.setSpinnerAdapter(IconSpinnerAdapter(binding.spinnerStatus))
        binding.spinnerStatus.setItems(
            arrayListOf(
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
        )

        binding.spinnerPriority.setSpinnerAdapter(IconSpinnerAdapter(binding.spinnerPriority))
        binding.spinnerPriority.setItems(
            arrayListOf(
                IconSpinnerItem(
                    iconRes = R.drawable.shape_3,
                    text = ContextCompat.getString(this, R.string.low)
                ),
                IconSpinnerItem(
                    iconRes = R.drawable.shape_2,
                    text = ContextCompat.getString(this, R.string.medium)
                ),
                IconSpinnerItem(
                    iconRes = R.drawable.shape_1,
                    text = ContextCompat.getString(this, R.string.high)
                )
            )
        )
    }

    private fun setItemsForSpinnerEmployee() {
        lifecycleScope.launch {
            var groupId = user.groupId
            var result = firestoreRepository.getListOfEmployees(groupId)

            result.onSuccess { users ->
                listOfEmployees.addAll(users)
                binding.spinnerEmployee.setItems(listOfEmployees.map { it.name })

                if (editTask != null) {
                    var selectedEmployeeId = editTask!!.assignedToEmployeeId
                    var selectedIndex =
                        users.indexOfFirst { employee -> employee.id == selectedEmployeeId }
                    binding.spinnerEmployee.selectItemByIndex(selectedIndex)
                }
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun setEditTask() {
        val gson = Gson()
        val json = intent.getStringExtra("task")
        if (json != null) {
            editTask = gson.fromJson(json, Task::class.java)
            if (editTask != null) {
                binding.txTitle.text = getString(R.string.edit_task)

                binding.textInputEditTextTitle.setText(editTask!!.title)
                binding.textInputEditTextDescription.setText(editTask!!.description)

                var selectIndexStatus = when (editTask!!.status) {
                    Status.NOT_STARTED -> 0
                    Status.IN_PROGRESS -> 1
                    Status.COMPLETED -> 2
                    Status.UNDER_REVIEW -> 3
                }
                binding.spinnerStatus.selectItemByIndex(selectIndexStatus)

                var selectIndexPriority = when (editTask!!.priority) {
                    Priority.LOW -> 0
                    Priority.MEDIUM -> 1
                    Priority.HIGH -> 2
                }
                binding.spinnerPriority.selectItemByIndex(selectIndexPriority)


                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = sdf.parse(editTask!!.deadline)
                val timestamp = date?.time ?: System.currentTimeMillis()
                binding.calendarView.setDate(timestamp, true, true)

                isVerified = editTask!!.isVerified
            }
        }
    }

    private fun setListener() {
        binding.btnCancel.setOnClickListener {
            navigateToMain()
        }

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth).toString()
        }

        binding.spinnerStatus.setOnSpinnerItemSelectedListener<IconSpinnerItem> { oldIndex, oldItem, newIndex, newItem ->
            selectedStatus = newItem.text.toString()
        }

        binding.spinnerPriority.setOnSpinnerItemSelectedListener<IconSpinnerItem> { oldIndex, oldItem, newIndex, newText ->
            selectedPriority = newText.text.toString()
        }

        binding.spinnerEmployee.setOnSpinnerItemSelectedListener<String> { oldIndex, oldItem, newIndex, newText ->
            selectedEmployeeIndex = newIndex
        }

        binding.btnSave.setOnClickListener {
            var groupId = user.groupId
            var title = binding.textInputEditTextTitle.text.toString()
            var description = binding.textInputEditTextDescription.text.toString()

            var isFieldsEmpty =
                title.isEmpty() || description.isEmpty() || selectedPriority.isEmpty() || selectedStatus.isEmpty() || selectedEmployeeIndex == -1

            if (isFieldsEmpty) {
                showToast(R.string.fill_all_fields)
                return@setOnClickListener
            }

            var task = Task(
                id = editTask?.id ?: "",
                groupId = groupId,
                title = title,
                description = description,
                priority = Priority.fromString(this, selectedPriority),
                status = Status.fromString(this, selectedStatus),
                deadline = selectedDate,
                assignedToEmployeeId = listOfEmployees[selectedEmployeeIndex].id,
                isVerified = isVerified
            )

            if (editTask != null) updateTask(task) else saveTask(task)
        }
    }

    private fun saveTask(task: Task) {
        lifecycleScope.launch {
            var result = firestoreRepository.addTask(task)
            result.onSuccess { message ->
                showToast(R.string.task_saved_successfully)
                navigateToMain()
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun updateTask(task: Task) {
        lifecycleScope.launch {
            var result = firestoreRepository.updateTask(task)
            result.onSuccess { message ->
                showToast(R.string.task_updated_successfully)
                navigateToMain()
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@AddTaskActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@AddTaskActivity, messageResId, duration).show()
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@AddTaskActivity, message, duration).show()
    }
}