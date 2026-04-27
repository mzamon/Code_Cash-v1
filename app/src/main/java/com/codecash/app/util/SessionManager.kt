package com.codecash.app.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "codecash_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val NO_USER = -1L
    }

    fun saveSession(userId: Long, username: String, displayName: String) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_DISPLAY_NAME, displayName)
            .apply()
    }

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getDisplayName(): String = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
    fun isLoggedIn(): Boolean = getUserId() != NO_USER

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
