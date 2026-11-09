package com.applamdep.customer.ui.booking

import androidx.lifecycle.viewModelScope
import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.policy.CancellationDecision
import com.applamdep.customer.domain.policy.CancellationPolicy
import com.applamdep.customer.domain.repository.BookingRepository
import com.applamdep.customer.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BookingFilterType {
    ALL, UPCOMING, COMPLETED_OR_CANCELLED
}

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (BaseViewModel)
 * 3. TÍNH CHẤT OOP: ĐA HÌNH (CancellationPolicy)
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (BookingRepository, WalletRepository, CancellationPolicy)
 */
class BookingHistoryViewModel(
    private val bookingRepository: BookingRepository,
    private val walletRepository: WalletRepository,
    private val cancellationPolicy: CancellationPolicy
) : BaseViewModel<List<Booking>>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI
    private val _filter = MutableStateFlow(BookingFilterType.ALL)
    val filter: StateFlow<BookingFilterType> = _filter.asStateFlow()

    private var rawBookings: List<Booking> = emptyList()

    fun loadCustomerBookings(customerId: String) {
        launchWithLoading {
            bookingRepository.getCustomerBookings(customerId).collect { list ->
                rawBookings = list
                applyFilterAndEmit()
            }
        }
    }

    fun setFilter(type: BookingFilterType) {
        _filter.value = type
        applyFilterAndEmit()
    }

    private fun applyFilterAndEmit() {
        val filtered = when (_filter.value) {
            BookingFilterType.ALL -> rawBookings
            BookingFilterType.UPCOMING -> rawBookings.filter {
                it.status == AppConstants.BOOKING_STATUS_CONFIRMED || it.status == AppConstants.BOOKING_STATUS_PENDING
            }
            BookingFilterType.COMPLETED_OR_CANCELLED -> rawBookings.filter {
                it.status == AppConstants.BOOKING_STATUS_COMPLETED || it.status == AppConstants.BOOKING_STATUS_CANCELLED
            }
        }
        updateState(UiState.Success(filtered))
    }

    /**
     * 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
     * Thẩm định điều kiện hủy và quyền lợi hoàn tiền cọc qua CancellationPolicy
     */
    fun checkCancellationEligibility(booking: Booking): CancellationDecision {
        return cancellationPolicy.evaluate(booking)
    }

    /**
     * 1. TÍNH CHẤT OOP: ĐÓNG GÓI LOGIC NGHIỆP VỤ HỦY VÀ HOÀN TIỀN
     * - Khách hủy trước ít nhất 2 tiếng: HỦY HỢP LỆ -> HOÀN CỌC VỀ VÍ TRONG ỨNG DỤNG
     * - Dưới 2 tiếng: Bị từ chối hủy hoặc mất cọc
     */
    fun cancelBooking(
        booking: Booking,
        reason: String,
        onSuccess: (message: String) -> Unit,
        onError: (error: String) -> Unit
    ) {
        val decision = cancellationPolicy.evaluate(booking)

        if (!decision.canCancel) {
            onError(decision.message)
            return
        }

        viewModelScope.launch {
            // 1. Cập nhật trạng thái booking sang CANCELLED
            val cancelResult = bookingRepository.cancelBooking(booking.id, reason)
            if (cancelResult.isFailure) {
                onError("Không thể hủy lịch hẹn: ${cancelResult.exceptionOrNull()?.message}")
                return@launch
            }

            // 2. Nếu có cọc và đủ điều kiện hoàn -> Hoàn tiền về ví
            if (decision.isRefundEligible && decision.refundAmount > 0) {
                val refundResult = walletRepository.refundDepositToWallet(
                    userId = booking.customerId,
                    bookingId = booking.id,
                    amount = decision.refundAmount,
                    reason = "Hoàn cọc do hủy lịch trước 2 tiếng: $reason"
                )
                if (refundResult.isSuccess) {
                    onSuccess("Hủy lịch thành công! Số tiền cọc ${decision.refundAmount}đ đã được hoàn 100% về ví trong ứng dụng.")
                } else {
                    onSuccess("Hủy lịch thành công nhưng có lỗi khi hoàn cọc ví. Vui lòng liên hệ hỗ trợ.")
                }
            } else {
                onSuccess("Hủy lịch hẹn thành công.")
            }
        }
    }
}
