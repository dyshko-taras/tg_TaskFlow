package com.student.taskflow.model

import com.google.firebase.firestore.DocumentId
import com.student.taskflow.model.enums.Priority
import com.student.taskflow.model.enums.Status

data class Task(
    @DocumentId val id: String = "",
    val groupId: String = "",
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.LOW,
    var status: Status = Status.NOT_STARTED,
    val deadline: String = "",
    val assignedToEmployeeId: String = "",
    var isVerified: Boolean = false
)

