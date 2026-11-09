package com.applamdep.merchant.core.base

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
 * Lớp cha trừu tượng BaseViewModel cung cấp cơ chế quản lý trạng thái UiState,
 * coroutine scope an toàn và xử lý lỗi đồng nhất cho mọi ViewModel của Merchant Portal.
 *
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * MutableStateFlow được giữ private, chỉ công khai StateFlow chỉ đọc ra ngoài UI.
 */
abstract class BaseViewModel<T>(
    initialState: UiState<T> = UiState.Idle
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState<T>> = MutableStateFlow(initialState)
    val uiState: StateFlow<UiState<T>> = _uiState.asStateFlow()

    protected fun updateState(newState: UiState<T>) {
        _uiState.value = newState
    }

    protected val currentState: UiState<T>
        get() = _uiState.value

    /**
     * Bọc Coroutine xử lý tự động bật Loading và bắt ngoại lệ đưa vào Error state
     */
    protected fun launchWithLoading(
        showLoadingState: Boolean = true,
        onError: ((Throwable) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            updateState(UiState.Error(throwable.localizedMessage ?: "Đã xảy ra lỗi không xác định", throwable))
            onError?.invoke(throwable)
        }

        viewModelScope.launch(handler) {
            if (showLoadingState) {
                updateState(UiState.Loading)
            }
            block()
        }
    }
}
