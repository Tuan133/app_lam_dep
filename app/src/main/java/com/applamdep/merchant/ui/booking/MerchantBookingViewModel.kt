package com.applamdep.merchant.ui.booking

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.rule.BookingFilterPolicy
import com.applamdep.merchant.domain.rule.BookingFilterType
import com.applamdep.merchant.domain.rule.BookingStateTransitionRule
import com.applamdep.merchant.domain.rule.MerchantAutoApprovalPolicy
import com.applamdep.merchant.domain.rule.MerchantAutoCompletionPolicy
import com.applamdep.merchant.domain.rule.MerchantCancellationPolicy
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null,
    private val nowProvider: () -> Long = { System.currentTimeMillis() }
) : BaseViewModel<List<Booking>>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        bookingRepository = BookingRepositoryImpl(),
        transactionRepository = TransactionRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val transitionRule = BookingStateTransitionRule()
    private val autoApprovalPolicy = MerchantAutoApprovalPolicy()
    private val cancellationPolicy = MerchantCancellationPolicy()
    private val autoCompletionPolicy = MerchantAutoCompletionPolicy()
    private val filterPolicy = BookingFilterPolicy()

    private val _currentFilter = MutableStateFlow(BookingFilterType.ALL)
    val currentFilter: StateFlow<BookingFilterType> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _completedDepositRevenue = MutableStateFlow(0L)
    val completedDepositRevenue: StateFlow<Long> = _completedDepositRevenue.asStateFlow()

    private var rawList: List<Booking> = emptyList()

    fun loadBookings(shopId: String = "shop_01") {
        launchWithLoading {
            // Tự động duyệt đơn chờ quá 30 phút từ lúc đặt và tự động hoàn thành dịch vụ quá 30 phút từ lúc kết thúc
            bookingRepository.checkAndAutoApproveBookings(shopId)
            val autoCompleted = bookingRepository.checkAndAutoCompleteBookings(shopId).getOrDefault(0)
            if (autoCompleted > 0) {
                releaseDepositsForCompletedBookings(shopId)
            }

            bookingRepository.getShopBookings(shopId).collect { list ->
                rawList = list
                _completedDepositRevenue.value = list
                    .filter { it.shopId == shopId && it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }
                    .sumOf { it.depositAmount }
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

        val now = nowProvider()
        val existingBooking = rawList.find { it.id == bookingId }
        if (currentStatus == MerchantConstants.BOOKING_STATUS_PENDING &&
            newStatus == MerchantConstants.BOOKING_STATUS_CANCELLED &&
            existingBooking != null &&
            autoApprovalPolicy.shouldAutoApprove(existingBooking, now)
        ) {
            viewModelScope.launch {
                bookingRepository.checkAndAutoApproveBookings(existingBooking.shopId)
                onError("Đã quá 30 phút kể từ lúc khách đặt dịch vụ. Đơn hàng đã được tự động duyệt!")
            }
            return
        }

        if (currentStatus == MerchantConstants.BOOKING_STATUS_CONFIRMED &&
            newStatus == MerchantConstants.BOOKING_STATUS_CANCELLED &&
            existingBooking != null &&
            !cancellationPolicy.canCancel(existingBooking, now)
        ) {
            onError("Đã trong vòng 30 phút trước khi dịch vụ bắt đầu, không thể hủy lịch!")
            return
        }

        if (newStatus == MerchantConstants.BOOKING_STATUS_COMPLETED &&
            existingBooking != null &&
            !autoCompletionPolicy.canComplete(existingBooking, now)
        ) {
            onError("Chưa đến giờ hoàn thành dịch vụ (${Formatters.formatTime(existingBooking.endAt)})!")
            return
        }

        viewModelScope.launch {
            val result = bookingRepository.updateBookingStatus(bookingId, newStatus, reason)
            if (result.isSuccess) {
                // Tự động đồng bộ tài chính ký quỹ (Escrow Requirement #9)
                when (newStatus) {
                    MerchantConstants.BOOKING_STATUS_COMPLETED -> {
                        transactionRepository.releaseDeposit(bookingId, existingBooking?.depositAmount)
                    }
                    MerchantConstants.BOOKING_STATUS_CANCELLED -> {
                        transactionRepository.refundDeposit(bookingId, existingBooking?.depositAmount)
                    }
                }

                val depositAmt = existingBooking?.depositAmount ?: 0L
                val msg = when (newStatus) {
                    MerchantConstants.BOOKING_STATUS_CONFIRMED -> "Đã xác nhận lịch hẹn thành công!"
                    MerchantConstants.BOOKING_STATUS_IN_SERVICE -> "Đã bắt đầu phục vụ khách hàng."
                    MerchantConstants.BOOKING_STATUS_COMPLETED ->
                        if (depositAmt > 0L) {
                            "Hoàn thành! Đã cộng ${Formatters.formatCurrency(depositAmt)} tiền cọc vào doanh thu shop. Khách đã có thể đánh giá."
                        } else {
                            "Đã hoàn thành dịch vụ! Khách hàng đã có thể đánh giá."
                        }
                    MerchantConstants.BOOKING_STATUS_CANCELLED ->
                        if (currentStatus == MerchantConstants.BOOKING_STATUS_PENDING) "Đã từ chối đơn hàng." else "Đã hủy lịch hẹn."
                    else -> "Cập nhật trạng thái thành công."
                }
                onSuccess(msg)
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Lỗi cập nhật trạng thái")
            }
        }
    }

    /**
     * Kích hoạt kiểm tra tự động duyệt các đơn PENDING đã quá 30 phút kể từ lúc khách đặt
     */
    fun triggerAutoApprove(shopId: String = "shop_01", onResult: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val res = bookingRepository.checkAndAutoApproveBookings(shopId)
            onResult(res.getOrDefault(0))
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
                releaseDepositsForCompletedBookings(shopId)
            }
            onResult(count)
        }
    }

    /**
     * Chạy cả hai chính sách tự động: tự động duyệt (30 phút chờ duyệt) & tự động hoàn thành (30 phút sau kết thúc)
     */
    fun triggerAutoPolicies(shopId: String = "shop_01", onResult: (approvedCount: Int, completedCount: Int) -> Unit) {
        viewModelScope.launch {
            val approvedCount = bookingRepository.checkAndAutoApproveBookings(shopId).getOrDefault(0)
            val completedCount = bookingRepository.checkAndAutoCompleteBookings(shopId).getOrDefault(0)
            if (completedCount > 0) {
                releaseDepositsForCompletedBookings(shopId)
            }
            onResult(approvedCount, completedCount)
        }
    }

    private suspend fun releaseDepositsForCompletedBookings(shopId: String) {
        val currentList = bookingRepository.getShopBookings(shopId).firstOrNull() ?: rawList
        currentList
            .filter { it.shopId == shopId && it.status == MerchantConstants.BOOKING_STATUS_COMPLETED }
            .forEach { transactionRepository.releaseDeposit(it.id, it.depositAmount) }
    }
}
