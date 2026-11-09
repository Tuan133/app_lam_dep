package com.applamdep.merchant

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.WorkSchedule
import com.applamdep.merchant.domain.rule.ScheduleSlotGenerator
import com.applamdep.merchant.domain.rule.ScheduleValidator
import com.applamdep.merchant.domain.rule.ServiceRemovalPolicy
import com.applamdep.merchant.domain.rule.ServiceValidator
import com.applamdep.merchant.domain.rule.SlotCapacityRule
import com.applamdep.merchant.domain.rule.SlotFilterPolicy
import com.applamdep.merchant.domain.rule.SlotFilterType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests kiểm thử toàn bộ các quy tắc nghiệp vụ Đóng gói và Đa hình của Giai đoạn 4:
 * - Chức năng 3: Quản lý dịch vụ (Validation & Chính sách ẩn/xóa an toàn)
 * - Chức năng 4: Quản lý ca làm việc (Validation ca & Sinh slot tự động)
 * - Chức năng 5: Quản lý khung giờ trống (Quy tắc bảo vệ sức chứa Capacity & Bộ lọc Slot)
 */
class MerchantServiceScheduleSlotTest {

    private val serviceValidator = ServiceValidator()
    private val serviceRemovalPolicy = ServiceRemovalPolicy()
    private val scheduleValidator = ScheduleValidator()
    private val slotGenerator = ScheduleSlotGenerator()
    private val slotCapacityRule = SlotCapacityRule()
    private val slotFilterPolicy = SlotFilterPolicy()

    // ==========================================
    // 1. KIỂM THỬ CHỨC NĂNG 3: QUẢN LÝ DỊCH VỤ
    // ==========================================

    @Test
    fun serviceValidator_validFields_returnsValid() {
        val result = serviceValidator.validate(
            name = "Sơn Gel Móng Hữu Cơ Pháp",
            price = 180000L,
            durationMinutes = 45
        )
        assertTrue(result is ServiceValidator.ValidationResult.Valid)
    }

    @Test
    fun serviceValidator_blankOrShortName_returnsInvalid() {
        val blankResult = serviceValidator.validate(name = "   ", price = 100000L, durationMinutes = 30)
        assertTrue(blankResult is ServiceValidator.ValidationResult.Invalid)

        val shortResult = serviceValidator.validate(name = "AB", price = 100000L, durationMinutes = 30)
        assertTrue(shortResult is ServiceValidator.ValidationResult.Invalid)
    }

    @Test
    fun serviceValidator_negativePrice_returnsInvalid() {
        val result = serviceValidator.validate(name = "Nối mi Classic", price = -50000L, durationMinutes = 60)
        assertTrue(result is ServiceValidator.ValidationResult.Invalid)
    }

    @Test
    fun serviceValidator_zeroOrNegativeDuration_returnsInvalid() {
        val zeroResult = serviceValidator.validate(name = "Chăm sóc da", price = 200000L, durationMinutes = 0)
        assertTrue(zeroResult is ServiceValidator.ValidationResult.Invalid)

        val negativeResult = serviceValidator.validate(name = "Chăm sóc da", price = 200000L, durationMinutes = -15)
        assertTrue(negativeResult is ServiceValidator.ValidationResult.Invalid)
    }

    @Test
    fun serviceRemovalPolicy_serviceWithActivePendingBooking_triggersSoftDelete() {
        val bookings = listOf(
            Booking(
                id = "BK-01",
                serviceIds = listOf("srv_nail_01"),
                status = MerchantConstants.BOOKING_STATUS_PENDING
            )
        )
        val decision = serviceRemovalPolicy.evaluateRemoval("srv_nail_01", bookings)
        assertTrue(decision is ServiceRemovalPolicy.ServiceRemovalDecision.SoftDelete)
    }

    @Test
    fun serviceRemovalPolicy_serviceWithActiveConfirmedBooking_triggersSoftDelete() {
        val bookings = listOf(
            Booking(
                id = "BK-02",
                serviceIds = listOf("srv_nail_01"),
                status = MerchantConstants.BOOKING_STATUS_CONFIRMED
            )
        )
        val decision = serviceRemovalPolicy.evaluateRemoval("srv_nail_01", bookings)
        assertTrue(decision is ServiceRemovalPolicy.ServiceRemovalDecision.SoftDelete)
    }

