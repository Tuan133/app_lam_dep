package com.applamdep.common

import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.data.repository.FakeBookingRepository
import com.applamdep.customer.data.repository.FakeReviewRepository
import com.applamdep.customer.data.repository.FakeShopRepository
import com.applamdep.customer.domain.model.Booking as CustomerBooking
import com.applamdep.customer.domain.model.Review as CustomerReview
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ReviewRepositoryImpl
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.model.Booking as MerchantBooking
import com.applamdep.merchant.domain.model.Review as MerchantReview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Giao diện đồng bộ sự kiện đơn đặt lịch, đánh giá và trạng thái mở/đóng cửa giữa Customer App và Merchant Portal.
 * Khi unit test khởi tạo repository độc lập (syncGateway = null), các repository hoạt động tách biệt.
 */
interface BookingSyncGateway {
    fun onCustomerBookingCreated(booking: CustomerBooking)
    fun onCustomerBookingCancelled(bookingId: String, reason: String)
    fun onCustomerBookingReviewed(bookingId: String)
    fun onCustomerReviewSubmitted(review: CustomerReview)
    fun onMerchantBookingUpdated(booking: MerchantBooking)
    fun onMerchantShopStatusUpdated(shopId: String, status: String) {}
}

/**
 * Cầu nối đồng bộ in-memory dùng chung khi ứng dụng khởi chạy cả DefaultAppContainer và DefaultMerchantContainer.
 */
object AppBookingSyncBridge : BookingSyncGateway {

    private data class PendingHeldTx(
        val bookingId: String,
        val shopId: String,
        val customerUid: String,
        val amount: Long
    )

    private var customerBookingRepo: FakeBookingRepository? = null
    private var customerReviewRepo: FakeReviewRepository? = null
    private var customerShopRepo: FakeShopRepository? = null
    private var merchantBookingRepo: BookingRepositoryImpl? = null
    private var merchantReviewRepo: ReviewRepositoryImpl? = null
    private var merchantTransactionRepo: TransactionRepositoryImpl? = null

    private val _shopStatusFlow = MutableStateFlow<Map<String, String>>(emptyMap())
    val shopStatusFlow: StateFlow<Map<String, String>> = _shopStatusFlow.asStateFlow()

    private val pendingCustomerBookings = mutableListOf<MerchantBooking>()
    private val pendingCustomerDeposits = mutableListOf<PendingHeldTx>()
    private val pendingCustomerCancellations = mutableListOf<Pair<String, String>>()
    private val pendingCustomerRefunds = mutableListOf<String>()
    private val pendingCustomerReviewed = mutableListOf<String>()
    private val pendingCustomerReviews = mutableListOf<MerchantReview>()
    private val pendingMerchantUpdates = mutableListOf<MerchantBooking>()
    private val pendingShopStatuses = mutableMapOf<String, String>()

    @Synchronized
    fun registerCustomerShopRepository(repo: FakeShopRepository) {
        customerShopRepo = repo
        pendingShopStatuses.forEach { (custShopId, status) ->
            repo.updateShopStatusFromSync(custShopId, status)
        }
        pendingShopStatuses.clear()
    }

    @Synchronized
    fun registerCustomerBookingRepository(repo: FakeBookingRepository) {
        customerBookingRepo = repo
        pendingMerchantUpdates.forEach { syncMerchantUpdateToCustomer(repo, it) }
        pendingMerchantUpdates.clear()
    }

    @Synchronized
    fun registerCustomerReviewRepository(repo: FakeReviewRepository) {
        customerReviewRepo = repo
    }

    @Synchronized
    fun registerMerchantBookingRepository(repo: BookingRepositoryImpl) {
        merchantBookingRepo = repo
        pendingCustomerBookings.forEach { repo.upsertFromSync(it) }
        pendingCustomerBookings.clear()
        pendingCustomerCancellations.forEach { (id, reason) -> repo.cancelFromSync(id, reason) }
        pendingCustomerCancellations.clear()
        pendingCustomerReviewed.forEach { repo.markReviewedFromSync(it) }
        pendingCustomerReviewed.clear()
    }

    @Synchronized
    fun registerMerchantReviewRepository(repo: ReviewRepositoryImpl) {
        merchantReviewRepo = repo
        pendingCustomerReviews.forEach { repo.addReviewFromSync(it) }
        pendingCustomerReviews.clear()
    }

