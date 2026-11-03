package com.applamdep.domain.policy

import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.policy.StandardCancellationPolicy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CancellationPolicyTest {

    private lateinit var policy: StandardCancellationPolicy

    @Before
    fun setUp() {
        policy = StandardCancellationPolicy()
    }

    @Test
    fun `cancel booking more than 2 hours before appointment should be eligible for 100 percent refund`() {
        val currentTime = 1000000000L
        val appointmentTime = currentTime + (3 * 3600 * 1000L) // 3 hours in the future (> 2h)

        val booking = Booking(
            id = "B-001",
            customerId = "user-1",
            customerName = "Khánh Linh",
            customerPhone = "0903123456",
            shopId = "shop-1",
            shopName = "L'Amour Luxury",
            shopAddress = "128 Pasteur, Bến Nghé, Quận 1",
            serviceId = "s-1",
            serviceName = "Sơn Gel Hữu Cơ Pháp",
            servicePrice = 180000L,
            durationMinutes = 45,
            bookingDate = "2026-10-24",
            bookingTime = "15:30",
            appointmentTimestamp = appointmentTime,
            requiresDeposit = true,
            depositAmount = 50000L,
            status = AppConstants.BOOKING_STATUS_CONFIRMED
        )

        val decision = policy.evaluate(booking, currentTimeMillis = currentTime)

        assertTrue("Khách phải được phép hủy trước 2 tiếng", decision.canCancel)
        assertTrue("Khách phải được hoàn tiền cọc", decision.isRefundEligible)
        assertEquals(50000L, decision.refundAmount)
        assertTrue(decision.message.contains("hoàn 100%"))
    }

    @Test
    fun `cancel booking less than 2 hours before appointment should NOT be eligible for refund`() {
        val currentTime = 1000000000L
        val appointmentTime = currentTime + (1 * 3600 * 1000L) // 1 hour in the future (< 2h)

        val booking = Booking(
            id = "B-002",
            customerId = "user-1",
            customerName = "Khánh Linh",
            customerPhone = "0903123456",
            shopId = "shop-1",
            shopName = "L'Amour Luxury",
            shopAddress = "128 Pasteur, Bến Nghé, Quận 1",
            serviceId = "s-1",
            serviceName = "Sơn Gel Hữu Cơ Pháp",
            servicePrice = 180000L,
            durationMinutes = 45,
            bookingDate = "2026-10-24",
            bookingTime = "15:30",
            appointmentTimestamp = appointmentTime,
            requiresDeposit = true,
            depositAmount = 50000L,
            status = AppConstants.BOOKING_STATUS_CONFIRMED
        )

        val decision = policy.evaluate(booking, currentTimeMillis = currentTime)

        assertFalse("Không được hủy khi còn dưới 2 tiếng", decision.canCancel)
        assertFalse("Không được hoàn cọc", decision.isRefundEligible)
        assertEquals(0L, decision.refundAmount)
        assertTrue(decision.message.contains("không thể hủy") || decision.message.contains("không được hoàn lại"))
    }

    @Test
    fun `cancel booking when appointment has already passed should fail`() {
        val currentTime = 1000000000L
        val appointmentTime = currentTime - (30 * 60 * 1000L) // 30 mins in the past

        val booking = Booking(
            id = "B-003",
            customerId = "user-1",
            customerName = "Khánh Linh",
            customerPhone = "0903123456",
            shopId = "shop-1",
            shopName = "L'Amour Luxury",
            shopAddress = "128 Pasteur, Bến Nghé, Quận 1",
            serviceId = "s-1",
            serviceName = "Sơn Gel Hữu Cơ Pháp",
            servicePrice = 180000L,
            durationMinutes = 45,
            bookingDate = "2026-10-24",
            bookingTime = "15:30",
            appointmentTimestamp = appointmentTime,
            requiresDeposit = false,
            depositAmount = 0L,
            status = AppConstants.BOOKING_STATUS_CONFIRMED
        )

        val decision = policy.evaluate(booking, currentTimeMillis = currentTime)

        assertFalse(decision.canCancel)
        assertFalse(decision.isRefundEligible)
        assertEquals(0L, decision.refundAmount)
    }
}
