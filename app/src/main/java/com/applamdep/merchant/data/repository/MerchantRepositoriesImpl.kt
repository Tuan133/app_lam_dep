package com.applamdep.merchant.data.repository

import com.applamdep.merchant.core.base.BaseRepository
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.model.FinancialSummary
import com.applamdep.merchant.domain.model.MerchantStats
import com.applamdep.merchant.domain.model.Review
import com.applamdep.merchant.domain.model.ServiceItem
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.SystemConfig
import com.applamdep.merchant.domain.model.Transaction
import com.applamdep.merchant.domain.model.WorkSchedule
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.PlanRepository
import com.applamdep.merchant.domain.repository.ReviewRepository
import com.applamdep.merchant.domain.repository.ScheduleRepository
import com.applamdep.merchant.domain.repository.ServiceRepository
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.repository.SlotRepository
import com.applamdep.merchant.domain.repository.StatsRepository
import com.applamdep.merchant.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseRepository
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Cài đặt ShopRepository
 */
class ShopRepositoryImpl : BaseRepository(), ShopRepository {

    private val _shopProfileFlow = MutableStateFlow<ShopProfile?>(
        ShopProfile(
            id = "shop_01",
            ownerUid = "merchant_user_01",
            name = "L'Amour Haute Nail & Lash Lounge",
            ownerName = "Master Khánh Linh",
            phone = "0903123456",
            email = "contact@lamourbeauty.vn",
            address = "172/5 Trần Phú, Phường Hải Châu, Đà Nẵng",
            lat = 16.0678,
            lng = 108.2208,
            description = "Tổ hợp làm móng nghệ thuật cao cấp, nối mi lụa tơ tằm và chăm sóc da mặt chuyên sâu. Không gian tiêu chuẩn 5 sao.",
            openingHours = "08:30 - 21:00",
            status = MerchantConstants.SHOP_STATUS_APPROVED,
            plan = MerchantConstants.PLAN_VIP,
            planTermMonths = 6,
            requireDeposit = true,
            depositOptions = listOf(50000L, 100000L, 150000L, 200000L, 500000L)
        )
    )

    override fun getShopProfile(ownerUid: String): Flow<ShopProfile?> = _shopProfileFlow.asStateFlow()

    override suspend fun registerShop(profile: ShopProfile): Result<String> = safeCall {
        val newId = "shop_${UUID.randomUUID().toString().take(8)}"
        val submitted = profile.copy(
            id = newId,
            status = MerchantConstants.SHOP_STATUS_PENDING,
            updatedAt = System.currentTimeMillis()
        )
        _shopProfileFlow.value = submitted
        newId
    }

