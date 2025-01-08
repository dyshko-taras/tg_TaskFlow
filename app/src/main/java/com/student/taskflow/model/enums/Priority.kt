package com.student.taskflow.model.enums

import android.content.Context
import com.student.taskflow.R

enum class Priority {
    LOW, MEDIUM, HIGH;

    companion object {
        fun fromString(context: Context, priority: String): Priority {
            return when (priority.lowercase()) {
                context.getString(R.string.low).lowercase() -> LOW
                context.getString(R.string.medium).lowercase() -> MEDIUM
                context.getString(R.string.high).lowercase() -> HIGH
                else -> throw IllegalArgumentException("Invalid priority: $priority")
            }
        }
    }
}