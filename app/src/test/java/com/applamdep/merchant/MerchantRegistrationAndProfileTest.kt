package com.applamdep.merchant

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.model.SystemConfig
import com.applamdep.merchant.domain.rule.DepositPolicyValidator
import com.applamdep.merchant.domain.rule.PlanFeeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests kiểm thử các quy tắc nghiệp vụ Đóng gói của Chức năng 1 và 2
 */
class MerchantRegistrationAndProfileTest {

    private val depositValidator = DepositPolicyValidator()
    private val feeCalculator = PlanFeeCalculator()

    @Test
    fun depositValidator_validFiveDistinctPositiveAmounts_returnsValid() {
        val options = listOf(50000L, 100000L, 150000L, 200000L, 500000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Valid)
    }

    @Test
    fun depositValidator_lessThanFiveAmounts_returnsInvalid() {
        val options = listOf(50000L, 100000L, 150000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Invalid)
    }

    @Test
    fun depositValidator_duplicateAmounts_returnsInvalid() {
        val options = listOf(50000L, 100000L, 100000L, 200000L, 500000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Invalid)
    }

    @Test
    fun depositValidator_negativeAmount_returnsInvalid() {
        val options = listOf(-50000L, 100000L, 150000L, 200000L, 500000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Invalid)
    }

