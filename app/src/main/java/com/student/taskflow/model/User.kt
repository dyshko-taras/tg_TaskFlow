package com.student.taskflow.model

import com.student.taskflow.model.enums.Role

data class User(
    val id: String,
    val groupId: String,
    val role: Role,
    val name: String,
    val email: String
)

