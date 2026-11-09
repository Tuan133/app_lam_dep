package com.applamdep.merchant.domain.repository

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
import kotlinx.coroutines.flow.Flow

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * 9 Repository Interfaces đóng vai trò ranh giới trừu tượng độc lập:
 * ViewModel CHỈ phụ thuộc vào các interface này, không truy cập trực tiếp Firebase hay Retrofit.
 */

// 1. Quản lý Hồ sơ & Đăng ký cửa hàng
interface ShopRepository {
    fun getShopProfile(ownerUid: String): Flow<ShopProfile?>
    suspend fun registerShop(profile: ShopProfile): Result<String>
    suspend fun updateProfile(profile: ShopProfile): Result<Unit>
}

// 2. Quản lý Dịch vụ (CRUD)
interface ServiceRepository {
    fun getServices(shopId: String): Flow<List<ServiceItem>>
    suspend fun addService(service: ServiceItem): Result<String>
    suspend fun updateService(service: ServiceItem): Result<Unit>
    suspend fun deleteOrHideService(serviceId: String, hasActiveBookings: Boolean): Result<Unit>
}

// 3. Quản lý Lịch làm việc
interface ScheduleRepository {
    fun getSchedule(shopId: String): Flow<WorkSchedule>
    suspend fun updateSchedule(schedule: WorkSchedule): Result<Unit>
}

// 4. Quản lý Khung giờ trống (Slots)
interface SlotRepository {
    fun getSlots(shopId: String, date: String): Flow<List<SlotItem>>
    suspend fun saveGeneratedSlots(slots: List<SlotItem>): Result<Unit>
    suspend fun updateSlotCapacity(slotId: String, newCapacity: Int): Result<Unit>
    suspend fun toggleSlotBlock(slotId: String, isBlocked: Boolean): Result<Unit>
}

// 5. Quản lý Booking & Trạng thái dịch vụ
interface BookingRepository {
    fun getShopBookings(shopId: String): Flow<List<Booking>>
    suspend fun updateBookingStatus(bookingId: String, newStatus: String, reason: String? = null): Result<Unit>
    suspend fun getExpiredInServiceBookings(shopId: String, thresholdMillis: Long): Result<List<Booking>>
    suspend fun checkAndAutoCompleteBookings(shopId: String): Result<Int>
}

// 6. Quản lý Đánh giá & Phản hồi
interface ReviewRepository {
    fun getShopReviews(shopId: String): Flow<List<Review>>
    suspend fun replyReview(reviewId: String, reply: String): Result<Unit>
    suspend fun reportReview(reviewId: String, reason: String): Result<Unit>
    suspend fun requestRemoveReview(reviewId: String, reason: String): Result<Unit>
}

// 7. Theo dõi Tiền cọc Ký quỹ (Escrow Transaction)
interface TransactionRepository {
    fun getShopTransactions(shopId: String): Flow<List<Transaction>>
    fun getFinancialSummary(shopId: String): Flow<FinancialSummary>
    suspend fun releaseDeposit(bookingId: String): Result<Unit>
    suspend fun refundDeposit(bookingId: String): Result<Unit>
}

// 8. Quản lý Gói đối tác & Cấu hình phí
interface PlanRepository {
    fun getSystemConfig(): Flow<SystemConfig>
    suspend fun initiatePlanPayment(shopId: String, plan: String, termMonths: Int): Result<String>
    suspend fun verifyPlanPayment(transactionId: String): Result<Boolean>
}

// 9. Thống kê kinh doanh cơ bản
interface StatsRepository {
    fun getShopStats(shopId: String): Flow<MerchantStats>
}
