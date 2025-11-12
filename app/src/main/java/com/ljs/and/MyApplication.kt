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

        // 발표용 더미 로그인 정보 설정
        AuthManager.codeVerifier = "dummy_code_verifier"
        AuthManager.state = "dummy_state"
        UserManager.email = "test@example.com"
        UserManager.userName = "홍길동"
        UserManager.warehouseName = "서울센터"
        
        // 더미 액세스 토큰 저장 (init과 saveAccessToken은 없는 메소드이므로 수정)
        TokenManager.setAccessToken("dummy_access_token")
    }
}
