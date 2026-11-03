package com.applamdep.merchant.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Tiện ích định dạng tiền tệ VNĐ và thời gian dùng chung trong Merchant Portal
 */
object Formatters {

    private val viLocale = Locale.forLanguageTag("vi-VN")

    fun formatCurrency(amount: Long): String {
        val symbols = DecimalFormatSymbols(viLocale).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,###", symbols)
        return "${formatter.format(amount)} đ"
    }

    fun formatDate(timestampMillis: Long, pattern: String = "dd/MM/yyyy"): String {
        val sdf = SimpleDateFormat(pattern, viLocale)
        return sdf.format(Date(timestampMillis))
    }

    fun formatDateTime(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm - dd/MM/yyyy", viLocale)
        return sdf.format(Date(timestampMillis))
    }

    fun formatTime(timestampMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", viLocale)
        return sdf.format(Date(timestampMillis))
    }
}
