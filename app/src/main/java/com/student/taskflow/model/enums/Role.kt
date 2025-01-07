package com.student.taskflow.model.enums

enum class Role {
    ADMIN, EMPLOYEE;

    companion object {
        fun getRoleByString(role: String): Role {
            return when (role.lowercase()) {
                "admin" -> ADMIN
                "employee" -> EMPLOYEE
                else -> throw IllegalArgumentException("Invalid role: $role")
            }
        }
    }
}