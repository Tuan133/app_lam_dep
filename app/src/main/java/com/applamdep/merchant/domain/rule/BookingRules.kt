package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Booking

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI & ĐA HÌNH (ENCAPSULATION & STATE PATTERN)
 * Quy tắc chuyển đổi trạng thái của đơn đặt lịch (Merchant Requirement #6):
 * - PENDING -> CONFIRMED (Xác nhận) hoặc CANCELLED (Từ chối)
 * - CONFIRMED -> IN_SERVICE (Bắt đầu phục vụ) hoặc CANCELLED (Hủy lịch)
 * - IN_SERVICE -> COMPLETED (Hoàn thành dịch vụ)
 * - COMPLETED, CANCELLED: Trạng thái kết thúc (Terminal states), không thể chuyển tiếp.
 */
class BookingStateTransitionRule {

    sealed interface TransitionResult {
        data object Valid : TransitionResult
        data class Invalid(val errorMessage: String) : TransitionResult
    }

    private val allowedTransitions: Map<String, List<String>> = mapOf(
        MerchantConstants.BOOKING_STATUS_PENDING to listOf(
            MerchantConstants.BOOKING_STATUS_CONFIRMED,
            MerchantConstants.BOOKING_STATUS_CANCELLED
        ),
        MerchantConstants.BOOKING_STATUS_CONFIRMED to listOf(
            MerchantConstants.BOOKING_STATUS_IN_SERVICE,
            MerchantConstants.BOOKING_STATUS_COMPLETED,
            MerchantConstants.BOOKING_STATUS_CANCELLED
        ),
        MerchantConstants.BOOKING_STATUS_IN_SERVICE to listOf(
            MerchantConstants.BOOKING_STATUS_COMPLETED
        ),
        MerchantConstants.BOOKING_STATUS_COMPLETED to emptyList(),
        MerchantConstants.BOOKING_STATUS_CANCELLED to emptyList()
    )

    fun isValidTransition(currentStatus: String, nextStatus: String): Boolean {
        val targets = allowedTransitions[currentStatus] ?: emptyList()
        return targets.contains(nextStatus)
    }

    fun validateTransition(currentStatus: String, nextStatus: String): TransitionResult {
        if (currentStatus == nextStatus) {
            return TransitionResult.Invalid("Lịch hẹn đang ở trạng thái này rồi")
        }
        if (isValidTransition(currentStatus, nextStatus)) {
            return TransitionResult.Valid
        }
        return TransitionResult.Invalid(
            "Không được phép chuyển trực tiếp từ '$currentStatus' sang '$nextStatus'"
        )
    }
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Chính sách tự động hoàn thành dịch vụ sau 30 phút kể từ khi thời gian dịch vụ kết thúc (Merchant Requirement #7):
 * - Khi đơn đang ở trạng thái IN_SERVICE
 * - Đã quá 30 phút kể từ thời điểm kết thúc dự kiến (endAt) mà tiệm chưa bấm nút "Hoàn thành"
 * - Hệ thống tự động chuyển sang COMPLETED và ghi nhận autoCompleted = true.
 */
class MerchantAutoCompletionPolicy {

    companion object {
        const val AUTO_COMPLETE_GRACE_PERIOD_MILLIS =
            MerchantConstants.AUTO_COMPLETE_DELAY_MINUTES * 60 * 1000L // 30 phút
    }

    fun canComplete(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (booking.status != MerchantConstants.BOOKING_STATUS_CONFIRMED &&
            booking.status != MerchantConstants.BOOKING_STATUS_IN_SERVICE
        ) {
            return false
        }
        if (booking.endAt <= 0L) {
            return true
        }
        return currentTimeMillis >= booking.endAt
    }

    fun autoCompleteDeadline(booking: Booking): Long {
        return if (booking.endAt > 0L) booking.endAt + AUTO_COMPLETE_GRACE_PERIOD_MILLIS else 0L
    }

    fun remainingBeforeAutoCompleteMillis(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        if ((booking.status != MerchantConstants.BOOKING_STATUS_CONFIRMED &&
                booking.status != MerchantConstants.BOOKING_STATUS_IN_SERVICE) ||
            booking.endAt <= 0L
        ) {
            return 0L
        }
        return (autoCompleteDeadline(booking) - currentTimeMillis).coerceAtLeast(0L)
    }

    fun shouldAutoComplete(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (booking.status != MerchantConstants.BOOKING_STATUS_CONFIRMED &&
            booking.status != MerchantConstants.BOOKING_STATUS_IN_SERVICE
        ) {
            return false
        }
        if (booking.endAt <= 0L) {
            return false
        }
        val threshold = booking.endAt + AUTO_COMPLETE_GRACE_PERIOD_MILLIS
        return currentTimeMillis >= threshold
    }

    fun findEligibleBookings(
        bookings: List<Booking>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): List<Booking> {
        return bookings.filter { shouldAutoComplete(it, currentTimeMillis) }
    }
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Chính sách tự động duyệt đơn sau 30 phút kể từ lúc khách hàng đặt dịch vụ:
 * - Salon có 30 phút kể từ thời điểm tạo đơn (createdAt) để bấm Từ chối đơn đang PENDING.
 * - Sau 30 phút nếu đơn vẫn ở trạng thái PENDING, hệ thống tự động duyệt sang CONFIRMED (autoApproved = true).
 */
class MerchantAutoApprovalPolicy {

    companion object {
        const val AUTO_APPROVE_WINDOW_MILLIS =
            MerchantConstants.AUTO_APPROVE_DELAY_MINUTES * 60 * 1000L // 30 phút
    }

    fun autoApproveDeadline(booking: Booking): Long {
        return if (booking.createdAt > 0L) booking.createdAt + AUTO_APPROVE_WINDOW_MILLIS else 0L
    }

    fun remainingRejectMillis(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        if (booking.status != MerchantConstants.BOOKING_STATUS_PENDING || booking.createdAt <= 0L) {
            return 0L
        }
        return (autoApproveDeadline(booking) - currentTimeMillis).coerceAtLeast(0L)
    }

    fun canReject(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        return booking.status == MerchantConstants.BOOKING_STATUS_PENDING &&
            booking.createdAt > 0L &&
            currentTimeMillis < autoApproveDeadline(booking)
    }

    fun shouldAutoApprove(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (booking.status != MerchantConstants.BOOKING_STATUS_PENDING) {
            return false
        }
        if (booking.createdAt <= 0L) {
            return false
        }
        return currentTimeMillis >= autoApproveDeadline(booking)
    }

    fun findEligibleBookings(
        bookings: List<Booking>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): List<Booking> {
        return bookings.filter { shouldAutoApprove(it, currentTimeMillis) }
    }
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Chính sách khóa hủy lịch đối với đơn đã xác nhận (CONFIRMED):
 * - Salon có thể hủy lịch đến trước thời điểm dịch vụ bắt đầu 30 phút (startAt - 30 phút).
 * - Khi còn <= 30 phút trước giờ bắt đầu (hoặc đã quá giờ bắt đầu), nút Hủy lịch bị khóa (disable).
 */
class MerchantCancellationPolicy {

    companion object {
        const val LOCK_WINDOW_BEFORE_START_MILLIS =
            MerchantConstants.MERCHANT_CANCEL_LOCK_BEFORE_START_MINUTES * 60 * 1000L // 30 phút
    }

    fun cancelDeadline(booking: Booking): Long {
        return if (booking.startAt > 0L) booking.startAt - LOCK_WINDOW_BEFORE_START_MILLIS else 0L
    }

    fun remainingCancelMillis(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Long {
        if (booking.status != MerchantConstants.BOOKING_STATUS_CONFIRMED || booking.startAt <= 0L) {
            return 0L
        }
        return (cancelDeadline(booking) - currentTimeMillis).coerceAtLeast(0L)
    }

    fun canCancel(
        booking: Booking,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Boolean {
        if (booking.status != MerchantConstants.BOOKING_STATUS_CONFIRMED) {
            return false
        }
        if (booking.startAt <= 0L) {
            return true
        }
        return currentTimeMillis < cancelDeadline(booking)
    }
}

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
 * Bộ lọc và tìm kiếm danh sách booking theo trạng thái & từ khóa
 */
enum class BookingFilterType(val displayName: String) {
    ALL("Tất cả"),
    PENDING("Chờ duyệt"),
    CONFIRMED("Đã xác nhận"),
    IN_SERVICE("Đang phục vụ"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy")
}

class BookingFilterPolicy {

    fun filter(
        bookings: List<Booking>,
        filter: BookingFilterType,
        searchQuery: String = ""
    ): List<Booking> {
        val query = searchQuery.trim().lowercase()

        return bookings.filter { bk ->
            val matchStatus = when (filter) {
                BookingFilterType.ALL -> true
                BookingFilterType.PENDING -> bk.status == MerchantConstants.BOOKING_STATUS_PENDING
                BookingFilterType.CONFIRMED -> bk.status == MerchantConstants.BOOKING_STATUS_CONFIRMED
                BookingFilterType.IN_SERVICE -> bk.status == MerchantConstants.BOOKING_STATUS_IN_SERVICE
                BookingFilterType.COMPLETED -> bk.status == MerchantConstants.BOOKING_STATUS_COMPLETED
                BookingFilterType.CANCELLED -> bk.status == MerchantConstants.BOOKING_STATUS_CANCELLED
            }

            val matchQuery = if (query.isBlank()) {
                true
            } else {
                bk.customerName.lowercase().contains(query) ||
                bk.customerPhone.contains(query) ||
                bk.id.lowercase().contains(query) ||
                bk.serviceNames.any { it.lowercase().contains(query) }
            }

            matchStatus && matchQuery
        }
    }
}
