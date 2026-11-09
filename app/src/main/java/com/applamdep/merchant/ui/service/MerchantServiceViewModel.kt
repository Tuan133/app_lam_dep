package com.applamdep.merchant.ui.service

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ServiceRepositoryImpl
import com.applamdep.merchant.domain.model.ServiceItem
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.ServiceRepository
import com.applamdep.merchant.domain.rule.ServiceRemovalPolicy
import com.applamdep.merchant.domain.rule.ServiceValidator
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc ServiceRepository & BookingRepository
 * ViewModel quản lý danh mục dịch vụ (Merchant Requirement #3 - CRUD, thiết lập giá, thời lượng,
 * validation nghiệp vụ và chính sách chuyển sang ẩn thay vì xóa vĩnh viễn khi có đơn đặt chưa hoàn tất).
 */
class MerchantServiceViewModel(
    private val serviceRepository: ServiceRepository = ServiceRepositoryImpl(),
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<List<ServiceItem>>() {

    // Hỗ trợ secondary constructor cho legacy DI
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        serviceRepository = ServiceRepositoryImpl(),
        bookingRepository = BookingRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val serviceValidator = ServiceValidator()
    private val serviceRemovalPolicy = ServiceRemovalPolicy()

    fun loadServices(shopId: String = "shop_01") {
        launchWithLoading {
            serviceRepository.getServices(shopId).collect { list ->
                updateState(UiState.Success(list))
            }
        }
    }

    fun addService(
        service: ServiceItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = serviceValidator.validate(
            name = service.name,
            price = service.price,
            durationMinutes = service.durationMinutes
        )
        if (validation is ServiceValidator.ValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        viewModelScope.launch {
            val result = serviceRepository.addService(service)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Không thể thêm dịch vụ: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun updateService(
        service: ServiceItem,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = serviceValidator.validate(
            name = service.name,
            price = service.price,
            durationMinutes = service.durationMinutes
        )
        if (validation is ServiceValidator.ValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        viewModelScope.launch {
            val result = serviceRepository.updateService(service)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Không thể cập nhật dịch vụ: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Nghiệp vụ xóa hoặc ẩn dịch vụ:
     * Kiểm tra danh sách đơn đặt lịch từ BookingRepository.
     * Nếu có đơn chưa hoàn tất (PENDING, CONFIRMED, IN_SERVICE), chuyển thành Ẩn thay vì xóa vĩnh viễn.
     */
    fun deleteOrHideService(
        serviceId: String,
        shopId: String = "shop_01",
        onDecision: (ServiceRemovalPolicy.ServiceRemovalDecision) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentBookings = bookingRepository.getShopBookings(shopId).firstOrNull() ?: emptyList()
                val decision = serviceRemovalPolicy.evaluateRemoval(serviceId, currentBookings)

                val hasActive = decision is ServiceRemovalPolicy.ServiceRemovalDecision.SoftDelete
                val result = serviceRepository.deleteOrHideService(serviceId, hasActive)

                if (result.isSuccess) {
                    onDecision(decision)
                } else {
                    onError("Thao tác thất bại: ${result.exceptionOrNull()?.localizedMessage}")
                }
            } catch (e: Exception) {
                onError("Lỗi khi xử lý dịch vụ: ${e.localizedMessage}")
            }
        }
    }

    // Tương thích ngược với lời gọi hàm cũ
    fun deleteService(serviceId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        deleteOrHideService(
            serviceId = serviceId,
            onDecision = { onSuccess() },
            onError = onError
        )
    }
}
