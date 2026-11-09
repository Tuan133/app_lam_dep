package com.example.app_lam_dep.model

data class BookingTicket(
    val id: String,
    val salonName: String,
    val serviceName: String,
    val servicePrice: String,
    val date: String,
    val timeSlot: String,
    val specialist: String,
    val customerName: String,
    val customerPhone: String,
    val note: String = "",
    val requiresDeposit: Boolean = true,
    val depositAmount: Long = 50000L,
    val status: String = "ĐÃ XÁC NHẬN & ĐẶT CỌC",
    val qrCodeId: String = "GLOW-" + (1000..9999).random(),
    val cancelReason: String = "",
    val isRefunded: Boolean = false
)

enum class TransactionType {
    TOPUP,      // Nạp tiền vào ví (+)
    DEPOSIT,    // Trừ cọc đặt lịch (-)
    REFUND,     // Hoàn cọc hủy lịch (+)
    PAYMENT     // Thanh toán dịch vụ (-)
}

data class WalletTransaction(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Long, // Dương nếu nạp/hoàn (+), Âm nếu thanh toán/cọc (-)
    val time: String,
    val type: TransactionType,
    val bookingId: String? = null
)

data class SalonShopInfo(
    val id: String,
    val name: String,
    val address: String,
    val phone: String,
    val openingHours: String,
    val rating: Double,
    val reviewCount: String,
    val description: String,
    val amenities: List<String>,
    val requiresDeposit: Boolean, // Tùy shop có cần cọc hay không
    val depositTiers: List<Long>,  // Shop set 5 mệnh giá cọc
    val isVip: Boolean = false,
    val membershipPlan: String = "Gói VIP Sponsor (Còn 24 ngày)"
)

data class MembershipPlanOption(
    val id: String,
    val name: String,
    val isVip: Boolean,
    val badge: String,
    val description: String,
    val regularFee: Long,
    val adFee: Long = 0L,
    val discountPercent: Int = 0,
    val isFreeFirstMonth: Boolean = true
) {
    val totalFee: Long get() = regularFee + adFee
}

data class AiShopRecommendation(
    val id: String,
    val salonName: String,
    val serviceSuggestion: String,
    val rating: Double,
    val distance: String,
    val requiresDeposit: Boolean,
    val depositInfo: String,
    val matchReason: String
)
