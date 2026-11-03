package com.applamdep.merchant

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.Transaction
import com.applamdep.merchant.domain.rule.EscrowStatusCalculator
import com.applamdep.merchant.domain.rule.ReviewReplyValidator
import com.applamdep.merchant.domain.rule.ReviewReportValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests kiểm thử các quy tắc nghiệp vụ Đóng gói và Đa hình cho:
 * - Chức năng 8: Phản hồi và tố cáo đánh giá vi phạm
 * - Chức năng 9: Theo dõi và phân bổ tiền cọc ký quỹ Escrow
 * - Chức năng 10: Thống kê kinh doanh và số dư tài chính
 */
class MerchantReviewFinanceStatsTest {

    private val replyValidator = ReviewReplyValidator()
    private val reportValidator = ReviewReportValidator()
    private val escrowCalculator = EscrowStatusCalculator()

    // ==========================================
    // 1. KIỂM THỬ CHỨC NĂNG 8: ĐÁNH GIÁ & PHẢN HỒI
    // ==========================================

    @Test
    fun reviewReplyValidator_validReply_returnsValid() {
        val result = replyValidator.validate("Cảm ơn bạn rất nhiều! Rất vui được phục vụ bạn.")
        assertTrue(result is ReviewReplyValidator.ReplyValidationResult.Valid)
    }

    @Test
    fun reviewReplyValidator_blankReply_returnsInvalid() {
        val result = replyValidator.validate("    ")
        assertTrue(result is ReviewReplyValidator.ReplyValidationResult.Invalid)
    }

    @Test
    fun reviewReplyValidator_tooShortReply_returnsInvalid() {
        val result = replyValidator.validate("Ok")
        assertTrue(result is ReviewReplyValidator.ReplyValidationResult.Invalid)
    }

    @Test
    fun reviewReplyValidator_tooLongReply_returnsInvalid() {
        val longText = "a".repeat(501)
        val result = replyValidator.validate(longText)
        assertTrue(result is ReviewReplyValidator.ReplyValidationResult.Invalid)
    }

    @Test
    fun reviewReportValidator_validReason_returnsValid() {
        val result = reportValidator.validate("Khách hàng dùng từ ngữ xúc phạm và không đúng sự thật")
        assertTrue(result is ReviewReportValidator.ReportValidationResult.Valid)
    }

    @Test
    fun reviewReportValidator_tooShortReason_returnsInvalid() {
        val result = reportValidator.validate("Spam")
        assertTrue(result is ReviewReportValidator.ReportValidationResult.Invalid)
    }

    // ==========================================
    // 2. KIỂM THỬ CHỨC NĂNG 9 & 10: TIỀN CỌC ESCROW & THỐNG KÊ
    // ==========================================

    @Test
    fun escrowCalculator_computesCorrectBalances() {
        val transactions = listOf(
            Transaction(id = "tx1", amount = 100000L, status = MerchantConstants.TX_STATUS_HELD),
            Transaction(id = "tx2", amount = 150000L, status = MerchantConstants.TX_STATUS_HELD),
            Transaction(id = "tx3", amount = 200000L, status = MerchantConstants.TX_STATUS_RELEASED_TO_SHOP),
            Transaction(id = "tx4", amount = 300000L, status = MerchantConstants.TX_STATUS_RELEASED_TO_SHOP),
            Transaction(id = "tx5", amount = 50000L, status = MerchantConstants.TX_STATUS_REFUNDED)
        )

        val summary = escrowCalculator.computeSummary(transactions)

        // Held = 100k + 150k = 250k
        assertEquals(250000L, summary.heldEscrowBalance)
        // Released = 200k + 300k = 500k
        assertEquals(500000L, summary.releasedTotalBalance)
        // Refunded = 50k
        assertEquals(50000L, summary.refundedTotalBalance)
        assertEquals(5, summary.transactions.size)
    }
}
