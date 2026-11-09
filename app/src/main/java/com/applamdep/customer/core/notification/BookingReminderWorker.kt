package com.applamdep.customer.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * NHẮC LỊCH HẸN TRƯỚC 30 PHÚT BẰNG WORKMANAGER (NHÓM 4 - P1)
 * Hoạt động độc lập trong nền kể cả khi ứng dụng đã đóng hoàn toàn.
 */
class BookingReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "GLOW_BOOKING_REMINDER_CHANNEL"
        const val KEY_SHOP_NAME = "shop_name"
        const val KEY_BOOKING_TIME = "booking_time"
        const val KEY_BOOKING_ID = "booking_id"
    }

    override suspend fun doWork(): Result {
        val shopName = inputData.getString(KEY_SHOP_NAME) ?: "Salon làm đẹp"
        val bookingTime = inputData.getString(KEY_BOOKING_TIME) ?: ""
        val bookingId = inputData.getString(KEY_BOOKING_ID) ?: ""

        sendReminderNotification(shopName, bookingTime, bookingId)

        return Result.success()
    }

    private fun sendReminderNotification(shopName: String, bookingTime: String, bookingId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Tạo Notification Channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nhắc lịch hẹn làm đẹp",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo nhắc hẹn 30 phút trước giờ làm dịch vụ"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Nhắc lịch hẹn làm đẹp sau 30 phút nữa!")
            .setContentText("Lịch hẹn của bạn tại $shopName sẽ bắt đầu lúc $bookingTime. Vui lòng sắp xếp tới đúng giờ nhé!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Lịch hẹn #$bookingId tại $shopName sẽ bắt đầu lúc $bookingTime. Quý khách vui lòng chuẩn bị di chuyển để được phục vụ tốt nhất!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(bookingId.hashCode(), notification)
    }
}
