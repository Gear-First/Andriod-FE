package com.ljs.and.data.remote

import com.ljs.and.data.remote.ReceivingApiService
import com.ljs.and.data.remote.ReleasingApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val WAREHOUSE_BASE_URL = "http://34.120.215.23/warehouse/"
    private const val ORDER_BASE_URL = "http://34.120.215.23/order/"

    private val client = OkHttpClient.Builder()
        .build()

    private val warehouseRetrofit = Retrofit.Builder()
        .baseUrl(WAREHOUSE_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val orderRetrofit = Retrofit.Builder()
        .baseUrl(ORDER_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val receivingApiService: ReceivingApiService = warehouseRetrofit.create(ReceivingApiService::class.java)
    val releasingApiService: ReleasingApiService = warehouseRetrofit.create(ReleasingApiService::class.java)
    val inventoryApiService: InventoryApiService = warehouseRetrofit.create(InventoryApiService::class.java)
    val purchaseOrderApiService: PurchaseOrderApiService = orderRetrofit.create(PurchaseOrderApiService::class.java)
}
