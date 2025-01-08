package com.student.taskflow.ui.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
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
    private val onItemClick: (Task) -> Unit
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

        when (task.status) {
            Status.NOT_STARTED -> binding.viewStatus.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.orange))

            Status.IN_PROGRESS -> binding.viewStatus.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.orange))

            Status.COMPLETED -> binding.viewStatus.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.green))

            Status.UNDER_REVIEW -> binding.viewStatus.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(binding.root.context, R.color.blue))
        }

        when (task.priority) {
            Priority.LOW -> {
                binding.txPriority.text = binding.root.context.getString(R.string.low)
                binding.txPriority.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.red)
                )
            }

            Priority.MEDIUM -> {
                binding.txPriority.text = binding.root.context.getString(R.string.medium)
                binding.txPriority.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.orange)
                )
            }

            Priority.HIGH -> {
                binding.txPriority.text = binding.root.context.getString(R.string.high)
                binding.txPriority.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.green)
                )
            }

        }
        binding.root.setOnClickListener {
            onItemClick(task)
        }
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {}
}
