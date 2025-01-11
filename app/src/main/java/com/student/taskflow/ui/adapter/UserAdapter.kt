package com.student.taskflow.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.student.taskflow.R
import com.student.taskflow.databinding.ItemUserBinding
import com.student.taskflow.model.User
import com.student.taskflow.model.enums.Role

class UserAdapter(
    private var users: List<User>,
    private val isAdmin: Boolean,
    private val onBtnDelete: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val binding = holder.binding
        val user = users[position]

        binding.txUserName.text = user.name.first().toString().uppercase()
        binding.txTitle.text = user.name
        binding.txPosition.text =
            binding.root.context.getString(R.string.position_line, user.position)
        binding.txEmail.text = binding.root.context.getString(R.string.email_line, user.email)
        binding.txMobileNumber.text =
            binding.root.context.getString(R.string.mobile_number_line, user.mobileNumber)

        if (isAdmin) {
            binding.btnDelete.visibility = View.VISIBLE
        } else {
            binding.btnDelete.visibility = View.INVISIBLE
        }

        if (user.role == Role.ADMIN) {
            binding.btnDelete.visibility = View.INVISIBLE
        }

        binding.btnDelete.setOnClickListener {
            onBtnDelete(user)
        }
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun updateUsers(newUserList: List<User>) {
        users = newUserList
        notifyDataSetChanged()
    }
}
