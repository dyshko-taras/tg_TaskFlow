package com.student.taskflow.repository.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.student.taskflow.repository.local.SharedPreferencesRepository
import com.student.taskflow.util.Result
import kotlinx.coroutines.tasks.await

object FirebaseAuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId == null) {
                return Result.Failure("Sign-in failed: User ID is null")
            }
            SharedPreferencesRepository.setClientId(userId)
            return Result.Success("Sign-in successful")
        } catch (e: FirebaseAuthException) {

            return when (e.errorCode) {
                "ERROR_INVALID_CREDENTIAL" -> Result.Failure("Invalid credentials provided.")
                "ERROR_INVALID_EMAIL" -> Result.Failure("Invalid email address.")
                "ERROR_WRONG_PASSWORD" -> Result.Failure("Incorrect password.")
                "ERROR_USER_NOT_FOUND" -> Result.Failure("No user found with this email.")
                else -> Result.Failure("Authentication failed: ${e.message}")
            }
        } catch (e: Exception) {
            return Result.Failure("Sign-in failed: ${e.localizedMessage}")
        }
    }

    suspend fun registerWithEmailAndPassword(email: String, password: String): Result {
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid

            if (userId == null) {
                return Result.Failure("Registration failed: User ID is null")
            }
            SharedPreferencesRepository.setClientId(userId)
            return Result.Success("Registration successful")
        } catch (e: FirebaseAuthException) {
            return Result.Failure("Registration failed: ${e.message}")
        } catch (e: Exception) {
            return Result.Failure("Registration failed: ${e.localizedMessage}")
        }
    }

    suspend fun resetPassword(email: String): Result {
        try {
            auth.sendPasswordResetEmail(email).await()
            return Result.Success("Password reset email sent successfully")
        } catch (e: FirebaseAuthException) {
            return Result.Failure("Failed to send password reset email: ${e.message}")
        } catch (e: Exception) {
            return Result.Failure("Failed to send password reset email: ${e.localizedMessage}")
        }
    }

    fun signOut(): Result {
        try {
            auth.signOut()
            SharedPreferencesRepository.clearClientId()
            return Result.Success("Sign-out successful")
        } catch (e: FirebaseAuthException) {
            return Result.Failure("Sign-out failed: ${e.message}")
        } catch (e: Exception) {
            return Result.Failure("Sign-out failed: ${e.localizedMessage}")
        }
    }
}