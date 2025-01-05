package com.student.taskflow.model.enums


enum class Status {
    NOT_STARTED, IN_PROGRESS, COMPLETED, UNDER_REVIEW;

    companion object {
        fun fromValue(value: String): Status {
            return try {
                Status.valueOf(value)
            } catch (e: IllegalArgumentException) {
                NOT_STARTED
            }
        }
    }
}