package com.applamdep.customer.core.base

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * State đại diện cho trạng thái của màn hình (Loading, Thành công, Lỗi),
 * đóng gói toàn bộ dữ liệu phản hồi từ logic nghiệp vụ để UI chỉ việc render.
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String, val throwable: Throwable? = null) : UiState<Nothing>
}
