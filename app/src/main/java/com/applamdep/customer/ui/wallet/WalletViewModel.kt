package com.applamdep.customer.ui.wallet

import androidx.lifecycle.viewModelScope
import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.TopUpTransaction
import com.applamdep.customer.domain.model.Transaction
import com.applamdep.customer.domain.model.Wallet
import com.applamdep.customer.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WalletData(
    val wallet: Wallet,
    val transactions: List<Transaction>
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (BaseViewModel)
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Chỉ phụ thuộc WalletRepository interface)
 */
class WalletViewModel(
    private val walletRepository: WalletRepository
) : BaseViewModel<WalletData>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI
    private val _pendingTopUpTx = MutableStateFlow<TopUpTransaction?>(null)
    val pendingTopUpTx: StateFlow<TopUpTransaction?> = _pendingTopUpTx.asStateFlow()

    private val userId = "user_cust_01"

    init {
        loadWalletData()
    }

    fun loadWalletData() {
        launchWithLoading {
            combine(
                walletRepository.getWalletBalance(userId),
                walletRepository.getTransactionHistory(userId)
            ) { wallet, txList ->
                WalletData(
                    wallet = wallet ?: Wallet(userId, 0L),
                    transactions = txList
                )
            }.collect { data ->
                updateState(UiState.Success(data))
            }
        }
    }

    /**
     * Khởi tạo yêu cầu nạp tiền vào ví qua mã QR giả lập
     */
    fun createTopUp(amount: Long, onQrReady: (TopUpTransaction) -> Unit) {
        viewModelScope.launch {
            val result = walletRepository.createTopUpRequest(userId, amount)
            if (result.isSuccess) {
                val tx = result.getOrThrow() as TopUpTransaction
                _pendingTopUpTx.value = tx
                onQrReady(tx)
                observePaymentStatus(tx.id)
            } else {
                handleError(result.exceptionOrNull() ?: Exception("Lỗi tạo mã QR nạp tiền"))
            }
        }
    }

    /**
     * Lắng nghe thời gian thực trạng thái giao dịch
     * (Khi người dùng bấm 'Xác nhận' trên trang web, app tự động chuyển PENDING -> SUCCESS)
     */
    private fun observePaymentStatus(txId: String) {
        viewModelScope.launch {
            walletRepository.observeTransactionStatus(txId).collect { status ->
                if (status == AppConstants.TX_STATUS_SUCCESS) {
                    _pendingTopUpTx.value = null
                }
            }
        }
    }

    /**
     * Mô phỏng xác nhận thanh toán thành công (dành cho demo một thiết bị)
     */
    fun confirmPaymentDemo(txId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = walletRepository.confirmTransactionSuccess(txId)
            if (result.isSuccess) {
                _pendingTopUpTx.value = null
                onSuccess()
            }
        }
    }

    fun clearPendingTopUp() {
        _pendingTopUpTx.value = null
    }
}
