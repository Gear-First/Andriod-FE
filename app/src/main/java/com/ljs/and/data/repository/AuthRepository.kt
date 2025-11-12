package com.ljs.and.data.repository

import com.ljs.and.data.remote.AuthApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    private val authApiService: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://34.120.215.23/auth/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    suspend fun getUserInfo(token: String) = runCatching {
        authApiService.getUserInfo(token)
    }
}
