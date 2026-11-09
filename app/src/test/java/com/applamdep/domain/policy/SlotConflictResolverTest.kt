package com.applamdep.domain.policy

import com.applamdep.customer.domain.model.ShopSlot
import com.applamdep.customer.domain.policy.SlotConflictResolver
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class SlotConflictResolverTest {

    private lateinit var resolver: SlotConflictResolver

    @Before
    fun setUp() {
        resolver = SlotConflictResolver()
    }

    @Test
    fun `resolveAvailableSlots filters out slots with full capacity`() {
        val slots = listOf(
            ShopSlot("s1", "shop1", "2026-12-31", "09:00", maxCapacity = 2, bookedCount = 2), // Full
            ShopSlot("s2", "shop1", "2026-12-31", "10:00", maxCapacity = 2, bookedCount = 1), // Available
            ShopSlot("s3", "shop1", "2026-12-31", "11:00", maxCapacity = 2, bookedCount = 0)  // Available
        )

        val futureDate = "2026-12-31"
        val available = resolver.resolveAvailableSlots(slots, futureDate, serviceDurationMinutes = 45)

        assertEquals(2, available.size)
        assertTrue(available.any { it.id == "s2" })
        assertTrue(available.any { it.id == "s3" })
        assertFalse(available.any { it.id == "s1" })
    }

    @Test
    fun `resolveAvailableSlots filters out past slots when booking for today`() {
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val mockCurrentDate = timeFormat.parse("2026-10-24 14:00")!!

        val slots = listOf(
            ShopSlot("s1", "shop1", "2026-10-24", "09:00", maxCapacity = 2, bookedCount = 0), // Past
            ShopSlot("s2", "shop1", "2026-10-24", "13:30", maxCapacity = 2, bookedCount = 0), // Past
            ShopSlot("s3", "shop1", "2026-10-24", "14:30", maxCapacity = 2, bookedCount = 0), // Future
            ShopSlot("s4", "shop1", "2026-10-24", "16:00", maxCapacity = 2, bookedCount = 0)  // Future
        )

        val available = resolver.resolveAvailableSlots(
            allSlots = slots,
            bookingDate = "2026-10-24",
            serviceDurationMinutes = 45,
            currentTime = mockCurrentDate
        )

        assertEquals(2, available.size)
        assertEquals("14:30", available[0].startTime)
        assertEquals("16:00", available[1].startTime)
    }

    @Test
    fun `isOverlapping correctly detects time overlap between services`() {
        // Slot A: 09:00 - 09:45 (45 min)
        // Slot B: 09:30 - 10:15 (45 min) -> Overlap!
        assertTrue(resolver.isOverlapping("09:00", 45, "09:30", 45))

        // Slot A: 09:00 - 10:00 (60 min)
        // Slot B: 10:00 - 11:00 (60 min) -> No overlap
        assertFalse(resolver.isOverlapping("09:00", 60, "10:00", 60))

        // Slot A: 14:00 - 15:00
        // Slot B: 11:00 - 12:00 -> No overlap
        assertFalse(resolver.isOverlapping("14:00", 60, "11:00", 60))
    }
}
