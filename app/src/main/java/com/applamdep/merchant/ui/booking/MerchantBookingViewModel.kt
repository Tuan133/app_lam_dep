package com.applamdep.merchant.ui.booking

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.rule.BookingFilterPolicy
import com.applamdep.merchant.domain.rule.BookingFilterType
import com.applamdep.merchant.domain.rule.BookingStateTransitionRule
import com.applamdep.merchant.domain.rule.MerchantAutoCompletionPolicy
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc BookingRepository & TransactionRepository
 * ViewModel điều phối:
 * - Chức năng 6: Quản lý danh sách lịch hẹn của cửa hàng và chuyển đổi trạng thái dịch vụ
 * - Chức năng 7: Quét và tự động chuyển đơn sang hoàn thành sau 30 phút kể từ lúc kết thúc dịch vụ
 * - Chức năng 9: Tự động giải ngân/hoàn cọc ký quỹ khi đơn hoàn thành/hủy
 */
class MerchantBookingViewModel(
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(),
    private val transactionRepository: TransactionRepository = TransactionRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<List<Booking>>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        bookingRepository = BookingRepositoryImpl(),
        transactionRepository = TransactionRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val transitionRule = BookingStateTransitionRule()
    private val autoCompletionPolicy = MerchantAutoCompletionPolicy()
    private val filterPolicy = BookingFilterPolicy()

    private val _currentFilter = MutableStateFlow(BookingFilterType.ALL)
    val currentFilter: StateFlow<BookingFilterType> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var rawList: List<Booking> = emptyList()

    fun loadBookings(shopId: String = "shop_01") {
        launchWithLoading {
            // Tự động kiểm tra hoàn thành sau 30 phút theo Requirement #7
            bookingRepository.checkAndAutoCompleteBookings(shopId)

            bookingRepository.getShopBookings(shopId).collect { list ->
                rawList = list
                applyFilterAndSearch()
            }
        }
    }

    fun setFilter(filter: BookingFilterType) {
        _currentFilter.value = filter
        applyFilterAndSearch()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilterAndSearch()
    }

    private fun applyFilterAndSearch() {
        val filtered = filterPolicy.filter(
            bookings = rawList,
            filter = _currentFilter.value,
            searchQuery = _searchQuery.value
        )
        updateState(UiState.Success(filtered))
    }

    fun updateStatus(
        bookingId: String,
        newStatus: String,
        currentStatus: String? = null,
        reason: String? = null,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Kiểm tra tính hợp lệ của bước chuyển trạng thái (State pattern)
        if (currentStatus != null) {
            val validation = transitionRule.validateTransition(currentStatus, newStatus)
            if (validation is BookingStateTransitionRule.TransitionResult.Invalid) {
                onError(validation.errorMessage)
                return
            }
        }

        viewModelScope.launch {
            val result = bookingRepository.updateBookingStatus(bookingId, newStatus, reason)
            if (result.isSuccess) {
                // Tự động đồng bộ tài chính ký quỹ (Escrow Requirement #9)
                when (newStatus) {
                    MerchantConstants.BOOKING_STATUS_COMPLETED -> {
                        transactionRepository.releaseDeposit(bookingId)
                    }
                    MerchantConstants.BOOKING_STATUS_CANCELLED -> {
                        transactionRepository.refundDeposit(bookingId)
                    }
                }

                val msg = when (newStatus) {
                    MerchantConstants.BOOKING_STATUS_CONFIRMED -> "Đã xác nhận lịch hẹn thành công!"
                    MerchantConstants.BOOKING_STATUS_IN_SERVICE -> "Đã bắt đầu phục vụ khách hàng."
                    MerchantConstants.BOOKING_STATUS_COMPLETED -> "Đã hoàn thành dịch vụ! Tiền cọc đã được giải ngân về ví cửa hàng."
                    MerchantConstants.BOOKING_STATUS_CANCELLED -> "Đã từ chối / hủy lịch hẹn."
                    else -> "Cập nhật trạng thái thành công."
                }
                onSuccess(msg)
            } else {
                onError("Lỗi cập nhật: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Kích hoạt kiểm tra tự động hoàn thành dịch vụ (sau 30 phút từ lúc kết thúc)
     */
    fun triggerAutoComplete(shopId: String = "shop_01", onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val res = bookingRepository.checkAndAutoCompleteBookings(shopId)
            val count = res.getOrDefault(0)
            if (count > 0) {
                // Tự động giải ngân cho các đơn vừa hoàn tất
                rawList.filter { it.shopId == shopId && it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }
                    .forEach { transactionRepository.releaseDeposit(it.id) }
            }
            onResult(count)
        }
    }
}