    override suspend fun updateProfile(profile: ShopProfile): Result<Unit> = safeCall {
        val current = _shopProfileFlow.value
        val updated = profile.copy(
            lastEditedAt = if (current?.isApproved == true && (current.name != profile.name || current.address != profile.address)) {
                System.currentTimeMillis()
            } else current?.lastEditedAt,
            updatedAt = System.currentTimeMillis()
        )
        _shopProfileFlow.value = updated
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt ServiceRepository
 */
class ServiceRepositoryImpl : BaseRepository(), ServiceRepository {

    private val _services = MutableStateFlow<List<ServiceItem>>(
        listOf(
            ServiceItem(
                id = "srv_01",
                shopId = "shop_01",
                name = "Sơn Gel Hữu Cơ Pháp + Dưỡng Móng",
                price = 180000L,
                durationMinutes = 45,
                description = "Sơn dưỡng bảo vệ móng tự nhiên, độ bền trên 4 tuần, màu sắc chuẩn phong cách Paris.",
                active = true
            ),
            ServiceItem(
                id = "srv_02",
                shopId = "shop_01",
                name = "Nối Mi Thiết Kế Sợi Lụa Tự Nhiên",
                price = 250000L,
                durationMinutes = 60,
                description = "Mi sợi lụa siêu mỏng nhẹ, không cộm ngứa, giữ nếp hoàn hảo 6 tuần.",
                active = true
            ),
            ServiceItem(
                id = "srv_03",
                shopId = "shop_01",
                name = "Liệu Trình Chăm Sóc Da Cấp Ẩm Chuyên Sâu",
                price = 350000L,
                durationMinutes = 75,
                description = "Tẩy tế bào chết vi hạt hữu cơ, điện di vitamin C và mặt nạ tảo biển vàng.",
                active = true
            )
        )
    )

    override fun getServices(shopId: String): Flow<List<ServiceItem>> = _services.asStateFlow()

    override suspend fun addService(service: ServiceItem): Result<String> = safeCall {
        val newId = "srv_${UUID.randomUUID().toString().take(6)}"
        val newItem = service.copy(id = newId, createdAt = System.currentTimeMillis())
        _services.value = _services.value + newItem
        newId
    }

    override suspend fun updateService(service: ServiceItem): Result<Unit> = safeCall {
        _services.value = _services.value.map { if (it.id == service.id) service else it }
    }

    override suspend fun deleteOrHideService(serviceId: String, hasActiveBookings: Boolean): Result<Unit> = safeCall {
        if (hasActiveBookings) {
            // Nghiệp vụ: Đang có khách đặt thì chuyển sang ẩn thay vì xóa
            _services.value = _services.value.map {
                if (it.id == serviceId) it.copy(active = false) else it
            }
        } else {
            _services.value = _services.value.filterNot { it.id == serviceId }
        }
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt ScheduleRepository
 */
class ScheduleRepositoryImpl : BaseRepository(), ScheduleRepository {

    private val _schedule = MutableStateFlow(
        WorkSchedule(
            shopId = "shop_01",
            workingDays = listOf(2, 3, 4, 5, 6, 7, 8),
            openTime = "08:30",
            closeTime = "21:00",
            breakStartTime = "12:00",
            breakEndTime = "13:00",
            offDates = listOf("2026-11-20"),
            slotDurationMinutes = 60,
            defaultCapacity = 3
        )
    )

    override fun getSchedule(shopId: String): Flow<WorkSchedule> = _schedule.asStateFlow()

    override suspend fun updateSchedule(schedule: WorkSchedule): Result<Unit> = safeCall {
        _schedule.value = schedule
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt SlotRepository
 */
class SlotRepositoryImpl : BaseRepository(), SlotRepository {

    private val _slots = MutableStateFlow<List<SlotItem>>(
        listOf(
            SlotItem(id = "slot_01", shopId = "shop_01", date = "Hôm nay", startTime = "09:00", endTime = "10:00", capacity = 3, booked = 2, isBlocked = false),
            SlotItem(id = "slot_02", shopId = "shop_01", date = "Hôm nay", startTime = "10:30", endTime = "11:30", capacity = 3, booked = 3, isBlocked = false),
            SlotItem(id = "slot_03", shopId = "shop_01", date = "Hôm nay", startTime = "13:30", endTime = "14:30", capacity = 3, booked = 1, isBlocked = false),
            SlotItem(id = "slot_04", shopId = "shop_01", date = "Hôm nay", startTime = "15:00", endTime = "16:00", capacity = 3, booked = 0, isBlocked = false),
            SlotItem(id = "slot_05", shopId = "shop_01", date = "Hôm nay", startTime = "16:30", endTime = "17:30", capacity = 3, booked = 0, isBlocked = true),
            SlotItem(id = "slot_06", shopId = "shop_01", date = "Hôm nay", startTime = "18:00", endTime = "19:00", capacity = 3, booked = 1, isBlocked = false)
        )
    )

    override fun getSlots(shopId: String, date: String): Flow<List<SlotItem>> = _slots.asStateFlow()

    override suspend fun saveGeneratedSlots(slots: List<SlotItem>): Result<Unit> = safeCall {
        _slots.value = slots
    }

    override suspend fun updateSlotCapacity(slotId: String, newCapacity: Int): Result<Unit> = safeCall {
        val current = _slots.value.find { it.id == slotId }
            ?: throw IllegalArgumentException("Khung giờ không tồn tại")
        if (newCapacity < current.booked) {
            throw IllegalStateException("Không thể giảm sức chứa nhỏ hơn số khách đã đặt (${current.booked})")
        }
        _slots.value = _slots.value.map {
            if (it.id == slotId) it.copy(capacity = newCapacity) else it
        }
    }

    override suspend fun toggleSlotBlock(slotId: String, isBlocked: Boolean): Result<Unit> = safeCall {
        _slots.value = _slots.value.map {
            if (it.id == slotId) it.copy(isBlocked = isBlocked) else it
        }
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt BookingRepository
 */
class BookingRepositoryImpl : BaseRepository(), BookingRepository {

    private val _bookings = MutableStateFlow<List<Booking>>(
        listOf(
            Booking(
                id = "BK-9021",
                customerUid = "cust_01",
                shopId = "shop_01",
                serviceIds = listOf("srv_01"),
                serviceNames = listOf("Sơn Gel Hữu Cơ Pháp + Dưỡng Móng"),
                slotId = "slot_01",
                startAt = System.currentTimeMillis() + 3600000L,
                endAt = System.currentTimeMillis() + 6300000L,
                customerName = "Mai Phương",
                customerPhone = "0905 112 233",
                note = "Khách thích màu thạch tự nhiên, nhẹ nhàng",
                status = MerchantConstants.BOOKING_STATUS_PENDING,
                depositAmount = 100000L
            ),
            Booking(
                id = "BK-9022",
                customerUid = "cust_02",
                shopId = "shop_01",
                serviceIds = listOf("srv_02"),
                serviceNames = listOf("Nối Mi Thiết Kế Sợi Lụa Tự Nhiên"),
                slotId = "slot_02",
                startAt = System.currentTimeMillis() - 1800000L,
                endAt = System.currentTimeMillis() + 1800000L,
                customerName = "Chị Thảo My",
                customerPhone = "0914 556 778",
                note = "Mắt hơi nhạy cảm với keo cay",
                status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                depositAmount = 100000L
            ),
            Booking(
                id = "BK-9018",
                customerUid = "cust_03",
                shopId = "shop_01",
                serviceIds = listOf("srv_03"),
                serviceNames = listOf("Liệu Trình Chăm Sóc Da Cấp Ẩm Chuyên Sâu"),
                slotId = "slot_03",
                startAt = System.currentTimeMillis() - 7200000L,
                endAt = System.currentTimeMillis() - 2700000L, // Kết thúc cách đây 45 phút (> 30p)
                customerName = "Chị Lan Anh",
                customerPhone = "0988 223 344",
                note = "Da thiên dầu vùng chữ T",
                status = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
                depositAmount = 150000L
            ),
            Booking(
                id = "BK-9010",
                customerUid = "cust_04",
                shopId = "shop_01",
                serviceIds = listOf("srv_01"),
                serviceNames = listOf("Sơn Gel Hữu Cơ Pháp"),
                slotId = "slot_04",
                startAt = System.currentTimeMillis() - 86400000L,
                endAt = System.currentTimeMillis() - 82800000L,
                customerName = "Bạn Thùy Tiên",
                customerPhone = "0932 778 899",
                status = MerchantConstants.BOOKING_STATUS_COMPLETED,
                completedAt = System.currentTimeMillis() - 82800000L,
                depositAmount = 100000L
            )
        )
    )

    override fun getShopBookings(shopId: String): Flow<List<Booking>> = _bookings.asStateFlow()

    override suspend fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        reason: String?
    ): Result<Unit> = safeCall {
        _bookings.value = _bookings.value.map { bk ->
            if (bk.id == bookingId) {
                bk.copy(
                    status = newStatus,
                    cancelReason = reason ?: bk.cancelReason,
                    cancelledAt = if (newStatus == MerchantConstants.BOOKING_STATUS_CANCELLED) System.currentTimeMillis() else bk.cancelledAt,
                    completedAt = if (newStatus == MerchantConstants.BOOKING_STATUS_COMPLETED) System.currentTimeMillis() else bk.completedAt
                )
            } else bk
        }
    }

    override suspend fun getExpiredInServiceBookings(
        shopId: String,
        thresholdMillis: Long
    ): Result<List<Booking>> = safeCall {
        _bookings.value.filter {
            it.shopId == shopId &&
            it.status == MerchantConstants.BOOKING_STATUS_IN_SERVICE &&
            it.endAt > 0 &&
            it.endAt <= thresholdMillis
        }
    }

    override suspend fun checkAndAutoCompleteBookings(shopId: String): Result<Int> = safeCall {
        val now = System.currentTimeMillis()
        val gracePeriod = 30 * 60 * 1000L // 30 phút theo Requirement #7
        var count = 0
        _bookings.value = _bookings.value.map { bk ->
            if (bk.shopId == shopId &&
                bk.status == MerchantConstants.BOOKING_STATUS_IN_SERVICE &&
                bk.endAt > 0 &&
                (now - bk.endAt) >= gracePeriod
            ) {
                count++
                bk.copy(
                    status = MerchantConstants.BOOKING_STATUS_COMPLETED,
                    completedAt = now,
                    autoCompleted = true
                )
            } else bk
        }
        count
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt ReviewRepository
 */
class ReviewRepositoryImpl : BaseRepository(), ReviewRepository {

    private val _reviews = MutableStateFlow<List<Review>>(
        listOf(
            Review(
                id = "rev_01",
                bookingId = "BK-9010",
                shopId = "shop_01",
                customerUid = "cust_04",
                customerName = "Thùy Tiên",
                rating = 5,
                content = "Không gian tiệm cực kỳ thư giãn, thợ làm móng tay nghề rất khéo và tỉ mỉ. Rất hài lòng!",
                createdAt = System.currentTimeMillis() - 86400000L,
                shopReply = "Dạ cảm ơn bạn Thùy Tiên nhiều ạ! L'Amour rất hân hạnh được đón tiếp bạn lần sau."
            ),
            Review(
                id = "rev_02",
                bookingId = "BK-8902",
                shopId = "shop_01",
                customerUid = "cust_05",
                customerName = "Minh Anh",
                rating = 4,
                content = "Dịch vụ nối mi đẹp tự nhiên, nhưng quán hôm đó hơi đông khách nên phải đợi 10 phút.",
                createdAt = System.currentTimeMillis() - 172800000L
            )
        )
    )

    override fun getShopReviews(shopId: String): Flow<List<Review>> = _reviews.asStateFlow()

    override suspend fun replyReview(reviewId: String, reply: String): Result<Unit> = safeCall {
        _reviews.value = _reviews.value.map {
            if (it.id == reviewId) it.copy(shopReply = reply, shopRepliedAt = System.currentTimeMillis()) else it
        }
    }

    override suspend fun reportReview(reviewId: String, reason: String): Result<Unit> = safeCall {
        _reviews.value = _reviews.value.map {
            if (it.id == reviewId) it.copy(reportedByShop = true, reportReason = reason, reportedAt = System.currentTimeMillis()) else it
        }
    }

    override suspend fun requestRemoveReview(reviewId: String, reason: String): Result<Unit> = safeCall {
        _reviews.value = _reviews.value.map {
            if (it.id == reviewId) it.copy(status = MerchantConstants.REVIEW_STATUS_REMOVE_REQUESTED, reportReason = reason, reportedAt = System.currentTimeMillis()) else it
        }
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt TransactionRepository
 */
class TransactionRepositoryImpl : BaseRepository(), TransactionRepository {

    private val _transactions = MutableStateFlow<List<Transaction>>(
        listOf(
            Transaction(id = "tx_01", type = MerchantConstants.TX_TYPE_DEPOSIT, bookingId = "BK-9021", shopId = "shop_01", customerUid = "cust_01", amount = 100000L, status = MerchantConstants.TX_STATUS_HELD, heldAt = System.currentTimeMillis() - 3600000L),
            Transaction(id = "tx_02", type = MerchantConstants.TX_TYPE_DEPOSIT, bookingId = "BK-9022", shopId = "shop_01", customerUid = "cust_02", amount = 100000L, status = MerchantConstants.TX_STATUS_HELD, heldAt = System.currentTimeMillis() - 7200000L),
            Transaction(id = "tx_03", type = MerchantConstants.TX_TYPE_RELEASE, bookingId = "BK-9010", shopId = "shop_01", customerUid = "cust_04", amount = 100000L, status = MerchantConstants.TX_STATUS_RELEASED_TO_SHOP, releasedAt = System.currentTimeMillis() - 82800000L),
            Transaction(id = "tx_04", type = MerchantConstants.TX_TYPE_REFUND, bookingId = "BK-8890", shopId = "shop_01", customerUid = "cust_09", amount = 50000L, status = MerchantConstants.TX_STATUS_REFUNDED, refundedAt = System.currentTimeMillis() - 259200000L)
        )
    )

    override fun getShopTransactions(shopId: String): Flow<List<Transaction>> = _transactions.asStateFlow()

    override fun getFinancialSummary(shopId: String): Flow<FinancialSummary> = _transactions.map { list ->
        val held = list.filter { it.status == MerchantConstants.TX_STATUS_HELD }.sumOf { it.amount }
        val released = list.filter { it.status == MerchantConstants.TX_STATUS_RELEASED_TO_SHOP }.sumOf { it.amount }
        val refunded = list.filter { it.status == MerchantConstants.TX_STATUS_REFUNDED }.sumOf { it.amount }
        FinancialSummary(
            heldEscrowBalance = held,
            releasedTotalBalance = released,
            refundedTotalBalance = refunded,
            transactions = list
        )
    }

    override suspend fun releaseDeposit(bookingId: String): Result<Unit> = safeCall {
        val now = System.currentTimeMillis()
        var updated = false
        val currentList = _transactions.value.map { tx ->
            if (tx.bookingId == bookingId && tx.status == MerchantConstants.TX_STATUS_HELD) {
                updated = true
                tx.copy(
                    type = MerchantConstants.TX_TYPE_RELEASE,
                    status = MerchantConstants.TX_STATUS_RELEASED_TO_SHOP,
                    releasedAt = now
                )
            } else tx
        }
        if (updated) {
            _transactions.value = currentList
        } else {
            val newTx = Transaction(
                id = "tx_${UUID.randomUUID().toString().take(6)}",
                type = MerchantConstants.TX_TYPE_RELEASE,
                bookingId = bookingId,
                shopId = "shop_01",
                amount = 100000L,
                status = MerchantConstants.TX_STATUS_RELEASED_TO_SHOP,
                releasedAt = now
            )
            _transactions.value = listOf(newTx) + _transactions.value
        }
    }

    override suspend fun refundDeposit(bookingId: String): Result<Unit> = safeCall {
        val now = System.currentTimeMillis()
        var updated = false
        val currentList = _transactions.value.map { tx ->
            if (tx.bookingId == bookingId && tx.status == MerchantConstants.TX_STATUS_HELD) {
                updated = true
                tx.copy(
                    type = MerchantConstants.TX_TYPE_REFUND,
                    status = MerchantConstants.TX_STATUS_REFUNDED,
                    refundedAt = now
                )
            } else tx
        }
        if (updated) {
            _transactions.value = currentList
        } else {
            val newTx = Transaction(
                id = "tx_${UUID.randomUUID().toString().take(6)}",
                type = MerchantConstants.TX_TYPE_REFUND,
                bookingId = bookingId,
                shopId = "shop_01",
                amount = 100000L,
                status = MerchantConstants.TX_STATUS_REFUNDED,
                refundedAt = now
            )
            _transactions.value = listOf(newTx) + _transactions.value
        }
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt PlanRepository
 */
class PlanRepositoryImpl : BaseRepository(), PlanRepository {

    private val _config = MutableStateFlow(
        SystemConfig(
            currencyExchangeRate = 25400.0,
            normalPlanFee = mapOf("3" to 300000L, "6" to 550000L, "12" to 1000000L),
            vipPlanFee = mapOf("3" to 600000L, "6" to 1100000L, "12" to 2000000L),
            isFirstMonthFree = true
        )
    )

    override fun getSystemConfig(): Flow<SystemConfig> = _config.asStateFlow()

    override suspend fun initiatePlanPayment(shopId: String, plan: String, termMonths: Int): Result<String> = safeCall {
        // Trả về URL cổng thanh toán QR giả lập
        "https://applamdep.mock/pay?shopId=$shopId&plan=$plan&term=$termMonths"
    }

    override suspend fun verifyPlanPayment(transactionId: String): Result<Boolean> = safeCall {
        true
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt StatsRepository
 */
class StatsRepositoryImpl(
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(),
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl()
) : BaseRepository(), StatsRepository {

    override fun getShopStats(shopId: String): Flow<MerchantStats> =
        bookingRepository.getShopBookings(shopId).map { bookings ->
            val pending = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_PENDING }
            val confirmed = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_CONFIRMED }
            val inService = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_IN_SERVICE }
            val completed = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }
            val cancelled = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_CANCELLED }
            val todayTotal = bookings.size
            val depositRevenue = bookings.filter { it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }.sumOf { it.depositAmount }

            MerchantStats(
                totalBookingsToday = todayTotal,
                pendingBookings = pending,
                confirmedBookings = confirmed,
                inServiceBookings = inService,
                completedBookings = completed,
                cancelledBookings = cancelled,
                estimatedRevenueToday = depositRevenue,
                totalRevenueMonth = depositRevenue * 15,
                averageRating = 4.9,
                totalReviews = 18
            )
        }
}
