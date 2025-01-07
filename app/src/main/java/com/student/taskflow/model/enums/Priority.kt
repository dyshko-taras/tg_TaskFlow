package com.student.taskflow.model.enums

enum class Priority {
    LOW, MEDIUM, HIGH;

    companion object {
        fun fromString(priority: String): Priority {
            return when (priority.lowercase()) {
                "low" -> LOW
                "medium" -> MEDIUM
                "high" -> HIGH
                else -> throw IllegalArgumentException("Invalid priority: $priority")
            }
        }
    }
}