package com.example.bettersneaker.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("better_sneaker_prefs", Context.MODE_PRIVATE)

    fun saveSession(token: String?, role: String?) {
        if (!token.isNullOrEmpty() && !role.isNullOrEmpty()) {
            prefs.edit().putString("auth_token", token).putString("user_role", role).apply()
        }
    }

    fun getToken(): String? {
        return prefs.getString("auth_token", null)
    }

    fun getUserRole(): String? {
        return prefs.getString("user_role", null)
    }

    fun logout() {
        prefs.edit().remove("auth_token").remove("user_role").apply()
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
}
