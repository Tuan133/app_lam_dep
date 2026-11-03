package com.applamdep.merchant.ui.profile

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.rule.DepositPolicyValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.applamdep.merchant.data.repository.ShopRepositoryImpl
import com.applamdep.merchant.domain.model.MerchantProfile

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION) - Phụ thuộc ShopRepository
 * ViewModel quản lý hiển thị và chỉnh sửa hồ sơ đối tác cửa hàng (Chức năng 2 - P0)
 */
class MerchantProfileViewModel(
    private val shopRepository: ShopRepository = ShopRepositoryImpl(),
    private val merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<ShopProfile>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI - MutableStateFlow private
    private val _originalProfile = MutableStateFlow<ShopProfile?>(null)
    val originalProfile: StateFlow<ShopProfile?> = _originalProfile.asStateFlow()

    private val depositValidator = DepositPolicyValidator()

    fun loadProfile(ownerUid: String = "merchant_user_01") {
        launchWithLoading {
            shopRepository.getShopProfile(ownerUid).collect { profile ->
                if (profile != null) {
                    _originalProfile.value = profile
                    updateState(UiState.Success(profile))
                } else {
                    updateState(UiState.Error("Chưa tìm thấy hồ sơ cửa hàng."))
                }
            }
        }
    }

    /**
     * Kiểm tra xem các trường quan trọng (Tên, Địa chỉ) có bị thay đổi khi đã APPROVED không
     */
    fun hasCriticalChanges(updated: ShopProfile): Boolean {
        val original = _originalProfile.value ?: return false
        return original.isApproved && (
            original.name.trim() != updated.name.trim() ||
            original.address.trim() != updated.address.trim()
        )
    }

    fun updateProfile(
        updatedProfile: ShopProfile,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        // Validate chính sách cọc
        val depositValidation = depositValidator.validateDepositOptions(
            requireDeposit = updatedProfile.requireDeposit,
            depositOptions = updatedProfile.depositOptions
        )
        if (depositValidation is DepositPolicyValidator.ValidationResult.Invalid) {
            onError(depositValidation.errorMessage)
            return
        }

        viewModelScope.launch {
            val isCritical = hasCriticalChanges(updatedProfile)
            val profileToSave = if (isCritical) {
                updatedProfile.copy(
                    lastEditedAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                updatedProfile.copy(updatedAt = System.currentTimeMillis())
            }

            val result = shopRepository.updateProfile(profileToSave)
            if (result.isSuccess) {
                _originalProfile.value = profileToSave
                updateState(UiState.Success(profileToSave))
                val msg = if (isCritical) {
                    "Cập nhật thành công! Thông tin tên/địa chỉ đã được ghi nhận (lastEditedAt) để Admin hậu kiểm."
                } else {
                    "Cập nhật hồ sơ cửa hàng thành công!"
                }
                onSuccess(msg)
            } else {
                onError("Lỗi cập nhật: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Tương thích ngược với màn hình MerchantRegistrationScreen cũ
     */
    fun registerNewShop(
        profile: MerchantProfile,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = shopRepository.registerShop(
                ShopProfile(
                    name = profile.shopName,
                    ownerName = profile.ownerName,
                    phone = profile.phone,
                    email = profile.email,
                    address = profile.address,
                    description = profile.description,
                    plan = profile.membershipPlan,
                    status = profile.status
                )
            )
            if (result.isSuccess) {
                onSuccess(result.getOrThrow())
            } else {
                onError("Đăng ký cơ sở thất bại: ${result.exceptionOrNull()?.message}")
            }
        }
    }
}
