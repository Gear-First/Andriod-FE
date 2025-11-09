package com.ljs.and.data.model

import android.content.Context
import android.content.SharedPreferences

object AuthManager {
    private const val PREFS_NAME = "AuthPrefs"
    private const val KEY_CODE_VERIFIER = "code_verifier"
    private const val KEY_STATE = "state"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    var codeVerifier: String?
        get() = prefs?.getString(KEY_CODE_VERIFIER, null)
        set(value) {
            prefs?.edit()?.putString(KEY_CODE_VERIFIER, value)?.apply()
        }

    var state: String?
        get() = prefs?.getString(KEY_STATE, null)
        set(value) {
            prefs?.edit()?.putString(KEY_STATE, value)?.apply()
        }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
