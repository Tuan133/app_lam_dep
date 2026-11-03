package com.applamdep.customer.domain.repository

import com.applamdep.customer.domain.model.Booking
import kotlinx.coroutines.flow.Flow

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Interface BookingRepository quản lý toàn bộ thao tác đặt lịch, lịch sử và hủy lịch.
 */
interface BookingRepository {
    /**
     * Tạo đặt lịch mới (sử dụng Firestore transaction để chống đặt trùng)
     */
    suspend fun createBooking(booking: Booking): Result<String>

    /**
     * Lắng nghe danh sách lịch hẹn của khách theo thời gian thực
     */
    fun getCustomerBookings(customerId: String): Flow<List<Booking>>

    /**
     * Lắng nghe danh sách lịch hẹn của cửa hàng theo thời gian thực (Merchant Portal)
     */
    fun getShopBookings(shopId: String): Flow<List<Booking>>

    /**
     * Lấy chi tiết một lịch hẹn
     */
    suspend fun getBookingById(bookingId: String): Result<Booking>

    /**
     * Cập nhật trạng thái lịch hẹn (PENDING -> CONFIRMED -> IN_SERVICE -> COMPLETED)
     */
    suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit>

    /**
     * Đánh dấu lịch hẹn đã được khách hàng gửi đánh giá
     */
    suspend fun markBookingReviewed(bookingId: String): Result<Unit>

    /**
     * Hủy lịch hẹn kèm lý do
     */
    suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit>
}
