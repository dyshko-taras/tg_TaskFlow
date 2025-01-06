package com.student.taskflow.repository.network

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.student.taskflow.model.Group
import com.student.taskflow.model.User
import kotlinx.coroutines.tasks.await

class FirebaseFirestoreRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private fun getCollection(collectionName: String) = db.collection(collectionName)

    private val collectionUsers = "users"
    private val collectionGroups = "groups"


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
                Result.failure(Exception("User not found in Firestore"))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get user from Firestore: ${e.message}"))
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
            return Result.success(document.exists())
        } catch (e: FirebaseFirestoreException) {
            Result.failure(Exception("Failed to get group from Firestore: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}