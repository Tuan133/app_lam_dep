package com.applamdep.merchant

import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.model.SystemConfig
import com.applamdep.merchant.domain.rule.DepositPolicyValidator
import com.applamdep.merchant.domain.rule.PlanFeeCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests kiểm thử các quy tắc nghiệp vụ Đóng gói của Chức năng 1 và 2
 */
class MerchantRegistrationAndProfileTest {

    private val depositValidator = DepositPolicyValidator()
    private val feeCalculator = PlanFeeCalculator()

    @Test
    fun depositValidator_validFiveDistinctPositiveAmounts_returnsValid() {
        val options = listOf(50000L, 100000L, 150000L, 200000L, 500000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Valid)
    }

    @Test
    fun depositValidator_lessThanFiveAmounts_returnsInvalid() {
        val options = listOf(50000L, 100000L, 150000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Invalid)
    }

    @Test
    fun depositValidator_duplicateAmounts_returnsInvalid() {
        val options = listOf(50000L, 100000L, 100000L, 200000L, 500000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Invalid)
    }

    @Test
    fun depositValidator_negativeAmount_returnsInvalid() {
        val options = listOf(-50000L, 100000L, 150000L, 200000L, 500000L)
        val result = depositValidator.validateDepositOptions(requireDeposit = true, depositOptions = options)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Invalid)
    }

    @Test
    fun depositValidator_whenDepositDisabled_alwaysReturnsValid() {
        val emptyOptions = emptyList<Long>()
        val result = depositValidator.validateDepositOptions(requireDeposit = false, depositOptions = emptyOptions)
        assertTrue(result is DepositPolicyValidator.ValidationResult.Valid)
    }

    @Test
    fun feeCalculator_computesCorrectFeeFromConfig_notHardcoded() {
        val config = SystemConfig(
            normalPlanFee = mapOf("3" to 300000L, "6" to 550000L, "12" to 1000000L),
            vipPlanFee = mapOf("3" to 600000L, "6" to 1100000L, "12" to 2000000L)
        )

        val normal3 = feeCalculator.calculateFee(config, MerchantConstants.PLAN_NORMAL, 3)
        assertEquals(300000L, normal3)

        val vip12 = feeCalculator.calculateFee(config, MerchantConstants.PLAN_VIP, 12)
        assertEquals(2000000L, vip12)
    }

    @Test
    fun criticalChangeDetection_approvedShopChangesName_shouldRecordAudit() {
        val original = ShopProfile(
            name = "Salon A",
            address = "123 Street",
            status = MerchantConstants.SHOP_STATUS_APPROVED
        )
        val updated = original.copy(name = "Salon B")

        val isCritical = original.isApproved && (original.name != updated.name || original.address != updated.address)
        assertTrue(isCritical)
    }

    @Test
    fun criticalChangeDetection_pendingShopChangesName_doesNotTriggerAudit() {
        val original = ShopProfile(
            name = "Salon A",
            address = "123 Street",
            status = MerchantConstants.SHOP_STATUS_PENDING
        )
        val updated = original.copy(name = "Salon B")

        val isCritical = original.isApproved && (original.name != updated.name || original.address != updated.address)
        assertFalse(isCritical)
    }
}
