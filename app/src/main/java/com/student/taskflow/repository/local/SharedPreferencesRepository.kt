package com.student.taskflow.repository.local

import android.content.Context
import android.content.SharedPreferences

object SharedPreferencesRepository {
    private lateinit var sharedPreferences: SharedPreferences
    private const val CLIENT_ID_KEY = "clientId"

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

    fun setClientId(clientId: String) {
        sharedPreferences.edit()
            .putString(CLIENT_ID_KEY, clientId)
            .apply()
    }

    fun getClientId(): String? {
        return sharedPreferences.getString(CLIENT_ID_KEY, null)
    }

    fun clearClientId() {
        sharedPreferences.edit()
            .remove(CLIENT_ID_KEY)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return getClientId() != null
    }
}
