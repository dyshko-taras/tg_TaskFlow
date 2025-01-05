package com.student.taskflow.model.enums

enum class Role {
    ADMIN, WORKER;

    companion object {
        fun fromValue(value: String): Role {
            return try {
                valueOf(value)
            } catch (e: IllegalArgumentException) {
                ADMIN
            }
        }
    }
}