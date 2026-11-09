package com.applamdep.customer.core.ai

import com.example.app_lam_dep.model.AiShopRecommendation

/**
 * THUẬT TOÁN RAG RETRIEVER (RETRIEVAL-AUGMENTED GENERATION)
 * Tiếp nhận truy vấn tự nhiên của khách hàng, phân tích ngữ nghĩa và trích xuất
 * các tài liệu Salon phù hợp nhất từ Tập tri thức (Knowledge Base) của file Excel.
 */
object AiRagRetriever {

    data class RetrievalResult(
        val matchedSalons: List<SalonKnowledgeDoc>,
        val recommendations: List<AiShopRecommendation>,
        val ragContextPrompt: String,
        val detectedIntent: String
    )

    /**
     * Thực hiện RAG Retrieval từ câu hỏi người dùng
     */
    fun retrieve(query: String): RetrievalResult {
        val q = query.lowercase().trim()

        // Phân tích ý định (Intent Detection)
        val isNoDepositIntent = q.contains("không cần cọc") || q.contains("không cọc") || 
                                q.contains("miễn cọc") || q.contains("giữ chỗ 0đ") || q.contains("0đ")
        val isLateNightIntent = q.contains("muộn") || q.contains("tối") || q.contains("20h") || 
                                q.contains("21h") || q.contains("chiều tối")
        val isNailIntent = q.contains("nail") || q.contains("móng") || q.contains("sơn gel") || 
                           q.contains("mắt mèo") || q.contains("đính đá")
        val isHairIntent = q.contains("tóc") || q.contains("hair") || q.contains("cắt") || 
                           q.contains("uốn") || q.contains("nhuộm") || q.contains("sóng lơi")
        val isSpaIntent = q.contains("spa") || q.contains("gội đầu") || q.contains("dưỡng sinh") || 
                          q.contains("massage") || q.contains("thư giãn") || q.contains("cổ vai gáy")
        val isAcneIntent = q.contains("mụn") || q.contains("trị mụn") || q.contains("da liễu") || 
                           q.contains("nặn mụn") || q.contains("thải độc")
        val isMakeupIntent = q.contains("makeup") || q.contains("trang điểm") || q.contains("cô dâu") || 
                             q.contains("dự tiệc") || q.contains("tiệc")

        // Chấm điểm mức độ liên quan (Relevance Scoring)
        val scoredList = SalonKnowledgeBase.salons.map { salon ->
            var score = 0

            // 1. Khớp tên tiệm trực tiếp (Trọng số cao nhất)
            if (q.contains(salon.name.lowercase()) || salon.name.lowercase().contains(q)) {
                score += 50
            }

            // 2. Khớp thể loại chính
            if (isNailIntent && salon.category == "Nail") score += 20
            if (isHairIntent && salon.category == "Hair") score += 20
            if (isSpaIntent && salon.category == "Spa") score += 20
            if (isAcneIntent && salon.category == "Skincare") score += 25
            if (isMakeupIntent && salon.category == "Makeup") score += 25

            // 3. Khớp quận huyện
            val districtNorm = salon.district.lowercase()
            if (q.contains(districtNorm) || q.contains(districtNorm.replace(" ", ""))) {
                score += 15
            }

            // 4. Khớp ý định chính sách cọc
            if (isNoDepositIntent && !salon.requireDeposit) {
                score += 30
            } else if (!isNoDepositIntent && salon.requireDeposit && q.contains("có cọc")) {
                score += 10
            }

            // 5. Khớp mở cửa muộn
            if (isLateNightIntent && salon.closeHourInt >= 21) {
                score += 20
            } else if (isLateNightIntent && salon.closeHourInt >= 20) {
                score += 10
            }

            // 6. Khớp từ khóa tìm kiếm
            for (kw in salon.searchKeywords) {
                if (q.contains(kw)) {
                    score += 5
                }
            }

            // 7. Điểm uy tín / Rating
            score += (salon.rating * 2).toInt()

            Pair(salon, score)
        }

        // Lấy top 3 salon có điểm số cao nhất
        val rankedSalons = scoredList.sortedByDescending { it.second }.map { it.first }.take(3)
        val finalSalons = if (rankedSalons.isNotEmpty()) rankedSalons else SalonKnowledgeBase.salons.take(3)

        // Tạo danh sách Card gợi ý UI
        val recommendations = finalSalons.map { s ->
            AiShopRecommendation(
                id = s.id,
                salonName = s.name,
                serviceSuggestion = s.highlightService,
                rating = s.rating,
                distance = "${s.district} • Giờ: ${s.openingHours.split("(").first().trim()}",
                requiresDeposit = s.requireDeposit,
                depositInfo = s.depositInfo,
                matchReason = s.specialty
            )
        }

        // Xây dựng Context Chunk (Ngữ cảnh RAG) để gửi vào Gemini LLM
        val contextBuilder = StringBuilder()
        contextBuilder.append("THÔNG TIN THỰC TẾ TRÍCH XUẤT TỪ DỮ LIỆU EXCEL (KNOWLEDGE BASE):\n")
        finalSalons.forEachIndexed { idx, s ->
            contextBuilder.append("${idx + 1}. Tên tiệm: ${s.name}\n")
            contextBuilder.append("   - Chuyên môn: ${s.specialty}\n")
            contextBuilder.append("   - Dịch vụ nổi bật: ${s.highlightService} (Giá tham khảo: ${s.priceRange})\n")
            contextBuilder.append("   - Địa chỉ: ${s.address} (${s.district})\n")
            contextBuilder.append("   - Hotline: ${s.phone} | Giờ làm việc: ${s.openingHours}\n")
            contextBuilder.append("   - Đánh giá: ${s.rating}★ (${s.reviewCount} lượt đánh giá)\n")
            contextBuilder.append("   - Chính sách cọc: ${s.depositInfo}\n")
            if (s.facebook.isNotBlank()) contextBuilder.append("   - Facebook: ${s.facebook}\n")
            if (s.tiktok.isNotBlank()) contextBuilder.append("   - TikTok: ${s.tiktok}\n")
            if (s.instagram.isNotBlank()) contextBuilder.append("   - Instagram: ${s.instagram}\n")
            if (s.website.isNotBlank()) contextBuilder.append("   - Website: ${s.website}\n")
            contextBuilder.append("\n")
        }

        val intent = when {
            isNoDepositIntent -> "Tìm tiệm không cọc"
            isLateNightIntent -> "Tìm tiệm mở muộn"
            isNailIntent -> "Dịch vụ Nail"
            isHairIntent -> "Dịch vụ Làm tóc"
            isSpaIntent -> "Dịch vụ Gội đầu/Spa"
            isAcneIntent -> "Dịch vụ Trị mụn/Da"
            isMakeupIntent -> "Dịch vụ Makeup"
            else -> "Tư vấn tổng quát"
        }

        return RetrievalResult(
            matchedSalons = finalSalons,
            recommendations = recommendations,
            ragContextPrompt = contextBuilder.toString(),
            detectedIntent = intent
        )
    }
}

