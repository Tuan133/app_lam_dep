package com.applamdep.customer.core.config

import android.content.Context
import android.content.SharedPreferences

/**
 * CẤU HÌNH GOOGLE GEMINI API CHO TRỢ LÝ AI GLOW (RAG BEAUTY ASSISTANT)
 *
 * BẠN CÓ THỂ THÊM GEMINI API KEY THEO 2 CÁCH:
 *
 * CÁCH 1: Dán trực tiếp API Key vào biến GEMINI_API_KEY bên dưới.
 * CÁCH 2: Dán trực tiếp trên màn hình ứng dụng: Mở cửa sổ "AI GLOW" -> Bấm biểu tượng bánh răng (⚙️) ở góc trên.
 *
 * Lấy API Key miễn phí tại: https://aistudio.google.com/app/apikey
 */
object AiConfig {

    // =====================================================================================
    // 👉👉👉 CHỖ ĐỂ BẠN THÊM GEMINI API KEY VÀO ĐÂY: 👈👈👈
    // Thay thế chuỗi rỗng bên dưới bằng API Key của bạn (bắt đầu bằng AIzaSy...)
    // =====================================================================================
    const val GEMINI_API_KEY: String = ""

    // Model Gemini được sử dụng (Mặc định: gemini-flash-lite-latest tốc độ cao < 1s, thông minh, hỗ trợ context lớn)
    const val GEMINI_MODEL: String = "gemini-flash-lite-latest"

    // Danh sách model ưu tiên tự động fallback nếu một model bị Google thay đổi/hết hạn
    val CANDIDATE_MODELS: List<String> = listOf(
        "gemini-flash-lite-latest",
        "gemini-3.5-flash-lite",
        "gemini-flash-latest",
        "gemini-pro-latest",
        "gemini-1.5-flash"
    )

    // Tên file SharedPreferences lưu key nhập từ màn hình app
    private const val PREF_NAME = "glow_ai_config"
    private const val KEY_USER_API_KEY = "user_gemini_api_key"

    /**
     * Lấy API Key có hiệu lực:
     * Ưu tiên 1: Key do người dùng/bạn dán trực tiếp trên màn hình app (lưu trong máy)
     * Ưu tiên 2: Key gán trực tiếp trong biến GEMINI_API_KEY ở file này
     */
    fun getEffectiveApiKey(context: Context): String {
        val userSavedKey = getUserApiKey(context)
        if (userSavedKey.isNotBlank()) {
            return userSavedKey
        }
        return GEMINI_API_KEY.trim()
    }

    /**
     * Kiểm tra xem đã có API Key hay chưa
     */
    fun hasApiKey(context: Context): Boolean {
        return getEffectiveApiKey(context).isNotBlank()
    }

    /**
     * Lưu API Key từ giao diện Cài đặt của chat AI
     */
    fun saveUserApiKey(context: Context, apiKey: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_USER_API_KEY, apiKey.trim()).apply()
    }

    /**
     * Đọc API Key đã lưu từ giao diện
     */
    fun getUserApiKey(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_API_KEY, "")?.trim() ?: ""
    }

    /**
     * Xóa API Key đã lưu
     */
    fun clearUserApiKey(context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_USER_API_KEY).apply()
    }
}

