package com.student.taskflow.repository.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.student.taskflow.model.Group
import com.student.taskflow.model.Poll
import com.student.taskflow.model.Task
import com.student.taskflow.model.User
import com.student.taskflow.model.enums.Role
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private fun getCollection(collectionName: String) = db.collection(collectionName)

    private val collectionUsers = "users"
    private val collectionGroups = "groups"
    private val collectionTasks = "tasks"
    private val collectionPolls = "polls"


    suspend fun addUser(user: User): Result<Unit> {
        return try {
            getCollection(collectionUsers).document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to save user to Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = getCollection(collectionUsers).document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get user from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAdmin(groupId: String): Result<User> {
        return try {
            val document = getCollection(collectionUsers)
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("role", Role.ADMIN.name)
                .get()
                .await()
            val user = document.documents.firstOrNull()?.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("User not found in Firestore"))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get user from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListOfEmployees(groupId: String): Result<List<User>> {
        return try {
            val document = getCollection(collectionUsers)
                .whereEqualTo("groupId", groupId)
                .whereEqualTo("role", Role.EMPLOYEE.name)
                .get()
                .await()

            val users = document.documents.mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get user from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            getCollection(collectionUsers).document(user.id).delete().await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to delete user from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addGroup(group: Group): Result<Unit> {
        return try {
            getCollection(collectionGroups).document(group.id).set(group).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to save group to Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isGroupExists(groupId: String): Result<Boolean> {
        return try {
            val document = getCollection(collectionGroups).document(groupId).get().await()
            Result.success(document.exists())
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get group from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addTask(task: Task): Result<Unit> {
        return try {
            getCollection(collectionTasks).add(task).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to save task to Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTask(task: Task): Result<Unit> {
        return try {
            getCollection(collectionTasks).document(task.id).set(task).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to update task in Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTask(task: Task): Result<Unit> {
        return try {
            getCollection(collectionTasks).document(task.id).delete().await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to delete task from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListOfTasks(groupId: String): Result<List<Task>> {
        return try {
            val document =
                getCollection(collectionTasks)
                    .orderBy("verified", Query.Direction.ASCENDING)
                    .whereEqualTo("groupId", groupId).get().await()
            val tasks = document.documents.mapNotNull { it.toObject(Task::class.java) }
            Result.success(tasks)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get task from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListOfTasksCurrentUser(userId: String): Result<List<Task>> {
        return try {
            val document =
                getCollection(collectionTasks)
                    .orderBy("verified", Query.Direction.ASCENDING)
                    .whereEqualTo("assignedToEmployeeId", userId).get().await()
            val tasks = document.documents.mapNotNull { it.toObject(Task::class.java) }
            Result.success(tasks)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get task from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPoll(poll: Poll): Result<Unit> {
        return try {
            getCollection(collectionPolls).add(poll).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to save poll to Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getListOfPolls(groupId: String): Result<List<Poll>> {
        return try {
            val document = getCollection(collectionPolls)
                .whereEqualTo("groupId", groupId).get().await()
            val polls = document.documents.mapNotNull { it.toObject(Poll::class.java) }
            Result.success(polls)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get poll from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePoll(poll: Poll): Result<Unit> {
        return try {
            getCollection(collectionPolls).document(poll.id).set(poll).await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to update poll in Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deletePoll(poll: Poll): Result<Unit> {
        return try {
            getCollection(collectionPolls).document(poll.id).delete().await()
            Result.success(Unit)
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to delete poll from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
