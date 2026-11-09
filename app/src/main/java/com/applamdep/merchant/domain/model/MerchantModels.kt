package com.applamdep.merchant.domain.model

import com.applamdep.merchant.core.constants.MerchantConstants

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Model thực thể Hồ sơ Cửa hàng (Shop Profile) đóng gói toàn bộ thuộc tính và logic nghiệp vụ.
 */
data class ShopProfile(
    val id: String = "",
    val ownerUid: String = "",
    val name: String = "",
    val ownerName: String = "",
    val address: String = "",
    val lat: Double = 10.7769,
    val lng: Double = 106.7009,
    val phone: String = "",
    val email: String = "",
    val description: String = "",
    val openingHours: String = "08:30 - 21:00",
    val images: List<String> = emptyList(),
    val status: String = MerchantConstants.SHOP_STATUS_DRAFT, // DRAFT, PENDING, APPROVED, REJECTED
    val rejectReason: String? = null,
    val plan: String = MerchantConstants.PLAN_NORMAL,         // NORMAL, VIP
    val planTermMonths: Int = 3,                              // 3, 6, 12 tháng
    val planStart: Long = System.currentTimeMillis(),
    val planExpiry: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L),
    val requireDeposit: Boolean = true,
    val depositOptions: List<Long> = listOf(50000L, 100000L, 150000L, 200000L, 500000L),
    val ratingAvg: Double = 5.0,
    val ratingCount: Int = 0,
    val lastEditedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isApproved: Boolean get() = status == MerchantConstants.SHOP_STATUS_APPROVED
    val isPending: Boolean get() = status == MerchantConstants.SHOP_STATUS_PENDING
    val isRejected: Boolean get() = status == MerchantConstants.SHOP_STATUS_REJECTED
    val isPlanValid: Boolean get() = planExpiry > System.currentTimeMillis()
    val isVip: Boolean get() = plan == MerchantConstants.PLAN_VIP
    val shopName: String get() = name
}

/**
 * Tương thích ngược với MerchantProfile cũ
 */
data class MerchantProfile(
    val id: String = "",
    val ownerId: String = "",
    val shopName: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val lat: Double = 10.7769,
    val lng: Double = 106.7009,
    val description: String = "",
    val openingHours: String = "09:00 - 21:00",
    val avatarUrl: String? = null,
    val coverImageUrl: String? = null,
    val licenseNumber: String = "",
    val status: String = "APPROVED",
    val membershipPlan: String = "VIP",
    val planExpiryMillis: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000L),
    val requireDeposit: Boolean = true,
    val depositOptions: List<Long> = listOf(50000L, 100000L, 200000L),
    val amenities: List<String> = listOf("Wifi tốc độ cao", "Phòng VIP riêng tư", "Trà thảo mộc", "Chỗ đậu xe hơi"),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isApproved: Boolean get() = status.equals("APPROVED", ignoreCase = true)
    val isVip: Boolean get() = membershipPlan.equals("VIP", ignoreCase = true)
}

/**
 * Model Dịch vụ (Service Item)
 */
data class ServiceItem(
    val id: String = "",
    val shopId: String = "",
    val name: String = "",
    val price: Long = 0L,
    val durationMinutes: Int = 45,
    val description: String = "",
    val category: String = "Làm đẹp",
    val imageUrl: String? = null,
    val active: Boolean = true,
    val flagged: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    val isActive: Boolean get() = active
}

/**
 * Model Ca làm việc & Lịch hoạt động tuần (Work Schedule)
 */
data class WorkSchedule(
    val shopId: String = "",
    val workingDays: List<Int> = listOf(2, 3, 4, 5, 6, 7, 8),
    val openTime: String = "08:30",
    val closeTime: String = "21:00",
    val breakStartTime: String? = "12:00",
    val breakEndTime: String? = "13:00",
    val offDates: List<String> = emptyList(),
    val slotDurationMinutes: Int = 60,
    val defaultCapacity: Int = 3
)

/**
 * Tương thích với MerchantSchedule cũ
 */
data class MerchantSchedule(
    val shopId: String = "",
    val workingDays: List<String> = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"),
    val openTime: String = "09:00",
    val closeTime: String = "21:00",
    val breakStartTime: String = "12:00",
    val breakEndTime: String = "13:00",
    val offDates: List<String> = emptyList(),
    val defaultSlotCapacity: Int = 3
)