    @Test
    fun depositValidator_whenDepositDisabled_alwaysReturnsValid() {
        val emptyOptions = emptyList<Long>()
        val result = depositValidator.validateDepositOptions(requireDeposit = false, depositOptions = emptyOptions)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Valid)
    }

    @Test
    fun feeCalculator_computesCorrectFeeFromConfig_notHardcoded() {
        val config = SystemConfig(
            normalPlanFee = mapOf("3" to 300000L, "6" to 550000L, "12" to 1000000L),
            vipPlanFee = mapOf("3" to 600000L, "6" to 1100000L, "12" to 2000000L)
        )

        val normal3 = feeCalculator.calculateFee(config, MerchantConstants.PLAN_NORMAL, 3)
        assertEquals(300000L, normal3)

        val vip12 = feeCalculator.calculateFee(config, MerchantConstants.PLAN_VIP, 12)
        assertEquals(2000000L, vip12)
    }

    @Test
    fun criticalChangeDetection_approvedShopChangesName_shouldRecordAudit() {
        val original = ShopProfile(
            name = "Salon A",
            address = "123 Street",
            status = MerchantConstants.SHOP_STATUS_APPROVED
        )
        val updated = original.copy(name = "Salon B")

        val isCritical = original.isApproved && (original.name != updated.name || original.address != updated.address)
        assertTrue(isCritical)
    }

    @Test
    fun criticalChangeDetection_pendingShopChangesName_doesNotTriggerAudit() {
        val original = ShopProfile(
            name = "Salon A",
            address = "123 Street",
            status = MerchantConstants.SHOP_STATUS_PENDING
        )
        val updated = original.copy(name = "Salon B")

        val isCritical = original.isApproved && (original.name != updated.name || original.address != updated.address)
        assertFalse(isCritical)
    }

    @Test
    fun filterActiveBookingsInWindow_filtersByDateAndTimeWindowCorrectly() {
        val bookings = listOf(
            com.applamdep.merchant.domain.model.Booking(
                id = "BK-1",
                slotId = "slot_01",
                status = MerchantConstants.BOOKING_STATUS_PENDING,
                bookingDate = "2026-10-29"
            ),
            com.applamdep.merchant.domain.model.Booking(
                id = "BK-2",
                slotId = "slot_03",
                status = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                bookingDate = "2026-10-29"
            ),
            com.applamdep.merchant.domain.model.Booking(
                id = "BK-3",
                slotId = "slot_04",
                status = MerchantConstants.BOOKING_STATUS_COMPLETED,
                bookingDate = "2026-10-29"
            ),
            com.applamdep.merchant.domain.model.Booking(
                id = "BK-4",
                slotId = "2026-10-30_slot_02",
                status = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
                bookingDate = "2026-10-30"
            )
        )

        val morning = com.applamdep.merchant.ui.profile.filterActiveBookingsInWindow(
            bookings = bookings,
            isoDate = "2026-10-29",
            window = com.applamdep.merchant.ui.profile.ShopClosureWindow.MORNING
        )
        val afternoon = com.applamdep.merchant.ui.profile.filterActiveBookingsInWindow(
            bookings = bookings,
            isoDate = "2026-10-29",
            window = com.applamdep.merchant.ui.profile.ShopClosureWindow.AFTERNOON
        )
        val fullDayNext = com.applamdep.merchant.ui.profile.filterActiveBookingsInWindow(
            bookings = bookings,
            isoDate = "2026-10-30",
            window = com.applamdep.merchant.ui.profile.ShopClosureWindow.FULL_DAY
        )

        org.junit.Assert.assertEquals(listOf("BK-1"), morning.map { it.id })
        org.junit.Assert.assertEquals(listOf("BK-2"), afternoon.map { it.id })
        org.junit.Assert.assertEquals(listOf("BK-4"), fullDayNext.map { it.id })
    }

    private class FirstCollectedException : RuntimeException()

    private suspend fun <T> firstOf(flow: kotlinx.coroutines.flow.Flow<T>): T {
        var captured: T? = null
        var hasValue = false
        try {
            flow.collect { value ->
                captured = value
                hasValue = true
                throw FirstCollectedException()
            }
        } catch (_: FirstCollectedException) {
        }
        check(hasValue) { "Flow emitted no items" }
        @Suppress("UNCHECKED_CAST")
        return captured as T
    }

    @Test
    fun cancelBookingsForShopClosure_cancelsWithin30MinLockSkipsCompletedAndSyncsGateway() = kotlinx.coroutines.runBlocking {
        val syncedUpdates = mutableListOf<com.applamdep.merchant.domain.model.Booking>()
        val fakeGateway = object : com.applamdep.common.BookingSyncGateway {
            override fun onCustomerBookingCreated(booking: com.applamdep.customer.domain.model.Booking) {}
            override fun onCustomerBookingCancelled(bookingId: String, reason: String) {}
            override fun onCustomerBookingReviewed(bookingId: String) {}
            override fun onCustomerReviewSubmitted(review: com.applamdep.customer.domain.model.Review) {}
            override fun onMerchantBookingUpdated(booking: com.applamdep.merchant.domain.model.Booking) {
                syncedUpdates.add(booking)
            }
        }
        val repo = com.applamdep.merchant.data.repository.BookingRepositoryImpl(syncGateway = fakeGateway)

        // BK-9023 đang ở CONFIRMED với giờ bắt đầu < 30 phút (bị khóa hủy đơn lẻ),
        // BK-9021 là PENDING, BK-9018 là IN_SERVICE, còn BK-9010 đã COMPLETED.
        val count = repo.cancelBookingsForShopClosure(
            bookingIds = listOf("BK-9021", "BK-9023", "BK-9018", "BK-9010"),
            reason = "Cửa hàng đóng cửa đột xuất"
        ).getOrThrow()

        org.junit.Assert.assertEquals(3, count)
        org.junit.Assert.assertEquals(listOf("BK-9021", "BK-9023", "BK-9018"), syncedUpdates.map { it.id })
        assertTrue(syncedUpdates.all {
            it.status == MerchantConstants.BOOKING_STATUS_CANCELLED &&
                it.cancelReason == "Cửa hàng đóng cửa đột xuất"
        })
        val currentBookings = firstOf(repo.getShopBookings("shop_01"))
        org.junit.Assert.assertEquals(
            MerchantConstants.BOOKING_STATUS_COMPLETED,
            currentBookings.first { it.id == "BK-9010" }.status
        )
    }

    @Test
    fun shopClosureStatus_propagatesToAppBookingSyncBridgeAndCustomerShopRepo() = kotlinx.coroutines.runBlocking {
        com.applamdep.common.AppBookingSyncBridge.resetForTesting()
        try {
            val customerShopRepo = com.applamdep.customer.data.repository.FakeShopRepository()
            com.applamdep.common.AppBookingSyncBridge.registerCustomerShopRepository(customerShopRepo)
            val merchantShopRepo = com.applamdep.merchant.data.repository.ShopRepositoryImpl(
                syncGateway = com.applamdep.common.AppBookingSyncBridge
            )

            val initial = firstOf(merchantShopRepo.getShopProfile("merchant_user_01"))!!
            assertFalse(initial.isClosed)
            org.junit.Assert.assertEquals(
                com.applamdep.customer.core.constants.AppConstants.SHOP_STATUS_APPROVED,
                customerShopRepo.getShopById("salon_01").getOrThrow().status
            )

            val closedProfile = initial.copy(
                status = MerchantConstants.SHOP_STATUS_CLOSED,
                closeReason = "Bảo trì tiệm",
                closedUntilDate = "2026-11-15"
            )
            merchantShopRepo.updateProfile(closedProfile).getOrThrow()

            val latestMerchant = firstOf(merchantShopRepo.getShopProfile("merchant_user_01"))!!
            assertTrue(latestMerchant.isClosed)
            org.junit.Assert.assertEquals(
                MerchantConstants.SHOP_STATUS_CLOSED,
                com.applamdep.common.AppBookingSyncBridge.shopStatusFlow.value["shop_01"]
            )
            org.junit.Assert.assertEquals(
                com.applamdep.customer.core.constants.AppConstants.SHOP_STATUS_CLOSED,
                com.applamdep.common.AppBookingSyncBridge.shopStatusFlow.value["salon_01"]
            )
            org.junit.Assert.assertEquals(
                com.applamdep.customer.core.constants.AppConstants.SHOP_STATUS_CLOSED,
                customerShopRepo.getShopById("salon_01").getOrThrow().status
            )
            assertTrue(
                firstOf(customerShopRepo.getApprovedShops())
                    .any { it.id == "salon_01" && it.status == com.applamdep.customer.core.constants.AppConstants.SHOP_STATUS_CLOSED }
            )

            val reopenedProfile = latestMerchant.copy(
                status = MerchantConstants.SHOP_STATUS_APPROVED,
                closeReason = null,
                closeNote = null,
                closedAt = null,
                closedUntilDate = null
            )
            merchantShopRepo.updateProfile(reopenedProfile).getOrThrow()

            org.junit.Assert.assertEquals(
                com.applamdep.customer.core.constants.AppConstants.SHOP_STATUS_APPROVED,
                customerShopRepo.getShopById("salon_01").getOrThrow().status
            )
        } finally {
            com.applamdep.common.AppBookingSyncBridge.resetForTesting()
        }
    }
}
