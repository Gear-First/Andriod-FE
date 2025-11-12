package com.ljs.and.data.remote.fake

import com.ljs.and.data.model.TokenResponse
import com.ljs.and.data.model.UserInfo
import com.ljs.and.data.remote.AuthApiService
import kotlinx.coroutines.delay

class FakeAuthApiService : AuthApiService {

    override suspend fun exchangeToken(
        authorization: String,
        grantType: String,
        code: String,
        redirectUri: String,
        codeVerifier: String
    ): TokenResponse {
        delay(500)
        return TokenResponse(
            accessToken = "fake-access-token-from-api",
            refreshToken = "fake-refresh-token-from-api",
            tokenType = "Bearer",
            expiresIn = 3600
        )
    }

    override suspend fun getUserInfo(token: String): UserInfo {
        delay(300)
        return UserInfo(sub = "test@example.com")
    }
}
