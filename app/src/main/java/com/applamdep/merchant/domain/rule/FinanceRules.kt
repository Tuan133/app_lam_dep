package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.FinancialSummary
import com.applamdep.merchant.domain.model.Transaction

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Lớp tính toán và phân bổ tài chính tiền cọc trung gian Escrow (Merchant Requirement #9):
 * - HELD: Tiền cọc sàn đang ký quỹ an toàn
 * - RELEASED_TO_SHOP: Tiền cọc đã giải ngân về tài khoản tiệm khi dịch vụ hoàn tất
 * - REFUNDED: Tiền cọc đã hoàn trả lại cho khách hàng
 */
class EscrowStatusCalculator {

    fun computeSummary(transactions: List<Transaction>): FinancialSummary {
        var held = 0L
        var released = 0L
        var refunded = 0L

        for (tx in transactions) {
            when (tx.status) {
                MerchantConstants.TX_STATUS_HELD -> held += tx.amount
                MerchantConstants.TX_STATUS_RELEASED_TO_SHOP -> released += tx.amount
                MerchantConstants.TX_STATUS_REFUNDED -> refunded += tx.amount
            }
        }

        return FinancialSummary(
            heldEscrowBalance = held,
            releasedTotalBalance = released,
            refundedTotalBalance = refunded,
            transactions = transactions
        )
    }
}
