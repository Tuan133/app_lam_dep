package com.applamdep.customer.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class RealtimeChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderRole: String, // "CUSTOMER" or "MERCHANT"
    val senderName: String,
    val text: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis()
)

object RealtimeChatRepository {
    private const val PREF_NAME = "realtime_chat_prefs"
    private const val KEY_MESSAGES = "chat_messages_map"
    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    private val defaultSeedMessages: Map<String, List<RealtimeChatMessage>> = mapOf(
        "salon_01" to listOf(
            RealtimeChatMessage(
                conversationId = "salon_01",
                senderRole = "MERCHANT",
                senderName = "1994 Nailsroom",
                text = "Dạ chào bạn, lịch hẹn chiều nay bên em đã chuẩn bị phòng dịch vụ sẵn sàng đón tiếp bạn rồi ạ! ✨",
                time = "09:47",
                timestamp = System.currentTimeMillis() - (6 * 3600 * 1000L)
            ),
            RealtimeChatMessage(
                conversationId = "salon_01",
                senderRole = "CUSTOMER",
                senderName = "Khánh Linh",
                text = "Cảm ơn shop nhiều nhé, mình sẽ ghé đúng giờ ạ.",
                time = "09:50",
                timestamp = System.currentTimeMillis() - (5 * 3600 * 1000L)
            )
        ),
        "salon_02" to listOf(
            RealtimeChatMessage(
                conversationId = "salon_02",
                senderRole = "MERCHANT",
                senderName = "BONNIE NAIL",
                text = "Dạ em gửi bạn bảng mẫu vẽ móng ombre pastel hot trend tuần này ạ ✨",
                time = "Hôm qua",
                timestamp = System.currentTimeMillis() - (24 * 3600 * 1000L)
            )
        ),
        "salon_09" to listOf(
            RealtimeChatMessage(
                conversationId = "salon_09",
                senderRole = "MERCHANT",
                senderName = "Thì Là SPA",
                text = "Chào bạn! Thì Là SPA có ưu đãi gội đầu dưỡng sinh bồ kết tươi giảm 30K hôm nay ạ 🌿",
                time = "Hôm qua",
                timestamp = System.currentTimeMillis() - (28 * 3600 * 1000L)
            )
        )
    )

