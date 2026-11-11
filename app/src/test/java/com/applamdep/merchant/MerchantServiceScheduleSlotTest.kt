package com.applamdep.merchant

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ScheduleRepositoryImpl
import com.applamdep.merchant.data.repository.ServiceRepositoryImpl
import com.applamdep.merchant.data.repository.SlotRepositoryImpl
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.model.ServiceItem
import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.WorkSchedule
import com.applamdep.merchant.domain.rule.ScheduleSlotGenerator
import com.applamdep.merchant.domain.rule.ScheduleValidator
import com.applamdep.merchant.domain.rule.ServiceRemovalPolicy
import com.applamdep.merchant.domain.rule.ServiceValidator
import com.applamdep.merchant.domain.rule.SlotBookingSynchronizer
import com.applamdep.merchant.domain.rule.SlotCapacityRule
import com.applamdep.merchant.domain.rule.SlotEditValidator
import com.applamdep.merchant.domain.rule.SlotFilterPolicy
import com.applamdep.merchant.domain.rule.SlotFilterType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    private val slotEditValidator = SlotEditValidator(slotCapacityRule)
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

    @Test
    fun serviceRepository_addAndUpdateServiceImage_persistsImageUrl() = runBlocking {
        val repo = ServiceRepositoryImpl()
        val newId = repo.addService(
            ServiceItem(
                shopId = "shop_01",
                name = "Vẽ Móng Mắt Mèo Cực Quang",
                price = 280000L,
                durationMinutes = 60,
                category = "Nail Art",
                imageUrl = "preset:nail_cateye_aurora"
            )
        ).getOrThrow()

        val created = repo.getServices("shop_01").first().first { it.id == newId }
        assertEquals("preset:nail_cateye_aurora", created.imageUrl)

        val updatedResult = repo.updateService(
            created.copy(imageUrl = "content://media/external/images/media/1234")
        )
        assertTrue(updatedResult.isSuccess)

        val refreshed = repo.getServices("shop_01").first().first { it.id == newId }
        assertEquals("content://media/external/images/media/1234", refreshed.imageUrl)
    }

    @Test
    fun slotEditValidator_validNonOverlappingEdit_returnsValid() {
        val target = SlotItem(id = "slot_01", startTime = "09:00", endTime = "10:15", capacity = 4, booked = 2)
        val others = listOf(
            SlotItem(id = "slot_02", startTime = "10:30", endTime = "11:30", capacity = 3, booked = 1)
        )
        val result = slotEditValidator.validate(target, others)
        assertTrue(result is SlotEditValidator.ValidationResult.Valid)
    }

    @Test
    fun slotEditValidator_overlappingOrInvalidTime_returnsInvalid() {
        val overlapping = SlotItem(id = "slot_01", startTime = "09:30", endTime = "11:00", capacity = 3, booked = 1)
        val others = listOf(
            SlotItem(id = "slot_02", startTime = "10:30", endTime = "11:30", capacity = 3, booked = 1)
        )
        assertTrue(slotEditValidator.validate(overlapping, others) is SlotEditValidator.ValidationResult.Invalid)

        val inverted = SlotItem(id = "slot_01", startTime = "10:00", endTime = "09:00", capacity = 3, booked = 1)
        assertTrue(slotEditValidator.validate(inverted, others) is SlotEditValidator.ValidationResult.Invalid)
    }

    @Test
    fun slotEditValidator_capacityBelowBooked_returnsInvalid() {
        val target = SlotItem(id = "slot_01", startTime = "09:00", endTime = "10:00", capacity = 1, booked = 2)
        val result = slotEditValidator.validate(target, emptyList())
        assertTrue(result is SlotEditValidator.ValidationResult.Invalid)
    }

    @Test
    fun slotRepository_updateSlot_persistsAndSortsChronologically() = runBlocking {
        val repo = SlotRepositoryImpl()
        val original = repo.getSlots("shop_01", "2026-10-29").first().first { it.id == "slot_04" }
        val edited = original.copy(startTime = "12:00", endTime = "13:00", capacity = 5)

        val updateRes = repo.updateSlot(edited)
        assertTrue(updateRes.isSuccess)

        val allAfter = repo.getSlots("shop_01", "2026-10-29").first()
        val updatedSlot = allAfter.first { it.id == "slot_04" }
        assertEquals("12:00", updatedSlot.startTime)
        assertEquals("13:00", updatedSlot.endTime)
        assertEquals(5, updatedSlot.capacity)
        assertEquals(allAfter.sortedBy { it.startTime }.map { it.id }, allAfter.map { it.id })
    }

    @Test
    fun defaultCapacity_andSlotCapacity_defaultToOneCustomerPerOneHourSlot() = runBlocking {
        assertEquals(1, WorkSchedule().defaultCapacity)
        assertEquals(1, SlotItem().capacity)
        val schedule = ScheduleRepositoryImpl().getSchedule("shop_01").first()
        assertEquals(1, schedule.defaultCapacity)
        assertEquals(60, schedule.slotDurationMinutes)
    }

    @Test
    fun slotRepository_isolatesSlotsByDate_withCapacityOneAndZeroBooked() = runBlocking {
        val repo = SlotRepositoryImpl()
        val todaySlots = repo.getSlots("shop_01", "2026-10-29").first()
        val tomorrowSlots = repo.getSlots("shop_01", "2026-10-30").first()

        assertEquals(7, todaySlots.size)
        assertTrue(todaySlots.all { it.capacity == 1 && it.date == "2026-10-29" })
        assertEquals(7, tomorrowSlots.size)
        assertTrue(tomorrowSlots.all { it.capacity == 1 && it.booked == 0 && !it.isBlocked && it.date == "2026-10-30" })
    }

    @Test
    fun slotBookingSynchronizer_matchesActualBookingsPerSlotAndDate() = runBlocking {
        val slotRepo = SlotRepositoryImpl()
        val bookingRepo = BookingRepositoryImpl()
        val synchronizer = SlotBookingSynchronizer()

        val todaySlots = slotRepo.getSlots("shop_01", "2026-10-29").first()
        val allBookings = bookingRepo.getShopBookings("shop_01").first()

        val todaySynced = synchronizer.synchronize(
            slots = todaySlots,
            allBookings = allBookings,
            shopId = "shop_01",
            selectedDate = "2026-10-29"
        )

        assertEquals(1, todaySynced.slots.first { it.id == "slot_01" }.booked)
        assertEquals(listOf("BK-9021"), todaySynced.slotBookings["slot_01"]!!.map { it.id })
        assertEquals(1, todaySynced.slots.first { it.id == "slot_02" }.booked)
        assertEquals(listOf("BK-9022"), todaySynced.slotBookings["slot_02"]!!.map { it.id })
        assertEquals(1, todaySynced.slots.first { it.id == "slot_03" }.booked)
        assertEquals(listOf("BK-9023"), todaySynced.slotBookings["slot_03"]!!.map { it.id })
        assertEquals(1, todaySynced.slots.first { it.id == "slot_04" }.booked)
        // BK-9010 là đơn ngày 2026-10-28 nên không trộn vào slot_04 ngày 2026-10-29
        assertEquals(listOf("BK-9018"), todaySynced.slotBookings["slot_04"]!!.map { it.id })
        assertEquals(0, todaySynced.slots.first { it.id == "slot_05" }.booked)
        assertTrue(todaySynced.slotBookings["slot_05"]!!.isEmpty())
        assertEquals(1, todaySynced.slots.first { it.id == "slot_06" }.booked)
        assertEquals(listOf("BK-9024"), todaySynced.slotBookings["slot_06"]!!.map { it.id })
        assertEquals(0, todaySynced.slots.first { it.id == "slot_07" }.booked)
        assertTrue(todaySynced.slotBookings["slot_07"]!!.isEmpty())

        val tomorrowSlots = slotRepo.getSlots("shop_01", "2026-10-30").first()
        val tomorrowSynced = synchronizer.synchronize(
            slots = tomorrowSlots,
            allBookings = allBookings,
            shopId = "shop_01",
            selectedDate = "2026-10-30"
        )
        assertTrue(tomorrowSynced.slots.all { it.booked == 0 })
        assertTrue(tomorrowSynced.slotBookings.values.all { it.isEmpty() })
    }

    @Test
    fun slotBookingSynchronizer_cancellingBooking_freesSlotCapacityWhilePreservingOrderHistory() = runBlocking {
        val baseTime = 1_760_000_000_000L
        val slotRepo = SlotRepositoryImpl()
        val bookingRepo = BookingRepositoryImpl(nowProvider = { baseTime })
        val synchronizer = SlotBookingSynchronizer()

        val cancelResult = bookingRepo.updateBookingStatus(
            bookingId = "BK-9022",
            newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
            reason = "Khách đổi lịch"
        )
        assertTrue(cancelResult.isSuccess)

        val todaySlots = slotRepo.getSlots("shop_01", "2026-10-29").first()
        val allBookings = bookingRepo.getShopBookings("shop_01").first()
        val synced = synchronizer.synchronize(
            slots = todaySlots,
            allBookings = allBookings,
            shopId = "shop_01",
            selectedDate = "2026-10-29"
        )

        val slot02 = synced.slots.first { it.id == "slot_02" }
        assertEquals(0, slot02.booked)
        assertEquals(1, synced.slotBookings["slot_02"]!!.size)
        assertEquals(MerchantConstants.BOOKING_STATUS_CANCELLED, synced.slotBookings["slot_02"]!![0].status)
        assertEquals("Khách đổi lịch", synced.slotBookings["slot_02"]!![0].cancelReason)
    }
}
