package com.student.taskflow.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.radiobutton.MaterialRadioButton
import com.student.taskflow.R
import com.student.taskflow.databinding.ActivityPollsBinding
import com.student.taskflow.model.Poll
import com.student.taskflow.model.enums.Role
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.repository.network.FirebaseFirestoreRepository
import com.student.taskflow.ui.adapter.PollAdapter
import kotlinx.coroutines.launch

class PollsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPollsBinding
    private lateinit var pollAdapter: PollAdapter
    private var isFirstGetPolls = true
    private val firestoreRepository = FirebaseFirestoreRepository()
    private val user = SharedPreferencesRepository.getUser()!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPollsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupUI()
        setListener()
        getPolls()
    }

    private fun setupUI() {
        binding.btnAddPoll.visibility =
            if (user.role == Role.ADMIN) View.VISIBLE else View.INVISIBLE
    }

    private fun setListener() {
        binding.btnHome.setOnClickListener {
            navigateToMain()
        }

        binding.btnMyGroup.setOnClickListener {
            navigateToMyGroup()
        }

        binding.btnAddPoll.setOnClickListener {
            navigateToAddPoll()
        }
    }

    private fun getPolls() {
        lifecycleScope.launch {
            val result = firestoreRepository.getListOfPolls(user.groupId)
            result.onSuccess { polls ->
                if (isFirstGetPolls) {
                    initRecyclerViewPolls(polls)
                    isFirstGetPolls = false
                } else {
                    pollAdapter.updatePolls(polls)
                }
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun initRecyclerViewPolls(polls: List<Poll>) {
        var isAdmin = user.role == Role.ADMIN

        var onBtnDelete: ((Poll) -> Unit) = { poll ->
            lifecycleScope.launch {
                firestoreRepository.deletePoll(poll).onSuccess {
                    getPolls()
                }.onFailure { error ->
                    showToast(error.message.toString())
                }
            }
        }

        var onItemClick: ((Poll) -> Unit) = { poll ->
            if (poll.votedUsers.contains(user.id)) {
                showAlertDialogPollResult(poll)
            } else {
                showAlertDialogPollActive(poll)
            }
        }

        pollAdapter = PollAdapter(polls, isAdmin, user, onItemClick, onBtnDelete)
        binding.rvPolls.adapter = pollAdapter

        hideLoading()
    }

    private fun showAlertDialogPollActive(poll: Poll) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_poll_active, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var tvTitle = view.findViewById<AppCompatTextView>(R.id.tvTitle)
        var btnClose = view.findViewById<AppCompatImageView>(R.id.btnClose)
        var tvDescription = view.findViewById<AppCompatTextView>(R.id.tvDescription)
        var rgOptions = view.findViewById<RadioGroup>(R.id.rgOptions)
        var rbOption1 = view.findViewById<MaterialRadioButton>(R.id.rbOption1)
        var rbOption2 = view.findViewById<MaterialRadioButton>(R.id.rbOption2)
        var rbOption3 = view.findViewById<MaterialRadioButton>(R.id.rbOption3)
        var btnCancel = view.findViewById<AppCompatButton>(R.id.btnCancel)
        var btnSave = view.findViewById<AppCompatButton>(R.id.btnSave)

        tvTitle.text = poll.title
        tvDescription.text = poll.description

        rbOption1.text = poll.options[0].text
        rbOption2.text = poll.options[1].text
        rbOption3.text = poll.options[2].text

        var indexSelectedOption = -1
        rgOptions.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbOption1 -> indexSelectedOption = 0
                R.id.rbOption2 -> indexSelectedOption = 1
                R.id.rbOption3 -> indexSelectedOption = 2
            }
        }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            if (indexSelectedOption == -1) {
                showToast(R.string.select_option)
                return@setOnClickListener
            } else {
                poll.votedUsers.add(user.id)
                poll.options[indexSelectedOption].votes++
                updatePoll(poll, dialog)
            }
        }
        dialog.show()
    }

    @SuppressLint("DefaultLocale")
    private fun showAlertDialogPollResult(poll: Poll) {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_poll_result, null)
        builder.setView(view)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var tvTitle = view.findViewById<AppCompatTextView>(R.id.tvTitle)
        var btnClose = view.findViewById<AppCompatImageView>(R.id.btnClose)
        var tvDescription = view.findViewById<AppCompatTextView>(R.id.tvDescription)
        var tvOption1 = view.findViewById<AppCompatTextView>(R.id.tvOption1)
        var tvOption2 = view.findViewById<AppCompatTextView>(R.id.tvOption2)
        var tvOption3 = view.findViewById<AppCompatTextView>(R.id.tvOption3)
        var tvOptionPercentage1 = view.findViewById<AppCompatTextView>(R.id.tvOptionPercentage1)
        var tvOptionPercentage2 = view.findViewById<AppCompatTextView>(R.id.tvOptionPercentage2)
        var tvOptionPercentage3 = view.findViewById<AppCompatTextView>(R.id.tvOptionPercentage3)
        var lpiOption1 = view.findViewById<LinearProgressIndicator>(R.id.lpiOption1)
        var lpiOption2 = view.findViewById<LinearProgressIndicator>(R.id.lpiOption2)
        var lpiOption3 = view.findViewById<LinearProgressIndicator>(R.id.lpiOption3)

        tvTitle.text = poll.title
        tvDescription.text = poll.description

        tvOption1.text = poll.options[0].text
        tvOption2.text = poll.options[1].text
        tvOption3.text = poll.options[2].text

        var totalVotes = poll.votedUsers.size
        var percentage1 = poll.options[0].votes.toFloat() / totalVotes.toFloat() * 100
        var percentage2 = poll.options[1].votes.toFloat() / totalVotes.toFloat() * 100
        var percentage3 = poll.options[2].votes.toFloat() / totalVotes.toFloat() * 100

        tvOptionPercentage1.text = String.format("%.1f%%", percentage1)
        tvOptionPercentage2.text = String.format("%.1f%%", percentage2)
        tvOptionPercentage3.text = String.format("%.1f%%", percentage3)

        lpiOption1.progress = percentage1.toInt()
        lpiOption2.progress = percentage2.toInt()
        lpiOption3.progress = percentage3.toInt()

        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun updatePoll(poll: Poll, dialog: AlertDialog) {
        lifecycleScope.launch {
            var result = firestoreRepository.updatePoll(poll)
            result.onSuccess { message ->
                showToast(R.string.poll_updated_successfully)
                getPolls()
                dialog.dismiss()
            }.onFailure { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this@PollsActivity, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToMyGroup() {
        val intent = Intent(this@PollsActivity, MyGroupActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0)
    }

    private fun navigateToAddPoll() {
        val intent = Intent(this@PollsActivity, AddPollActivity::class.java)
        startActivity(intent)
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@PollsActivity, message, duration).show()
    }

    fun showToast(@StringRes messageResId: Int, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this@PollsActivity, messageResId, duration).show()
    }
}