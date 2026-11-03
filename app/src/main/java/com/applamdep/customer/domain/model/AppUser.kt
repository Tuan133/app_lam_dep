package com.applamdep.customer.domain.model

enum class UserRole {
    CUSTOMER,   // Khách hàng
    MERCHANT,   // Cửa hàng / Đối tác salon
    ADMIN       // Quản trị viên hệ thống
}

enum class AccountStatus {
    APPROVED,           // Đã duyệt & Đang hoạt động
    PENDING_APPROVAL,   // Chờ Admin duyệt (Bắt buộc cho tài khoản Cửa hàng mới)
    REJECTED,           // Bị Admin từ chối
    LOCKED              // Tạm khóa do vi phạm
}

data class AppUser(
    val gmail: String,
    val password: String,
    val role: UserRole,
    var status: AccountStatus = if (role == UserRole.MERCHANT) AccountStatus.PENDING_APPROVAL else AccountStatus.APPROVED,
    val fullName: String = "",
    val salonName: String = "",
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

