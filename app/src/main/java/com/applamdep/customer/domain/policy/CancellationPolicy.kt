package com.applamdep.customer.domain.policy

import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking

/**
 * Kết quả thẩm định chính sách hủy lịch
 */
data class CancellationDecision(
    val canCancel: Boolean,
    val isRefundEligible: Boolean,
    val refundAmount: Long,
    val message: String
)

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
 * Interface CancellationPolicy cho phép linh hoạt định nghĩa các chính sách hủy khác nhau
 * (ví dụ: StandardPolicy cho khách thường, FlexiblePolicy cho khách VIP hoặc sự kiện đặc biệt).
 */
interface CancellationPolicy {
    fun evaluate(booking: Booking, currentTimeMillis: Long = System.currentTimeMillis()): CancellationDecision
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Triển khai chính sách hủy tiêu chuẩn của sàn:
 * - Hủy trước giờ hẹn ít nhất 2 tiếng: ĐƯỢC HOÀN 100% CỌC về ví trong ứng dụng.
 * - Hủy trong vòng dưới 2 tiếng: KHÔNG ĐƯỢC HOÀN CỌC (vô hiệu hóa nút hoặc cảnh báo mất cọc).
 */
class StandardCancellationPolicy : CancellationPolicy {

    override fun evaluate(
        booking: Booking,
        currentTimeMillis: Long
    ): CancellationDecision {
        val remainingMillis = booking.appointmentTimestamp - currentTimeMillis

        return if (remainingMillis >= AppConstants.CANCELLATION_DEADLINE_MILLIS) {
            // Hủy trước >= 2 tiếng: Đạt điều kiện hoàn cọc về ví
            CancellationDecision(
                canCancel = true,
                isRefundEligible = booking.requiresDeposit && booking.depositAmount > 0,
                refundAmount = if (booking.requiresDeposit) booking.depositAmount else 0L,
                message = "Bạn hủy trước giờ hẹn hơn 2 tiếng. Toàn bộ tiền cọc (${booking.depositAmount}đ) sẽ được hoàn 100% về ví trong ứng dụng."
            )
        } else if (remainingMillis > 0) {
            // Hủy dưới 2 tiếng: Quá hạn hoàn cọc
            CancellationDecision(
                canCancel = false,
                isRefundEligible = false,
                refundAmount = 0L,
                message = "Đã trong vòng 2 tiếng trước giờ hẹn. Theo quy định sàn, bạn không thể hủy và sẽ không được hoàn lại tiền cọc."
            )
        } else {
            // Giờ hẹn đã qua
            CancellationDecision(
                canCancel = false,
                isRefundEligible = false,
                refundAmount = 0L,
                message = "Lịch hẹn đã qua giờ diễn ra."
            )
        }
    }
}
