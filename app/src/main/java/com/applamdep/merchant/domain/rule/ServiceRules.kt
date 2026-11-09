package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Booking

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Lớp kiểm tra tính hợp lệ của thông tin Dịch vụ Salon (Merchant Requirement #3):
 * - Tên dịch vụ: Không rỗng, tối thiểu 3 ký tự.
 * - Giá dịch vụ: Không được âm (>= 0 VNĐ).
 * - Thời lượng: Lớn hơn 0 phút, tối đa 480 phút (8 giờ).
 */
class ServiceValidator {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val message: String) : ValidationResult
    }

    fun validate(name: String, price: Long, durationMinutes: Int): ValidationResult {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            return ValidationResult.Invalid("Tên dịch vụ không được để trống")
        }
        if (trimmedName.length < 3) {
            return ValidationResult.Invalid("Tên dịch vụ phải có ít nhất 3 ký tự")
        }
        if (price < 0L) {
            return ValidationResult.Invalid("Giá dịch vụ không được âm")
        }
        if (durationMinutes <= 0) {
            return ValidationResult.Invalid("Thời lượng dịch vụ phải lớn hơn 0 phút")
        }
        if (durationMinutes > 480) {
            return ValidationResult.Invalid("Thời lượng dịch vụ không vượt quá 8 giờ (480 phút)")
        }
        return ValidationResult.Valid
    }
}

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH & QUY TẮC NGHIỆP VỤ (POLYMORPHISM & BUSINESS RULE)
 * Chính sách xóa / ẩn dịch vụ:
 * - Khi dịch vụ đang có đơn đặt hẹn chưa hoàn tất (PENDING, CONFIRMED, IN_SERVICE),
 *   hệ thống tự động chuyển sang chế độ ẨN (Soft Delete / Inactive) thay vì xóa vĩnh viễn
 *   nhằm bảo vệ toàn vẹn dữ liệu đơn đặt lịch và cọc của khách hàng.
 * - Ngược lại, nếu không có đơn đặt hẹn nào bị ràng buộc, cho phép xóa vĩnh viễn (Hard Delete).
 */
class ServiceRemovalPolicy {

    sealed interface ServiceRemovalDecision {
        data class SoftDelete(val reason: String) : ServiceRemovalDecision
        data class HardDelete(val message: String) : ServiceRemovalDecision
    }

    fun evaluateRemoval(
        serviceId: String,
        bookings: List<Booking>
    ): ServiceRemovalDecision {
        val activeStatuses = listOf(
            MerchantConstants.BOOKING_STATUS_PENDING,
            MerchantConstants.BOOKING_STATUS_CONFIRMED,
            MerchantConstants.BOOKING_STATUS_IN_SERVICE
        )

        val hasActiveBookings = bookings.any { bk ->
            bk.status in activeStatuses && bk.serviceIds.contains(serviceId)
        }

        return if (hasActiveBookings) {
            ServiceRemovalDecision.SoftDelete(
                "Dịch vụ đang có đơn đặt hẹn chưa hoàn tất. Hệ thống đã tự động chuyển sang chế độ tạm ẩn để bảo đảm an toàn dữ liệu lịch hẹn."
            )
        } else {
            ServiceRemovalDecision.HardDelete("Đã xóa vĩnh viễn dịch vụ khỏi danh mục.")
        }
    }
}
