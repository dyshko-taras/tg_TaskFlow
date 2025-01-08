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
import com.skydoves.powerspinner.IconSpinnerAdapter
import com.skydoves.powerspinner.IconSpinnerItem
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityAddTaskBinding
import com.student.taskflow.model.Task
import com.student.taskflow.model.enums.Priority
import com.student.taskflow.model.enums.Status
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private var firestoreRepository = FirebaseFirestoreRepository()
    private var user = SharedPreferencesRepository.getUser()!!
    private var selectedDate: String = LocalDate.now().toString()
    private var selectedPriority: String = ""
    private var selectedStatus: String = ""
    private var selectedEmployee: String = ""

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
        setItemsForSpinner()
        setListener()
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

    private fun setItemsForSpinner() {
        lifecycleScope.launch {
            var groupId = user.groupId
            var result = firestoreRepository.getListOfEmployees(groupId)

            result.onSuccess { users ->
                binding.spinnerEmployee.setItems(users.map { it.name })
            }.onFailure { error ->
                showToast(error.message.toString())
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
            selectedEmployee = newText
        }

        binding.btnSave.setOnClickListener {
            var groupId = user.groupId
            var title = binding.textInputEditTextTitle.text.toString()
            var description = binding.textInputEditTextDescription.text.toString()

            var isFieldsEmpty =
                title.isEmpty() || description.isEmpty() || selectedPriority.isEmpty() || selectedStatus.isEmpty() || selectedEmployee.isEmpty()

            if (isFieldsEmpty) {
                showToast(R.string.fill_all_fields)
                return@setOnClickListener
            }

            var task = Task(
                groupId = groupId,
                title = title,
                description = description,
                priority = Priority.fromString(selectedPriority),
                status = Status.fromString(selectedStatus),
                deadline = selectedDate,
                assignedTo = selectedEmployee,
                isVerified = false
            )
            saveTask(task)
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