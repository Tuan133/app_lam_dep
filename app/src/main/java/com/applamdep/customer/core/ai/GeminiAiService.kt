package com.applamdep.customer.core.ai

import android.content.Context
import com.applamdep.customer.core.config.AiConfig
import com.example.app_lam_dep.model.AiShopRecommendation
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * SERVICE KẾT NỐI GOOGLE GEMINI API VÀ HỆ THỐNG RAG
 * - Tự động trích xuất ngữ cảnh RAG từ Excel
 * - Gửi prompt đến Google Gemini (gemini-1.5-flash)
 * - Tự động fallback thông minh sang Local RAG khi chưa nhập Key hoặc mất mạng
 */
class GeminiAiService(private val context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    data class AiResponse(
        val replyText: String,
        val recommendations: List<AiShopRecommendation>,
        val isGeminiLive: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Xử lý câu hỏi của khách hàng bằng mô hình RAG + Gemini
     */
    suspend fun askAiGlow(userQuery: String): AiResponse = withContext(Dispatchers.IO) {
        // 1. Bước R (Retrieval): Trích xuất thông tin liên quan từ Knowledge Base Excel
        val ragResult = AiRagRetriever.retrieve(userQuery)

        val apiKey = AiConfig.getEffectiveApiKey(context)

        // 2. Nếu chưa có API Key: Dùng bộ Local RAG Engine
        if (apiKey.isBlank()) {
            val localReply = generateLocalRagResponse(userQuery, ragResult)
            return@withContext AiResponse(
                replyText = localReply,
                recommendations = ragResult.recommendations,
                isGeminiLive = false
            )
        }

        // 3. Bước AG (Augmented Generation): Gửi ngữ cảnh RAG vào Gemini LLM
        try {
            val systemInstruction = """
                Bạn là Trợ lý AI GLOW - chuyên gia tư vấn làm đẹp và đặt lịch salon thượng lưu của ứng dụng Glow Prestige.
                Phong cách: Tinh tế, ấm áp, chu đáo, xưng "em" và gọi khách là "bạn" hoặc "quý khách".
                
                Nhiệm vụ:
                1. Dựa trên THÔNG TIN THỰC TẾ (Knowledge Base trích xuất từ dữ liệu Excel của hệ thống) dưới đây, hãy trả lời câu hỏi của khách hàng một cách chính xác, ngắn gọn, hấp dẫn (tối đa 3-4 câu).
                2. Nêu bật tên salon, địa chỉ, giờ mở cửa, số hotline và chính sách cọc (có cọc hay miễn cọc).
                3. Hướng dẫn khách hàng có thể bấm nút "Xem Shop" hoặc "Đặt lịch ngay" bên dưới để được giữ chỗ 100%.
                
                ${ragResult.ragContextPrompt}
            """.trimIndent()

            val fullPrompt = "$systemInstruction\n\nCâu hỏi của khách: $userQuery"

            val requestJson = JsonObject().apply {
                val contentsArray = com.google.gson.JsonArray()
                val contentObj = JsonObject()
                val partsArray = com.google.gson.JsonArray()
                val partObj = JsonObject()
                partObj.addProperty("text", fullPrompt)
                partsArray.add(partObj)
                contentObj.add("parts", partsArray)
                contentsArray.add(contentObj)
                add("contents", contentsArray)

                val generationConfig = JsonObject().apply {
                    addProperty("temperature", 0.6)
                    addProperty("maxOutputTokens", 500)
                }
                add("generationConfig", generationConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            // Thử lần lượt các model Gemini (tự động fallback nếu model bị Google cập nhật/đổi tên)
            var lastResponseCode = 0
            for (modelName in AiConfig.CANDIDATE_MODELS) {
                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/${modelName}:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(endpoint)
                    .post(requestBody)
                    .build()

                try {
                    val response = httpClient.newCall(request).execute()
                    lastResponseCode = response.code
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                        val parsed = gson.fromJson(responseBody, JsonObject::class.java)
                        val candidates = parsed.getAsJsonArray("candidates")
                        if (candidates != null && candidates.size() > 0) {
                            val firstCandidate = candidates.get(0).asJsonObject
                            val content = firstCandidate.getAsJsonObject("content")
                            val parts = content.getAsJsonArray("parts")
                            if (parts != null && parts.size() > 0) {
                                val aiText = parts.get(0).asJsonObject.get("text").asString.trim()
                                return@withContext AiResponse(
                                    replyText = aiText,
                                    recommendations = ragResult.recommendations,
                                    isGeminiLive = true
                                )
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Tiếp tục thử model kế tiếp
                }
            }

            // Nếu tất cả các model đều trả về lỗi (ví dụ Sai Key, Quota), fallback sang Local RAG
            val fallbackReply = generateLocalRagResponse(userQuery, ragResult) + 
                "\n\n*(Lưu ý: Đang dùng phản hồi RAG cục bộ do Gemini API phản hồi mã: $lastResponseCode)*"
            return@withContext AiResponse(
                replyText = fallbackReply,
                recommendations = ragResult.recommendations,
                isGeminiLive = false,
                errorMessage = "Gemini Code: $lastResponseCode"
            )

        } catch (e: Exception) {
            val fallbackReply = generateLocalRagResponse(userQuery, ragResult)
            return@withContext AiResponse(
                replyText = fallbackReply,
                recommendations = ragResult.recommendations,
                isGeminiLive = false,
                errorMessage = e.message
            )
        }
    }

    /**
     * Bộ sinh phản hồi RAG cục bộ thông minh (chạy offline / khi chưa cấu hình Key)
     */
    private fun generateLocalRagResponse(
        query: String,
        ragResult: AiRagRetriever.RetrievalResult
    ): String {
        val topSalon = ragResult.matchedSalons.firstOrNull() ?: SalonKnowledgeBase.salons.first()
        val otherSalons = ragResult.matchedSalons.drop(1)

        val sb = StringBuilder()
        sb.append("Dạ em chào bạn! Trợ lý AI GLOW ✨ đã phân tích câu hỏi của bạn:\n\n")

        when (ragResult.detectedIntent) {
            "Tìm tiệm không cọc" -> {
                sb.append("Dạ đây là các salon tiêu biểu hỗ trợ **MIỄN CỌC 0Đ (Giữ chỗ miễn phí)** phù hợp nhất:\n")
                sb.append("💎 **${topSalon.name}** tại ${topSalon.address} (Mở cửa: ${topSalon.openingHours}). Hotline: ${topSalon.phone}.\n")
                sb.append("✨ Nổi bật với: ${topSalon.specialty}.")
            }
            "Tìm tiệm mở muộn" -> {
                sb.append("Dạ bạn cần làm đẹp buổi tối muộn, em xin gợi ý salon mở cửa đến 21h:\n")
                sb.append("💎 **${topSalon.name}** (${topSalon.address}) mở cửa đến **${topSalon.closeHourInt}h**. Dịch vụ nổi bật: ${topSalon.highlightService}.\n")
            }
            "Dịch vụ Nail" -> {
                sb.append("Về dịch vụ làm móng nghệ thuật, em gợi ý bạn ghé thăm **${topSalon.name}** (${topSalon.address}):\n")
                sb.append("💅 ${topSalon.specialty}. Điểm đánh giá: ${topSalon.rating}★ (${topSalon.reviewCount} đánh giá).\n")
                sb.append("📌 Chính sách cọc: ${topSalon.depositInfo}.")
            }
            "Dịch vụ Gội đầu/Spa" -> {
                sb.append("Về thư giãn và gội đầu dưỡng sinh, salon lý tưởng nhất là **${topSalon.name}** (${topSalon.address}):\n")
                sb.append("🌿 ${topSalon.specialty}. Dịch vụ khuyên dùng: ${topSalon.highlightService}.\n")
                sb.append("📌 Đặt lịch: ${topSalon.phone} (Mở cửa ${topSalon.openingHours}).")
            }
            "Dịch vụ Trị mụn/Da" -> {
                sb.append("Về điều trị mụn và phục hồi da chuyên sâu chuẩn y khoa:\n")
                sb.append("💆‍♀️ **${topSalon.name}** tại ${topSalon.address}.\n")
                sb.append("✨ ${topSalon.specialty} (${topSalon.depositInfo}).")
            }
            "Dịch vụ Làm tóc" -> {
                sb.append("Về tạo kiểu tóc thời thượng và uốn sóng lơi:\n")
                sb.append("✂️ **${topSalon.name}** tại ${topSalon.address} (${topSalon.district}).\n")
                sb.append("✨ ${topSalon.specialty}. Hotline: ${topSalon.phone}.")
            }
            "Dịch vụ Makeup" -> {
                sb.append("Về trang điểm cô dâu và dự tiệc chuyên nghiệp:\n")
                sb.append("💄 **${topSalon.name}** tại ${topSalon.address}.\n")
                sb.append("✨ ${topSalon.specialty}. Đánh giá: ${topSalon.rating}★.")
            }
            else -> {
                sb.append("Em đã chọn lọc được salon xuất sắc nhất theo yêu cầu của bạn là **${topSalon.name}** tại ${topSalon.address}.\n")
                sb.append("✨ Chuyên môn: ${topSalon.specialty}.\n")
                sb.append("⏰ Giờ hoạt động: ${topSalon.openingHours} | Hotline: ${topSalon.phone}.")
            }
        }

        if (otherSalons.isNotEmpty()) {
            sb.append("\n\nNgoài ra, bạn cũng có thể tham khảo thêm: ")
            sb.append(otherSalons.joinToString(", ") { "**${it.name}** (${it.district})" })
            sb.append(".")
        }

        sb.append("\n\n👉 Bạn hãy bấm **\"Xem Shop\"** để khám phá chi tiết hoặc bấm **\"Đặt lịch ngay\"** để em giữ chỗ nhé!")

        // Nếu chưa cài key, thêm hướng dẫn nhẹ
        if (!AiConfig.hasApiKey(context)) {
            sb.append("\n\n💡 *(Mẹo: Bấm biểu tượng ⚙️ ở góc trên để dán Google Gemini API Key và kích hoạt trí tuệ nhân tạo Gemini 1.5 Flash)*")
        }

        return sb.toString()
    }
}

