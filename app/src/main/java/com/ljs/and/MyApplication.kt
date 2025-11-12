package com.ljs.and

import android.app.Application
import com.ljs.and.data.model.AuthManager
import com.ljs.and.data.model.TokenManager
import com.ljs.and.data.model.UserManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 앱이 시작될 때 UserManager와 AuthManager를 초기화합니다.
        UserManager.init(this)
        AuthManager.init(this)
//        TokenManager.init(this)
    }
}
