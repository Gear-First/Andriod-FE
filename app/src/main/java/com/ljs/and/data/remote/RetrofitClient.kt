package com.ljs.and.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://34.120.215.23/warehouse/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val receivingApiService: ReceivingApiService = retrofit.create(ReceivingApiService::class.java)
    val releasingApiService: ReleasingApiService = retrofit.create(ReleasingApiService::class.java)
}
