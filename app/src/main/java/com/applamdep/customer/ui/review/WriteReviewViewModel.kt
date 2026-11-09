package com.applamdep.customer.ui.review

import androidx.lifecycle.viewModelScope
import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.model.Review
import com.applamdep.customer.domain.repository.BookingRepository
import com.applamdep.customer.domain.repository.ReviewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) từ BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) qua ReviewRepository và BookingRepository
 * Quản lý trạng thái viết đánh giá của khách hàng (Nhóm chức năng 5 - P0)
 */
class WriteReviewViewModel(
    private val reviewRepository: ReviewRepository,
    private val bookingRepository: BookingRepository
) : BaseViewModel<Unit>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI (Encapsulation)
    private val _targetBooking = MutableStateFlow<Booking?>(null)
    val targetBooking: StateFlow<Booking?> = _targetBooking.asStateFlow()

    private val _rating = MutableStateFlow(5)
    val rating: StateFlow<Int> = _rating.asStateFlow()

    private val _comment = MutableStateFlow("")
    val comment: StateFlow<String> = _comment.asStateFlow()

    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    private val _selectedImages = MutableStateFlow<List<String>>(emptyList())
    val selectedImages: StateFlow<List<String>> = _selectedImages.asStateFlow()

    fun loadBooking(bookingId: String) {
        viewModelScope.launch {
            val result = bookingRepository.getBookingById(bookingId)
            result.onSuccess { b ->
                _targetBooking.value = b
            }.onFailure {
                updateState(UiState.Error("Không tìm thấy thông tin lịch hẹn: ${it.message}"))
            }
        }
    }

    fun setRating(stars: Int) {
        _rating.value = stars.coerceIn(1, 5)
    }

    fun setComment(text: String) {
        _comment.value = text
    }

    fun toggleTag(tag: String) {
        val current = _selectedTags.value.toMutableSet()
        if (current.contains(tag)) {
            current.remove(tag)
        } else {
            current.add(tag)
        }
        _selectedTags.value = current
    }

    fun addMockImage(imageUrl: String) {
        val current = _selectedImages.value.toMutableList()
        current.add(imageUrl)
        _selectedImages.value = current
    }

    fun removeImage(index: Int) {
        val current = _selectedImages.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _selectedImages.value = current
        }
    }

    fun submitReview(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val booking = _targetBooking.value
        if (booking == null) {
            onError("Chưa tải được thông tin lịch hẹn.")
            return
        }

        if (_comment.value.trim().length < 5) {
            onError("Vui lòng viết nhận xét tối thiểu 5 ký tự để giúp salon cải thiện dịch vụ.")
            return
        }

        viewModelScope.launch {
            updateState(UiState.Loading)
            val reviewId = "REV-" + UUID.randomUUID().toString().take(8).uppercase()
            val review = Review(
                id = reviewId,
                bookingId = booking.id,
                shopId = booking.shopId,
                customerId = booking.customerId,
                customerName = booking.customerName,
                rating = _rating.value,
                comment = _comment.value.trim(),
                tags = _selectedTags.value.toList(),
                images = _selectedImages.value,
                status = "ACTIVE"
            )

            val submitResult = reviewRepository.submitReview(review)
            if (submitResult.isSuccess) {
                // Đánh dấu booking đã được review
                bookingRepository.markBookingReviewed(booking.id)
                updateState(UiState.Success(Unit))
                onSuccess()
            } else {
                val err = submitResult.exceptionOrNull()?.message ?: "Có lỗi xảy ra khi gửi đánh giá"
                updateState(UiState.Error(err))
                onError(err)
            }
        }
    }
}
