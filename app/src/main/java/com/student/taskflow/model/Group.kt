package com.student.taskflow.model

import com.google.firebase.firestore.DocumentId

data class Group(
    @DocumentId val id: String,
    val name: String,
    val adminId: String
)
