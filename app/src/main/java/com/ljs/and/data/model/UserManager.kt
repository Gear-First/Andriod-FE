package com.ljs.and.data.model

import android.content.Context
import android.content.SharedPreferences

object UserManager {
    private const val PREFS_NAME = "UserPrefs"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name" // userName 추가
    private const val KEY_WAREHOUSE_NAME = "warehouse_name" // warehouseName 추가

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    var email: String?
        get() = prefs?.getString(KEY_USER_EMAIL, null)
        set(value) {
            prefs?.edit()?.putString(KEY_USER_EMAIL, value)?.apply()
        }

    var userName: String? // userName 추가
        get() = prefs?.getString(KEY_USER_NAME, null)
        set(value) {
            prefs?.edit()?.putString(KEY_USER_NAME, value)?.apply()
        }

    var warehouseName: String? // warehouseName 추가
        get() = prefs?.getString(KEY_WAREHOUSE_NAME, null)
        set(value) {
            prefs?.edit()?.putString(KEY_WAREHOUSE_NAME, value)?.apply()
        }

    fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
