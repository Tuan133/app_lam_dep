package com.applamdep.customer.core.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Helper hỗ trợ lên lịch nhắc nhở 30 phút trước hẹn bằng WorkManager
 */
object NotificationHelper {

    /**
     * Lên lịch thông báo trước 30 phút
     */
    fun scheduleBookingReminder(
        context: Context,
        bookingId: String,
        shopName: String,
        bookingTime: String,
        appointmentTimestamp: Long
    ) {
        val thirtyMinutesInMillis = 30 * 60 * 1000L
        val triggerTime = appointmentTimestamp - thirtyMinutesInMillis
        val delayMillis = triggerTime - System.currentTimeMillis()

        // Nếu còn hơn 30 phút thì lên lịch, nếu còn ít hơn hoặc đã qua thì nhắc ngay sau 5 giây
        val effectiveDelay = if (delayMillis > 0) delayMillis else 5000L

        val inputData = Data.Builder()
            .putString(BookingReminderWorker.KEY_BOOKING_ID, bookingId)
            .putString(BookingReminderWorker.KEY_SHOP_NAME, shopName)
            .putString(BookingReminderWorker.KEY_BOOKING_TIME, bookingTime)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<BookingReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(effectiveDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_$bookingId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}
