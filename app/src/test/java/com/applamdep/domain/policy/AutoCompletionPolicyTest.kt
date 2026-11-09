package com.applamdep.domain.policy

import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking
import com.applamdep.merchant.domain.policy.StandardAutoCompletionPolicy
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AutoCompletionPolicyTest {

    private lateinit var policy: StandardAutoCompletionPolicy

    @Before
    fun setUp() {
        policy = StandardAutoCompletionPolicy()
    }

    @Test
    fun `shouldAutoComplete returns true when 30 minutes have elapsed after service duration`() {
        val appointmentStart = 1000000000L
        val durationMinutes = 60 // 1 hour
        val currentTime = appointmentStart + (95 * 60 * 1000L) // 95 mins elapsed

        val booking = Booking(
            id = "B-AUTO-1",
            customerId = "c1",
            customerName = "Khánh Linh",
            customerPhone = "0903123456",
            shopId = "s1",
            shopName = "L'Amour",
            shopAddress = "128 Pasteur, Bến Nghé, Quận 1",
            serviceId = "srv1",
            serviceName = "Chăm sóc da",
            servicePrice = 200000L,
            bookingDate = "2026-10-24",
            bookingTime = "10:00",
            appointmentTimestamp = appointmentStart,
            durationMinutes = durationMinutes,
            requiresDeposit = false,
            depositAmount = 0L,
            status = AppConstants.BOOKING_STATUS_CONFIRMED
        )

        assertTrue(
            "Booking phải tự động hoàn thành sau khi kết thúc dịch vụ 30 phút",
            policy.shouldAutoComplete(booking, currentTimestamp = currentTime)
        )
    }

    @Test
    fun `shouldAutoComplete returns false when within 30 minutes of service end`() {
        val appointmentStart = 1000000000L
        val durationMinutes = 60
        val currentTime = appointmentStart + (75 * 60 * 1000L)

        val booking = Booking(
            id = "B-AUTO-2",
            customerId = "c1",
            customerName = "Khánh Linh",
            customerPhone = "0903123456",
            shopId = "s1",
            shopName = "L'Amour",
            shopAddress = "128 Pasteur, Bến Nghé, Quận 1",
            serviceId = "srv1",
            serviceName = "Chăm sóc da",
            servicePrice = 200000L,
            bookingDate = "2026-10-24",
            bookingTime = "10:00",
            appointmentTimestamp = appointmentStart,
            durationMinutes = durationMinutes,
            requiresDeposit = false,
            depositAmount = 0L,
            status = AppConstants.BOOKING_STATUS_IN_SERVICE
        )

        assertFalse(
            "Booking chưa được tự động hoàn thành nếu chưa đủ 30 phút sau khi kết thúc",
            policy.shouldAutoComplete(booking, currentTimestamp = currentTime)
        )
    }

    @Test
    fun `shouldAutoComplete returns false for already completed or cancelled bookings`() {
        val appointmentStart = 1000000000L
        val currentTime = appointmentStart + (200 * 60 * 1000L)

        val completedBooking = Booking(
            id = "B-AUTO-3",
            customerId = "c1",
            customerName = "Khánh Linh",
            customerPhone = "0903123456",
            shopId = "s1",
            shopName = "L'Amour",
            shopAddress = "128 Pasteur, Bến Nghé, Quận 1",
            serviceId = "srv1",
            serviceName = "Chăm sóc da",
            servicePrice = 200000L,
            bookingDate = "2026-10-24",
            bookingTime = "10:00",
            appointmentTimestamp = appointmentStart,
            durationMinutes = 60,
            requiresDeposit = false,
            depositAmount = 0L,
            status = AppConstants.BOOKING_STATUS_COMPLETED
        )

        val cancelledBooking = completedBooking.copy(
            id = "B-AUTO-4",
            status = AppConstants.BOOKING_STATUS_CANCELLED
        )

        assertFalse(policy.shouldAutoComplete(completedBooking, currentTime))
        assertFalse(policy.shouldAutoComplete(cancelledBooking, currentTime))
    }
}
