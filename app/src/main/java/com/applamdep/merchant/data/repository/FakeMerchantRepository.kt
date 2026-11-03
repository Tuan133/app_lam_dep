package com.applamdep.merchant.data.repository

import com.applamdep.customer.core.base.BaseRepository
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.model.Review
import com.applamdep.customer.domain.model.ServiceItem
import com.applamdep.customer.domain.model.ShopSlot
import com.applamdep.customer.domain.repository.BookingRepository
import com.applamdep.customer.domain.repository.ReviewRepository
import com.applamdep.merchant.domain.model.*
import com.applamdep.merchant.domain.policy.AutoCompletionPolicy
import com.applamdep.merchant.domain.repository.MerchantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) từ BaseRepository
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) cài đặt MerchantRepository
 * Quản lý dữ liệu giả lập cho Cổng Đối tác (Merchant Portal).
 */
class FakeMerchantRepository(
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository,
    private val autoCompletionPolicy: AutoCompletionPolicy
) : BaseRepository(), MerchantRepository {

    // 1. Hồ sơ tiệm đối tác mẫu (1994 Nailsroom - salon_01)
    private val profileFlow = MutableStateFlow<MerchantProfile?>(
        MerchantProfile(
            id = "salon_01",
            ownerId = "owner_linh_01",
            shopName = "1994 Nailsroom",
            ownerName = "Master Khánh Linh",
            phone = "0762792671",
            email = "contact@1994nailsroom.vn",
            address = "172/5 Trần Phú, Hải Châu, Đà Nẵng",
            description = "Tổ hợp chăm sóc móng, nối mi thiết kế và dịch vụ làm đẹp hàng đầu Đà Nẵng. Không gian sang trọng, dịch vụ chuẩn mực và thiết bị tiệt trùng y khoa.",
            openingHours = "9h - 20h",
            status = "APPROVED",
            membershipPlan = "VIP",
            requireDeposit = true,
            depositOptions = listOf(50000L, 100000L, 150000L, 200000L, 300000L),
            amenities = listOf("Wifi 6 tốc độ cao", "Phòng VIP riêng tư", "Trà hoa thảo mộc", "Chỗ đậu xe có bảo vệ", "Nước uống miễn phí")
        )
    )

    // 2. Danh mục dịch vụ của tiệm
    private val servicesFlow = MutableStateFlow<List<ServiceItem>>(
        listOf(
            ServiceItem(
                id = "srv_01",
                shopId = "shop_lamour",
                name = "Sơn Gel Hữu Cơ Pháp + Dưỡng Móng",
                description = "Sơn dưỡng phục hồi, không hại móng tự nhiên, độ bền trên 4 tuần.",
                price = 180000L,
                durationMinutes = 45,
                category = "Nail Art & Care",
                imageUrl = "https://images.unsplash.com/photo-1604654894610-df63bc536371?w=500&auto=format&fit=crop"
            ),
            ServiceItem(
                id = "srv_02",
                shopId = "shop_lamour",
                name = "Nối Mi Thiết Kế Katun / Tự Nhiên",
                description = "Sợi mi lụa siêu nhẹ nhập khẩu Hàn Quốc, không cộm, không cay mắt.",
                price = 320000L,
                durationMinutes = 60,
                category = "Lash & Brow",
                imageUrl = "https://images.unsplash.com/photo-1519014816548-bf5fe059798b?w=500&auto=format&fit=crop"
            ),
            ServiceItem(
                id = "srv_03",
                shopId = "shop_lamour",
                name = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                description = "Bấm huyệt đả thông kinh lạc, nước gội thảo mộc nấu tươi trong ngày.",
                price = 250000L,
                durationMinutes = 60,
                category = "Spa & Massage",
                imageUrl = "https://images.unsplash.com/photo-1560750588-73207b1ef5b8?w=500&auto=format&fit=crop"
            ),
            ServiceItem(
                id = "srv_04",
                shopId = "shop_lamour",
                name = "Massage Cổ Vai Gáy Đá Nóng",
                description = "Giải tỏa căng thẳng văn phòng, lưu thông khí huyết với tinh dầu gừng.",
                price = 350000L,
                durationMinutes = 60,
                category = "Spa & Massage",
                imageUrl = "https://images.unsplash.com/photo-1544161515-4ab6ce6db874?w=500&auto=format&fit=crop"
            )
        )
    )

    // 3. Lịch làm việc & Ca làm
    private val scheduleFlow = MutableStateFlow(
        MerchantSchedule(
            shopId = "shop_lamour",
            workingDays = listOf("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"),
            openTime = "09:00",
            closeTime = "21:00",
            breakStartTime = "12:30",
            breakEndTime = "13:30",
            defaultSlotCapacity = 3
        )
    )

    // 4. Khung giờ slots
    private val slotsFlow = MutableStateFlow<List<ShopSlot>>(
        listOf(
            ShopSlot("slot_01", "shop_lamour", "2026-10-23", "09:00", maxCapacity = 3, bookedCount = 1, isAvailable = true),
            ShopSlot("slot_02", "shop_lamour", "2026-10-23", "10:30", maxCapacity = 3, bookedCount = 2, isAvailable = true),
            ShopSlot("slot_03", "shop_lamour", "2026-10-23", "13:30", maxCapacity = 3, bookedCount = 0, isAvailable = true),
            ShopSlot("slot_04", "shop_lamour", "2026-10-23", "15:00", maxCapacity = 3, bookedCount = 3, isAvailable = false),
            ShopSlot("slot_05", "shop_lamour", "2026-10-23", "16:30", maxCapacity = 3, bookedCount = 1, isAvailable = true),
            ShopSlot("slot_06", "shop_lamour", "2026-10-23", "18:00", maxCapacity = 3, bookedCount = 2, isAvailable = true),
            ShopSlot("slot_07", "shop_lamour", "2026-10-23", "19:30", maxCapacity = 3, bookedCount = 1, isAvailable = true)
        )
    )

    // 5. Thống kê & Tài chính
    private val statsFlow = MutableStateFlow(MerchantStats())
    private val financeFlow = MutableStateFlow(
        MerchantFinancialSummary(
            heldEscrowBalance = 150000L,
            releasedTotalBalance = 3850000L,
            refundedTotalBalance = 100000L,
            bookingTransactions = listOf(
                MerchantEscrowItem("ESC-01", "BK-8921", "Khánh Linh", "Sơn Gel Hữu Cơ Pháp", 50000L, "HELD", "Hôm nay 15:30"),
                MerchantEscrowItem("ESC-02", "BK-9012", "Phương Thảo", "Sơn Gel Hữu Cơ Pháp", 50000L, "HELD", "Hôm nay 10:30"),
                MerchantEscrowItem("ESC-03", "BK-8840", "Khánh Linh", "Gội Đầu Dưỡng Sinh", 50000L, "RELEASED_TO_SHOP", "Hôm qua 14:00"),
                MerchantEscrowItem("ESC-04", "BK-8700", "Ngọc Hân", "Nối Mi Katun", 100000L, "RELEASED_TO_SHOP", "21/10/2026"),
                MerchantEscrowItem("ESC-05", "BK-8650", "Hải Yến", "Massage Cổ Vai Gáy", 50000L, "REFUNDED", "20/10/2026 (Khách hủy trước 3h)")
            )
        )
    )

    override fun getMerchantProfile(ownerId: String): Flow<MerchantProfile?> = profileFlow

    override suspend fun registerShop(profile: MerchantProfile): Result<String> = safeCall {
        val newShopId = "shop_" + UUID.randomUUID().toString().take(6)
        val submitted = profile.copy(id = newShopId, status = "PENDING")
        profileFlow.value = submitted
        newShopId
    }

    override suspend fun updateProfile(profile: MerchantProfile): Result<Unit> = safeCall {
        profileFlow.value = profile
    }

    override fun getServices(shopId: String): Flow<List<ServiceItem>> {
        val targetId = when (shopId) {
            "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> shopId
        }
        return servicesFlow.map { list ->
            list.filter { it.shopId == targetId || it.shopId == shopId || (targetId == "salon_01" && it.shopId == "shop_lamour") }
        }
    }

    override suspend fun addService(service: ServiceItem): Result<String> = safeCall {
        val newId = "srv_" + UUID.randomUUID().toString().take(6)
        val created = service.copy(id = newId)
        val current = servicesFlow.value.toMutableList()
        current.add(0, created)
        servicesFlow.value = current
        newId
    }

    override suspend fun updateService(service: ServiceItem): Result<Unit> = safeCall {
        val updated = servicesFlow.value.map { if (it.id == service.id) service else it }
        servicesFlow.value = updated
    }

    override suspend fun deleteService(serviceId: String): Result<Unit> = safeCall {
        val filtered = servicesFlow.value.filter { it.id != serviceId }
        servicesFlow.value = filtered
    }

    override fun getSchedule(shopId: String): Flow<MerchantSchedule> = scheduleFlow

    override suspend fun updateSchedule(schedule: MerchantSchedule): Result<Unit> = safeCall {
        scheduleFlow.value = schedule
    }

    override fun getSlots(shopId: String, date: String): Flow<List<ShopSlot>> {
        val targetId = when (shopId) {
            "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> shopId
        }
        return slotsFlow.map { list ->
            list.filter { it.shopId == targetId || it.shopId == shopId || (targetId == "salon_01" && it.shopId == "shop_lamour") }
        }
    }

    override suspend fun updateSlot(slot: ShopSlot): Result<Unit> = safeCall {
        val updated = slotsFlow.value.map { if (it.id == slot.id) slot else it }
        slotsFlow.value = updated
    }

    override suspend fun toggleSlotAvailability(slotId: String, isAvailable: Boolean): Result<Unit> = safeCall {
        val updated = slotsFlow.value.map { 
            if (it.id == slotId) it.copy(isAvailable = isAvailable) else it 
        }
        slotsFlow.value = updated
    }

    override fun getMerchantBookings(shopId: String): Flow<List<Booking>> {
        return bookingRepository.getShopBookings(shopId)
    }

    override suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit> = safeCall {
        bookingRepository.updateBookingStatus(bookingId, newStatus)

        // Nếu chuyển sang COMPLETED, giải ngân tiền cọc cho shop
        if (newStatus == AppConstants.BOOKING_STATUS_COMPLETED) {
            val currentFinance = financeFlow.value
            val updatedTxs = currentFinance.bookingTransactions.map { tx ->
                if (tx.bookingId == bookingId && tx.status == "HELD") {
                    tx.copy(status = "RELEASED_TO_SHOP")
                } else tx
            }
            val heldDelta = 50000L
            financeFlow.value = currentFinance.copy(
                heldEscrowBalance = (currentFinance.heldEscrowBalance - heldDelta).coerceAtLeast(0L),
                releasedTotalBalance = currentFinance.releasedTotalBalance + heldDelta,
                bookingTransactions = updatedTxs
            )
        }
    }

    /**
     * 3. TÍNH CHẤT OOP: ĐA HÌNH
     * Tự động hoàn thành các lịch hẹn đã kết thúc quá 30 phút theo chính sách AutoCompletionPolicy
     */
    override suspend fun checkAndAutoCompleteBookings(shopId: String): Result<Int> = safeCall {
        var completedCount = 0
        val currentBookings = bookingRepository.getShopBookings(shopId).first()

        val eligibleBookings = autoCompletionPolicy.findEligibleBookings(currentBookings)
        for (booking in eligibleBookings) {
            updateBookingStatus(booking.id, AppConstants.BOOKING_STATUS_COMPLETED)
            completedCount++
        }
        completedCount
    }

    override fun getMerchantReviews(shopId: String): Flow<List<Review>> {
        return reviewRepository.getShopReviews(shopId)
    }

    override suspend fun replyReview(reviewId: String, reply: String): Result<Unit> {
        return reviewRepository.replyReview(reviewId, reply)
    }

    override suspend fun reportReview(reviewId: String, reason: String): Result<Unit> {
        return reviewRepository.reportReview(reviewId, reason)
    }

    override fun getFinancialSummary(shopId: String): Flow<MerchantFinancialSummary> = financeFlow

    override fun getMerchantStats(shopId: String): Flow<MerchantStats> = statsFlow
}
