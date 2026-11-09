package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.domain.model.SlotItem

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Quy tắc nghiệp vụ điều chỉnh sức chứa khung giờ (Merchant Requirement #5):
 * - Quy tắc cốt lõi: KHÔNG ĐƯỢC PHÉP giảm sức chứa (Capacity) thấp hơn số lượng khách
 *   đã đặt hẹn thành công trước đó (Booked).
 * - Sức chứa phải >= 1.
 */
class SlotCapacityRule {

    sealed interface CapacityValidationResult {
        data object Valid : CapacityValidationResult
        data class Invalid(val errorMessage: String) : CapacityValidationResult
    }

    fun validateCapacityChange(currentBooked: Int, newCapacity: Int): CapacityValidationResult {
        if (newCapacity <= 0) {
            return CapacityValidationResult.Invalid("Sức chứa tối thiểu phải là 1 khách")
        }
        if (newCapacity < currentBooked) {
            return CapacityValidationResult.Invalid(
                "Không thể giảm số lượng phục vụ xuống $newCapacity vì đã có $currentBooked khách đặt hẹn trước."
            )
        }
        return CapacityValidationResult.Valid
    }
}

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
 * Lọc danh sách khung giờ phục vụ theo trạng thái:
 * - ALL: Tất cả khung giờ
 * - AVAILABLE: Còn chỗ nhận khách
 * - FULL: Đã kín chỗ
 * - BLOCKED: Khung giờ đang bị chủ quán khóa
 */
enum class SlotFilterType(val displayName: String) {
    ALL("Tất cả"),
    AVAILABLE("Còn chỗ"),
    FULL("Đã đầy"),
    BLOCKED("Đã khóa")
}

class SlotFilterPolicy {

    fun filter(slots: List<SlotItem>, filterType: SlotFilterType): List<SlotItem> {
        return when (filterType) {
            SlotFilterType.ALL -> slots
            SlotFilterType.AVAILABLE -> slots.filter { !it.isBlocked && it.booked < it.capacity }
            SlotFilterType.FULL -> slots.filter { !it.isBlocked && it.booked >= it.capacity }
            SlotFilterType.BLOCKED -> slots.filter { it.isBlocked }
        }
    }
}