    @Test
    fun serviceRemovalPolicy_serviceWithActiveInServiceBooking_triggersSoftDelete() {
        val bookings = listOf(
            Booking(
                id = "BK-03",
                serviceIds = listOf("srv_nail_01"),
                status = MerchantConstants.BOOKING_STATUS_IN_SERVICE
            )
        )
        val decision = serviceRemovalPolicy.evaluateRemoval("srv_nail_01", bookings)
        assertTrue(decision is ServiceRemovalPolicy.ServiceRemovalDecision.SoftDelete)
    }

    @Test
    fun serviceRemovalPolicy_serviceWithCompletedOrCancelledBookings_triggersHardDelete() {
        val bookings = listOf(
            Booking(
                id = "BK-04",
                serviceIds = listOf("srv_nail_01"),
                status = MerchantConstants.BOOKING_STATUS_COMPLETED
            ),
            Booking(
                id = "BK-05",
                serviceIds = listOf("srv_nail_01"),
                status = MerchantConstants.BOOKING_STATUS_CANCELLED
            )
        )
        val decision = serviceRemovalPolicy.evaluateRemoval("srv_nail_01", bookings)
        assertTrue(decision is ServiceRemovalPolicy.ServiceRemovalDecision.HardDelete)
    }

    @Test
    fun serviceRemovalPolicy_serviceWithNoBookings_triggersHardDelete() {
        val decision = serviceRemovalPolicy.evaluateRemoval("srv_unused", emptyList())
        assertTrue(decision is ServiceRemovalPolicy.ServiceRemovalDecision.HardDelete)
    }

    // ==========================================
    // 2. KIỂM THỬ CHỨC NĂNG 4: QUẢN LÝ LỊCH LÀM VIỆC
    // ==========================================

    @Test
    fun scheduleValidator_validSchedule_returnsValid() {
        val schedule = WorkSchedule(
            workingDays = listOf(2, 3, 4, 5, 6, 7),
            openTime = "08:30",
            closeTime = "21:00",
            breakStartTime = "12:00",
            breakEndTime = "13:00",
            slotDurationMinutes = 60,
            defaultCapacity = 3
        )
        val result = scheduleValidator.validate(schedule)
        assertTrue(result is ScheduleValidator.ValidationResult.Valid)
    }

    @Test
    fun scheduleValidator_emptyWorkingDays_returnsInvalid() {
        val schedule = WorkSchedule(workingDays = emptyList())
        val result = scheduleValidator.validate(schedule)
        assertTrue(result is ScheduleValidator.ValidationResult.Invalid)
    }

    @Test
    fun scheduleValidator_closeTimeBeforeOpenTime_returnsInvalid() {
        val schedule = WorkSchedule(openTime = "21:00", closeTime = "08:30")
        val result = scheduleValidator.validate(schedule)
        assertTrue(result is ScheduleValidator.ValidationResult.Invalid)
    }

    @Test
    fun scheduleValidator_breakTimeOutsideWorkHours_returnsInvalid() {
        val schedule = WorkSchedule(
            openTime = "09:00",
            closeTime = "18:00",
            breakStartTime = "19:00",
            breakEndTime = "20:00"
        )
        val result = scheduleValidator.validate(schedule)
        assertTrue(result is ScheduleValidator.ValidationResult.Invalid)
    }

    @Test
    fun scheduleValidator_breakEndTimeBeforeStartTime_returnsInvalid() {
        val schedule = WorkSchedule(
            openTime = "08:00",
            closeTime = "20:00",
            breakStartTime = "13:00",
            breakEndTime = "12:00"
        )
        val result = scheduleValidator.validate(schedule)
        assertTrue(result is ScheduleValidator.ValidationResult.Invalid)
    }

    @Test
    fun scheduleSlotGenerator_generatesCorrectSlotsAndSkipsBreakTime() {
        val schedule = WorkSchedule(
            openTime = "08:00",
            closeTime = "12:00",
            breakStartTime = "10:00",
            breakEndTime = "11:00",
            slotDurationMinutes = 60,
            defaultCapacity = 3
        )
        val slots = slotGenerator.generateSlots(schedule, "2026-10-29", "shop_01")

        // 08:00 - 09:00 (slot 1)
        // 09:00 - 10:00 (slot 2)
        // 10:00 - 11:00 (trùng giờ nghỉ -> bị bỏ qua)
        // 11:00 - 12:00 (slot 3)
        assertEquals(3, slots.size)
        assertEquals("08:00", slots[0].startTime)
        assertEquals("09:00", slots[0].endTime)
        assertEquals("09:00", slots[1].startTime)
        assertEquals("10:00", slots[1].endTime)
        assertEquals("11:00", slots[2].startTime)
        assertEquals("12:00", slots[2].endTime)
    }

