package com.student.taskflow.util

fun String.containsDigit(): Boolean {
    return this.any { it.isDigit() }
}