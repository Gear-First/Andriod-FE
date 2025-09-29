package com.ljs.and.network

import com.ljs.and.data.InventoryItem
import retrofit2.Response
import retrofit2.http.GET

interface InventoryApi {
    @GET("api/v1/parts")
    suspend fun getItems(): Response<List<InventoryItem>>
}