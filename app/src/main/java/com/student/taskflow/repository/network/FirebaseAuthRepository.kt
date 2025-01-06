package com.student.taskflow.repository.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.student.taskflow.repository.local.SharedPreferencesRepository
import kotlinx.coroutines.tasks.await

object FirebaseAuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<String> {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId == null) {
                return Result.failure(Exception("Sign-in failed: User ID is null"))
            }
            return Result.success(userId)
        } catch (e: FirebaseAuthException) {
            return Result.failure(Exception("Sign-in failed: ${e.message}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun registerWithEmailAndPassword(email: String, password: String): Result<String> {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId == null) {
                return Result.failure(Exception("Registration failed: User ID is null"))
            }
            return Result.success(userId)
        } catch (e: FirebaseAuthException) {
            return Result.failure(Exception("Registration failed: ${e.message}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    suspend fun resetPassword(email: String): Result<String> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success("Password reset email sent successfully")
        } catch (e: FirebaseAuthException) {
            Result.failure(Exception("Failed to send password reset email: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut(): Result<String> {
        try {
            auth.signOut()
            SharedPreferencesRepository.clearUser()
            return Result.success("Sign-out successful")
        } catch (e: FirebaseAuthException) {
            return Result.failure(Exception("Sign-out failed: ${e.message}"))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}