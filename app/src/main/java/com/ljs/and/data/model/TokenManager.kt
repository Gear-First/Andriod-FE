package com.ljs.and.data.model

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val REFRESH_TOKEN = "refresh_token"

    private var accessToken: String? = null

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAccessToken(): String? {
        return accessToken
    }

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    fun getRefreshToken(context: Context): String? {
        return getPreferences(context).getString(REFRESH_TOKEN, null)
    }

    fun setRefreshToken(context: Context, token: String?) {
        getPreferences(context).edit().putString(REFRESH_TOKEN, token).apply()
    }

    fun clearTokens(context: Context) {
        accessToken = null
        getPreferences(context).edit().remove(REFRESH_TOKEN).apply()
    }
}
