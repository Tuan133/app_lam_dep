package com.applamdep.customer.domain.model

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Model thực thể Shop đóng gói tất cả các thuộc tính của cửa hàng.
 */
data class Shop(
    val id: String,
    val ownerId: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val phone: String,
    val openingHours: String,
    val description: String,
    val images: List<String>,
    val rating: Double,
    val reviewCount: Int,
    val status: String,                  // APPROVED, PENDING
    val plan: String,                    // NORMAL, VIP
    val planExpiryMillis: Long,          // Hạn gói
    val requireDeposit: Boolean,         // Có yêu cầu cọc hay không
    val depositOptions: List<Long>,      // 5 mệnh giá cọc do shop cấu hình
    val amenities: List<String> = emptyList(),
    val facebook: String? = null,
    val instagram: String? = null,
    val tiktok: String? = null,
    val website: String? = null
) {
    // Nghiệp vụ đóng gói: Kiểm tra shop có còn hạn hoạt động hay không
    val isPlanValid: Boolean
        get() = planExpiryMillis > System.currentTimeMillis()

    // Nghiệp vụ đóng gói: Kiểm tra xem có phải đối tác VIP hợp lệ
    val isVipActive: Boolean
        get() = plan.equals("VIP", ignoreCase = true) && isPlanValid
}

data class ServiceItem(
    val id: String,
    val shopId: String,
    val name: String,
    val description: String,
    val price: Long,
    val durationMinutes: Int,            // Thời lượng dịch vụ (ví dụ: 30, 45, 60 phút)
    val category: String,
    val imageUrl: String? = null,
    val isActive: Boolean = true
)

data class ShopSlot(
    val id: String,
    val shopId: String,
    val date: String,                    // YYYY-MM-DD
    val startTime: String,               // Ví dụ "09:00", "10:30"
    val maxCapacity: Int,                // Số khách phục vụ tối đa cùng lúc
    val bookedCount: Int,                // Số chỗ đã đặt
    val isAvailable: Boolean = true
) {
    // Khung giờ còn nhận khách nếu chưa đầy chỗ và chưa bị đánh dấu khóa
    val hasCapacity: Boolean
        get() = isAvailable && bookedCount < maxCapacity
}
