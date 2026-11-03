package com.applamdep.merchant

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.rule.BookingFilterPolicy
import com.applamdep.merchant.domain.rule.BookingFilterType
import com.applamdep.merchant.domain.rule.BookingStateTransitionRule
import com.applamdep.merchant.domain.rule.MerchantAutoCompletionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests kiểm thử quy tắc nghiệp vụ Đóng gói & Đa hình cho:
 * - Chức năng 6: Quản lý lịch hẹn & Quy tắc chuyển đổi trạng thái dịch vụ (State Machine)
 * - Chức năng 7: Chính sách tự động chuyển sang hoàn thành sau 30 phút kể từ lúc kết thúc dịch vụ
 */
class MerchantBookingTest {

    private val transitionRule = BookingStateTransitionRule()
    private val autoCompletionPolicy = MerchantAutoCompletionPolicy()
    private val filterPolicy = BookingFilterPolicy()

    // ==========================================
    // 1. KIỂM THỬ CHỨC NĂNG 6: QUY TẮC CHUYỂN TRẠNG THÁI
    // ==========================================

    @Test
    fun transitionRule_pendingToConfirmed_isValid() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_PENDING,
            nextStatus = MerchantConstants.BOOKING_STATUS_CONFIRMED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Valid)
    }

    @Test
    fun transitionRule_pendingToCancelled_isValid() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_PENDING,
            nextStatus = MerchantConstants.BOOKING_STATUS_CANCELLED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Valid)
    }

    @Test
    fun transitionRule_confirmedToInService_isValid() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            nextStatus = MerchantConstants.BOOKING_STATUS_IN_SERVICE
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Valid)
    }

    @Test
    fun transitionRule_confirmedToCancelled_isValid() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            nextStatus = MerchantConstants.BOOKING_STATUS_CANCELLED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Valid)
    }

    @Test
    fun transitionRule_inServiceToCompleted_isValid() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
            nextStatus = MerchantConstants.BOOKING_STATUS_COMPLETED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Valid)
    }

    @Test
    fun transitionRule_pendingDirectlyToCompleted_isRejected() {
        // Chưa phục vụ mà nhảy thẳng sang hoàn thành -> Bị chặn
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_PENDING,
            nextStatus = MerchantConstants.BOOKING_STATUS_COMPLETED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Invalid)
    }

    @Test
    fun transitionRule_completedIsTerminalState_cannotTransition() {
        // Đã hoàn thành thì không thể hủy hay quay lại
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_COMPLETED,
            nextStatus = MerchantConstants.BOOKING_STATUS_CANCELLED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Invalid)
    }

    @Test
    fun transitionRule_cancelledIsTerminalState_cannotTransition() {
        // Đã hủy thì không thể chuyển sang trạng thái khác
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
            nextStatus = MerchantConstants.BOOKING_STATUS_CONFIRMED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Invalid)
    }

    @Test
    fun transitionRule_sameStateTransition_isRejected() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_PENDING,
            nextStatus = MerchantConstants.BOOKING_STATUS_PENDING
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Invalid)
    }

    // ==========================================
    // 2. KIỂM THỬ CHỨC NĂNG 7: TỰ ĐỘNG HOÀN THÀNH (+30 PHÚT)
    // ==========================================

    @Test
    fun autoCompletionPolicy_inServiceOver30MinutesPastEndAt_shouldAutoComplete() {
        val endAt = 1000000000L
        val gracePeriod = 30 * 60 * 1000L
        val currentTime = endAt + gracePeriod + 5000L // Quá 30 phút

        val booking = Booking(
            id = "BK-01",
            status = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
            endAt = endAt
        )

        val shouldComplete = autoCompletionPolicy.shouldAutoComplete(booking, currentTime)
        assertTrue("Đơn đang phục vụ quá 30p kể từ lúc kết thúc phải được tự động hoàn thành", shouldComplete)
    }

    @Test
    fun autoCompletionPolicy_inServiceUnder30MinutesPastEndAt_shouldNotAutoComplete() {
        val endAt = 1000000000L
        val gracePeriod = 30 * 60 * 1000L
        val currentTime = endAt + gracePeriod - 10000L // Chưa đủ 30 phút

        val booking = Booking(
            id = "BK-02",
            status = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
            endAt = endAt
        )

        val shouldComplete = autoCompletionPolicy.shouldAutoComplete(booking, currentTime)
        assertFalse("Chưa quá 30 phút thì không được tự động hoàn tất", shouldComplete)
    }

    @Test
    fun autoCompletionPolicy_pendingBookingOver30Minutes_shouldNotAutoComplete() {
        // Đơn đang ở trạng thái PENDING thì không được tự động hoàn tất
        val endAt = 1000000000L
        val currentTime = endAt + (45 * 60 * 1000L)

        val booking = Booking(
            id = "BK-03",
            status = MerchantConstants.BOOKING_STATUS_PENDING,
            endAt = endAt
        )

        val shouldComplete = autoCompletionPolicy.shouldAutoComplete(booking, currentTime)
        assertFalse(shouldComplete)
    }

    @Test
    fun autoCompletionPolicy_findEligibleBookings_filtersAccurately() {
        val endAt = 1000000000L
        val now = endAt + (35 * 60 * 1000L)

        val bookings = listOf(
            Booking(id = "1", status = MerchantConstants.BOOKING_STATUS_IN_SERVICE, endAt = endAt), // đủ điều kiện
            Booking(id = "2", status = MerchantConstants.BOOKING_STATUS_IN_SERVICE, endAt = now),   // chưa hết giờ
            Booking(id = "3", status = MerchantConstants.BOOKING_STATUS_COMPLETED, endAt = endAt)   // đã hoàn thành
        )

        val eligible = autoCompletionPolicy.findEligibleBookings(bookings, now)
        assertEquals(1, eligible.size)
        assertEquals("1", eligible[0].id)
    }

    // ==========================================
    // 3. KIỂM THỬ BỘ LỌC VÀ TÌM KIẾM BOOKING
    // ==========================================

    @Test
    fun bookingFilterPolicy_filtersByStatusAndSearchQuery() {
        val bookings = listOf(
            Booking(
                id = "BK-9021",
                customerName = "Mai Phương",
                customerPhone = "0905112233",
                serviceNames = listOf("Sơn Gel Hữu Cơ"),
                status = MerchantConstants.BOOKING_STATUS_PENDING
            ),
            Booking(
                id = "BK-9022",
                customerName = "Thảo My",
                customerPhone = "0914556778",
                serviceNames = listOf("Nối Mi Thiết Kế"),
                status = MerchantConstants.BOOKING_STATUS_CONFIRMED
            ),
            Booking(
                id = "BK-9018",
                customerName = "Lan Anh",
                customerPhone = "0988223344",
                serviceNames = listOf("Chăm Sóc Da"),
                status = MerchantConstants.BOOKING_STATUS_IN_SERVICE
            )
        )

        // 1. Lọc theo trạng thái CONFIRMED
        val confirmedList = filterPolicy.filter(bookings, BookingFilterType.CONFIRMED)
        assertEquals(1, confirmedList.size)
        assertEquals("BK-9022", confirmedList[0].id)

        // 2. Tìm kiếm theo tên khách
        val searchByName = filterPolicy.filter(bookings, BookingFilterType.ALL, "Phương")
        assertEquals(1, searchByName.size)
        assertEquals("BK-9021", searchByName[0].id)

        // 3. Tìm kiếm theo số điện thoại
        val searchByPhone = filterPolicy.filter(bookings, BookingFilterType.ALL, "0988")
        assertEquals(1, searchByPhone.size)
        assertEquals("BK-9018", searchByPhone[0].id)

        // 4. Tìm kiếm theo tên dịch vụ
        val searchByService = filterPolicy.filter(bookings, BookingFilterType.ALL, "Nối mi")
        assertEquals(1, searchByService.size)
        assertEquals("BK-9022", searchByService[0].id)
    }
}
