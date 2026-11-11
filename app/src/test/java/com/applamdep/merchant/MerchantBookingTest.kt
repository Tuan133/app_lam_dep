package com.applamdep.merchant

import com.applamdep.common.BookingSyncGateway
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.data.repository.FakeBookingRepository
import com.applamdep.customer.data.repository.FakeReviewRepository
import com.applamdep.customer.domain.model.Booking as CustomerBooking
import com.applamdep.customer.domain.model.Review as CustomerReview
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ReviewRepositoryImpl
import com.applamdep.merchant.data.repository.StatsRepositoryImpl
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.rule.BookingFilterPolicy
import com.applamdep.merchant.domain.rule.BookingFilterType
import com.applamdep.merchant.domain.rule.BookingStateTransitionRule
import com.applamdep.merchant.domain.rule.MerchantAutoApprovalPolicy
import com.applamdep.merchant.domain.rule.MerchantAutoCompletionPolicy
import com.applamdep.merchant.domain.rule.MerchantCancellationPolicy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
    private val autoApprovalPolicy = MerchantAutoApprovalPolicy()
    private val cancellationPolicy = MerchantCancellationPolicy()
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

    // ==========================================
    // 4. KIỂM THỬ QUY TẮC 30 PHÚT TỪ CHỐI & TỰ ĐỘNG DUYỆT ĐƠN
    // ==========================================

    @Test
    fun autoApprovalPolicy_within30MinutesOfBooking_allowsRejectAndDoesNotAutoApprove() {
        val createdAt = 1_000_000_000L
        val currentTime = createdAt + (29 * 60 * 1000L) // Còn 1 phút

        val booking = Booking(
            id = "BK-APP-1",
            status = MerchantConstants.BOOKING_STATUS_PENDING,
            createdAt = createdAt
        )

        assertTrue(autoApprovalPolicy.canReject(booking, currentTime))
        assertFalse(autoApprovalPolicy.shouldAutoApprove(booking, currentTime))
        assertEquals(60 * 1000L, autoApprovalPolicy.remainingRejectMillis(booking, currentTime))
    }

    @Test
    fun autoApprovalPolicy_after30MinutesOfBooking_autoApprovesAndBlocksReject() {
        val createdAt = 1_000_000_000L
        val currentTime = createdAt + (30 * 60 * 1000L) // Đúng 30 phút

        val booking = Booking(
            id = "BK-APP-2",
            status = MerchantConstants.BOOKING_STATUS_PENDING,
            createdAt = createdAt
        )

        assertFalse(autoApprovalPolicy.canReject(booking, currentTime))
        assertTrue(autoApprovalPolicy.shouldAutoApprove(booking, currentTime))
        assertEquals(0L, autoApprovalPolicy.remainingRejectMillis(booking, currentTime))
    }

    @Test
    fun autoApprovalPolicy_nonPendingStatus_neverAutoApproves() {
        val createdAt = 1_000_000_000L
        val currentTime = createdAt + (60 * 60 * 1000L)

        val confirmed = Booking(id = "BK-1", status = MerchantConstants.BOOKING_STATUS_CONFIRMED, createdAt = createdAt)
        val cancelled = Booking(id = "BK-2", status = MerchantConstants.BOOKING_STATUS_CANCELLED, createdAt = createdAt)

        assertFalse(autoApprovalPolicy.shouldAutoApprove(confirmed, currentTime))
        assertFalse(autoApprovalPolicy.shouldAutoApprove(cancelled, currentTime))
    }

    @Test
    fun bookingRepository_rejectWithin30Minutes_succeeds() = runBlocking {
        var fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }

        // BK-9021 được khởi tạo với createdAt = initNow - 5 phút (còn 25 phút)
        val autoCount = repo.checkAndAutoApproveBookings("shop_01").getOrThrow()
        assertEquals(0, autoCount)

        fakeNow += 10 * 60 * 1000L // Mới trôi qua 15 phút tổng cộng
        val rejectResult = repo.updateBookingStatus(
            bookingId = "BK-9021",
            newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
            reason = "Quán kín lịch đột xuất"
        )
        assertTrue(rejectResult.isSuccess)

        val updated = repo.getShopBookings("shop_01").first().first { it.id == "BK-9021" }
        assertEquals(MerchantConstants.BOOKING_STATUS_CANCELLED, updated.status)
        assertEquals("Quán kín lịch đột xuất", updated.cancelReason)
    }

    @Test
    fun bookingRepository_after30Minutes_autoApprovesAndPreventsLateReject() = runBlocking {
        var fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }

        // Tiến thời gian thêm 25 phút => đủ 30 phút từ lúc khách đặt
        fakeNow += 25 * 60 * 1000L

        val lateRejectResult = repo.updateBookingStatus(
            bookingId = "BK-9021",
            newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
            reason = "Từ chối muộn"
        )
        assertTrue(lateRejectResult.isFailure)

        val updated = repo.getShopBookings("shop_01").first().first { it.id == "BK-9021" }
        assertEquals(MerchantConstants.BOOKING_STATUS_CONFIRMED, updated.status)
        assertTrue(updated.autoApproved)
    }

    @Test
    fun bookingRepository_checkAndAutoApproveBookings_confirmsExpiredPendingOrders() = runBlocking {
        var fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }

        fakeNow += 30 * 60 * 1000L
        val count = repo.checkAndAutoApproveBookings("shop_01").getOrThrow()
        assertEquals(1, count)

        val updated = repo.getShopBookings("shop_01").first().first { it.id == "BK-9021" }
        assertEquals(MerchantConstants.BOOKING_STATUS_CONFIRMED, updated.status)
        assertTrue(updated.autoApproved)
    }

    @Test
    fun formatters_formatCountdown_formatsMinutesAndSecondsCorrectly() {
        assertEquals("00:00", Formatters.formatCountdown(0L))
        assertEquals("00:01", Formatters.formatCountdown(500L))
        assertEquals("01:05", Formatters.formatCountdown(65_000L))
        assertEquals("30:00", Formatters.formatCountdown(30 * 60 * 1000L))
    }

    // ==========================================
    // 5. KIỂM THỬ QUY TẮC KHÓA HỦY LỊCH 30 PHÚT TRƯỚC GIỜ HẸN & LÝ DO ĐỀ XUẤT
    // ==========================================

    @Test
    fun cancellationPolicy_confirmedMoreThan30MinutesBeforeStart_allowsCancel() {
        val startAt = 2_000_000_000L
        val currentTime = startAt - (45 * 60 * 1000L) // Còn 45 phút trước giờ hẹn

        val booking = Booking(
            id = "BK-CN-1",
            status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            startAt = startAt
        )

        assertTrue(cancellationPolicy.canCancel(booking, currentTime))
        assertEquals(15 * 60 * 1000L, cancellationPolicy.remainingCancelMillis(booking, currentTime))
    }

    @Test
    fun cancellationPolicy_confirmedWithin30MinutesOfStart_disablesCancel() {
        val startAt = 2_000_000_000L
        val atExactly30Min = startAt - (30 * 60 * 1000L)
        val at10Min = startAt - (10 * 60 * 1000L)
        val afterStart = startAt + (5 * 60 * 1000L)

        val booking = Booking(
            id = "BK-CN-2",
            status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            startAt = startAt
        )

        assertFalse(cancellationPolicy.canCancel(booking, atExactly30Min))
        assertFalse(cancellationPolicy.canCancel(booking, at10Min))
        assertFalse(cancellationPolicy.canCancel(booking, afterStart))
        assertEquals(0L, cancellationPolicy.remainingCancelMillis(booking, atExactly30Min))
    }

    @Test
    fun cancellationPolicy_nonConfirmedStatus_returnsFalse() {
        val startAt = 2_000_000_000L
        val currentTime = startAt - (60 * 60 * 1000L)

        val pending = Booking(id = "BK-1", status = MerchantConstants.BOOKING_STATUS_PENDING, startAt = startAt)
        val inService = Booking(id = "BK-2", status = MerchantConstants.BOOKING_STATUS_IN_SERVICE, startAt = startAt)

        assertFalse(cancellationPolicy.canCancel(pending, currentTime))
        assertFalse(cancellationPolicy.canCancel(inService, currentTime))
    }

    @Test
    fun bookingRepository_cancelConfirmedOrderMoreThan30MinutesBeforeStart_succeeds() = runBlocking {
        val fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }
        val chosenReason = MerchantConstants.SUGGESTED_CANCEL_REASONS.first()

        // BK-9022 bắt đầu sau 2 tiếng -> được phép hủy
        val result = repo.updateBookingStatus(
            bookingId = "BK-9022",
            newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
            reason = chosenReason
        )
        assertTrue(result.isSuccess)

        val updated = repo.getShopBookings("shop_01").first().first { it.id == "BK-9022" }
        assertEquals(MerchantConstants.BOOKING_STATUS_CANCELLED, updated.status)
        assertEquals(chosenReason, updated.cancelReason)
    }

    @Test
    fun bookingRepository_cancelConfirmedOrderWithin30MinutesBeforeStart_fails() = runBlocking {
        val fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }

        // BK-9023 bắt đầu sau 20 phút (< 30 phút) -> bị khóa hủy lịch
        val result = repo.updateBookingStatus(
            bookingId = "BK-9023",
            newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
            reason = MerchantConstants.SUGGESTED_CANCEL_REASONS.first()
        )
        assertTrue(result.isFailure)

        val unchanged = repo.getShopBookings("shop_01").first().first { it.id == "BK-9023" }
        assertEquals(MerchantConstants.BOOKING_STATUS_CONFIRMED, unchanged.status)
    }

    @Test
    fun suggestedCancelReasons_areNonEmptyAndDistinct() {
        assertTrue(MerchantConstants.SUGGESTED_CANCEL_REASONS.isNotEmpty())
        assertEquals(
            MerchantConstants.SUGGESTED_CANCEL_REASONS.size,
            MerchantConstants.SUGGESTED_CANCEL_REASONS.distinct().size
        )
    }

    // ==========================================
    // 6. KIỂM THỬ NÚT HOÀN THÀNH, TỰ DUYỆT SAU 30P, CỘNG DOANH THU & KHÁCH ĐÁNH GIÁ
    // ==========================================

    @Test
    fun transitionRule_confirmedDirectlyToCompleted_isValid() {
        val result = transitionRule.validateTransition(
            currentStatus = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            nextStatus = MerchantConstants.BOOKING_STATUS_COMPLETED
        )
        assertTrue(result is BookingStateTransitionRule.TransitionResult.Valid)
    }

    @Test
    fun autoCompletionPolicy_canComplete_respectsServiceEndTime() {
        val endAt = 2_000_000_000L
        val booking = Booking(
            id = "BK-CMP-1",
            status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            endAt = endAt
        )

        assertFalse(autoCompletionPolicy.canComplete(booking, endAt - 1000L))
        assertTrue(autoCompletionPolicy.canComplete(booking, endAt))
        assertTrue(autoCompletionPolicy.canComplete(booking, endAt + 60_000L))
    }

    @Test
    fun autoCompletionPolicy_confirmedOver30MinutesPastEndAt_shouldAutoComplete() {
        val endAt = 2_000_000_000L
        val booking = Booking(
            id = "BK-CMP-2",
            status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
            endAt = endAt
        )

        assertFalse(autoCompletionPolicy.shouldAutoComplete(booking, endAt + (29 * 60 * 1000L)))
        assertTrue(autoCompletionPolicy.shouldAutoComplete(booking, endAt + (30 * 60 * 1000L)))
    }

    @Test
    fun bookingRepository_completeConfirmedOrderBeforeEndAt_fails() = runBlocking {
        val fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }

        // BK-9022 kết thúc sau 3 tiếng -> chưa thể bấm Hoàn thành
        val result = repo.updateBookingStatus("BK-9022", MerchantConstants.BOOKING_STATUS_COMPLETED)
        assertTrue(result.isFailure)

        val unchanged = repo.getShopBookings("shop_01").first().first { it.id == "BK-9022" }
        assertEquals(MerchantConstants.BOOKING_STATUS_CONFIRMED, unchanged.status)
    }

    @Test
    fun bookingRepository_completeConfirmedOrderAtEndAt_addsDepositToRevenueAndEnablesCustomerReview() = runBlocking {
        val fakeNow = 1_700_000_000_000L
        lateinit var customerBookingRepo: FakeBookingRepository
        val localGateway = object : BookingSyncGateway {
            override fun onCustomerBookingCreated(booking: CustomerBooking) = Unit
            override fun onCustomerBookingCancelled(bookingId: String, reason: String) = Unit
            override fun onCustomerBookingReviewed(bookingId: String) = Unit
            override fun onCustomerReviewSubmitted(review: CustomerReview) = Unit
            override fun onMerchantBookingUpdated(booking: Booking) {
                customerBookingRepo.syncFromMerchant(
                    booking = CustomerBooking(
                        id = booking.id,
                        customerId = booking.customerUid,
                        customerName = booking.customerName,
                        customerPhone = booking.customerPhone,
                        shopId = "salon_01",
                        shopName = "1994 Nailsroom",
                        shopAddress = "172/5 Trần Phú, Hải Châu, Đà Nẵng",
                        serviceId = "srv_01",
                        serviceName = booking.serviceNames.firstOrNull() ?: "Dịch vụ",
                        servicePrice = 200000L,
                        bookingDate = "2026-11-10",
                        bookingTime = "22:00",
                        appointmentTimestamp = booking.startAt,
                        durationMinutes = 45,
                        requiresDeposit = booking.depositAmount > 0L,
                        depositAmount = booking.depositAmount,
                        status = booking.status,
                        isReviewed = booking.isReviewed
                    ),
                    createIfMissing = true
                )
            }
        }
        customerBookingRepo = FakeBookingRepository(syncGateway = localGateway)
        val bookingRepo = BookingRepositoryImpl(nowProvider = { fakeNow }, syncGateway = localGateway)
        val transactionRepo = TransactionRepositoryImpl()
        val statsRepo = StatsRepositoryImpl(bookingRepository = bookingRepo, reviewRepository = ReviewRepositoryImpl())

        val revenueBefore = statsRepo.getShopStats("shop_01").first().estimatedRevenueToday
        val releasedBefore = transactionRepo.getFinancialSummary("shop_01").first().releasedTotalBalance

        // BK-9024 đã qua giờ hoàn thành 5 phút, cọc 100.000 đ
        val completeRes = bookingRepo.updateBookingStatus("BK-9024", MerchantConstants.BOOKING_STATUS_COMPLETED)
        assertTrue(completeRes.isSuccess)
        transactionRepo.releaseDeposit("BK-9024", 100000L)

        val revenueAfter = statsRepo.getShopStats("shop_01").first().estimatedRevenueToday
        val releasedAfter = transactionRepo.getFinancialSummary("shop_01").first().releasedTotalBalance
        assertEquals(revenueBefore + 100000L, revenueAfter)
        assertEquals(releasedBefore + 100000L, releasedAfter)

        // Khách hàng user_cust_01 thấy đơn ở trạng thái COMPLETED và chưa đánh giá -> có thể viết đánh giá
        val custOrder = customerBookingRepo.getCustomerBookings("user_cust_01").first().find { it.id == "BK-9024" }
        assertTrue(custOrder != null && custOrder.status == AppConstants.BOOKING_STATUS_COMPLETED && !custOrder.isReviewed)
    }

    @Test
    fun bookingRepository_autoCompleteConfirmedOrderAfter30MinutesPastEndAt_succeeds() = runBlocking {
        var fakeNow = 1_700_000_000_000L
        val repo = BookingRepositoryImpl { fakeNow }

        // BK-9024 endAt = initNow - 5p => tiến thêm 25p là đúng 30p sau endAt
        fakeNow += 25 * 60 * 1000L
        val autoCount = repo.checkAndAutoCompleteBookings("shop_01").getOrThrow()
        assertTrue(autoCount >= 1)

        val bk9024 = repo.getShopBookings("shop_01").first().first { it.id == "BK-9024" }
        assertEquals(MerchantConstants.BOOKING_STATUS_COMPLETED, bk9024.status)
        assertTrue(bk9024.autoCompleted)
    }

    @Test
    fun appBookingSyncBridge_buffersAndReplaysEventsRegardlessOfRegistrationOrder() = runBlocking {
        com.applamdep.common.AppBookingSyncBridge.resetForTesting()
        try {
            val now = 1_700_000_000_000L
            // Tình huống 1: Merchant mở & hoàn thành BK-9024 trước khi Customer repository khởi tạo
            val merchantRepo1 = BookingRepositoryImpl(
                syncGateway = com.applamdep.common.AppBookingSyncBridge,
                nowProvider = { now }
            )
            com.applamdep.common.AppBookingSyncBridge.registerMerchantBookingRepository(merchantRepo1)
            assertTrue(merchantRepo1.updateBookingStatus("BK-9024", MerchantConstants.BOOKING_STATUS_COMPLETED).isSuccess)

            val lateCustomerRepo = FakeBookingRepository(syncGateway = com.applamdep.common.AppBookingSyncBridge)
            com.applamdep.common.AppBookingSyncBridge.registerCustomerBookingRepository(lateCustomerRepo)
            val syncedForReview = lateCustomerRepo.getCustomerBookings("user_cust_01").first().find { it.id == "BK-9024" }
            assertTrue(syncedForReview != null && syncedForReview.status == AppConstants.BOOKING_STATUS_COMPLETED)

            // Tình huống 2: Customer đặt đơn mới trước khi Merchant repository khởi tạo
            com.applamdep.common.AppBookingSyncBridge.resetForTesting()
            val earlyCustomerRepo = FakeBookingRepository(syncGateway = com.applamdep.common.AppBookingSyncBridge)
            com.applamdep.common.AppBookingSyncBridge.registerCustomerBookingRepository(earlyCustomerRepo)
            val newCustBooking = CustomerBooking(
                id = "BK-SYNC-99",
                customerId = "user_cust_01",
                customerName = "Khánh Linh",
                customerPhone = "0903 123 456",
                shopId = "salon_01",
                shopName = "1994 Nailsroom",
                shopAddress = "172/5 Trần Phú",
                serviceId = "srv_01",
                serviceName = "Sơn Gel Hữu Cơ Pháp",
                servicePrice = 180000L,
                bookingDate = "2026-10-30",
                bookingTime = "10:30",
                appointmentTimestamp = now + 7200_000L,
                durationMinutes = 60,
                requiresDeposit = true,
                depositAmount = 100000L,
                status = AppConstants.BOOKING_STATUS_PENDING,
                createdAt = now
            )
            assertTrue(earlyCustomerRepo.createBooking(newCustBooking).isSuccess)

            val lateMerchantRepo = BookingRepositoryImpl(
                syncGateway = com.applamdep.common.AppBookingSyncBridge,
                nowProvider = { now }
            )
            val lateTxRepo = TransactionRepositoryImpl()
            com.applamdep.common.AppBookingSyncBridge.registerMerchantBookingRepository(lateMerchantRepo)
            com.applamdep.common.AppBookingSyncBridge.registerMerchantTransactionRepository(lateTxRepo)

            val syncedToMerchant = lateMerchantRepo.getShopBookings("shop_01").first().find { it.id == "BK-SYNC-99" }
            assertTrue(syncedToMerchant != null && syncedToMerchant.slotId == "2026-10-30_slot_02")
            assertEquals("2026-10-30", syncedToMerchant!!.bookingDate)
            val heldTx = lateTxRepo.getShopTransactions("shop_01").first().find { it.bookingId == "BK-SYNC-99" }
            assertTrue(heldTx != null && heldTx.amount == 100000L)
        } finally {
            com.applamdep.common.AppBookingSyncBridge.resetForTesting()
        }
    }
}
