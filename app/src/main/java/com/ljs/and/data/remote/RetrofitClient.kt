package com.ljs.and.data.remote

import com.ljs.and.data.model.TokenManager
import com.ljs.and.data.remote.fake.FakeAuthApiService
import com.ljs.and.data.remote.fake.FakeHomeApiService
import com.ljs.and.data.remote.fake.FakeInventoryApiService
import com.ljs.and.data.remote.fake.FakePurchaseOrderApiService
import com.ljs.and.data.remote.fake.FakeReceivingApiService
import com.ljs.and.data.remote.fake.FakeReleasingApiService
import com.ljs.and.data.remote.fake.FakeSearchApiService
import com.ljs.and.data.remote.fake.FakeUserApiService
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
        .client(authenticatedClient) // 인증 클라이언트로 변경
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val orderRetrofit = Retrofit.Builder()
        .baseUrl(ORDER_BASE_URL)
        .client(authenticatedClient) // 기존 인증 클라이언트 사용
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // --- API 서비스 --- 
    val authApiService: AuthApiService by lazy {
        FakeAuthApiService()
        // orderRetrofit.create(AuthApiService::class.java)
    }

    val homeApiService: HomeApiService by lazy {
        FakeHomeApiService()
        // warehouseRetrofit.create(HomeApiService::class.java)
    }

    val userApiService: UserApiService by lazy {
        FakeUserApiService()
        // warehouseRetrofit.create(UserApiService::class.java)
    }

    val inventoryApiService: InventoryApiService by lazy {
        // 발표용 더미 데이터 사용
        FakeInventoryApiService()
        // warehouseRetrofit.create(InventoryApiService::class.java)
    }
    val purchaseOrderApiService: PurchaseOrderApiService by lazy {
        // 발표용 더미 데이터 사용
        FakePurchaseOrderApiService()
        // orderRetrofit.create(PurchaseOrderApiService::class.java)
    }
    val releasingApiService: ReleasingApiService by lazy {
        // 발표용 더미 데이터 사용
        FakeReleasingApiService()
        // warehouseRetrofit.create(ReleasingApiService::class.java)
    }
    val receivingApiService: ReceivingApiService by lazy {
        // 발표용 더미 데이터 사용
        FakeReceivingApiService()
        // warehouseRetrofit.create(ReceivingApiService::class.java)
    }
    val searchApiService: SearchApiService by lazy {
        // 발표용 더미 데이터 사용
        FakeSearchApiService()
        // warehouseRetrofit.create(SearchApiService::class.java)
    }
}
