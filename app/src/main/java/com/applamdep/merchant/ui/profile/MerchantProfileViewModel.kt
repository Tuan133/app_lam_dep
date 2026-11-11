package com.applamdep.merchant.ui.profile

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ScheduleRepositoryImpl
import com.applamdep.merchant.data.repository.ShopRepositoryImpl
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.model.MerchantProfile
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.ScheduleRepository
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.repository.TransactionRepository
import com.applamdep.merchant.domain.rule.DepositPolicyValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (INHERITANCE) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION) - Phụ thuộc ShopRepository, BookingRepository & TransactionRepository
 * ViewModel quản lý hiển thị, chỉnh sửa hồ sơ đối tác, đăng ký gói hội viên và quy trình đóng cửa (Chức năng 2 - P0)
 */
class MerchantProfileViewModel(
    private val shopRepository: ShopRepository = ShopRepositoryImpl(),
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(),
    private val transactionRepository: TransactionRepository = TransactionRepositoryImpl(),
    private val scheduleRepository: ScheduleRepository = ScheduleRepositoryImpl(),
    private val merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<ShopProfile>() {

    constructor(
        shopRepository: ShopRepository,
        merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository?
    ) : this(
        shopRepository = shopRepository,
        bookingRepository = BookingRepositoryImpl(),
        transactionRepository = TransactionRepositoryImpl(),
        scheduleRepository = ScheduleRepositoryImpl(),
        merchantRepository = merchantRepository
    )

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI - MutableStateFlow private
    private val _originalProfile = MutableStateFlow<ShopProfile?>(null)
    val originalProfile: StateFlow<ShopProfile?> = _originalProfile.asStateFlow()

    private val _shopBookings = MutableStateFlow<List<Booking>>(emptyList())
    val shopBookings: StateFlow<List<Booking>> = _shopBookings.asStateFlow()

    private val depositValidator = DepositPolicyValidator()
    private var bookingsJob: Job? = null

    fun loadProfile(ownerUid: String = "merchant_user_01") {
        launchWithLoading {
            shopRepository.getShopProfile(ownerUid).collect { profile ->
                if (profile != null) {
                    _originalProfile.value = profile
                    updateState(UiState.Success(profile))
                    if (bookingsJob == null || bookingsJob?.isActive != true) {
                        bookingsJob = viewModelScope.launch {
                            bookingRepository.getShopBookings(profile.id).collect { list ->
                                _shopBookings.value = list
                            }
                        }
                    }
                } else {
                    updateState(UiState.Error("Chưa tìm thấy hồ sơ cửa hàng."))
                }
            }
        }
    }

    /**
     * Đăng ký / nâng cấp gói hội viên ngay trong Quản lý hồ sơ cửa hàng
     */
    fun updateMembershipPlan(
        plan: String,
        termMonths: Int,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _originalProfile.value
            ?: (uiState.value as? UiState.Success)?.data
        if (current == null) {
            onError("Không tìm thấy hồ sơ cửa hàng.")
            return
        }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val totalDays = (termMonths.coerceAtLeast(1) + 1) * 30L // Tặng kèm 1 tháng miễn phí
            val updated = current.copy(
                plan = plan,
                planTermMonths = termMonths,
                planStart = now,
                planExpiry = now + totalDays * 24L * 60L * 60L * 1000L,
                updatedAt = now
            )
            val result = shopRepository.updateProfile(updated)
            if (result.isSuccess) {
                _originalProfile.value = updated
                updateState(UiState.Success(updated))
                val planLabel = if (plan == MerchantConstants.PLAN_VIP) "Gói VIP" else "Gói Thường"
                onSuccess("Đã kích hoạt $planLabel ($termMonths tháng) thành công!")
            } else {
                onError("Không thể cập nhật gói hội viên: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Quy trình đóng cửa nhánh A: Đóng cửa cho đến khi chủ tiệm mở lại
     */
    fun closeShopUntilReopen(
        reason: String,
        note: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _originalProfile.value
            ?: (uiState.value as? UiState.Success)?.data
        if (current == null) {
            onError("Không tìm thấy hồ sơ cửa hàng.")
            return
        }
        val cleanReason = reason.trim().ifBlank { "Cửa hàng bận đột xuất" }
        val cleanNote = note.trim().ifBlank { null }
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updated = current.copy(
                status = MerchantConstants.SHOP_STATUS_CLOSED,
                closeReason = cleanReason,
                closeNote = cleanNote,
                closedAt = now,
                closedUntilDate = null,
                updatedAt = now
            )
            val result = shopRepository.updateProfile(updated)
            if (result.isSuccess) {
                _originalProfile.value = updated
                updateState(UiState.Success(updated))
                onSuccess()
            } else {
                onError("Không thể đóng cửa hàng: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Quy trình đóng cửa nhánh B: Chọn ngày/giờ đóng cửa và hủy hàng loạt lịch hẹn đã chọn
     */
    fun closeShopForScheduleAndCancelBookings(
        date: String,
        bookingIdsToCancel: List<String>,
        cancelReason: String,
        proposalNote: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _originalProfile.value
            ?: (uiState.value as? UiState.Success)?.data
        if (current == null) {
            onError("Không tìm thấy hồ sơ cửa hàng.")
            return
        }
        val cleanReason = cancelReason.trim().ifBlank { "Cửa hàng đóng cửa tạm thời" }
        val cleanProposal = proposalNote.trim()
        val combinedReason = if (cleanProposal.isEmpty()) {
            cleanReason
        } else {
            "$cleanReason · Đề xuất: $cleanProposal"
        }

        viewModelScope.launch {
            val existingBookings = bookingRepository.getShopBookings(current.id).firstOrNull() ?: _shopBookings.value
            val cancelledCount = if (bookingIdsToCancel.isNotEmpty()) {
                val res = bookingRepository.cancelBookingsForShopClosure(bookingIdsToCancel, combinedReason)
                if (res.isFailure) {
                    onError("Không thể hủy lịch hẹn: ${res.exceptionOrNull()?.localizedMessage}")
                    return@launch
                }
                val idSet = bookingIdsToCancel.toSet()
                existingBookings
                    .filter { it.id in idSet }
                    .forEach { bk ->
                        if (bk.depositAmount > 0L) {
                            transactionRepository.refundDeposit(bk.id, bk.depositAmount)
                        }
                    }
                res.getOrDefault(0)
            } else {
                0
            }

            if (date.isNotBlank()) {
                val schedule = scheduleRepository.getSchedule(current.id).firstOrNull()
                if (schedule != null && date !in schedule.offDates) {
                    scheduleRepository.updateSchedule(schedule.copy(offDates = schedule.offDates + date))
                }
            }

            val now = System.currentTimeMillis()
            val updated = current.copy(
                status = MerchantConstants.SHOP_STATUS_CLOSED,
                closeReason = cleanReason,
                closeNote = cleanProposal.ifBlank { null },
                closedAt = now,
                closedUntilDate = date.ifBlank { null },
                updatedAt = now
            )
            val result = shopRepository.updateProfile(updated)
            if (result.isSuccess) {
                _originalProfile.value = updated
                updateState(UiState.Success(updated))
                onSuccess(cancelledCount)
            } else {
                onError("Không thể cập nhật trạng thái đóng cửa: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Mở cửa hoạt động trở lại
     */
    fun reopenShop(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val current = _originalProfile.value
            ?: (uiState.value as? UiState.Success)?.data
        if (current == null) {
            onError("Không tìm thấy hồ sơ cửa hàng.")
            return
        }
        viewModelScope.launch {
            val closedDateToRemove = current.closedUntilDate
            if (!closedDateToRemove.isNullOrBlank()) {
                val schedule = scheduleRepository.getSchedule(current.id).firstOrNull()
                if (schedule != null && closedDateToRemove in schedule.offDates) {
                    scheduleRepository.updateSchedule(
                        schedule.copy(offDates = schedule.offDates.filterNot { it == closedDateToRemove })
                    )
                }
            }
            val now = System.currentTimeMillis()
            val updated = current.copy(
                status = MerchantConstants.SHOP_STATUS_APPROVED,
                closeReason = null,
                closeNote = null,
                closedAt = null,
                closedUntilDate = null,
                updatedAt = now
            )
            val result = shopRepository.updateProfile(updated)
            if (result.isSuccess) {
                _originalProfile.value = updated
                updateState(UiState.Success(updated))
                onSuccess()
            } else {
                onError("Không thể mở cửa lại: ${result.exceptionOrNull()?.localizedMessage}")
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
