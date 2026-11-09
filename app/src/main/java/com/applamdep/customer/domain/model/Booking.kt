package com.applamdep.customer.domain.model

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Model Booking đóng gói toàn bộ thông tin đặt lịch, liên kết dịch vụ và thanh toán cọc.
 */
data class Booking(
    val id: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerNote: String = "",
    val shopId: String,
    val shopName: String,
    val shopAddress: String,
    val serviceId: String,
    val serviceName: String,
    val servicePrice: Long,
    val bookingDate: String,             // YYYY-MM-DD
    val bookingTime: String,             // HH:mm
    val appointmentTimestamp: Long,      // Epoch millis của thời điểm hẹn
    val durationMinutes: Int,            // Thời lượng
    val requiresDeposit: Boolean,
    val depositAmount: Long = 0L,
    val depositTransactionId: String? = null,
    val status: String,                  // PENDING, CONFIRMED, IN_SERVICE, COMPLETED, CANCELLED
    val cancelReason: String? = null,
    val cancelledAt: Long? = null,
    val isReviewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
