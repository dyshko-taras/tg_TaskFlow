package com.student.taskflow.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityAddPollBinding
import com.student.taskflow.model.OptionsItem
import com.student.taskflow.model.Poll
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import kotlinx.coroutines.launch

class AddPollActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPollBinding
    private var firestoreRepository = FirebaseFirestoreRepository()
    private var user = SharedPreferencesRepository.getUser()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddPollBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setListener()
    }

    private fun setListener() {
        binding.btnCancel.setOnClickListener {
            navigateToPolls()
        }

        binding.btnSave.setOnClickListener {
            var groupId = user.groupId
            var title = binding.textInputEditTextTitle.text.toString()
            var description = binding.textInputEditTextDescription.text.toString()
            var options1 = binding.textInputEditTextOption1.text.toString()
            var options2 = binding.textInputEditTextOption2.text.toString()
            var options3 = binding.textInputEditTextOption3.text.toString()

            var isFieldsEmpty =
                title.isEmpty() || description.isEmpty() || options1.isEmpty() || options2.isEmpty() || options3.isEmpty()

            if (isFieldsEmpty) {
                showToast(R.string.fill_all_fields)
                return@setOnClickListener
            }

            var poll = Poll(
                id = "",
                groupId = groupId,
                title = title,
                description = description,
                options = listOf(
                    OptionsItem(text = options1, votes = 0),
                    OptionsItem(text = options2, votes = 0),
                    OptionsItem(text = options3, votes = 0),
                ),
                votedUsers = mutableListOf(),
            )

            savePoll(poll)
        }
    }

    private fun savePoll(poll: Poll) {
        lifecycleScope.launch {
            var result = firestoreRepository.addPoll(poll)
            result.onSuccess { message ->
                showToast(R.string.poll_saved_successfully)
                navigateToPolls()
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun navigateToPolls() {
        val intent = Intent(this@AddPollActivity, PollsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@AddPollActivity, messageResId, duration).show()
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@AddPollActivity, message, duration).show()
    }
}