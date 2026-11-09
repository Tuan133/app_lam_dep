package com.applamdep.merchant.domain.repository

import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.model.Review
import com.applamdep.customer.domain.model.ServiceItem
import com.applamdep.customer.domain.model.ShopSlot
import com.applamdep.merchant.domain.model.MerchantFinancialSummary
import com.applamdep.merchant.domain.model.MerchantProfile
import com.applamdep.merchant.domain.model.MerchantSchedule
import com.applamdep.merchant.domain.model.MerchantStats
import kotlinx.coroutines.flow.Flow

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Interface MerchantRepository quản lý toàn bộ các phân hệ của Merchant Portal:
 * 1. Đăng ký cơ sở
 * 2. Quản lý hồ sơ
 * 3. Quản lý dịch vụ
 * 4. Quản lý lịch làm việc
 * 5. Quản lý khung giờ trống
 * 6. Quản lý booking
 * 7. Quản lý trạng thái & Tự động hoàn thành
 * 8. Phản hồi và tố cáo đánh giá
 * 9. Theo dõi tiền cọc giải ngân
 * 10. Thống kê kinh doanh
 */
interface MerchantRepository {
    // 1 & 2: Hồ sơ cơ sở & Đăng ký
    fun getMerchantProfile(ownerId: String): Flow<MerchantProfile?>
    suspend fun registerShop(profile: MerchantProfile): Result<String>
    suspend fun updateProfile(profile: MerchantProfile): Result<Unit>

    // 3: Quản lý dịch vụ (CRUD)
    fun getServices(shopId: String): Flow<List<ServiceItem>>
    suspend fun addService(service: ServiceItem): Result<String>
    suspend fun updateService(service: ServiceItem): Result<Unit>
    suspend fun deleteService(serviceId: String): Result<Unit>

    // 4 & 5: Quản lý ca làm việc và khung giờ (Slots)
    fun getSchedule(shopId: String): Flow<MerchantSchedule>
    suspend fun updateSchedule(schedule: MerchantSchedule): Result<Unit>
    fun getSlots(shopId: String, date: String): Flow<List<ShopSlot>>
    suspend fun updateSlot(slot: ShopSlot): Result<Unit>
    suspend fun toggleSlotAvailability(slotId: String, isAvailable: Boolean): Result<Unit>

    // 6 & 7: Quản lý lịch hẹn & Tự động hoàn thành
    fun getMerchantBookings(shopId: String): Flow<List<Booking>>
    suspend fun updateBookingStatus(bookingId: String, newStatus: String): Result<Unit>
    suspend fun checkAndAutoCompleteBookings(shopId: String): Result<Int>

    // 8: Đánh giá & Phản hồi
    fun getMerchantReviews(shopId: String): Flow<List<Review>>
    suspend fun replyReview(reviewId: String, reply: String): Result<Unit>
    suspend fun reportReview(reviewId: String, reason: String): Result<Unit>

    // 9 & 10: Theo dõi cọc, doanh thu và thống kê
    fun getFinancialSummary(shopId: String): Flow<MerchantFinancialSummary>
    fun getMerchantStats(shopId: String): Flow<MerchantStats>
}
