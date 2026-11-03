package com.applamdep.customer.domain.repository

import com.applamdep.customer.domain.model.Review
import kotlinx.coroutines.flow.Flow

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Quản lý đọc và gửi đánh giá dịch vụ.
 */
interface ReviewRepository {
    /**
     * Lấy danh sách đánh giá của shop (loại bỏ các đánh giá có status REMOVED)
     */
    fun getShopReviews(shopId: String): Flow<List<Review>>

    /**
     * Lấy danh sách các đánh giá đã gửi của một khách hàng
     */
    fun getCustomerReviews(customerId: String): Flow<List<Review>>

    /**
     * Gửi đánh giá mới cho booking đã hoàn thành
     */
    suspend fun submitReview(review: Review): Result<Unit>

    /**
     * Cửa hàng phản hồi lại đánh giá của khách
     */
    suspend fun replyReview(reviewId: String, reply: String): Result<Unit>

    /**
     * Cửa hàng báo cáo/tố cáo đánh giá vi phạm gửi quản trị viên
     */
    suspend fun reportReview(reviewId: String, reason: String): Result<Unit>
}
