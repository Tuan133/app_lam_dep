package com.applamdep.merchant.ui.schedule

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.data.repository.ScheduleRepositoryImpl
import com.applamdep.merchant.data.repository.SlotRepositoryImpl
import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.WorkSchedule
import com.applamdep.merchant.domain.repository.ScheduleRepository
import com.applamdep.merchant.domain.repository.SlotRepository
import com.applamdep.merchant.domain.rule.ScheduleSlotGenerator
import com.applamdep.merchant.domain.rule.ScheduleValidator
import com.applamdep.merchant.domain.rule.SlotCapacityRule
import com.applamdep.merchant.domain.rule.SlotFilterPolicy
import com.applamdep.merchant.domain.rule.SlotFilterType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Dữ liệu đóng gói ca làm việc và danh sách slots theo ngày
 */
data class ScheduleScreenData(
    val schedule: WorkSchedule,
    val slots: List<SlotItem>,
    val filteredSlots: List<SlotItem>,
    val activeFilter: SlotFilterType = SlotFilterType.ALL
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc ScheduleRepository & SlotRepository
 * ViewModel điều phối:
 * - Chức năng 4: Quản lý ca làm việc (Ngày trong tuần, giờ mở/đóng cửa, giờ nghỉ trưa, ngày nghỉ lễ)
 * - Chức năng 5: Quản lý khung giờ trống (Slots) theo ngày, điều chỉnh sức chứa (Capacity) với quy tắc
 *   nghiêm ngặt không giảm dưới số khách đã đặt trước, và khóa/mở nhận khách từng khung giờ.
 */
class MerchantScheduleViewModel(
    private val scheduleRepository: ScheduleRepository = ScheduleRepositoryImpl(),
    private val slotRepository: SlotRepository = SlotRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<ScheduleScreenData>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        scheduleRepository = ScheduleRepositoryImpl(),
        slotRepository = SlotRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val scheduleValidator = ScheduleValidator()
    private val slotCapacityRule = SlotCapacityRule()
    private val slotGenerator = ScheduleSlotGenerator()
    private val slotFilterPolicy = SlotFilterPolicy()

    private val _selectedDate = MutableStateFlow("2026-10-29")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _slotFilter = MutableStateFlow(SlotFilterType.ALL)
    val slotFilter: StateFlow<SlotFilterType> = _slotFilter.asStateFlow()

    private var currentSchedule: WorkSchedule = WorkSchedule()
    private var allSlotsForDate: List<SlotItem> = emptyList()

    fun loadScheduleAndSlots(shopId: String = "shop_01") {
        launchWithLoading {
            scheduleRepository.getSchedule(shopId).collect { sched ->
                currentSchedule = sched
                slotRepository.getSlots(shopId, _selectedDate.value).collect { slots ->
                    allSlotsForDate = slots
                    emitSuccessState()
                }
            }
        }
    }

    fun setSelectedDate(date: String, shopId: String = "shop_01") {
        _selectedDate.value = date
        launchWithLoading {
            slotRepository.getSlots(shopId, date).collect { slots ->
                allSlotsForDate = slots
                emitSuccessState()
            }
        }
    }

    fun setSlotFilter(filter: SlotFilterType) {
        _slotFilter.value = filter
        emitSuccessState()
    }

    private fun emitSuccessState() {
        val filtered = slotFilterPolicy.filter(allSlotsForDate, _slotFilter.value)
        updateState(
            UiState.Success(
                ScheduleScreenData(
                    schedule = currentSchedule,
                    slots = allSlotsForDate,
                    filteredSlots = filtered,
                    activeFilter = _slotFilter.value
                )
            )
        )
    }

    /**
     * Chức năng 4: Cập nhật ca làm việc
     */
    fun updateSchedule(
        schedule: WorkSchedule,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = scheduleValidator.validate(schedule)
        if (validation is ScheduleValidator.ValidationResult.Invalid) {
            onError(validation.message)
            return
        }

        viewModelScope.launch {
            val result = scheduleRepository.updateSchedule(schedule)
            if (result.isSuccess) {
                currentSchedule = schedule
                emitSuccessState()
                onSuccess()
            } else {
                onError("Không thể lưu cấu hình ca làm: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Chức năng 5: Cập nhật sức chứa (Capacity) của khung giờ.
     * Quy tắc nghiệp vụ bắt buộc: Không cho phép giảm sức chứa thấp hơn số lượng khách đã đặt hẹn!
     */
    fun updateSlotCapacity(
        slotId: String,
        currentBooked: Int,
        newCapacity: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = slotCapacityRule.validateCapacityChange(
            currentBooked = currentBooked,
            newCapacity = newCapacity
        )
        if (validation is SlotCapacityRule.CapacityValidationResult.Invalid) {
            onError(validation.errorMessage)
            return
        }

        viewModelScope.launch {
            val result = slotRepository.updateSlotCapacity(slotId, newCapacity)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Không thể điều chỉnh sức chứa: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Chức năng 5: Khóa hoặc Mở khóa nhận khách một khung giờ cụ thể
     */
    fun toggleSlotBlock(
        slotId: String,
        isBlocked: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = slotRepository.toggleSlotBlock(slotId, isBlocked)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError("Không thể thay đổi trạng thái khung giờ: ${result.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    /**
     * Chức năng 5: Tự động tạo lại danh sách Slot theo ca làm việc đã cấu hình
     */
    fun generateSlotsFromSchedule(
        shopId: String = "shop_01",
        date: String = _selectedDate.value,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val sched = scheduleRepository.getSchedule(shopId).firstOrNull() ?: currentSchedule
                val newSlots = slotGenerator.generateSlots(schedule = sched, date = date, shopId = shopId)
                val result = slotRepository.saveGeneratedSlots(newSlots)
                if (result.isSuccess) {
                    allSlotsForDate = newSlots
                    emitSuccessState()
                    onSuccess(newSlots.size)
                } else {
                    onError("Không thể tạo khung giờ: ${result.exceptionOrNull()?.localizedMessage}")
                }
            } catch (e: Exception) {
                onError("Lỗi khi sinh khung giờ: ${e.localizedMessage}")
            }
        }
    }

    // Tương thích ngược với các method cũ
    fun loadSchedule(shopId: String = "shop_01") = loadScheduleAndSlots(shopId)

    fun toggleSlotAvailability(slotId: String, isAvailable: Boolean, onSuccess: () -> Unit) {
        toggleSlotBlock(slotId, isBlocked = !isAvailable, onSuccess = onSuccess, onError = {})
    }
}
