package com.applamdep.customer.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE)
 * BaseViewModel là lớp cha chứa logic dùng chung cho tất cả các ViewModel trong app
 * (quản lý CoroutineScope, xử lý Exception, emit Loading/Error).
 *
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * StateFlow được đóng gói: _uiState là private MutableStateFlow, chỉ ViewModel mới có quyền ghi.
 * Bên ngoài UI chỉ quan sát thông qua StateFlow (read-only) công khai.
 */
abstract class BaseViewModel<T> : ViewModel() {

    // Đóng gói trạng thái nội bộ
    private val _uiState = MutableStateFlow<UiState<T>>(UiState.Idle)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    // Exception Handler chung cho Coroutines
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Cập nhật trạng thái mới
     */
    protected fun updateState(newState: UiState<T>) {
        _uiState.value = newState
    }

    /**
     * Helper thực thi suspend function có tự động bật Loading và bắt Lỗi
     */
    protected fun launchWithLoading(block: suspend CoroutineScope.() -> Unit) {
        _uiState.value = UiState.Loading
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    /**
     * Kế thừa cho phép các ViewModel con override để xử lý mã lỗi cụ thể nếu cần
     */
    protected open fun handleError(throwable: Throwable) {
        _uiState.value = UiState.Error(
            message = throwable.localizedMessage ?: "Đã có lỗi không xác định xảy ra",
            throwable = throwable
        )
    }
}
