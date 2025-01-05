package com.student.taskflow.model.enums

enum class Priority {
    LOW, MEDIUM, HIGH;

    companion object {
        fun fromValue(value: String): Priority {
            return try {
                Priority.valueOf(value)
            } catch (e: IllegalArgumentException) {
                LOW
            }
        }
    }
}