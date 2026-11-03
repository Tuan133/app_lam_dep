package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.SystemConfig

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
 * Interface PaymentMethod định nghĩa đa hình các hình thức xử lý phí gói hội viên của đối tác:
 * - FreeTrialPaymentMethod: Miễn phí tháng đầu tiên theo chương trình khuyến mãi
 * - MockQrPaymentMethod: Thanh toán phí gói thật/demo qua mã QR và xác nhận web
 */
interface PaymentMethod {
    suspend fun processPayment(
        shopId: String,
        plan: String,
        termMonths: Int,
        feeAmount: Long
    ): PaymentResult
}

sealed interface PaymentResult {
    data class ImmediateSuccess(val message: String) : PaymentResult
    data class QrPending(val qrUrl: String, val transactionId: String, val amount: Long) : PaymentResult
    data class Failed(val error: String) : PaymentResult
}

/**
 * Phương thức thanh toán miễn phí tháng đầu (Đa hình)
 */
class FreeTrialPaymentMethod : PaymentMethod {
    override suspend fun processPayment(
        shopId: String,
        plan: String,
        termMonths: Int,
        feeAmount: Long
    ): PaymentResult {
        return PaymentResult.ImmediateSuccess(
            "Chúc mừng bạn! Cửa hàng được kích hoạt miễn phí tháng đầu tiên."
        )
    }
}

/**
 * Phương thức thanh toán QR giả lập (Đa hình)
 */
class MockQrPaymentMethod(
    private val paymentInitiator: suspend (shopId: String, plan: String, termMonths: Int) -> Result<String>
) : PaymentMethod {
    override suspend fun processPayment(
        shopId: String,
        plan: String,
        termMonths: Int,
        feeAmount: Long
    ): PaymentResult {
        val result = paymentInitiator(shopId, plan, termMonths)
        return if (result.isSuccess) {
            val qrUrl = result.getOrThrow()
            PaymentResult.QrPending(
                qrUrl = qrUrl,
                transactionId = "TX-PLAN-${System.currentTimeMillis().toString().takeLast(6)}",
                amount = feeAmount
            )
        } else {
            PaymentResult.Failed(result.exceptionOrNull()?.localizedMessage ?: "Khởi tạo thanh toán QR thất bại")
        }
    }
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Bộ tính phí gói dịch vụ đối tác, lấy từ SystemConfig không hard-code.
 * VIP = Phí cơ bản theo kỳ hạn + Phí dịch vụ quảng bá ưu tiên.
 */
class PlanFeeCalculator {
    fun calculateFee(config: SystemConfig, plan: String, termMonths: Int): Long {
        val key = termMonths.toString()
        return if (plan == MerchantConstants.PLAN_VIP) {
            config.vipPlanFee[key] ?: (config.vipPlanFee["3"] ?: 600000L)
        } else {
            config.normalPlanFee[key] ?: (config.normalPlanFee["3"] ?: 300000L)
        }
    }
}
