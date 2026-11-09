package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.WorkSchedule
import java.util.Locale

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Lớp kiểm tra tính hợp lệ của cấu hình ca làm việc (Merchant Requirement #4):
 * - Ngày làm việc trong tuần: Phải chọn ít nhất 1 ngày.
 * - Giờ mở/đóng cửa: Giờ đóng cửa phải sau giờ mở cửa.
 * - Khoảng nghỉ giữa ca: Nếu có, giờ nghỉ phải nằm trong khoảng giờ mở - đóng cửa.
 */
class ScheduleValidator {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val message: String) : ValidationResult
    }

    fun validate(schedule: WorkSchedule): ValidationResult {
        if (schedule.workingDays.isEmpty()) {
            return ValidationResult.Invalid("Vui lòng chọn ít nhất 1 ngày làm việc trong tuần")
        }

        val openMinutes = parseTimeToMinutes(schedule.openTime)
            ?: return ValidationResult.Invalid("Định dạng giờ mở cửa không hợp lệ (HH:mm)")
        val closeMinutes = parseTimeToMinutes(schedule.closeTime)
            ?: return ValidationResult.Invalid("Định dạng giờ đóng cửa không hợp lệ (HH:mm)")

        if (closeMinutes <= openMinutes) {
            return ValidationResult.Invalid("Giờ đóng cửa (${schedule.closeTime}) phải sau giờ mở cửa (${schedule.openTime})")
        }

        if (schedule.defaultCapacity <= 0) {
            return ValidationResult.Invalid("Sức chứa mặc định mỗi khung giờ phải lớn hơn 0")
        }

        if (schedule.slotDurationMinutes <= 0) {
            return ValidationResult.Invalid("Thời lượng mỗi khung giờ phải lớn hơn 0 phút")
        }

        val breakStart = schedule.breakStartTime
        val breakEnd = schedule.breakEndTime
        if (!breakStart.isNullOrBlank() && !breakEnd.isNullOrBlank()) {
            val breakStartMin = parseTimeToMinutes(breakStart)
                ?: return ValidationResult.Invalid("Định dạng giờ bắt đầu nghỉ không hợp lệ")
            val breakEndMin = parseTimeToMinutes(breakEnd)
                ?: return ValidationResult.Invalid("Định dạng giờ kết thúc nghỉ không hợp lệ")

            if (breakEndMin <= breakStartMin) {
                return ValidationResult.Invalid("Giờ kết thúc nghỉ ($breakEnd) phải sau giờ bắt đầu nghỉ ($breakStart)")
            }

            if (breakStartMin < openMinutes || breakEndMin > closeMinutes) {
                return ValidationResult.Invalid("Khoảng nghỉ trưa ($breakStart - $breakEnd) phải nằm trong khung giờ mở - đóng cửa ($schedule.openTime - $schedule.closeTime)")
            }
        }

        return ValidationResult.Valid
    }

    companion object {
        fun parseTimeToMinutes(timeStr: String): Int? {
            val parts = timeStr.trim().split(":")
            if (parts.size != 2) return null
            val hour = parts[0].toIntOrNull() ?: return null
            val minute = parts[1].toIntOrNull() ?: return null
            if (hour !in 0..23 || minute !in 0..59) return null
            return hour * 60 + minute
        }

        fun formatMinutesToTime(minutes: Int): String {
            val h = minutes / 60
            val m = minutes % 60
            return String.format(Locale.US, "%02d:%02d", h, m)
        }
    }
}

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Tự động phân chia ca làm việc thành danh sách các khung giờ trống (Slots)
 * dựa theo thời lượng slotDurationMinutes và bỏ qua/khóa giờ nghỉ giữa ca.
 */
class ScheduleSlotGenerator {

    fun generateSlots(
        schedule: WorkSchedule,
        date: String,
        shopId: String = schedule.shopId
    ): List<SlotItem> {
        val openMin = ScheduleValidator.parseTimeToMinutes(schedule.openTime) ?: (8 * 60 + 30)
        val closeMin = ScheduleValidator.parseTimeToMinutes(schedule.closeTime) ?: (21 * 60)
        val duration = if (schedule.slotDurationMinutes > 0) schedule.slotDurationMinutes else 60

        val breakStartMin = schedule.breakStartTime?.let { ScheduleValidator.parseTimeToMinutes(it) }
        val breakEndMin = schedule.breakEndTime?.let { ScheduleValidator.parseTimeToMinutes(it) }

        val slots = mutableListOf<SlotItem>()
        var current = openMin
        var index = 1

        while (current + duration <= closeMin) {
            val slotStart = current
            val slotEnd = current + duration

            val isOverlappingBreak = if (breakStartMin != null && breakEndMin != null) {
                // Kiểm tra slot có giao cắt với giờ nghỉ trưa hay không
                slotStart < breakEndMin && slotEnd > breakStartMin
            } else false

            if (!isOverlappingBreak) {
                val startStr = ScheduleValidator.formatMinutesToTime(slotStart)
                val endStr = ScheduleValidator.formatMinutesToTime(slotEnd)

                slots.add(
                    SlotItem(
                        id = "slot_${date.replace("-", "")}_$index",
                        shopId = shopId,
                        date = date,
                        startTime = startStr,
                        endTime = endStr,
                        capacity = schedule.defaultCapacity,
                        booked = 0,
                        isBlocked = false
                    )
                )
                index++
            }

            current += duration
        }

        return slots
    }
}
