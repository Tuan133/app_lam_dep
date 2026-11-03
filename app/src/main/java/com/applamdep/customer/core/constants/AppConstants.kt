package com.applamdep.customer.core.constants

/**
 * Các hằng số quy chuẩn hệ thống
 */
object AppConstants {
    // Quy tắc hoàn tiền cọc tối thiểu 2 tiếng trước giờ hẹn
    const val CANCELLATION_DEADLINE_HOURS = 2L
    const val CANCELLATION_DEADLINE_MILLIS = CANCELLATION_DEADLINE_HOURS * 60 * 60 * 1000L

    // Trạng thái cửa hàng
    const val SHOP_STATUS_APPROVED = "APPROVED"
    const val SHOP_STATUS_PENDING = "PENDING"

    // Gói thành viên cửa hàng
    const val PLAN_VIP = "VIP"
    const val PLAN_NORMAL = "NORMAL"

    // Trạng thái đặt lịch
    const val BOOKING_STATUS_PENDING = "PENDING"
    const val BOOKING_STATUS_CONFIRMED = "CONFIRMED"
    const val BOOKING_STATUS_IN_SERVICE = "IN_SERVICE"
    const val BOOKING_STATUS_COMPLETED = "COMPLETED"
    const val BOOKING_STATUS_CANCELLED = "CANCELLED"

    // Trạng thái giao dịch tiền (Sàn giữ tiền)
    const val TX_STATUS_PENDING = "PENDING"
    const val TX_STATUS_SUCCESS = "SUCCESS"
    const val TX_STATUS_FAILED = "FAILED"
    const val TX_STATUS_HELD = "HELD"                         // Sàn đang giữ cọc
    const val TX_STATUS_REFUNDED = "REFUNDED"                 // Đã hoàn cọc về ví khách
    const val TX_STATUS_RELEASED = "RELEASED_TO_SHOP"         // Đã giải ngân cho tiệm

    // URL trang web xác nhận thanh toán giả lập
    const val DEFAULT_MOCK_PAYMENT_URL = "https://applamdep-customer.web.app/mock-payment.html"
}
