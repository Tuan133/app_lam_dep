package com.applamdep.customer.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * DỊCH VỤ NHẬN THÔNG BÁO TỪ FIREBASE CLOUD MESSAGING (FCM) (P1)
 * Nhận thông báo xác nhận booking, chuyển cọc HELD hoặc hoàn cọc REFUNDED
 */
class CustomerFcmService : FirebaseMessagingService() {

    companion object {
        const val FCM_CHANNEL_ID = "GLOW_FCM_CHANNEL"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Thông báo từ Glow Beauty"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Bạn có cập nhật mới về lịch hẹn."

        showNotification(title, body)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Lưu token lên Firestore users/{userId}/fcmToken khi có backend thật
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FCM_CHANNEL_ID,
                "Thông báo dịch vụ sàn",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, FCM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
