package com.student.taskflow.model

import com.google.firebase.firestore.DocumentId

data class Poll(
    @DocumentId val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val options: List<OptionsItem> = emptyList(),
    var votedUsers: MutableList<String> = mutableListOf()
)