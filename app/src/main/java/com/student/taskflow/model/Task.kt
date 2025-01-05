package com.student.taskflow.model

import com.student.taskflow.model.enums.Priority
import com.student.taskflow.model.enums.Status

data class Task(
    val id: String,
    val groupId: String,
    val title: String,
    val description: String,
    val priority: Priority,
    val status: Status,
    val deadline: String,
    val assignedTo: String
)

