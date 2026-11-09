package com.applamdep.merchant.domain.policy

import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH & TRỪU TƯỢNG (POLYMORPHISM & ABSTRACTION)
 * Chính sách tự động hoàn thành dịch vụ sau 30 phút kể từ khi thời gian dịch vụ kết thúc
 * nếu cửa hàng quên không bấm xác nhận hoàn thành (Merchant Requirement #7).
 */
interface AutoCompletionPolicy {
    /**
     * Kiểm tra một booking có đủ điều kiện tự động hoàn thành hay không
     */
    fun shouldAutoComplete(booking: Booking, currentTimestamp: Long = System.currentTimeMillis()): Boolean

    /**
     * Lọc danh sách các booking cần tự động chuyển trạng thái sang COMPLETED
     */
    fun findEligibleBookings(bookings: List<Booking>, currentTimestamp: Long = System.currentTimeMillis()): List<Booking>
}

class StandardAutoCompletionPolicy : AutoCompletionPolicy {

    companion object {
        // 30 phút sau khi kết thúc ca làm dịch vụ
        const val GRACE_PERIOD_MILLIS = 30 * 60 * 1000L
    }

    override fun shouldAutoComplete(booking: Booking, currentTimestamp: Long): Boolean {
        // Chỉ áp dụng cho booking đang ở trạng thái CONFIRMED hoặc IN_SERVICE
        if (booking.status != AppConstants.BOOKING_STATUS_CONFIRMED && 
            booking.status != AppConstants.BOOKING_STATUS_IN_SERVICE) {
            return false
        }

        // Thời điểm kết thúc dự kiến = Thời điểm hẹn + Thời lượng làm dịch vụ
        val serviceEndTimestamp = booking.appointmentTimestamp + (booking.durationMinutes * 60 * 1000L)

        // Tự động hoàn thành sau khi kết thúc dịch vụ 30 phút
        val autoCompletionThreshold = serviceEndTimestamp + GRACE_PERIOD_MILLIS

        return currentTimestamp >= autoCompletionThreshold
    }

    override fun findEligibleBookings(bookings: List<Booking>, currentTimestamp: Long): List<Booking> {
        return bookings.filter { shouldAutoComplete(it, currentTimestamp) }
    }
}
