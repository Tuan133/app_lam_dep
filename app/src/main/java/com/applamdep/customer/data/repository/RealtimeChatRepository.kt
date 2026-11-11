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
    private const val KEY_READ_COUNTS = "chat_read_counts_map"
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
        ),
        "salon_glamour" to listOf(
            RealtimeChatMessage(
                conversationId = "salon_glamour",
                senderRole = "MERCHANT",
                senderName = "The Glamour Hair Salon",
                text = "Dạ salon có thể phục vụ làm tóc vào khung giờ 16:30 chiều nay ạ.",
                time = "Thứ 6",
                timestamp = System.currentTimeMillis() - (48 * 3600 * 1000L)
            )
        ),
        "cust_mai_phuong" to listOf(
            RealtimeChatMessage(
                conversationId = "cust_mai_phuong",
                senderRole = "MERCHANT",
                senderName = "1994 Nailsroom",
                text = "Chào Mai Phương ạ, tiệm có thể hỗ trợ tư vấn đặt lịch làm nail giúp bạn nha 💅",
                time = "10:12",
                timestamp = System.currentTimeMillis() - (4 * 3600 * 1000L)
            ),
            RealtimeChatMessage(
                conversationId = "cust_mai_phuong",
                senderRole = "CUSTOMER",
                senderName = "Mai Phương",
                text = "Dạ cho em hỏi tiệm còn nhận slot làm nail 16:30 không ạ?",
                time = "10:15",
                timestamp = System.currentTimeMillis() - (3 * 3600 * 1000L)
            )
        ),
        "cust_thu_ha" to listOf(
            RealtimeChatMessage(
                conversationId = "cust_thu_ha",
                senderRole = "CUSTOMER",
                senderName = "Thu Hà",
                text = "Móng làm xinh lắm shop ơi, giữ màu bền rất thích ạ! ✨",
                time = "Hôm qua",
                timestamp = System.currentTimeMillis() - (22 * 3600 * 1000L)
            ),
            RealtimeChatMessage(
                conversationId = "cust_thu_ha",
                senderRole = "MERCHANT",
                senderName = "1994 Nailsroom",
                text = "Dạ tiệm cảm ơn Thu Hà nhiều ạ! Lần tới ghé lại để bên em chăm sóc tiếp cho mình nhé 💖",
                time = "Hôm qua",
                timestamp = System.currentTimeMillis() - (21 * 3600 * 1000L)
            )
        ),
        "cust_thao_my" to listOf(
            RealtimeChatMessage(
                conversationId = "cust_thao_my",
                senderRole = "CUSTOMER",
                senderName = "Thảo My",
                text = "Chiều nay em qua nối mi sợi lụa, tiệm chuẩn bị giúp em keo ít cay nhé ạ.",
                time = "08:30",
                timestamp = System.currentTimeMillis() - (7 * 3600 * 1000L)
            ),
            RealtimeChatMessage(
                conversationId = "cust_thao_my",
                senderRole = "MERCHANT",
                senderName = "1994 Nailsroom",
                text = "Dạ tiệm đã ghi chú chuẩn bị dòng keo dịu nhẹ cho mắt nhạy cảm của chị Thảo My rồi ạ!",
                time = "08:35",
                timestamp = System.currentTimeMillis() - (6 * 3600 * 1000L)
            )
        )
    )

    private val defaultReadCounts: Map<String, Int> = mapOf(
        "MERCHANT:salon_01" to 1,       // Tin mới từ Khánh Linh (tin thứ 2) chưa đọc -> 1 unread
        "MERCHANT:cust_mai_phuong" to 2, // Đã xem
        "MERCHANT:cust_thu_ha" to 2,     // Đã xem
        "MERCHANT:cust_thao_my" to 2,    // Đã xem
        "CUSTOMER:salon_01" to 2,
        "CUSTOMER:salon_02" to 0,        // 1 tin chào từ BONNIE NAIL chưa đọc
        "CUSTOMER:salon_09" to 1,
        "CUSTOMER:salon_glamour" to 1
    )

    private val _messagesFlow = MutableStateFlow<Map<String, List<RealtimeChatMessage>>>(defaultSeedMessages)
    val messagesFlow: StateFlow<Map<String, List<RealtimeChatMessage>>> = _messagesFlow.asStateFlow()

    private val _readCountsFlow = MutableStateFlow<Map<String, Int>>(defaultReadCounts)
    val readCountsFlow: StateFlow<Map<String, Int>> = _readCountsFlow.asStateFlow()

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
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _messagesFlow.value = defaultSeedMessages
            }
        } else {
            _messagesFlow.value = defaultSeedMessages
        }

        val readJson = prefs?.getString(KEY_READ_COUNTS, null)
        if (!readJson.isNullOrBlank()) {
            try {
                val readType = object : TypeToken<Map<String, Int>>() {}.type
                val savedReads: Map<String, Int>? = gson.fromJson(readJson, readType)
                if (!savedReads.isNullOrEmpty()) {
                    val mergedReads = defaultReadCounts.toMutableMap()
                    mergedReads.putAll(savedReads)
                    _readCountsFlow.value = mergedReads
                    return
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        _readCountsFlow.value = defaultReadCounts
    }

    private fun saveToStorage() {
        prefs?.edit()
            ?.putString(KEY_MESSAGES, gson.toJson(_messagesFlow.value))
            ?.putString(KEY_READ_COUNTS, gson.toJson(_readCountsFlow.value))
            ?.apply()
    }

    private fun readKey(convId: String, viewerRole: String): String = "${viewerRole.uppercase()}:$convId"

    @Synchronized
    fun getUnreadCount(convId: String, viewerRole: String): Int {
        val list = _messagesFlow.value[convId].orEmpty()
        if (list.isEmpty()) return 0
        val readCount = _readCountsFlow.value[readKey(convId, viewerRole)] ?: 0
        if (readCount >= list.size) return 0
        return list.drop(readCount).count { !it.senderRole.equals(viewerRole, ignoreCase = true) }
    }

    @Synchronized
    fun markAsRead(convId: String, viewerRole: String) {
        val total = _messagesFlow.value[convId]?.size ?: 0
        val key = readKey(convId, viewerRole)
        val current = _readCountsFlow.value[key] ?: 0
        if (total > current) {
            val updated = _readCountsFlow.value.toMutableMap()
            updated[key] = total
            _readCountsFlow.value = updated
            saveToStorage()
        }
    }

    @Synchronized
    internal fun resetForTesting() {
        prefs = null
        _messagesFlow.value = defaultSeedMessages
        _readCountsFlow.value = defaultReadCounts
    }

    fun getConversationId(targetName: String): String {
        val clean = targetName.removePrefix("Khách hàng:").trim()
        return when {
            clean.contains("1994", true) || clean.contains("L'Amour", true) || clean.contains("Khánh Linh", true) -> "salon_01"
            clean.contains("BONNIE", true) -> "salon_02"
            clean.contains("GÀ", true) || clean.contains("Ga Spa", true) -> "salon_03"
            clean.contains("Thì Là", true) -> "salon_09"
            clean.contains("Glamour", true) -> "salon_glamour"
            clean.contains("Mai Phương", true) -> "cust_mai_phuong"
            clean.contains("Thu Hà", true) -> "cust_thu_ha"
            clean.contains("Thảo My", true) -> "cust_thao_my"
            else -> {
                val ascii = java.text.Normalizer.normalize(clean, java.text.Normalizer.Form.NFD)
                    .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                    .replace('đ', 'd')
                    .replace('Đ', 'D')
                val slug = ascii.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
                "conv_${slug.ifBlank { "default" }}"
            }
        }
    }

    fun getMessages(convId: String): List<RealtimeChatMessage> {
        return _messagesFlow.value[convId] ?: emptyList()
    }

    fun getLatestMessage(convId: String): RealtimeChatMessage? {
        return _messagesFlow.value[convId]?.lastOrNull()
    }

    @Synchronized
    private fun appendMessageInternal(conversationId: String, msg: RealtimeChatMessage, markSenderRead: Boolean) {
        val currentMap = _messagesFlow.value.toMutableMap()
        val currentList = (currentMap[conversationId] ?: emptyList()).toMutableList()
        currentList.add(msg)
        currentMap[conversationId] = currentList
        _messagesFlow.value = currentMap

        if (markSenderRead) {
            val updatedReads = _readCountsFlow.value.toMutableMap()
            updatedReads[readKey(conversationId, msg.senderRole)] = currentList.size
            _readCountsFlow.value = updatedReads
        }
        saveToStorage()
    }

    fun sendMessage(
        conversationId: String,
        senderRole: String,
        senderName: String,
        text: String,
        autoReply: Boolean = true
    ) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val newMsg = RealtimeChatMessage(
            conversationId = conversationId,
            senderRole = senderRole,
            senderName = senderName,
            text = trimmed,
            time = timeNow
        )

        appendMessageInternal(conversationId, newMsg, markSenderRead = true)

        // Phản hồi tự động Realtime từ Salon nếu tin nhắn gửi từ Khách hàng
        if (senderRole == "CUSTOMER" && autoReply) {
            val salonDisplayName = when (conversationId) {
                "salon_01" -> "1994 Nailsroom"
                "salon_02" -> "BONNIE NAIL"
                "salon_03" -> "GÀ SPA"
                "salon_09" -> "Thì Là SPA"
                "salon_glamour" -> "The Glamour Hair Salon"
                else -> "Salon Đối Tác"
            }
            CoroutineScope(Dispatchers.IO).launch {
                delay(1200)
                val replyText = generateSalonReply(salonDisplayName, trimmed)
                val replyTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                val replyMsg = RealtimeChatMessage(
                    conversationId = conversationId,
                    senderRole = "MERCHANT",
                    senderName = salonDisplayName,
                    text = replyText,
                    time = replyTime
                )
                appendMessageInternal(conversationId, replyMsg, markSenderRead = false)
            }
        }
    }

    private fun generateSalonReply(salonName: String, customerMsg: String): String {
        val q = customerMsg.lowercase()
        return when {
            q.contains("hi") || q.contains("chào") || q.contains("hello") || q.contains("alo") ->
                "Dạ $salonName xin chào bạn! ✨ Cảm ơn bạn đã nhắn tin cho salon. Tiệm có thể hỗ trợ gì cho lịch hẹn của bạn hôm nay ạ? 💅"
            q.contains("giá") || q.contains("nhiêu") || q.contains("cost") || q.contains("tiền") ->
                "Dạ bảng giá tại $salonName được niêm yết rõ ràng trên app: Sơn Gel Thạch 160K, Mắt Mèo Cực Quang 250K. Giá đã bao gồm cắt da sạch sẽ và không phụ thu ạ! 💕"
            q.contains("địa chỉ") || q.contains("ở đâu") || q.contains("chỗ") || q.contains("vị trí") ->
                "Dạ cơ sở $salonName ở tại trung tâm Hải Châu, Đà Nẵng (dễ tìm, có chỗ đỗ ô tô thoải mái) 📍"
            q.contains("mẫu") || q.contains("nail") || q.contains("móng") ->
                "Dạ $salonName có sẵn hơn 200 mẫu thiết kế và bảng màu sơn thạch Hàn Quốc mới về. Bạn có thể gửi ảnh mẫu bạn thích qua đây để thợ chuẩn bị trước nha! 💖"
            q.contains("giờ") || q.contains("trễ") || q.contains("sớm") || q.contains("hẹn") || q.contains("slot") ->
                "Dạ $salonName đã ghi nhận yêu cầu khung giờ của bạn và ưu tiên giữ slot đẹp. Rất vui được đón tiếp bạn ạ! ⏰✨"
            else ->
                "Dạ $salonName đã nhận được tin nhắn của bạn: \"$customerMsg\". Nhân viên tư vấn đang xem và sẽ phản hồi chi tiết cho bạn ngay ạ! ✨"
        }
    }
}

