package com.student.taskflow.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.student.taskflow.R
import com.student.taskflow.databinding.ItemTaskBinding
import com.student.taskflow.model.Task
import com.student.taskflow.model.enums.Priority
import com.student.taskflow.model.enums.Status

class TaskAdapter(
    private val tasks: List<Task>,
    private val isAdmin: Boolean,
    private val onCheckBoxClick: (Task) -> Unit,
    private val onBtnMoreClick: (View, Task) -> Unit,
    private val onBtnInfoClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val binding = holder.binding
        val task = tasks[position]

        binding.cbTitle.isChecked = task.isVerified
        binding.cbTitle.text = task.title
        binding.txDescription.text = task.description
        binding.txDateline.text = binding.root.context.getString(R.string.due_date, task.deadline)

        when (task.priority) {
            Priority.LOW -> binding.viewPriority.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.green))

            Priority.MEDIUM -> binding.viewPriority.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.orange))

            Priority.HIGH -> binding.viewPriority.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.red))
        }

        when (task.status) {
            Status.NOT_STARTED -> {
                binding.txStatus.text = binding.root.context.getString(R.string.not_started)
                binding.txStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.red)
                )
            }

            Status.IN_PROGRESS -> {
                binding.txStatus.text = binding.root.context.getString(R.string.in_progress)
                binding.txStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.orange)
                )
            }

            Status.COMPLETED -> {
                binding.txStatus.text = binding.root.context.getString(R.string.completed)
                binding.txStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.green)
                )
            }

            Status.UNDER_REVIEW -> {
                binding.txStatus.text = binding.root.context.getString(R.string.under_review)
                binding.txStatus.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.purple)
                )
            }
        }

        binding.cbTitle.setOnCheckedChangeListener { _, isChecked ->
            task.isVerified = isChecked
            onCheckBoxClick(task)
        }

        binding.btnMore.visibility = if (isAdmin) View.VISIBLE else View.INVISIBLE
        binding.btnMore.setOnClickListener {
            onBtnMoreClick(binding.btnMore, task)
        }

        binding.btnInfo.visibility = if (!isAdmin) View.VISIBLE else View.INVISIBLE
        binding.btnInfo.setOnClickListener {
            onBtnInfoClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {}
}
