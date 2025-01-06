package com.student.taskflow.repository.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.student.taskflow.model.User

object SharedPreferencesRepository {
    private lateinit var sharedPreferences: SharedPreferences
    private var gson = Gson()

    private const val USER_KEY = "user"

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    fun setUser(user: User) {
        var json = gson.toJson(user)
        sharedPreferences.edit().putString(USER_KEY, json).apply()
    }

    fun getUser(): User? {
        var json = sharedPreferences.getString(USER_KEY, null)
        return gson.fromJson(json, User::class.java)
    }

    fun clearUser() {
        sharedPreferences.edit().remove(USER_KEY).apply()
    }

    fun isLoggedIn(): Boolean {
        return getUser() != null
    }
}