    @Synchronized
    fun registerMerchantTransactionRepository(repo: TransactionRepositoryImpl) {
        merchantTransactionRepo = repo
        pendingCustomerDeposits.forEach {
            repo.ensureHeldTransaction(it.bookingId, it.shopId, it.customerUid, it.amount)
        }
        pendingCustomerDeposits.clear()
        pendingCustomerRefunds.forEach { repo.refundFromSync(it) }
        pendingCustomerRefunds.clear()
    }

    @Synchronized
    internal fun resetForTesting() {
        customerBookingRepo = null
        customerReviewRepo = null
        customerShopRepo = null
        merchantBookingRepo = null
        merchantReviewRepo = null
        merchantTransactionRepo = null
        _shopStatusFlow.value = emptyMap()
        pendingCustomerBookings.clear()
        pendingCustomerDeposits.clear()
        pendingCustomerCancellations.clear()
        pendingCustomerRefunds.clear()
        pendingCustomerReviewed.clear()
        pendingCustomerReviews.clear()
        pendingMerchantUpdates.clear()
        pendingShopStatuses.clear()
    }

    @Synchronized
    override fun onMerchantShopStatusUpdated(shopId: String, status: String) {
        val currentMap = _shopStatusFlow.value.toMutableMap()
        currentMap[shopId] = status
        val customerShopId = toCustomerShopId(shopId)
        currentMap[customerShopId] = status
        _shopStatusFlow.value = currentMap

        val repo = customerShopRepo
        if (repo != null) {
            repo.updateShopStatusFromSync(customerShopId, status)
        } else {
            pendingShopStatuses[customerShopId] = status
        }
    }

    @Synchronized
    override fun onCustomerBookingCreated(booking: CustomerBooking) {
        val targetShopId = toMerchantShopId(booking.shopId) ?: return
        val startAt = booking.appointmentTimestamp
        val durationMillis = booking.durationMinutes.coerceAtLeast(30) * 60_000L
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val resolvedDate = when {
            booking.bookingDate.isBlank() || booking.bookingDate == todayStr -> "2026-10-29"
            else -> booking.bookingDate
        }
        val baseSlotId = mapTimeToSlotId(booking.bookingTime)
        val mappedSlotId = if (resolvedDate == "2026-10-29") baseSlotId else "${resolvedDate}_$baseSlotId"
        val merchantBooking = MerchantBooking(
            id = booking.id,
            customerUid = booking.customerId,
            shopId = targetShopId,
            serviceIds = listOf(booking.serviceId),
            serviceNames = listOf(booking.serviceName),
            slotId = mappedSlotId,
            startAt = startAt,
            endAt = startAt + durationMillis,
            customerName = booking.customerName,
            customerPhone = booking.customerPhone,
            note = booking.customerNote,
            status = MerchantConstants.BOOKING_STATUS_PENDING,
            depositAmount = booking.depositAmount,
            createdAt = booking.createdAt,
            isReviewed = booking.isReviewed,
            bookingDate = resolvedDate
        )
        val bookingRepo = merchantBookingRepo
        if (bookingRepo != null) {
            bookingRepo.upsertFromSync(merchantBooking)
        } else {
            pendingCustomerBookings.add(merchantBooking)
        }
        if (booking.requiresDeposit && booking.depositAmount > 0L) {
            val txRepo = merchantTransactionRepo
            if (txRepo != null) {
                txRepo.ensureHeldTransaction(
                    bookingId = booking.id,
                    shopId = targetShopId,
                    customerUid = booking.customerId,
                    amount = booking.depositAmount
                )
            } else {
                pendingCustomerDeposits.add(
                    PendingHeldTx(booking.id, targetShopId, booking.customerId, booking.depositAmount)
                )
            }
        }
    }

    @Synchronized
    override fun onCustomerBookingCancelled(bookingId: String, reason: String) {
        val bookingRepo = merchantBookingRepo
        if (bookingRepo != null) {
            bookingRepo.cancelFromSync(bookingId, reason)
        } else {
            pendingCustomerCancellations.add(bookingId to reason)
        }
        val txRepo = merchantTransactionRepo
        if (txRepo != null) {
            txRepo.refundFromSync(bookingId)
        } else {
            pendingCustomerRefunds.add(bookingId)
        }
    }

