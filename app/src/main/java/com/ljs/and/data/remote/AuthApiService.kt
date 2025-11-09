package com.ljs.and.data.remote

import com.ljs.and.data.model.TokenResponse
import com.ljs.and.data.model.UserInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {

    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun exchangeToken(
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("client_id") clientId: String,
        @Field("code_verifier") codeVerifier: String
    ): TokenResponse

    @GET("userinfo")
    suspend fun getUserInfo(
        @Header("Authorization") token: String
    ): UserInfo
}
