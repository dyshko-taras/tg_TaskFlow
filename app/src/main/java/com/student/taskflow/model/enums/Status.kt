package com.student.taskflow.model.enums

enum class Status {
    NOT_STARTED, IN_PROGRESS, COMPLETED, UNDER_REVIEW;

    companion object {
        fun fromString(status: String): Status {
            return when (status.lowercase()) {
                "not started" -> NOT_STARTED
                "in progress" -> IN_PROGRESS
                "completed" -> COMPLETED
                "under review" -> UNDER_REVIEW
                else -> throw IllegalArgumentException("Invalid status: $status")
            }
        }
    }
}