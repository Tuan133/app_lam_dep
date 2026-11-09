package com.applamdep.customer.domain.repository

import com.applamdep.customer.domain.model.Transaction
import com.applamdep.customer.domain.model.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Interface WalletRepository quản lý số dư ví và lịch sử giao dịch.
 * Khách hàng không được tự ý sửa số dư từ Client mà mọi thao tác đều qua phương thức chuẩn.
 */
interface WalletRepository {
    /**
     * Lắng nghe số dư ví theo thời gian thực
     */
    fun getWalletBalance(userId: String): Flow<Wallet?>

    /**
     * Lắng nghe lịch sử giao dịch
     */
    fun getTransactionHistory(userId: String): Flow<List<Transaction>>

    /**
     * Tạo yêu cầu nạp tiền vào ví qua mã QR giả lập
     */
    suspend fun createTopUpRequest(userId: String, amount: Long): Result<Transaction>

    /**
     * Khóa tiền cọc (Sàn giữ HELD)
     */
    suspend fun holdDeposit(userId: String, bookingId: String, shopId: String, amount: Long): Result<Transaction>

    /**
     * Hoàn tiền cọc về ví khách (REFUNDED)
     */
    suspend fun refundDepositToWallet(userId: String, bookingId: String, amount: Long, reason: String): Result<Transaction>

    /**
     * Lắng nghe trạng thái giao dịch (ví dụ khi người dùng quét QR và bấm xác nhận trên web)
     */
    fun observeTransactionStatus(transactionId: String): Flow<String>

    /**
     * Xác nhận giao dịch thành công (do web xác nhận hoặc callback webhook)
     */
    suspend fun confirmTransactionSuccess(transactionId: String): Result<Unit>
}

