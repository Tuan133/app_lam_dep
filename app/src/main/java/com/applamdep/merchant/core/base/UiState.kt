package com.applamdep.merchant.core.base

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Đóng gói các trạng thái phản hồi của giao diện UI:
 * Khởi tạo (Idle), Đang tải dữ liệu (Loading), Thành công (Success), Thất bại (Error)
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}