    // ==========================================
    // 3. KIỂM THỬ CHỨC NĂNG 5: QUẢN LÝ KHUNG GIỜ (SLOTS)
    // ==========================================

    @Test
    fun slotCapacityRule_increaseCapacity_returnsValid() {
        val result = slotCapacityRule.validateCapacityChange(currentBooked = 2, newCapacity = 4)
        assertTrue(result is SlotCapacityRule.CapacityValidationResult.Valid)
    }

    @Test
    fun slotCapacityRule_decreaseCapacityAboveBooked_returnsValid() {
        val result = slotCapacityRule.validateCapacityChange(currentBooked = 2, newCapacity = 3)
        assertTrue(result is SlotCapacityRule.CapacityValidationResult.Valid)
    }

    @Test
    fun slotCapacityRule_decreaseCapacityEqualToBooked_returnsValid() {
        val result = slotCapacityRule.validateCapacityChange(currentBooked = 2, newCapacity = 2)
        assertTrue(result is SlotCapacityRule.CapacityValidationResult.Valid)
    }

    @Test
    fun slotCapacityRule_decreaseCapacityBelowBooked_returnsInvalid() {
        // Đã có 2 khách đặt, cố giảm xuống 1 -> Bị chặn nghiêm ngặt!
        val result = slotCapacityRule.validateCapacityChange(currentBooked = 2, newCapacity = 1)
        assertTrue(result is SlotCapacityRule.CapacityValidationResult.Invalid)
        val invalid = result as SlotCapacityRule.CapacityValidationResult.Invalid
        assertTrue(invalid.errorMessage.contains("2 khách đặt hẹn trước"))
    }

    @Test
    fun slotCapacityRule_zeroOrNegativeCapacity_returnsInvalid() {
        val zeroResult = slotCapacityRule.validateCapacityChange(currentBooked = 0, newCapacity = 0)
        assertTrue(zeroResult is SlotCapacityRule.CapacityValidationResult.Invalid)

        val negativeResult = slotCapacityRule.validateCapacityChange(currentBooked = 0, newCapacity = -1)
        assertTrue(negativeResult is SlotCapacityRule.CapacityValidationResult.Invalid)
    }

    @Test
    fun slotFilterPolicy_filtersAvailableSlotsCorrectly() {
        val slots = listOf(
            SlotItem(id = "1", capacity = 3, booked = 1, isBlocked = false), // available
            SlotItem(id = "2", capacity = 3, booked = 3, isBlocked = false), // full
            SlotItem(id = "3", capacity = 3, booked = 0, isBlocked = true)   // blocked
        )
        val filtered = slotFilterPolicy.filter(slots, SlotFilterType.AVAILABLE)
        assertEquals(1, filtered.size)
        assertEquals("1", filtered[0].id)
    }

    @Test
    fun slotFilterPolicy_filtersFullSlotsCorrectly() {
        val slots = listOf(
            SlotItem(id = "1", capacity = 3, booked = 1, isBlocked = false),
            SlotItem(id = "2", capacity = 3, booked = 3, isBlocked = false), // full
            SlotItem(id = "3", capacity = 3, booked = 0, isBlocked = true)
        )
        val filtered = slotFilterPolicy.filter(slots, SlotFilterType.FULL)
        assertEquals(1, filtered.size)
        assertEquals("2", filtered[0].id)
    }

    @Test
    fun slotFilterPolicy_filtersBlockedSlotsCorrectly() {
        val slots = listOf(
            SlotItem(id = "1", capacity = 3, booked = 1, isBlocked = false),
            SlotItem(id = "2", capacity = 3, booked = 3, isBlocked = false),
            SlotItem(id = "3", capacity = 3, booked = 0, isBlocked = true)   // blocked
        )
        val filtered = slotFilterPolicy.filter(slots, SlotFilterType.BLOCKED)
        assertEquals(1, filtered.size)
        assertEquals("3", filtered[0].id)
    }
}
