package com.ljs.and.data.remote

import com.ljs.and.data.model.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val WAREHOUSE_BASE_URL = "http://34.120.215.23/warehouse/"
    private const val ORDER_BASE_URL = "http://34.120.215.23/order/"

    // 임시로 하드코딩된 토큰 사용
    private const val TEMP_AUTH_TOKEN = "eyJraWQiOiJkOTQzNTRlNi0xZGYwLTQ2NjktYmYyZC0wY2VkNjljMjliZDMiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxIiwiYXVkIjoiZ2VhcmZpcnN0LWNsaWVudC1tb2JpbGUiLCJuYmYiOjE3NjI3MTkxMDIsInNjb3BlIjpbIm9wZW5pZCIsIm9mZmxpbmVfYWNjZXNzIiwiZW1haWwiXSwiaXNzIjoiaHR0cDovLzM0LjEyMC4yMTUuMjMvYXV0aCIsIm5hbWUiOiJ0ZXN0Iiwid29ya190eXBlIjoi67O47IKsIiwicmFuayI6Iu2MgOyepSIsImV4cCI6MTc2MjcyMDkwMiwicmVnaW9uIjoi7ISc7Jq4IiwiaWF0IjoxNzYyNzE5MTAyLCJqdGkiOiI3ODIzZmU0OC05YzM5LTQ5N2EtODM4YS1lZDM4MzdkNTMxZjUifQ.k5_fVjwvX0Eiue3-gJ1wp-g1SCj1jhz2NtS8QlSg7H35eEVBOHA-IOARUWkE4uK1PYHMT-EBRzL6sw6sv6ruOZ9tmr0K8OJ1AMHZ8BDCdH0Fhx05LXta8mUDM3srnBecPFy23EU_Z4MdgKxFg_HlNuX902x_ocTYirya4_WfbjQz4Eu8AwsGs16pnicHQ-9WccURy0x6jlf6TntoFUGJ2uBgPIhf9HdNo7B2Nw0mdWjriyWpe2zELEHtmoDpt7UzdECUIcwxOPu41s2npaPDy97Q1ScfR9hAAT_5-yDwux7r1ZI7yzktvZaQe3Bps-EFmB_nKiZTGNFVa5K5waCqEg"

    private val authInterceptor = Interceptor { chain ->
        val token = TokenManager.getAccessToken() ?: TEMP_AUTH_TOKEN
        val request = if (token.isNotEmpty()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
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

    val inventoryApiService: InventoryApiService by lazy { warehouseRetrofit.create(InventoryApiService::class.java) }
    val purchaseOrderApiService: PurchaseOrderApiService by lazy { orderRetrofit.create(PurchaseOrderApiService::class.java) }
    val releasingApiService: ReleasingApiService by lazy { warehouseRetrofit.create(ReleasingApiService::class.java) }
    val receivingApiService: ReceivingApiService by lazy { warehouseRetrofit.create(ReceivingApiService::class.java) }
}
