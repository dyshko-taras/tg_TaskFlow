package com.student.taskflow.util

sealed class Result {
    data class Success(val message: String) : Result()
    data class Failure(val message: String) : Result()
}