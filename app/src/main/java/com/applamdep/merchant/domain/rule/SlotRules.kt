package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Booking
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

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Kiểm tra tính hợp lệ khi chủ salon chỉnh sửa một khung giờ cụ thể:
 * - Sức chứa không thấp hơn số khách đã đặt (kế thừa SlotCapacityRule).
 * - Giờ bắt đầu và giờ kết thúc hợp lệ định dạng HH:mm, giờ kết thúc > giờ bắt đầu.
 * - Không chồng lấn thời gian với các khung giờ khác trong cùng ngày.
 */
class SlotEditValidator(
    private val capacityRule: SlotCapacityRule = SlotCapacityRule()
) {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val errorMessage: String) : ValidationResult
    }

    fun validate(
        editedSlot: SlotItem,
        otherSlots: List<SlotItem>
    ): ValidationResult {
        val capacityResult = capacityRule.validateCapacityChange(editedSlot.booked, editedSlot.capacity)
        if (capacityResult is SlotCapacityRule.CapacityValidationResult.Invalid) {
            return ValidationResult.Invalid(capacityResult.errorMessage)
        }

        val startMin = ScheduleValidator.parseTimeToMinutes(editedSlot.startTime)
            ?: return ValidationResult.Invalid("Giờ bắt đầu không đúng định dạng (HH:mm, ví dụ 09:00)")
        val endMin = ScheduleValidator.parseTimeToMinutes(editedSlot.endTime)
            ?: return ValidationResult.Invalid("Giờ kết thúc không đúng định dạng (HH:mm, ví dụ 10:00)")

        if (endMin <= startMin) {
            return ValidationResult.Invalid(
                "Giờ kết thúc (${editedSlot.endTime}) phải sau giờ bắt đầu (${editedSlot.startTime})"
            )
        }

        val conflict = otherSlots.firstOrNull { other ->
            if (other.id == editedSlot.id) return@firstOrNull false
            val otherStart = ScheduleValidator.parseTimeToMinutes(other.startTime) ?: return@firstOrNull false
            val otherEnd = ScheduleValidator.parseTimeToMinutes(other.endTime) ?: return@firstOrNull false
            startMin < otherEnd && otherStart < endMin
        }
        if (conflict != null) {
            return ValidationResult.Invalid(
                "Khung giờ bị trùng với ${conflict.startTime} - ${conflict.endTime}"
            )
        }

        return ValidationResult.Valid
    }
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Đồng bộ danh sách khung giờ (Slots) với đơn hàng thực tế theo ngày:
 * - Gom toàn bộ đơn hàng thuộc từng khung giờ trong ngày (để hiển thị chi tiết khi nhấn vào slot).
 * - Tính số chỗ đã đặt (`booked`) chỉ từ các đơn đang giữ chỗ (PENDING, CONFIRMED, IN_SERVICE),
 *   giải phóng chỗ tự động khi đơn bị hủy/từ chối hoặc hoàn thành.
 */
class SlotBookingSynchronizer(
    private val defaultSeededDate: String = "2026-10-29"
) {

    data class SyncedSlotData(
        val slots: List<SlotItem>,
        val slotBookings: Map<String, List<Booking>>
    )

    private val activeSlotStatuses = setOf(
        MerchantConstants.BOOKING_STATUS_PENDING,
        MerchantConstants.BOOKING_STATUS_CONFIRMED,
        MerchantConstants.BOOKING_STATUS_IN_SERVICE
    )

    fun synchronize(
        slots: List<SlotItem>,
        allBookings: List<Booking>,
        shopId: String,
        selectedDate: String
    ): SyncedSlotData {
        val ordersForDate = allBookings.filter {
            it.shopId == shopId && bookingMatchesDate(it, selectedDate)
        }
        val ordersBySlot = mutableMapOf<String, List<Booking>>()
        val syncedSlots = slots.map { slot ->
            val matchedOrders = ordersForDate
                .filter { bookingMatchesSlot(it, slot) }
                .sortedWith(compareBy<Booking>({ bookingSortOrder(it.status) }, { it.startAt }))
            ordersBySlot[slot.id] = matchedOrders
            val activeCount = matchedOrders.count { it.status in activeSlotStatuses }
            slot.copy(booked = activeCount)
        }
        return SyncedSlotData(slots = syncedSlots, slotBookings = ordersBySlot)
    }

    private fun bookingSortOrder(status: String): Int = when (status) {
        MerchantConstants.BOOKING_STATUS_IN_SERVICE -> 0
        MerchantConstants.BOOKING_STATUS_CONFIRMED -> 1
        MerchantConstants.BOOKING_STATUS_PENDING -> 2
        MerchantConstants.BOOKING_STATUS_COMPLETED -> 3
        else -> 4
    }

    private fun bookingMatchesDate(booking: Booking, selectedDate: String): Boolean {
        if (booking.bookingDate.isNotBlank()) {
            return booking.bookingDate == selectedDate
        }
        return selectedDate == defaultSeededDate
    }

    private fun bookingMatchesSlot(booking: Booking, slot: SlotItem): Boolean {
        if (booking.slotId == slot.id) return true
        if (slot.date.isNotBlank()) {
            val bareSlotId = slot.id.removePrefix("${slot.date}_")
            val bareBookingSlotId = booking.slotId.removePrefix("${slot.date}_")
            return bareSlotId == bareBookingSlotId
        }
        return false
    }
}
