package com.student.taskflow.model.enums

import android.content.Context
import com.student.taskflow.R

enum class Status {
    NOT_STARTED, IN_PROGRESS, COMPLETED, UNDER_REVIEW;

    companion object {
        fun fromString(context: Context, status: String): Status {
            return when (status.lowercase()) {
                context.getString(R.string.not_started).lowercase() -> NOT_STARTED
                context.getString(R.string.in_progress).lowercase() -> IN_PROGRESS
                context.getString(R.string.completed).lowercase() -> COMPLETED
                context.getString(R.string.under_review).lowercase() -> UNDER_REVIEW
                else -> throw IllegalArgumentException("Invalid status: $status")
            }
        }
    }
}