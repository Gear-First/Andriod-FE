package com.ljs.and.data.remote

import com.ljs.and.data.model.UserDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UserApiService {
    @GET("api/v1/getUser")
    suspend fun getUser(
        @Header("Authorization") token: String,
        @Query("userId") userId: Long
    ): UserDto
}
