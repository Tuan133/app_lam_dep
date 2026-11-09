package com.applamdep.customer.domain.payment

import com.applamdep.customer.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Kết quả khởi tạo thanh toán
 */
sealed interface PaymentResult {
    data class Success(val transaction: Transaction) : PaymentResult
    data class RequiresQrScan(val transactionId: String, val qrUrl: String) : PaymentResult
    data class Error(val message: String) : PaymentResult
}

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
 * Interface PaymentMethod cho phép trừu tượng hóa các phương thức thanh toán khác nhau trên sàn
 * (Thanh toán bằng ví trong app, hoặc Quét mã QR trang web giả lập, hoặc sau này mở rộng thêm cổng VNPAY/MoMo).
 */
interface PaymentMethod {
    val methodName: String

    /**
     * Khởi tạo quá trình thanh toán
     */
    suspend fun processPayment(
        userId: String,
        amount: Long,
        note: String
    ): PaymentResult

    /**
     * Lắng nghe trạng thái giao dịch
     */
    fun checkStatus(transactionId: String): Flow<String>
}

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH - PHƯƠNG THỨC 1: Quét mã QR trang web giả lập
 */
class QrPayment(
    private val walletRepository: com.applamdep.customer.domain.repository.WalletRepository
) : PaymentMethod {
    override val methodName: String = "MOCK_QR"

    override suspend fun processPayment(
        userId: String,
        amount: Long,
        note: String
    ): PaymentResult {
        val result = walletRepository.createTopUpRequest(userId, amount)
        return if (result.isSuccess) {
            val tx = result.getOrThrow() as com.applamdep.customer.domain.model.TopUpTransaction
            PaymentResult.RequiresQrScan(
                transactionId = tx.id,
                qrUrl = tx.qrUrl ?: "https://applamdep-customer.web.app/mock-payment.html?txId=${tx.id}&amount=$amount"
            )
        } else {
            PaymentResult.Error(result.exceptionOrNull()?.message ?: "Lỗi khởi tạo QR thanh toán")
        }
    }

    override fun checkStatus(transactionId: String): Flow<String> {
        return walletRepository.observeTransactionStatus(transactionId)
    }
}

/**
 * 3. TÍNH CHẤT OOP: ĐA HÌNH - PHƯƠNG THỨC 2: Trừ số dư ví trong app (Sàn giữ cọc HELD)
 */
class WalletPayment(
    private val walletRepository: com.applamdep.customer.domain.repository.WalletRepository,
    private val bookingId: String,
    private val shopId: String
) : PaymentMethod {
    override val methodName: String = "APP_WALLET"

    override suspend fun processPayment(
        userId: String,
        amount: Long,
        note: String
    ): PaymentResult {
        val result = walletRepository.holdDeposit(
            userId = userId,
            bookingId = bookingId,
            shopId = shopId,
            amount = amount
        )
        return if (result.isSuccess) {
            PaymentResult.Success(result.getOrThrow())
        } else {
            PaymentResult.Error(result.exceptionOrNull()?.message ?: "Số dư ví không đủ hoặc lỗi trừ cọc")
        }
    }

    override fun checkStatus(transactionId: String): Flow<String> {
        return walletRepository.observeTransactionStatus(transactionId)
    }
}

