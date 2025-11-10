package com.ljs.and.data.remote

import com.ljs.and.data.model.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val WAREHOUSE_BASE_URL = "http://34.120.215.23/warehouse/"
    private const val ORDER_BASE_URL = "http://34.120.215.23/order/"

    // --- 인증이 필요한 클라이언트 ---
    private val authInterceptor = Interceptor { chain ->
        val token = TokenManager.getAccessToken()
        val request = if (!token.isNullOrEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val authenticatedClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()

    // --- 인증이 필요 없는 클라이언트 ---
    private val publicClient = OkHttpClient.Builder().build()

    // --- Retrofit 인스턴스 ---
    private val warehouseRetrofit = Retrofit.Builder()
        .baseUrl(WAREHOUSE_BASE_URL)
        .client(publicClient) // 인증이 필요 없는 클라이언트 사용
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val orderRetrofit = Retrofit.Builder()
        .baseUrl(ORDER_BASE_URL)
        .client(authenticatedClient) // 기존 인증 클라이언트 사용
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- API 서비스 --- 
    val inventoryApiService: InventoryApiService by lazy {
        warehouseRetrofit.create(InventoryApiService::class.java)
    }
    val purchaseOrderApiService: PurchaseOrderApiService by lazy {
        orderRetrofit.create(PurchaseOrderApiService::class.java)
    }
    val releasingApiService: ReleasingApiService by lazy {
        warehouseRetrofit.create(ReleasingApiService::class.java)
    }
    val receivingApiService: ReceivingApiService by lazy {
        warehouseRetrofit.create(ReceivingApiService::class.java)
    }
}
