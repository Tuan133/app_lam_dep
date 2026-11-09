package com.applamdep.merchant.ui.review

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.data.repository.ReviewRepositoryImpl
import com.applamdep.merchant.domain.model.Review
import com.applamdep.merchant.domain.repository.ReviewRepository
import com.applamdep.merchant.domain.rule.ReviewReplyValidator
import com.applamdep.merchant.domain.rule.ReviewReportValidator
import kotlinx.coroutines.launch

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc ReviewRepository
 * ViewModel quản lý phản hồi và tố cáo đánh giá khách hàng (Merchant Requirement #8)
 */
class MerchantReviewViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<List<Review>>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        reviewRepository = ReviewRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val replyValidator = ReviewReplyValidator()
    private val reportValidator = ReviewReportValidator()

    fun loadReviews(shopId: String = "shop_01") {
        launchWithLoading {
            reviewRepository.getShopReviews(shopId).collect { list ->
                updateState(UiState.Success(list))
            }
        }
    }

    fun replyReview(
        reviewId: String,
        replyText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = replyValidator.validate(replyText)
        if (validation is ReviewReplyValidator.ReplyValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        viewModelScope.launch {
            val result = reviewRepository.replyReview(reviewId, replyText.trim())
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Lỗi khi gửi phản hồi: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun reportReview(
        reviewId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = reportValidator.validate(reason)
        if (validation is ReviewReportValidator.ReportValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        viewModelScope.launch {
            val result = reviewRepository.reportReview(reviewId, reason.trim())
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Lỗi khi gửi yêu cầu khiếu nại: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun requestRemoveReview(
        reviewId: String,
        reason: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = reportValidator.validate(reason)
        if (validation is ReviewReportValidator.ReportValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        viewModelScope.launch {
            val result = reviewRepository.requestRemoveReview(reviewId, reason.trim())
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Lỗi khi gửi yêu cầu gỡ đánh giá: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }
}
