package com.ljs.and.data.local

object TokenManager {
    private var token: String? = null

    fun getToken(): String? {
        return token
    }

    fun setToken(newToken: String?) {
        token = newToken
    }
}
