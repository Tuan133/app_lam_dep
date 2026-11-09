package com.applamdep.customer.ui.booking

import androidx.lifecycle.viewModelScope
import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.model.ServiceItem
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.domain.model.ShopSlot
import com.applamdep.customer.domain.policy.SlotConflictResolver
import com.applamdep.customer.domain.repository.BookingRepository
import com.applamdep.customer.domain.repository.ShopRepository
import com.applamdep.customer.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * State dữ liệu của màn hình Đặt lịch ("Chọn lịch")
 */
data class BookingFormData(
    val shop: Shop,
    val selectedService: ServiceItem,
    val availableDates: List<String>,
    val selectedDate: String,
    val availableSlots: List<ShopSlot>,
    val selectedSlot: ShopSlot? = null,
    val customerName: String = "Khánh Linh",
    val customerPhone: String = "0903 123 456",
    val customerNote: String = "",
    val selectedDepositAmount: Long = 0L,
    val nameError: String? = null,
    val phoneError: String? = null,
    val slotError: String? = null,
    val isSubmitting: Boolean = false,
    val bookingSuccessId: String? = null
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (BaseViewModel)
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Chỉ phụ thuộc Repository Interfaces và Policy)
 */
class BookingViewModel(
    private val shopRepository: ShopRepository,
    private val bookingRepository: BookingRepository,
    private val walletRepository: WalletRepository,
    private val slotConflictResolver: SlotConflictResolver
) : BaseViewModel<BookingFormData>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI - Form StateFlow nội bộ
    private val _formData = MutableStateFlow<BookingFormData?>(null)
    val formData: StateFlow<BookingFormData?> = _formData.asStateFlow()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun initBooking(shopId: String, serviceId: String) {
        launchWithLoading {
            val shop = shopRepository.getShopById(shopId).getOrThrow()
            var targetService: ServiceItem? = null

            // Lấy danh sách dịch vụ để tìm serviceId
            shopRepository.getShopServices(shopId).collect { services ->
                targetService = services.find { it.id == serviceId } ?: services.firstOrNull()

                if (targetService != null) {
                    val dates = generateNext7Days()
                    val initialDate = dates.first()

                    // Nạp khung giờ của ngày đầu tiên
                    loadSlotsForDate(shop, targetService!!, initialDate, dates)
                }
            }
        }
    }

    private fun loadSlotsForDate(
        shop: Shop,
        service: ServiceItem,
        date: String,
        availableDates: List<String>
    ) {
        viewModelScope.launch {
            shopRepository.getShopSlots(shop.id, date).collect { rawSlots ->
                // 1. TÍNH CHẤT OOP: ĐÓNG GÓI NGHIỆP VỤ XUNG ĐỘT KHUNG GIỜ
                // Gọi SlotConflictResolver để lọc:
                // - Ẩn khung giờ hết chỗ (bookedCount >= maxCapacity)
                // - Ẩn khung giờ đã qua thời điểm hiện tại nếu là hôm nay
                val validSlots = slotConflictResolver.resolveAvailableSlots(
                    allSlots = rawSlots,
                    bookingDate = date,
                    serviceDurationMinutes = service.durationMinutes
                )

                val defaultDeposit = if (shop.requireDeposit && shop.depositOptions.isNotEmpty()) {
                    shop.depositOptions.first()
                } else 0L

                val current = _formData.value
                val updatedData = current?.copy(
                    selectedDate = date,
                    availableSlots = validSlots,
                    selectedSlot = null,
                    slotError = null
                ) ?: BookingFormData(
                    shop = shop,
                    selectedService = service,
                    availableDates = availableDates,
                    selectedDate = date,
                    availableSlots = validSlots,
                    selectedDepositAmount = defaultDeposit
                )

                _formData.value = updatedData
                updateState(UiState.Success(updatedData))
            }
        }
    }

    fun onDateSelected(newDate: String) {
        val current = _formData.value ?: return
        loadSlotsForDate(current.shop, current.selectedService, newDate, current.availableDates)
    }

    fun onSlotSelected(slot: ShopSlot) {
        _formData.value = _formData.value?.copy(
            selectedSlot = slot,
            slotError = null
        )
        _formData.value?.let { updateState(UiState.Success(it)) }
    }

    fun onDepositSelected(amount: Long) {
        _formData.value = _formData.value?.copy(selectedDepositAmount = amount)
        _formData.value?.let { updateState(UiState.Success(it)) }
    }

    fun onNameChanged(name: String) {
        _formData.value = _formData.value?.copy(customerName = name, nameError = null)
        _formData.value?.let { updateState(UiState.Success(it)) }
    }

    fun onPhoneChanged(phone: String) {
        _formData.value = _formData.value?.copy(customerPhone = phone, phoneError = null)
        _formData.value?.let { updateState(UiState.Success(it)) }
    }

    fun onNoteChanged(note: String) {
        _formData.value = _formData.value?.copy(customerNote = note)
        _formData.value?.let { updateState(UiState.Success(it)) }
    }

    /**
     * Xác thực thông tin form đặt lịch (Validate)
     */
    private fun validateForm(): Boolean {
        val current = _formData.value ?: return false
        var isValid = true
        var nameErr: String? = null
        var phoneErr: String? = null
        var slotErr: String? = null

        if (current.customerName.isBlank()) {
            nameErr = "Vui lòng nhập họ và tên của bạn"
            isValid = false
        }

        if (current.customerPhone.isBlank() || current.customerPhone.length < 9) {
            phoneErr = "Vui lòng nhập số điện thoại hợp lệ (tối thiểu 9 số)"
            isValid = false
        }

        if (current.selectedSlot == null) {
            slotErr = "Vui lòng chọn khung giờ hẹn phù hợp"
            isValid = false
        }

        _formData.value = current.copy(
            nameError = nameErr,
            phoneError = phoneErr,
            slotError = slotErr
        )
        _formData.value?.let { updateState(UiState.Success(it)) }

        return isValid
    }

    /**
     * Thực hiện gửi đặt lịch (Tạo Booking và Giữ cọc HELD nếu có)
     */
    fun submitBooking(customerId: String, onSuccess: (bookingId: String) -> Unit) {
        if (!validateForm()) return

        val current = _formData.value ?: return

        viewModelScope.launch {
            _formData.value = current.copy(isSubmitting = true)
            _formData.value?.let { updateState(UiState.Success(it)) }

            val bookingId = "BK-" + (1000..9999).random()
            val appointmentTimeMillis = parseAppointmentTimestamp(
                dateStr = current.selectedDate,
                timeStr = current.selectedSlot!!.startTime
            )

            val newBooking = Booking(
                id = bookingId,
                customerId = customerId,
                customerName = current.customerName.trim(),
                customerPhone = current.customerPhone.trim(),
                customerNote = current.customerNote.trim(),
                shopId = current.shop.id,
                shopName = current.shop.name,
                shopAddress = current.shop.address,
                serviceId = current.selectedService.id,
                serviceName = current.selectedService.name,
                servicePrice = current.selectedService.price,
                bookingDate = current.selectedDate,
                bookingTime = current.selectedSlot.startTime,
                appointmentTimestamp = appointmentTimeMillis,
                durationMinutes = current.selectedService.durationMinutes,
                requiresDeposit = current.shop.requireDeposit,
                depositAmount = if (current.shop.requireDeposit) current.selectedDepositAmount else 0L,
                depositTransactionId = if (current.shop.requireDeposit) "TX-DEP-$bookingId" else null,
                status = AppConstants.BOOKING_STATUS_CONFIRMED
            )

            // 1. Nếu có cọc: Sàn giữ cọc (HELD)
            if (newBooking.requiresDeposit && newBooking.depositAmount > 0) {
                val holdResult = walletRepository.holdDeposit(
                    userId = customerId,
                    bookingId = bookingId,
                    shopId = current.shop.id,
                    amount = newBooking.depositAmount
                )
                if (holdResult.isFailure) {
                    _formData.value = current.copy(isSubmitting = false)
                    handleError(holdResult.exceptionOrNull() ?: Exception("Lỗi trừ cọc ví"))
                    return@launch
                }
            }

            // 2. Tạo booking trên Firestore/Repository
            val bookingResult = bookingRepository.createBooking(newBooking)
            if (bookingResult.isSuccess) {
                _formData.value = current.copy(isSubmitting = false, bookingSuccessId = bookingId)
                onSuccess(bookingId)
            } else {
                _formData.value = current.copy(isSubmitting = false)
                handleError(bookingResult.exceptionOrNull() ?: Exception("Lỗi tạo booking"))
            }
        }
    }

    private fun generateNext7Days(): List<String> {
        val list = mutableListOf<String>()
        val cal = Calendar.getInstance()
        for (i in 0..6) {
            list.add(dateFormat.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    private fun parseAppointmentTimestamp(dateStr: String, timeStr: String): Long {
        val fullFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return try {
            fullFormat.parse("$dateStr $timeStr")?.time ?: (System.currentTimeMillis() + 3600000L)
        } catch (e: Exception) {
            System.currentTimeMillis() + 3600000L
        }
    }
}
