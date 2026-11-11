package com.applamdep.merchant.ui.schedule

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ScheduleRepositoryImpl
import com.applamdep.merchant.data.repository.SlotRepositoryImpl
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.WorkSchedule
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.ScheduleRepository
import com.applamdep.merchant.domain.repository.SlotRepository
import com.applamdep.merchant.domain.rule.ScheduleSlotGenerator
import com.applamdep.merchant.domain.rule.ScheduleValidator
import com.applamdep.merchant.domain.rule.SlotBookingSynchronizer
import com.applamdep.merchant.domain.rule.SlotCapacityRule
import com.applamdep.merchant.domain.rule.SlotEditValidator
import com.applamdep.merchant.domain.rule.SlotFilterPolicy
import com.applamdep.merchant.domain.rule.SlotFilterType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Dữ liệu đóng gói ca làm việc và danh sách slots theo ngày
 */
data class ScheduleScreenData(
    val schedule: WorkSchedule,
    val slots: List<SlotItem>,
    val filteredSlots: List<SlotItem>,
    val activeFilter: SlotFilterType = SlotFilterType.ALL,
    val slotBookings: Map<String, List<Booking>> = emptyMap()
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc ScheduleRepository, SlotRepository & BookingRepository
 * ViewModel điều phối:
 * - Chức năng 4: Quản lý ca làm việc (Ngày trong tuần, giờ mở/đóng cửa, giờ nghỉ trưa, ngày nghỉ lễ)
 * - Chức năng 5: Quản lý khung giờ trống (Slots) theo ngày, điều chỉnh sức chứa (Capacity) với quy tắc
 *   nghiêm ngặt không giảm dưới số khách đã đặt trước, đồng bộ đơn hàng thực tế theo từng slot,
 *   và khóa/mở nhận khách từng khung giờ.
 */
class MerchantScheduleViewModel(
    private val scheduleRepository: ScheduleRepository = ScheduleRepositoryImpl(),
    private val slotRepository: SlotRepository = SlotRepositoryImpl(),
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<ScheduleScreenData>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        scheduleRepository = ScheduleRepositoryImpl(),
        slotRepository = SlotRepositoryImpl(),
        bookingRepository = BookingRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val scheduleValidator = ScheduleValidator()
    private val slotCapacityRule = SlotCapacityRule()
    private val slotEditValidator = SlotEditValidator(slotCapacityRule)
    private val slotGenerator = ScheduleSlotGenerator()
    private val slotFilterPolicy = SlotFilterPolicy()
    private val slotBookingSynchronizer = SlotBookingSynchronizer(
        defaultSeededDate = SlotRepositoryImpl.DEFAULT_SEEDED_DATE
    )

    private val _selectedDate = MutableStateFlow(SlotRepositoryImpl.DEFAULT_SEEDED_DATE)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _slotFilter = MutableStateFlow(SlotFilterType.ALL)
    val slotFilter: StateFlow<SlotFilterType> = _slotFilter.asStateFlow()

    private var currentSchedule: WorkSchedule = WorkSchedule()
    private var allSlotsForDate: List<SlotItem> = emptyList()
    private var currentSlotBookings: Map<String, List<Booking>> = emptyMap()
    private var observeJob: Job? = null

    fun loadScheduleAndSlots(shopId: String = "shop_01") {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            updateState(UiState.Loading)
            try {
                _selectedDate.collectLatest { date ->
                    combine(
                        scheduleRepository.getSchedule(shopId),
                        slotRepository.getSlots(shopId, date),
                        bookingRepository.getShopBookings(shopId),
                        _slotFilter
                    ) { sched, slots, bookings, filter ->
                        buildScreenData(
                            schedule = sched,
                            slots = slots,
                            allBookings = bookings,
                            shopId = shopId,
                            selectedDate = date,
                            filter = filter
                        )
                    }.collect { data ->
                        currentSchedule = data.schedule
                        allSlotsForDate = data.slots
                        currentSlotBookings = data.slotBookings
                        updateState(UiState.Success(data))
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateState(UiState.Error(e.localizedMessage ?: "Không thể tải dữ liệu khung giờ", e))
            }
        }
    }

    fun setSelectedDate(date: String, shopId: String = "shop_01") {
        _selectedDate.value = date
        if (observeJob == null || observeJob?.isActive != true) {
            loadScheduleAndSlots(shopId)
        }
    }

    fun setSlotFilter(filter: SlotFilterType) {
        _slotFilter.value = filter
        if (observeJob == null || observeJob?.isActive != true) {
            emitSuccessState()
        }
    }

    private fun buildScreenData(
        schedule: WorkSchedule,
        slots: List<SlotItem>,
        allBookings: List<Booking>,
        shopId: String,
        selectedDate: String,
        filter: SlotFilterType
    ): ScheduleScreenData {
        val synced = slotBookingSynchronizer.synchronize(
            slots = slots,
            allBookings = allBookings,
            shopId = shopId,
            selectedDate = selectedDate
        )
        val filtered = slotFilterPolicy.filter(synced.slots, filter)
        return ScheduleScreenData(
            schedule = schedule,
            slots = synced.slots,
            filteredSlots = filtered,
            activeFilter = filter,
            slotBookings = synced.slotBookings
        )
    }

    private fun emitSuccessState() {
        val filtered = slotFilterPolicy.filter(allSlotsForDate, _slotFilter.value)
        updateState(
            UiState.Success(
                ScheduleScreenData(
                    schedule = currentSchedule,
                    slots = allSlotsForDate,
                    filteredSlots = filtered,
                    activeFilter = _slotFilter.value,
                    slotBookings = currentSlotBookings
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
     * Chức năng 5: Chỉnh sửa chi tiết 1 khung giờ (giờ bắt đầu/kết thúc, sức chứa, khóa)
     */
    fun updateSlot(
        editedSlot: SlotItem,
        onSuccess: (SlotItem) -> Unit,
        onError: (String) -> Unit
    ) {
        val validation = slotEditValidator.validate(editedSlot, allSlotsForDate)
        if (validation is SlotEditValidator.ValidationResult.Invalid) {
            onError(validation.errorMessage)
            return
        }

        val startMin = ScheduleValidator.parseTimeToMinutes(editedSlot.startTime)!!
        val endMin = ScheduleValidator.parseTimeToMinutes(editedSlot.endTime)!!
        val normalized = editedSlot.copy(
            startTime = ScheduleValidator.formatMinutesToTime(startMin),
            endTime = ScheduleValidator.formatMinutesToTime(endMin)
        )

        viewModelScope.launch {
            val result = slotRepository.updateSlot(normalized)
            if (result.isSuccess) {
                onSuccess(normalized)
            } else {
                onError("Không thể cập nhật khung giờ: ${result.exceptionOrNull()?.localizedMessage}")
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
                    if (observeJob == null || observeJob?.isActive != true) {
                        val bookings = bookingRepository.getShopBookings(shopId).firstOrNull().orEmpty()
                        val data = buildScreenData(
                            schedule = sched,
                            slots = newSlots,
                            allBookings = bookings,
                            shopId = shopId,
                            selectedDate = date,
                            filter = _slotFilter.value
                        )
                        allSlotsForDate = data.slots
                        currentSlotBookings = data.slotBookings
                        updateState(UiState.Success(data))
                    }
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