    @Synchronized
    override fun onCustomerBookingReviewed(bookingId: String) {
        val bookingRepo = merchantBookingRepo
        if (bookingRepo != null) {
            bookingRepo.markReviewedFromSync(bookingId)
        } else {
            pendingCustomerReviewed.add(bookingId)
        }
    }

    @Synchronized
    override fun onCustomerReviewSubmitted(review: CustomerReview) {
        val merchantShopId = toMerchantShopId(review.shopId) ?: review.shopId
        val merchantReview = MerchantReview(
            id = review.id,
            bookingId = review.bookingId,
            shopId = merchantShopId,
            customerUid = review.customerId,
            customerName = review.customerName,
            rating = review.rating,
            content = review.comment,
            images = review.images,
            createdAt = review.createdAt,
            status = review.status
        )
        val reviewRepo = merchantReviewRepo
        if (reviewRepo != null) {
            reviewRepo.addReviewFromSync(merchantReview)
        } else {
            pendingCustomerReviews.add(merchantReview)
        }
    }

    @Synchronized
    override fun onMerchantBookingUpdated(booking: MerchantBooking) {
        val repo = customerBookingRepo
        if (repo != null) {
            syncMerchantUpdateToCustomer(repo, booking)
        } else {
            pendingMerchantUpdates.removeAll { it.id == booking.id }
            pendingMerchantUpdates.add(booking)
        }
    }

    private fun syncMerchantUpdateToCustomer(repo: FakeBookingRepository, booking: MerchantBooking) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startDate = if (booking.startAt > 0L) Date(booking.startAt) else Date(booking.createdAt)
        val durationMins = if (booking.startAt > 0L && booking.endAt > booking.startAt) {
            ((booking.endAt - booking.startAt) / 60_000L).toInt().coerceAtLeast(15)
        } else 45

        val mappedCustomerBooking = CustomerBooking(
            id = booking.id,
            customerId = booking.customerUid,
            customerName = booking.customerName,
            customerPhone = booking.customerPhone,
            customerNote = booking.note,
            shopId = toCustomerShopId(booking.shopId),
            shopName = "1994 Nailsroom (L'Amour)",
            shopAddress = "172/5 Trần Phú, Hải Châu, Đà Nẵng",
            serviceId = booking.serviceIds.firstOrNull() ?: "srv_01",
            serviceName = booking.serviceNames.joinToString(", ").ifBlank { "Dịch vụ làm đẹp" },
            servicePrice = maxOf(booking.depositAmount * 2L, 180000L),
            bookingDate = dateFormat.format(startDate),
            bookingTime = timeFormat.format(startDate),
            appointmentTimestamp = if (booking.startAt > 0L) booking.startAt else System.currentTimeMillis(),
            durationMinutes = durationMins,
            requiresDeposit = booking.depositAmount > 0L,
            depositAmount = booking.depositAmount,
            status = booking.status,
            cancelReason = booking.cancelReason,
            cancelledAt = booking.cancelledAt,
            isReviewed = booking.isReviewed,
            createdAt = booking.createdAt
        )
        repo.syncFromMerchant(
            booking = mappedCustomerBooking,
            createIfMissing = booking.customerUid == "user_cust_01" &&
                (booking.status == AppConstants.BOOKING_STATUS_COMPLETED ||
                 booking.status == AppConstants.BOOKING_STATUS_CONFIRMED ||
                 booking.status == AppConstants.BOOKING_STATUS_CANCELLED)
        )
    }

    private fun toMerchantShopId(customerShopId: String): String? {
        return when (customerShopId) {
            "salon_01", "shop_lamour", "shop_01" -> "shop_01"
            else -> null
        }
    }

    private fun mapTimeToSlotId(time: String): String {
        return when (time.trim()) {
            "09:00" -> "slot_01"
            "10:00", "10:30" -> "slot_02"
            "11:30", "13:30" -> "slot_03"
            "14:00", "15:00" -> "slot_04"
            "15:30", "16:30" -> "slot_05"
            "17:00", "18:00" -> "slot_06"
            "18:30", "19:30" -> "slot_07"
            else -> "slot_01"
        }
    }

    private fun toCustomerShopId(merchantShopId: String): String {
        return when (merchantShopId) {
            "shop_01", "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> merchantShopId
        }
    }
}

