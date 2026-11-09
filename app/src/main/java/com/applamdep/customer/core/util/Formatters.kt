package com.applamdep.customer.core.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tiện ích định dạng tiền tệ và thời gian
 */
object CurrencyFormatter {
    private val vietnameseFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).apply {
        maximumFractionDigits = 0
    }

    fun formatVnd(amount: Long): String {
        return vietnameseFormat.format(amount).replace("₫", "đ")
    }
}

object DateTimeFormatter {
    private val displayFormat = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
    private val timeOnlyFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatDateTime(timestampMillis: Long): String = displayFormat.format(Date(timestampMillis))
    fun formatTime(timestampMillis: Long): String = timeOnlyFormat.format(Date(timestampMillis))
    fun formatDate(timestampMillis: Long): String = dateOnlyFormat.format(Date(timestampMillis))
}
