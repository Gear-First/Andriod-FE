package com.ljs.and.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.ljs.and.MainActivity
import com.ljs.and.data.model.AuthManager
import com.ljs.and.data.model.TokenManager
import com.ljs.and.data.remote.AuthApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthCallbackActivity : ComponentActivity() {

    private val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://34.120.215.23/auth/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(this) // AuthManager 초기화

        val data = intent.data
        if (data != null && data.scheme == "gearfirst" && data.host == "callback") {
            val code = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")

            if (code != null && state != null && state == AuthManager.state) {
                lifecycleScope.launch {
                    try {
                        val response = authApiService.exchangeToken(
                            grantType = "authorization_code",
                            code = code,
                            redirectUri = "gearfirst://callback",
                            clientId = "gearfirst-client-mobile",
                            codeVerifier = AuthManager.codeVerifier!!
                        )
                        TokenManager.setAccessToken(response.accessToken)
                        TokenManager.setRefreshToken(this@AuthCallbackActivity, response.refreshToken)

                        // [로그 추가] 발급받은 토큰 확인
                        Log.d("AuthCallbackActivity", "Access Token: ${response.accessToken}")
                        Log.d("AuthCallbackActivity", "Refresh Token: ${response.refreshToken}")

                        AuthManager.clear() // 성공 시 저장된 값 삭제

                        val intent = Intent(this@AuthCallbackActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        Log.e("AuthCallbackActivity", "Error exchanging token", e)
                        AuthManager.clear() // 실패 시 저장된 값 삭제
                    }
                }
            } else {
                Log.e("AuthCallbackActivity", "Invalid state or missing code")
                AuthManager.clear() // 실패 시 저장된 값 삭제
            }
        }
    }
}
