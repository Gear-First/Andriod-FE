package com.ljs.and.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.ljs.and.MainActivity
import com.ljs.and.data.model.AuthManager
import com.ljs.and.data.model.TokenManager
import com.ljs.and.data.model.UserManager
import com.ljs.and.data.remote.AuthApiService
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthCallbackActivity : ComponentActivity() {

    private val authApiService: AuthApiService by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl("http://34.120.215.23/auth/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        if (data != null && data.scheme == "gearfirst" && data.host == "callback") {
            val code = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")

            if (code != null && state != null && state == AuthManager.state) {
                lifecycleScope.launch {
                    try {
                        // 1. 토큰 교환
                        val tokenResponse = authApiService.exchangeToken(
                            grantType = "authorization_code",
                            code = code,
                            redirectUri = "gearfirst://callback",
                            clientId = "gearfirst-client-mobile",
                            codeVerifier = AuthManager.codeVerifier!!
                        )
                        val accessToken = tokenResponse.accessToken
                        TokenManager.setAccessToken(accessToken)
                        TokenManager.setRefreshToken(this@AuthCallbackActivity, tokenResponse.refreshToken)

                        Log.d("AuthCallbackActivity", "Access Token: $accessToken")

                        // 2. 사용자 정보 요청
                        val userInfo = authApiService.getUserInfo("Bearer $accessToken")

                        // 3. 사용자 정보 저장 (이메일은 동적, 나머지는 하드코딩)
                        UserManager.email = userInfo.sub
                        UserManager.userName = "이창고" // 임시 하드코딩
                        UserManager.warehouseName = "서울 중앙 창고" // 임시 하드코딩

                        Log.d("AuthCallbackActivity", "User email stored: ${userInfo.sub}")

                        AuthManager.clear() // 성공 시 임시 인증 값 삭제

                        // 메인 액티비티로 이동
                        val intent = Intent(this@AuthCallbackActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        Log.e("AuthCallbackActivity", "Auth process failed", e)
                        // 실패 시 모든 관련 데이터 삭제
                        AuthManager.clear()
                        TokenManager.clearTokens(this@AuthCallbackActivity)
                        UserManager.clear()
                    }
                }
            } else {
                Log.e("AuthCallbackActivity", "Invalid state or missing code")
                AuthManager.clear()
            }
        }
    }
}
