package com.applamdep.merchant.domain.rule

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Quy tắc nghiệp vụ kiểm tra phản hồi đánh giá và tố cáo vi phạm (Merchant Requirement #8):
 * - Phản hồi từ cửa hàng: Không được rỗng, tối thiểu 5 ký tự, tối đa 500 ký tự.
 * - Lý do tố cáo vi phạm: Tối thiểu 10 ký tự để cung cấp thông tin minh bạch cho Admin hậu kiểm.
 */
class ReviewReplyValidator {

    sealed interface ReplyValidationResult {
        data object Valid : ReplyValidationResult
        data class Invalid(val message: String) : ReplyValidationResult
    }

    fun validate(replyText: String): ReplyValidationResult {
        val trimmed = replyText.trim()
        if (trimmed.isEmpty()) {
            return ReplyValidationResult.Invalid("Nội dung phản hồi không được để trống")
        }
        if (trimmed.length < 5) {
            return ReplyValidationResult.Invalid("Nội dung phản hồi phải có ít nhất 5 ký tự")
        }
        if (trimmed.length > 500) {
            return ReplyValidationResult.Invalid("Nội dung phản hồi không được vượt quá 500 ký tự")
        }
        return ReplyValidationResult.Valid
    }
}

class ReviewReportValidator {

    sealed interface ReportValidationResult {
        data object Valid : ReportValidationResult
        data class Invalid(val message: String) : ReportValidationResult
    }

    fun validate(reason: String): ReportValidationResult {
        val trimmed = reason.trim()
        if (trimmed.isEmpty()) {
            return ReportValidationResult.Invalid("Vui lòng nêu rõ lý do khiếu nại / tố cáo")
        }
        if (trimmed.length < 10) {
            return ReportValidationResult.Invalid("Lý do khiếu nại phải có ít nhất 10 ký tự để Admin có căn cứ xử lý")
        }
        return ReportValidationResult.Valid
    }
}
