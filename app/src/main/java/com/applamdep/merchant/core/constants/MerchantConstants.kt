package com.applamdep.merchant.core.constants

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Lưu trữ tập trung các hằng số hợp đồng dữ liệu dùng chung và quy tắc nghiệp vụ
 * giữa Customer App, Merchant Portal và Admin Web.
 */
object MerchantConstants {
    // Tên các Collections Firestore (Hợp đồng dữ liệu không thay đổi)
    const val COLL_USERS = "users"
    const val COLL_SHOPS = "shops"
    const val COLL_SERVICES = "services"
    const val COLL_WORK_SCHEDULES = "workSchedules"
    const val COLL_SLOTS = "slots"
    const val COLL_BOOKINGS = "bookings"
    const val COLL_TRANSACTIONS = "transactions"
    const val COLL_REVIEWS = "reviews"
    const val COLL_CONFIG = "config"
    const val DOC_SYSTEM_CONFIG = "system_config"

    // Role
    const val ROLE_MERCHANT = "MERCHANT"
    const val ROLE_CUSTOMER = "CUSTOMER"
    const val ROLE_ADMIN = "ADMIN"

    // Trạng thái cửa hàng (Shop Status)
    const val SHOP_STATUS_DRAFT = "DRAFT"
    const val SHOP_STATUS_PENDING = "PENDING"
    const val SHOP_STATUS_APPROVED = "APPROVED"
    const val SHOP_STATUS_REJECTED = "REJECTED"

    // Gói dịch vụ đối tác
    const val PLAN_NORMAL = "NORMAL"
    const val PLAN_VIP = "VIP"

    // Trạng thái đơn đặt lịch (Booking Status)
    const val BOOKING_STATUS_PENDING = "PENDING"
    const val BOOKING_STATUS_CONFIRMED = "CONFIRMED"
    const val BOOKING_STATUS_IN_SERVICE = "IN_SERVICE"
    const val BOOKING_STATUS_COMPLETED = "COMPLETED"
    const val BOOKING_STATUS_CANCELLED = "CANCELLED"

    // Trạng thái giao dịch cọc ký quỹ (Transaction Status)
    const val TX_TYPE_DEPOSIT = "DEPOSIT"
    const val TX_TYPE_REFUND = "REFUND"
    const val TX_TYPE_RELEASE = "RELEASE"

    const val TX_STATUS_PENDING = "PENDING"
    const val TX_STATUS_HELD = "HELD"
    const val TX_STATUS_REFUNDED = "REFUNDED"
    const val TX_STATUS_RELEASED_TO_SHOP = "RELEASED_TO_SHOP"

    // Trạng thái đánh giá (Review Status)
    const val REVIEW_STATUS_ACTIVE = "ACTIVE"
    const val REVIEW_STATUS_REMOVE_REQUESTED = "REMOVE_REQUESTED"
    const val REVIEW_STATUS_REMOVED = "REMOVED"

    // Quy tắc nghiệp vụ bắt buộc
    const val AUTO_COMPLETE_DELAY_MINUTES = 30L
    const val CANCELLATION_REFUND_LIMIT_HOURS = 2L
    const val REQUIRED_DEPOSIT_OPTIONS_COUNT = 5
}
