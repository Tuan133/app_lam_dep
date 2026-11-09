package com.applamdep.merchant.domain.rule

import com.applamdep.merchant.core.constants.MerchantConstants

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Lớp chuyên trách kiểm tra quy tắc nghiệp vụ chính sách đặt cọc của cửa hàng:
 * - Phải đủ đúng 5 mệnh giá
 * - Các mệnh giá phải là số nguyên dương (> 0)
 * - Các mệnh giá không được trùng lặp
 */
class DepositPolicyValidator {

    sealed interface ValidationResult {
        data object Valid : ValidationResult
        data class Invalid(val errorMessage: String) : ValidationResult
    }

    fun validateDepositOptions(
        requireDeposit: Boolean,
        depositOptions: List<Long>
    ): ValidationResult {
        if (!requireDeposit) {
            return ValidationResult.Valid
        }

        if (depositOptions.size != MerchantConstants.REQUIRED_DEPOSIT_OPTIONS_COUNT) {
            return ValidationResult.Invalid(
                "Chính sách cọc yêu cầu nhập chính xác đúng 5 mệnh giá cố định (hiện có: ${depositOptions.size})"
            )
        }

        if (depositOptions.any { it <= 0L }) {
            return ValidationResult.Invalid("Tất cả các mức tiền cọc phải lớn hơn 0 VNĐ")
        }

        val uniqueSet = depositOptions.toSet()
        if (uniqueSet.size != depositOptions.size) {
            return ValidationResult.Invalid("Các mức tiền cọc không được trùng nhau")
        }

        return ValidationResult.Valid
    }
}
