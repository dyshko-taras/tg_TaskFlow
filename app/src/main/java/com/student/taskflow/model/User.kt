package com.student.taskflow.model

import com.student.taskflow.model.enums.Role

data class User(
    var id: String = "",
    var groupId: String = "",
    val role: Role = Role.EMPLOYEE,
    val name: String = "",
    val email: String = "",
    val position: String = "",
    val mobileNumber: String = "",
)

