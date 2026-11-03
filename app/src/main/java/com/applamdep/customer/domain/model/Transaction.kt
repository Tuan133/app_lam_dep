package com.applamdep.customer.domain.model

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE)
 * Lớp cha Transaction định nghĩa cấu trúc gốc của mọi giao dịch tài chính trên sàn.
 * Các loại giao dịch cụ thể (Nạp ví, Cọc, Hoàn tiền, Giải ngân) kế thừa lại để mở rộng thuộc tính.
 */
sealed class Transaction(
    open val id: String,
    open val userId: String,
    open val amount: Long,
    open val status: String,             // PENDING, SUCCESS, FAILED, HELD, REFUNDED, RELEASED_TO_SHOP
    open val createdAt: Long = System.currentTimeMillis()
)

/**
 * Lớp con 1: Giao dịch Nạp tiền vào ví
 */
data class TopUpTransaction(
    override val id: String,
    override val userId: String,
    override val amount: Long,
    override val status: String,
    override val createdAt: Long = System.currentTimeMillis(),
    val qrUrl: String? = null,
    val paymentGateway: String = "MOCK_QR"
) : Transaction(id, userId, amount, status, createdAt)

/**
 * Lớp con 2: Giao dịch Đặt cọc (Sàn giữ tiền HELD)
 */
data class DepositTransaction(
    override val id: String,
    override val userId: String,
    override val amount: Long,
    override val status: String,         // HELD (Sàn tạm giữ)
    override val createdAt: Long = System.currentTimeMillis(),
    val bookingId: String,
    val shopId: String,
    val heldAt: Long = System.currentTimeMillis(),
    val releasedAt: Long? = null
) : Transaction(id, userId, amount, status, createdAt)

/**
 * Lớp con 3: Giao dịch Hoàn cọc về ví khách
 */
data class RefundTransaction(
    override val id: String,
    override val userId: String,
    override val amount: Long,
    override val status: String,         // REFUNDED
    override val createdAt: Long = System.currentTimeMillis(),
    val originalDepositTxId: String,
    val bookingId: String,
    val refundReason: String
) : Transaction(id, userId, amount, status, createdAt)

/**
 * Lớp con 4: Giao dịch Giải ngân cọc cho quán
 */
data class ReleasedToShopTransaction(
    override val id: String,
    override val userId: String,
    override val amount: Long,
    override val status: String,         // RELEASED_TO_SHOP
    override val createdAt: Long = System.currentTimeMillis(),
    val bookingId: String,
    val shopId: String
) : Transaction(id, userId, amount, status, createdAt)

/**
 * Model Ví tiền
 */
data class Wallet(
    val userId: String,
    val balance: Long,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Model Đánh giá
 */
data class Review(
    val id: String,
    val bookingId: String,
    val shopId: String,
    val customerId: String,
    val customerName: String,
    val customerAvatar: String? = null,
    val rating: Int,                     // 1..5
    val comment: String,
    val tags: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val status: String = "ACTIVE",       // ACTIVE, REMOVE_REQUESTED, REMOVED
    val replyComment: String? = null,
    val repliedAt: Long? = null,
    val reportReason: String? = null,
    val reportedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