    private val _messagesFlow = MutableStateFlow<Map<String, List<RealtimeChatMessage>>>(defaultSeedMessages)
    val messagesFlow: StateFlow<Map<String, List<RealtimeChatMessage>>> = _messagesFlow.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            loadFromStorage()
        }
    }

    private fun loadFromStorage() {
        val json = prefs?.getString(KEY_MESSAGES, null)
        if (!json.isNullOrBlank()) {
            try {
                val type = object : TypeToken<Map<String, List<RealtimeChatMessage>>>() {}.type
                val saved: Map<String, List<RealtimeChatMessage>>? = gson.fromJson(json, type)
                if (!saved.isNullOrEmpty()) {
                    val merged = defaultSeedMessages.toMutableMap()
                    saved.forEach { (k, v) ->
                        if (v.isNotEmpty()) merged[k] = v
                    }
                    _messagesFlow.value = merged
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _messagesFlow.value = defaultSeedMessages
    }

    private fun saveToStorage() {
        prefs?.edit()?.putString(KEY_MESSAGES, gson.toJson(_messagesFlow.value))?.apply()
    }

    fun getConversationId(targetName: String): String {
        return when {
            targetName.contains("1994", true) || targetName.contains("L'Amour", true) || targetName.contains("Linh", true) -> "salon_01"
            targetName.contains("BONNIE", true) -> "salon_02"
            targetName.contains("GÀ", true) || targetName.contains("Ga Spa", true) -> "salon_03"
            targetName.contains("Thì Là", true) -> "salon_09"
            else -> "salon_01"
        }
    }

    fun getMessages(convId: String): List<RealtimeChatMessage> {
        return _messagesFlow.value[convId] ?: emptyList()
    }

    fun getLatestMessage(convId: String): RealtimeChatMessage? {
        return _messagesFlow.value[convId]?.lastOrNull()
    }

    fun sendMessage(
        conversationId: String,
        senderRole: String,
        senderName: String,
        text: String,
        autoReply: Boolean = true
    ) {
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = RealtimeChatMessage(
            conversationId = conversationId,
            senderRole = senderRole,
            senderName = senderName,
            text = text,
            time = timeNow
        )

        val currentMap = _messagesFlow.value.toMutableMap()
        val currentList = (currentMap[conversationId] ?: emptyList()).toMutableList()
        currentList.add(newMsg)
        currentMap[conversationId] = currentList
        _messagesFlow.value = currentMap
        saveToStorage()

        // Phản hồi tự động Realtime từ Salon nếu tin nhắn gửi từ Khách hàng
        if (senderRole == "CUSTOMER" && autoReply) {
            CoroutineScope(Dispatchers.IO).launch {
                delay(1200)
                val replyText = generateSalonReply(conversationId, text)
                val replyTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val replyMsg = RealtimeChatMessage(
                    conversationId = conversationId,
                    senderRole = "MERCHANT",
                    senderName = if (conversationId == "salon_01") "1994 Nailsroom" else "Salon Đối Tác",
                    text = replyText,
                    time = replyTime
                )

                val updatedMap = _messagesFlow.value.toMutableMap()
                val updatedList = (updatedMap[conversationId] ?: emptyList()).toMutableList()
                updatedList.add(replyMsg)
                updatedMap[conversationId] = updatedList
                _messagesFlow.value = updatedMap
                saveToStorage()
            }
        }
    }

    private fun generateSalonReply(convId: String, customerMsg: String): String {
        val q = customerMsg.lowercase()
        return when {
            q.contains("hi") || q.contains("chào") || q.contains("hello") || q.contains("alo") ->
                "Dạ 1994 Nailsroom xin chào bạn! ✨ Cảm ơn bạn đã nhắn tin cho salon. Tiệm có thể hỗ trợ gì cho lịch hẹn của bạn chiều nay ạ? 💅"
            q.contains("giá") || q.contains("nhiêu") || q.contains("cost") || q.contains("tiền") ->
                "Dạ bảng giá bên em được niêm yết rõ ràng trên app: Sơn Gel Thạch 160K, Mắt Mèo VIP 250K. Giá đã bao gồm cắt da sạch sẽ và không phụ thu ạ! 💕"
            q.contains("địa chỉ") || q.contains("ở đâu") || q.contains("chỗ") || q.contains("vị trí") ->
                "Dạ cơ sở của bên em ở tại: 172/5 Trần Phú, Phường Hải Châu 1, Quận Hải Châu, Đà Nẵng ạ (ngay gần cầu Rồng, có chỗ đỗ ô tô thoải mái) 📍"
            q.contains("mẫu") || q.contains("nail") || q.contains("móng") ->
                "Dạ tiệm có sẵn hơn 200 mẫu nail box thiết kế và bảng màu sơn thạch Hàn Quốc mới về. Bạn có thể gửi ảnh mẫu móng bạn thích qua đây để thợ chuẩn bị trước nha! 💖"
            q.contains("giờ") || q.contains("trễ") || q.contains("sớm") || q.contains("hẹn") ->
                "Dạ salon đã ghi nhận thông tin lịch hẹn của bạn và giữ chỗ ưu tiên trong 30 phút. Rất vui được đón tiếp bạn chiều nay ạ! ⏰✨"
            else ->
                "Dạ 1994 Nailsroom đã nhận được tin nhắn của bạn: \"$customerMsg\". Nhân viên quản lý salon đang xem và sẽ tư vấn chi tiết cho bạn ngay ạ! ✨"
        }
    }
}

