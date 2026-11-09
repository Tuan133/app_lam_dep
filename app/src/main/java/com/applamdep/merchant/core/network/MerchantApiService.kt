package com.applamdep.merchant.core.network

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
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Interface Retrofit định nghĩa các REST API ngoại vi:
 * - Giả lập thanh toán phí gói nâng cấp đối tác qua mã QR
 * - Truy vấn trạng thái thanh toán
 * - Lấy tỷ giá và cấu hình phí gói từ hệ thống
 */
interface MerchantApiService {

    @POST("api/v1/merchant/plan/payment-qr")
    suspend fun createPlanPaymentQr(
        @Body request: PlanPaymentQrRequest
    ): PlanPaymentQrResponse

    @GET("api/v1/merchant/plan/payment-status/{transactionId}")
    suspend fun checkPlanPaymentStatus(
        @Path("transactionId") transactionId: String
    ): PlanPaymentStatusResponse

    @GET("api/v1/config/rates")
    suspend fun getExchangeRates(): Map<String, Double>
}

data class PlanPaymentQrRequest(
    val shopId: String,
    val planType: String,
    val termMonths: Int,
    val amount: Long,
    val note: String
)

data class PlanPaymentQrResponse(
    val transactionId: String,
    val qrUrl: String,
    val expiresAt: Long
)

data class PlanPaymentStatusResponse(
    val transactionId: String,
    val status: String // "SUCCESS", "PENDING", "FAILED"
)

/**
 * Singleton Retrofit Client cho Merchant Portal
 */
object MerchantRetrofitClient {
    private const val BASE_URL = "https://api.applamdep.mock/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: MerchantApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MerchantApiService::class.java)
    }
}
