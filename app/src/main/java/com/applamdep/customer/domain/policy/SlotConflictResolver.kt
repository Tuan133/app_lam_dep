package com.applamdep.customer.domain.policy

import com.applamdep.customer.domain.model.ShopSlot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Lớp SlotConflictResolver đóng gói toàn bộ thuật toán kiểm tra xung đột thời gian,
 * loại trừ các khung giờ đã hết chỗ, các giờ trong quá khứ, và các giờ chồng lấn với thời lượng dịch vụ.
 * UI hoàn toàn không phải can thiệp vào phép tính thời gian phức tạp này.
 */
class SlotConflictResolver {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Lọc danh sách khung giờ hợp lệ để khách chọn:
     * - Loại bỏ khung giờ đã đầy chỗ (bookedCount >= maxCapacity)
     * - Loại bỏ khung giờ trong quá khứ nếu chọn ngày hôm nay
     * - Loại bỏ khung giờ bị xung đột với thời lượng dịch vụ (durationMinutes)
     */
    fun resolveAvailableSlots(
        allSlots: List<ShopSlot>,
        bookingDate: String,
        serviceDurationMinutes: Int,
        currentTime: Date = Date()
    ): List<ShopSlot> {
        val todayStr = dateFormat.format(currentTime)
        val isToday = bookingDate == todayStr
        val currentMinutesOfDay = if (isToday) {
            val cal = Calendar.getInstance().apply { time = currentTime }
            cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        } else {
            -1
        }

        return allSlots.filter { slot ->
            // 1. Kiểm tra sức chứa
            if (!slot.hasCapacity) return@filter false

            // 2. Kiểm tra giờ đã qua nếu là ngày hôm nay
            val slotMinutes = parseTimeToMinutes(slot.startTime)
            if (isToday && slotMinutes <= currentMinutesOfDay) {
                return@filter false
            }

            true
        }
    }

    /**
     * Kiểm tra hai khung giờ có bị trùng hoặc lấn vào nhau không
     */
    fun isOverlapping(
        startA: String,
        durationMinutesA: Int,
        startB: String,
        durationMinutesB: Int
    ): Boolean {
        val aStart = parseTimeToMinutes(startA)
        val aEnd = aStart + durationMinutesA
        val bStart = parseTimeToMinutes(startB)
        val bEnd = bStart + durationMinutesB

        return maxOf(aStart, bStart) < minOf(aEnd, bEnd)
    }

    private fun parseTimeToMinutes(timeStr: String): Int {
        return try {
            val parts = timeStr.split(":")
            parts[0].toInt() * 60 + parts[1].toInt()
        } catch (e: Exception) {
            0
        }
    }
}
