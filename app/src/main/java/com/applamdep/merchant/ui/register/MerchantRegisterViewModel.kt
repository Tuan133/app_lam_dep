package com.applamdep.merchant.ui.register

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.model.SystemConfig
import com.applamdep.merchant.domain.repository.PlanRepository
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.rule.DepositPolicyValidator
import com.applamdep.merchant.domain.rule.FreeTrialPaymentMethod
import com.applamdep.merchant.domain.rule.MockQrPaymentMethod
import com.applamdep.merchant.domain.rule.PaymentMethod
import com.applamdep.merchant.domain.rule.PaymentResult
import com.applamdep.merchant.domain.rule.PlanFeeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class RegisterFormData(
    val name: String = "",
    val ownerName: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val lat: Double = 16.0678,
    val lng: Double = 108.2208,
    val description: String = "",
    val openingHours: String = "08:30 - 21:00",
    val images: List<String> = emptyList(),
    val licenseNumber: String = "",
    val plan: String = MerchantConstants.PLAN_NORMAL,
    val planTermMonths: Int = 3,
    val requireDeposit: Boolean = true,
    val depositOptions: List<Long> = listOf(50000L, 100000L, 150000L, 200000L, 500000L)
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION) - Phụ thuộc ShopRepository và PlanRepository
 */
class MerchantRegisterViewModel(
    private val shopRepository: ShopRepository,
    private val planRepository: PlanRepository
) : BaseViewModel<ShopProfile>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI - MutableStateFlow private
    private val _formData = MutableStateFlow(RegisterFormData())
    val formData: StateFlow<RegisterFormData> = _formData.asStateFlow()

    private val _config = MutableStateFlow(SystemConfig())
    val config: StateFlow<SystemConfig> = _config.asStateFlow()

    private val _paymentModal = MutableStateFlow<PaymentResult?>(null)
    val paymentModal: StateFlow<PaymentResult?> = _paymentModal.asStateFlow()

    private val depositValidator = DepositPolicyValidator()
    private val feeCalculator = PlanFeeCalculator()

    init {
        loadSystemConfig()
    }

    private fun loadSystemConfig() {
        viewModelScope.launch {
            planRepository.getSystemConfig().collect {
                _config.value = it
            }
        }
    }

    fun updateFormData(update: (RegisterFormData) -> RegisterFormData) {
        _formData.value = update(_formData.value)
    }

    fun calculateCurrentFee(): Long {
        return feeCalculator.calculateFee(
            config = _config.value,
            plan = _formData.value.plan,
            termMonths = _formData.value.planTermMonths
        )
    }

    fun submitRegistration(
        ownerUid: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val form = _formData.value

        // Validate thông tin bắt buộc
        if (form.name.isBlank() || form.ownerName.isBlank() || form.phone.isBlank() || form.address.isBlank()) {
            onError("Vui lòng điền đầy đủ các mục thông tin bắt buộc (*)")
            return
        }

        // 1. Validate chính sách cọc bằng Validator đóng gói
        val depositValidation = depositValidator.validateDepositOptions(
            requireDeposit = form.requireDeposit,
            depositOptions = form.depositOptions
        )
        if (depositValidation is DepositPolicyValidator.ValidationResult.Invalid) {
            onError(depositValidation.errorMessage)
            return
        }

        launchWithLoading {
            val isFirstMonthFree = _config.value.isFirstMonthFree
            val calculatedFee = calculateCurrentFee()

            // 3. TÍNH CHẤT OOP: ĐA HÌNH (POLYMORPHISM)
            // Lựa chọn phương thức thanh toán phù hợp
            val paymentMethod: PaymentMethod = if (isFirstMonthFree) {
                FreeTrialPaymentMethod()
            } else {
                MockQrPaymentMethod { shopId, plan, term ->
                    planRepository.initiatePlanPayment(shopId, plan, term)
                }
            }

            val profileToSubmit = ShopProfile(
                id = "",
                ownerUid = ownerUid,
                name = form.name.trim(),
                ownerName = form.ownerName.trim(),
                phone = form.phone.trim(),
                email = form.email.trim(),
                address = form.address.trim(),
                lat = form.lat,
                lng = form.lng,
                description = form.description.trim(),
                openingHours = form.openingHours.trim(),
                images = form.images,
                status = MerchantConstants.SHOP_STATUS_PENDING,
                plan = form.plan,
                planTermMonths = form.planTermMonths,
                planStart = System.currentTimeMillis(),
                planExpiry = System.currentTimeMillis() + (form.planTermMonths * 30L * 24 * 60 * 60 * 1000L),
                requireDeposit = form.requireDeposit,
                depositOptions = form.depositOptions,
                updatedAt = System.currentTimeMillis()
            )

            val result = shopRepository.registerShop(profileToSubmit)
            if (result.isSuccess) {
                val newShopId = result.getOrThrow()
                val paymentRes = paymentMethod.processPayment(
                    shopId = newShopId,
                    plan = form.plan,
                    termMonths = form.planTermMonths,
                    feeAmount = calculatedFee
                )

                if (paymentRes is PaymentResult.QrPending) {
                    _paymentModal.value = paymentRes
                } else {
                    updateState(UiState.Success(profileToSubmit.copy(id = newShopId)))
                    onSuccess(newShopId)
                }
            } else {
                onError("Gửi hồ sơ thất bại: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun dismissPaymentModal() {
        _paymentModal.value = null
    }

    fun confirmPaymentAndFinish(onFinished: () -> Unit) {
        _paymentModal.value = null
        onFinished()
    }
}