/**
 * Model Khung giờ trống (Slot Item)
 */
data class SlotItem(
    val id: String = "",
    val shopId: String = "",
    val date: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val capacity: Int = 3,
    val booked: Int = 0,
    val isBlocked: Boolean = false
) {
    val remainingCapacity: Int get() = (capacity - booked).coerceAtLeast(0)
    val hasCapacity: Boolean get() = !isBlocked && booked < capacity
    val isAvailable: Boolean get() = !isBlocked && booked < capacity
    val maxCapacity: Int get() = capacity
    val bookedCount: Int get() = booked
}

/**
 * Model Đơn đặt lịch (Booking)
 */
data class Booking(
    val id: String = "",
    val customerUid: String = "",
    val shopId: String = "",
    val serviceIds: List<String> = emptyList(),
    val serviceNames: List<String> = emptyList(),
    val slotId: String = "",
    val startAt: Long = 0L,
    val endAt: Long = 0L,
    val customerName: String = "",
    val customerPhone: String = "",
    val note: String = "",
    val status: String = MerchantConstants.BOOKING_STATUS_PENDING,
    val depositAmount: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val autoCompleted: Boolean = false,
    val cancelReason: String? = null,
    val cancelledAt: Long? = null
)

/**
 * Model Giao dịch cọc trung gian (Transaction - Escrow)
 */
data class Transaction(
    val id: String = "",
    val type: String = MerchantConstants.TX_TYPE_DEPOSIT,
    val bookingId: String = "",
    val shopId: String = "",
    val customerUid: String = "",
    val amount: Long = 0L,
    val status: String = MerchantConstants.TX_STATUS_HELD,
    val heldAt: Long? = null,
    val releasedAt: Long? = null,
    val refundedAt: Long? = null
)

/**
 * Model Đánh giá khách hàng (Review)
 */
data class Review(
    val id: String = "",
    val bookingId: String = "",
    val shopId: String = "",
    val customerUid: String = "",
    val customerName: String = "Khách hàng",
    val rating: Int = 5,
    val content: String = "",
    val images: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val shopReply: String? = null,
    val shopRepliedAt: Long? = null,
    val status: String = MerchantConstants.REVIEW_STATUS_ACTIVE,
    val reportedByShop: Boolean = false,
    val reportReason: String? = null,
    val reportedAt: Long? = null
)

/**
 * Model Cấu hình hệ thống (System Config)
 */
data class SystemConfig(
    val currencyExchangeRate: Double = 25000.0,
    val normalPlanFee: Map<String, Long> = mapOf("3" to 300000L, "6" to 550000L, "12" to 1000000L),
    val vipPlanFee: Map<String, Long> = mapOf("3" to 600000L, "6" to 1100000L, "12" to 2000000L),
    val isFirstMonthFree: Boolean = true
)

/**
 * Thống kê tổng quan hoạt động đối tác
 */
data class MerchantStats(
    val totalBookingsToday: Int = 6,
    val pendingBookings: Int = 2,
    val confirmedBookings: Int = 3,
    val inServiceBookings: Int = 1,
    val completedBookings: Int = 18,
    val cancelledBookings: Int = 1,
    val estimatedRevenueToday: Long = 1850000L,
    val totalRevenueMonth: Long = 42600000L,
    val averageRating: Double = 4.9,
    val totalReviews: Int = 48
)

/**
 * Model Tổng kết tài chính tiền cọc (Escrow Financial Summary)
 */
data class FinancialSummary(
    val heldEscrowBalance: Long = 0L,
    val releasedTotalBalance: Long = 0L,
    val refundedTotalBalance: Long = 0L,
    val transactions: List<Transaction> = emptyList()
)

data class MerchantFinancialSummary(
    val heldEscrowBalance: Long = 150000L,
    val releasedTotalBalance: Long = 3450000L,
    val refundedTotalBalance: Long = 100000L,
    val bookingTransactions: List<MerchantEscrowItem> = emptyList()
)

data class MerchantEscrowItem(
    val id: String = "",
    val bookingId: String = "",
    val customerName: String = "",
    val serviceName: String = "",
    val depositAmount: Long = 0L,
    val status: String = "HELD",
    val time: String = ""
)
