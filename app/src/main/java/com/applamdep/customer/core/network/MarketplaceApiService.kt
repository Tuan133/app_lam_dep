package com.applamdep.customer.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Interface Retrofit cho các API sàn làm đẹp
 * (Tạo giao dịch, QR giả lập, tỷ giá, chatbot)
 */
interface MarketplaceApiService {

    @GET("api/v1/config/rates")
    suspend fun getExchangeRates(): Map<String, Double>

    @POST("api/v1/payment/create-qr")
    suspend fun createPaymentQr(
        @Body request: PaymentQrRequest
    ): PaymentQrResponse

    @GET("api/v1/payment/status/{transactionId}")
    suspend fun getPaymentStatus(
        @Path("transactionId") transactionId: String
    ): PaymentStatusResponse

    @POST("api/v1/ai/recommend")
    suspend fun getAiRecommendation(
        @Body request: AiRecommendRequest
    ): AiRecommendResponse
}

data class PaymentQrRequest(val transactionId: String, val amount: Long, val note: String)
data class PaymentQrResponse(val transactionId: String, val qrUrl: String, val expiresAt: Long)
data class PaymentStatusResponse(val transactionId: String, val status: String)
data class AiRecommendRequest(val query: String, val lat: Double?, val lng: Double?)
data class AiRecommendResponse(val recommendations: List<String>)

/**
 * DTO dữ liệu cửa hàng từ Firebase Realtime Database
 * Endpoint: https://applamdep-default-rtdb.asia-southeast1.firebasedatabase.app/salons.json
 */
data class FirebaseSalonDto(
    val name: String? = null,
    val address: String? = null,
    val hours: String? = null,
    val phone: String? = null,
    val facebook: String? = null,
    val instagram: String? = null,
    val tiktok: String? = null,
    val website: String? = null
)

interface FirebaseSalonApiService {
    @GET("salons.json")
    suspend fun getSalons(): Map<String, FirebaseSalonDto>
}

object FirebaseApiClient {
    private const val FIREBASE_RTDB_URL = "https://applamdep-default-rtdb.asia-southeast1.firebasedatabase.app/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val salonService: FirebaseSalonApiService by lazy {
        Retrofit.Builder()
            .baseUrl(FIREBASE_RTDB_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FirebaseSalonApiService::class.java)
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://api.applamdep.mock/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: MarketplaceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MarketplaceApiService::class.java)
    }
}

