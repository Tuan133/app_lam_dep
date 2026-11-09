package com.applamdep.customer.ui.review

import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.domain.model.Review
import com.applamdep.customer.domain.repository.ReviewRepository

/**
 * ViewModel quản lý danh sách đánh giá đã đăng của khách hàng
 */
class CustomerReviewsViewModel(
    private val reviewRepository: ReviewRepository
) : BaseViewModel<List<Review>>() {

    fun loadReviews(customerId: String = "user_cust_01") {
        launchWithLoading {
            reviewRepository.getCustomerReviews(customerId).collect { list ->
                updateState(UiState.Success(list))
            }
        }
    }
}
