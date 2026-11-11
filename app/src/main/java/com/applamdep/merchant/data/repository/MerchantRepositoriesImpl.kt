package com.applamdep.merchant.data.repository

import com.applamdep.common.BookingSyncGateway
import com.applamdep.merchant.core.base.BaseRepository
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.util.Formatters
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
import com.applamdep.merchant.domain.rule.MerchantAutoApprovalPolicy
import com.applamdep.merchant.domain.rule.MerchantAutoCompletionPolicy
import com.applamdep.merchant.domain.rule.MerchantCancellationPolicy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseRepository
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Cài đặt ShopRepository
 */
class ShopRepositoryImpl(
    private val syncGateway: BookingSyncGateway? = null
) : BaseRepository(), ShopRepository {

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
        syncGateway?.onMerchantShopStatusUpdated(newId, submitted.status)
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
        syncGateway?.onMerchantShopStatusUpdated(updated.id, updated.status)
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
                category = "Nail Art",
                imageUrl = "preset:srv_nail_gel",
                description = "Sơn dưỡng bảo vệ móng tự nhiên, độ bền trên 4 tuần, màu sắc chuẩn phong cách Paris.",
                active = true
            ),
            ServiceItem(
                id = "srv_02",
                shopId = "shop_01",
                name = "Nối Mi Thiết Kế Sợi Lụa Tự Nhiên",
                price = 250000L,
                durationMinutes = 60,
                category = "Nối Mi & Mày",
                imageUrl = "preset:srv_eyelash",
                description = "Mi sợi lụa siêu mỏng nhẹ, không cộm ngứa, giữ nếp hoàn hảo 6 tuần.",
                active = true
            ),
            ServiceItem(
                id = "srv_03",
                shopId = "shop_01",
                name = "Liệu Trình Chăm Sóc Da Cấp Ẩm Chuyên Sâu",
                price = 350000L,
                durationMinutes = 75,
                category = "Chăm Sóc Da",
                imageUrl = "preset:srv_spa_facial",
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
            defaultCapacity = 1
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

    companion object {
        const val DEFAULT_SEEDED_DATE = "2026-10-29"
    }

    private fun createDefaultSlotsForDate(shopId: String, date: String): List<SlotItem> {
        val isInitialDay = date == DEFAULT_SEEDED_DATE
        val prefix = if (isInitialDay) "" else "${date}_"
        return listOf(
            SlotItem(id = "${prefix}slot_01", shopId = shopId, date = date, startTime = "09:00", endTime = "10:00", capacity = 1, booked = 0, isBlocked = false),
            SlotItem(id = "${prefix}slot_02", shopId = shopId, date = date, startTime = "10:30", endTime = "11:30", capacity = 1, booked = 0, isBlocked = false),
            SlotItem(id = "${prefix}slot_03", shopId = shopId, date = date, startTime = "13:30", endTime = "14:30", capacity = 1, booked = 0, isBlocked = false),
            SlotItem(id = "${prefix}slot_04", shopId = shopId, date = date, startTime = "15:00", endTime = "16:00", capacity = 1, booked = 0, isBlocked = false),
            SlotItem(id = "${prefix}slot_05", shopId = shopId, date = date, startTime = "16:30", endTime = "17:30", capacity = 1, booked = 0, isBlocked = isInitialDay),
            SlotItem(id = "${prefix}slot_06", shopId = shopId, date = date, startTime = "18:00", endTime = "19:00", capacity = 1, booked = 0, isBlocked = false),
            SlotItem(id = "${prefix}slot_07", shopId = shopId, date = date, startTime = "19:30", endTime = "20:30", capacity = 1, booked = 0, isBlocked = false)
        )
    }

    private val _slots = MutableStateFlow<List<SlotItem>>(
        createDefaultSlotsForDate("shop_01", DEFAULT_SEEDED_DATE)
    )

    private fun ensureSlotsForDate(shopId: String, date: String) {
        if (date.isBlank()) return
        val hasDate = _slots.value.any { it.shopId == shopId && it.date == date }
        if (!hasDate) {
            _slots.value = _slots.value + createDefaultSlotsForDate(shopId, date)
        }
    }

    override fun getSlots(shopId: String, date: String): Flow<List<SlotItem>> {
        ensureSlotsForDate(shopId, date)
        return _slots.asStateFlow().map { all ->
            all.filter { it.shopId == shopId && (date.isBlank() || it.date == date) }
                .sortedBy { it.startTime }
        }
    }

    override suspend fun saveGeneratedSlots(slots: List<SlotItem>): Result<Unit> = safeCall {
        if (slots.isEmpty()) return@safeCall
        val targetKeys = slots.map { it.shopId to it.date }.toSet()
        _slots.value = (_slots.value.filterNot { (it.shopId to it.date) in targetKeys } + slots)
            .sortedWith(compareBy<SlotItem>({ it.date }, { it.startTime }))
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

    override suspend fun updateSlot(slot: SlotItem): Result<Unit> = safeCall {
        val current = _slots.value.find { it.id == slot.id }
            ?: throw IllegalArgumentException("Khung giờ không tồn tại")
        if (slot.capacity < current.booked) {
            throw IllegalStateException("Không thể giảm sức chứa nhỏ hơn số khách đã đặt (${current.booked})")
        }
        val updated = slot.copy(
            startTime = slot.startTime.trim(),
            endTime = slot.endTime.trim(),
            booked = current.booked
        )
        _slots.value = _slots.value
            .map { if (it.id == slot.id) updated else it }
            .sortedWith(compareBy<SlotItem>({ it.date }, { it.startTime }))
    }
}

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG - Cài đặt BookingRepository
 */
class BookingRepositoryImpl(
    private val syncGateway: BookingSyncGateway? = null,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : BaseRepository(), BookingRepository {

    private val autoApprovalPolicy = MerchantAutoApprovalPolicy()
    private val autoCompletionPolicy = MerchantAutoCompletionPolicy()
    private val cancellationPolicy = MerchantCancellationPolicy()

    private val _bookings = MutableStateFlow<List<Booking>>(
        run {
            val initNow = nowProvider()
            listOf(
                Booking(
                    id = "BK-9021",
                    customerUid = "cust_01",
                    shopId = "shop_01",
                    serviceIds = listOf("srv_01"),
                    serviceNames = listOf("Sơn Gel Hữu Cơ Pháp + Dưỡng Móng"),
                    slotId = "slot_01",
                    startAt = initNow + 3600000L,
                    endAt = initNow + 6300000L,
                    customerName = "Mai Phương",
                    customerPhone = "0905 112 233",
                    note = "Khách thích màu thạch tự nhiên, nhẹ nhàng",
                    status = MerchantConstants.BOOKING_STATUS_PENDING,
                    depositAmount = 100000L,
                    createdAt = initNow - 5 * 60 * 1000L,
                    bookingDate = "2026-10-29"
                ),
                Booking(
                    id = "BK-9022",
                    customerUid = "cust_02",
                    shopId = "shop_01",
                    serviceIds = listOf("srv_02"),
                    serviceNames = listOf("Nối Mi Thiết Kế Sợi Lụa Tự Nhiên"),
                    slotId = "slot_02",
                    startAt = initNow + 2 * 3600000L,
                    endAt = initNow + 3 * 3600000L,
                    customerName = "Chị Thảo My",
                    customerPhone = "0914 556 778",
                    note = "Mắt hơi nhạy cảm với keo cay",
                    status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                    depositAmount = 100000L,
                    createdAt = initNow - 2 * 3600000L,
                    bookingDate = "2026-10-29"
                ),
                Booking(
                    id = "BK-9023",
                    customerUid = "cust_06",
                    shopId = "shop_01",
                    serviceIds = listOf("srv_04"),
                    serviceNames = listOf("Gội Đầu Dưỡng Sinh Thảo Dược"),
                    slotId = "slot_03",
                    startAt = initNow + 20 * 60 * 1000L, // Sắp bắt đầu sau 20 phút (< 30p -> khóa hủy lịch)
                    endAt = initNow + 65 * 60 * 1000L,
                    customerName = "Bạn Ngọc Hân",
                    customerPhone = "0909 445 566",
                    note = "Chuẩn bị đến tiệm",
                    status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                    depositAmount = 50000L,
                    createdAt = initNow - 3600000L,
                    bookingDate = "2026-10-29"
                ),
                Booking(
                    id = "BK-9024",
                    customerUid = "user_cust_01",
                    shopId = "shop_01",
                    serviceIds = listOf("srv_01"),
                    serviceNames = listOf("Sơn Gel Hữu Cơ Pháp"),
                    slotId = "slot_06",
                    startAt = initNow - 50 * 60 * 1000L,
                    endAt = initNow - 5 * 60 * 1000L, // Đã đến giờ hoàn thành (cách đây 5p) -> nút Hoàn thành hiện đỏ Active
                    customerName = "Khánh Linh",
                    customerPhone = "0903 123 456",
                    note = "Làm xong chuẩn bị duyệt hoàn thành",
                    status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                    depositAmount = 100000L,
                    createdAt = initNow - 2 * 3600000L,
                    bookingDate = "2026-10-29"
                ),
                Booking(
                    id = "BK-9018",
                    customerUid = "cust_03",
                    shopId = "shop_01",
                    serviceIds = listOf("srv_03"),
                    serviceNames = listOf("Liệu Trình Chăm Sóc Da Cấp Ẩm Chuyên Sâu"),
                    slotId = "slot_04",
                    startAt = initNow - 7200000L,
                    endAt = initNow - 2700000L, // Kết thúc cách đây 45 phút (> 30p)
                    customerName = "Chị Lan Anh",
                    customerPhone = "0988 223 344",
                    note = "Da thiên dầu vùng chữ T",
                    status = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
                    depositAmount = 150000L,
                    createdAt = initNow - 3 * 3600000L,
                    bookingDate = "2026-10-29"
                ),
                Booking(
                    id = "BK-9010",
                    customerUid = "cust_04",
                    shopId = "shop_01",
                    serviceIds = listOf("srv_01"),
                    serviceNames = listOf("Sơn Gel Hữu Cơ Pháp"),
                    slotId = "slot_04",
                    startAt = initNow - 86400000L,
                    endAt = initNow - 82800000L,
                    customerName = "Bạn Thùy Tiên",
                    customerPhone = "0932 778 899",
                    status = MerchantConstants.BOOKING_STATUS_COMPLETED,
                    completedAt = initNow - 82800000L,
                    depositAmount = 100000L,
                    createdAt = initNow - 90000000L,
                    bookingDate = "2026-10-28"
                )
            )
        }
    )

    override fun getShopBookings(shopId: String): Flow<List<Booking>> = _bookings.asStateFlow()

    override suspend fun updateBookingStatus(
        bookingId: String,
        newStatus: String,
        reason: String?
    ): Result<Unit> = safeCall {
        val now = nowProvider()
        val target = _bookings.value.find { it.id == bookingId }
            ?: throw IllegalStateException("Không tìm thấy đơn hàng #$bookingId")

        if (target.status == MerchantConstants.BOOKING_STATUS_PENDING &&
            newStatus == MerchantConstants.BOOKING_STATUS_CANCELLED &&
            autoApprovalPolicy.shouldAutoApprove(target, now)
        ) {
            _bookings.value = _bookings.value.map { bk ->
                if (bk.id == bookingId) {
                    bk.copy(
                        status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                        autoApproved = true
                    )
                } else bk
            }
            throw IllegalStateException("Đã quá 30 phút kể từ lúc khách đặt dịch vụ. Đơn hàng đã được tự động duyệt!")
        }

        if (target.status == MerchantConstants.BOOKING_STATUS_CONFIRMED &&
            newStatus == MerchantConstants.BOOKING_STATUS_CANCELLED &&
            !cancellationPolicy.canCancel(target, now)
        ) {
            throw IllegalStateException("Đã trong vòng 30 phút trước khi dịch vụ bắt đầu, không thể hủy lịch!")
        }

        if (newStatus == MerchantConstants.BOOKING_STATUS_COMPLETED &&
            !autoCompletionPolicy.canComplete(target, now)
        ) {
            throw IllegalStateException("Chưa đến giờ hoàn thành dịch vụ (${Formatters.formatTime(target.endAt)})!")
        }

        var updatedBooking: Booking? = null
        _bookings.value = _bookings.value.map { bk ->
            if (bk.id == bookingId) {
                bk.copy(
                    status = newStatus,
                    cancelReason = reason ?: bk.cancelReason,
                    cancelledAt = if (newStatus == MerchantConstants.BOOKING_STATUS_CANCELLED) now else bk.cancelledAt,
                    completedAt = if (newStatus == MerchantConstants.BOOKING_STATUS_COMPLETED) now else bk.completedAt
                ).also { updatedBooking = it }
            } else bk
        }
        updatedBooking?.let { syncGateway?.onMerchantBookingUpdated(it) }
    }

    override suspend fun getExpiredInServiceBookings(
        shopId: String,
        thresholdMillis: Long
    ): Result<List<Booking>> = safeCall {
        _bookings.value.filter {
            it.shopId == shopId &&
            (it.status == MerchantConstants.BOOKING_STATUS_CONFIRMED ||
                it.status == MerchantConstants.BOOKING_STATUS_IN_SERVICE) &&
            it.endAt > 0 &&
            it.endAt <= thresholdMillis
        }
    }

    override suspend fun checkAndAutoCompleteBookings(shopId: String): Result<Int> = safeCall {
        val now = nowProvider()
        val newlyCompleted = mutableListOf<Booking>()
        _bookings.value = _bookings.value.map { bk ->
            if (bk.shopId == shopId && autoCompletionPolicy.shouldAutoComplete(bk, now)) {
                bk.copy(
                    status = MerchantConstants.BOOKING_STATUS_COMPLETED,
                    completedAt = now,
                    autoCompleted = true
                ).also { newlyCompleted.add(it) }
            } else bk
        }
        newlyCompleted.forEach { syncGateway?.onMerchantBookingUpdated(it) }
        newlyCompleted.size
    }

    override suspend fun checkAndAutoApproveBookings(shopId: String): Result<Int> = safeCall {
        val now = nowProvider()
        val newlyApproved = mutableListOf<Booking>()
        _bookings.value = _bookings.value.map { bk ->
            if (bk.shopId == shopId && autoApprovalPolicy.shouldAutoApprove(bk, now)) {
                bk.copy(
                    status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                    autoApproved = true
                ).also { newlyApproved.add(it) }
            } else bk
        }
        newlyApproved.forEach { syncGateway?.onMerchantBookingUpdated(it) }
        newlyApproved.size
    }

    fun upsertFromSync(booking: Booking) {
        val current = _bookings.value
        if (current.any { it.id == booking.id }) {
            _bookings.value = current.map { if (it.id == booking.id) booking else it }
        } else {
            _bookings.value = listOf(booking) + current
        }
    }

    fun markReviewedFromSync(bookingId: String) {
        _bookings.value = _bookings.value.map {
            if (it.id == bookingId) it.copy(isReviewed = true) else it
        }
    }

    fun cancelFromSync(bookingId: String, reason: String) {
        val now = nowProvider()
        _bookings.value = _bookings.value.map {
            if (it.id == bookingId && it.status != MerchantConstants.BOOKING_STATUS_COMPLETED) {
                it.copy(
                    status = MerchantConstants.BOOKING_STATUS_CANCELLED,
                    cancelReason = reason,
                    cancelledAt = now
                )
            } else it
        }
    }

    override suspend fun cancelBookingsForShopClosure(
        bookingIds: List<String>,
        reason: String
    ): Result<Int> = safeCall {
        if (bookingIds.isEmpty()) return@safeCall 0
        val idSet = bookingIds.toSet()
        val now = nowProvider()
        val cancellableStatuses = setOf(
            MerchantConstants.BOOKING_STATUS_PENDING,
            MerchantConstants.BOOKING_STATUS_CONFIRMED,
            MerchantConstants.BOOKING_STATUS_IN_SERVICE
        )
        val cancelledList = mutableListOf<Booking>()
        _bookings.value = _bookings.value.map { bk ->
            if (bk.id in idSet && bk.status in cancellableStatuses) {
                bk.copy(
                    status = MerchantConstants.BOOKING_STATUS_CANCELLED,
                    cancelReason = reason,
                    cancelledAt = now
                ).also { cancelledList.add(it) }
            } else bk
        }
        cancelledList.forEach { syncGateway?.onMerchantBookingUpdated(it) }
        cancelledList.size
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

    fun addReviewFromSync(review: Review) {
        if (_reviews.value.none { it.id == review.id }) {
            _reviews.value = listOf(review) + _reviews.value
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
            Transaction(id = "tx_05", type = MerchantConstants.TX_TYPE_DEPOSIT, bookingId = "BK-9023", shopId = "shop_01", customerUid = "cust_06", amount = 50000L, status = MerchantConstants.TX_STATUS_HELD, heldAt = System.currentTimeMillis() - 3600000L),
            Transaction(id = "tx_06", type = MerchantConstants.TX_TYPE_DEPOSIT, bookingId = "BK-9024", shopId = "shop_01", customerUid = "user_cust_01", amount = 100000L, status = MerchantConstants.TX_STATUS_HELD, heldAt = System.currentTimeMillis() - 7200000L),
            Transaction(id = "tx_07", type = MerchantConstants.TX_TYPE_DEPOSIT, bookingId = "BK-9018", shopId = "shop_01", customerUid = "cust_03", amount = 150000L, status = MerchantConstants.TX_STATUS_HELD, heldAt = System.currentTimeMillis() - 10800000L),
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

    override suspend fun releaseDeposit(bookingId: String, amount: Long?): Result<Unit> = safeCall {
        if (_transactions.value.any { it.bookingId == bookingId && it.status == MerchantConstants.TX_STATUS_RELEASED_TO_SHOP }) {
            return@safeCall
        }
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
        } else if ((amount ?: 100000L) > 0L) {
            val newTx = Transaction(
                id = "tx_${UUID.randomUUID().toString().take(6)}",
                type = MerchantConstants.TX_TYPE_RELEASE,
                bookingId = bookingId,
                shopId = "shop_01",
                amount = amount ?: 100000L,
                status = MerchantConstants.TX_STATUS_RELEASED_TO_SHOP,
                releasedAt = now
            )
            _transactions.value = listOf(newTx) + _transactions.value
        }
    }

    override suspend fun refundDeposit(bookingId: String, amount: Long?): Result<Unit> = safeCall {
        if (_transactions.value.any { it.bookingId == bookingId && it.status == MerchantConstants.TX_STATUS_REFUNDED }) {
            return@safeCall
        }
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
        } else if ((amount ?: 100000L) > 0L) {
            val newTx = Transaction(
                id = "tx_${UUID.randomUUID().toString().take(6)}",
                type = MerchantConstants.TX_TYPE_REFUND,
                bookingId = bookingId,
                shopId = "shop_01",
                amount = amount ?: 100000L,
                status = MerchantConstants.TX_STATUS_REFUNDED,
                refundedAt = now
            )
            _transactions.value = listOf(newTx) + _transactions.value
        }
    }

    fun ensureHeldTransaction(bookingId: String, shopId: String, customerUid: String, amount: Long) {
        if (amount <= 0L || _transactions.value.any { it.bookingId == bookingId }) return
        val newTx = Transaction(
            id = "tx_${UUID.randomUUID().toString().take(6)}",
            type = MerchantConstants.TX_TYPE_DEPOSIT,
            bookingId = bookingId,
            shopId = shopId,
            customerUid = customerUid,
            amount = amount,
            status = MerchantConstants.TX_STATUS_HELD,
            heldAt = System.currentTimeMillis()
        )
        _transactions.value = listOf(newTx) + _transactions.value
    }

    fun refundFromSync(bookingId: String) {
        val now = System.currentTimeMillis()
        _transactions.value = _transactions.value.map { tx ->
            if (tx.bookingId == bookingId && tx.status == MerchantConstants.TX_STATUS_HELD) {
                tx.copy(
                    type = MerchantConstants.TX_TYPE_REFUND,
                    status = MerchantConstants.TX_STATUS_REFUNDED,
                    refundedAt = now
                )
            } else tx
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
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl(),
    private val transactionRepository: TransactionRepository? = null
) : BaseRepository(), StatsRepository {

    override fun getShopStats(shopId: String): Flow<MerchantStats> {
        val bookingsFlow = bookingRepository.getShopBookings(shopId)
        return if (transactionRepository != null) {
            combine(bookingsFlow, transactionRepository.getShopTransactions(shopId)) { bookings, transactions ->
                // Doanh thu chỉ tính từ tiền cọc sàn giữ đã giải ngân cho tiệm sau khi hoàn thành dịch vụ
                val releasedDepositRevenue = transactions
                    .filter { it.shopId == shopId && it.status == MerchantConstants.TX_STATUS_RELEASED_TO_SHOP }
                    .sumOf { it.amount }
                buildStats(bookings, releasedDepositRevenue)
            }
        } else {
            bookingsFlow.map { bookings ->
                val depositRevenue = bookings
                    .filter { it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }
                    .sumOf { it.depositAmount }
                buildStats(bookings, depositRevenue)
            }
        }
    }

    private fun buildStats(
        bookings: List<Booking>,
        depositRevenue: Long
    ): MerchantStats {
        val pending = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_PENDING }
        val confirmed = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_CONFIRMED }
        val inService = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_IN_SERVICE }
        val completed = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }
        val cancelled = bookings.count { it.status == MerchantConstants.BOOKING_STATUS_CANCELLED }

        return MerchantStats(
            totalBookingsToday = bookings.size,
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
