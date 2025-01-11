package com.student.taskflow.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.student.taskflow.R
import com.student.taskflow.databinding.ItemPollBinding
import com.student.taskflow.model.Poll
import com.student.taskflow.model.User

class PollAdapter(
    private var polls: List<Poll>,
    private val isAdmin: Boolean,
    private val user: User,
    private val onItemClick: (Poll) -> Unit,
    private val onBtnDelete: (Poll) -> Unit
) : RecyclerView.Adapter<PollAdapter.PollViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollViewHolder {
        val binding = ItemPollBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PollViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PollViewHolder, position: Int) {
        val binding = holder.binding
        val poll = polls[position]

        binding.txTitle.text = poll.title
        binding.txDescription.text = poll.description

        var isVoted = poll.votedUsers.contains(user.id)

        if (isVoted) {
            binding.txVoted.text = "+"
            var colorGreen = ContextCompat.getColor(binding.root.context, R.color.green)
            binding.txVoted.setTextColor(colorGreen)
        } else {
            binding.txVoted.text = "?"
            var colorRed = ContextCompat.getColor(binding.root.context, R.color.red)
            binding.txVoted.setTextColor(colorRed)
        }

        if (isAdmin) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.INVISIBLE
        }
        binding.btnDelete.setOnClickListener { onBtnDelete(poll) }

        binding.root.setOnClickListener { onItemClick(poll) }

    }

    override fun getItemCount(): Int = polls.size

    inner class PollViewHolder(val binding: ItemPollBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun updatePolls(newPollList: List<Poll>) {
        polls = newPollList
        notifyDataSetChanged()
    }

}