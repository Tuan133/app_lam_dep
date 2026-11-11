package com.applamdep.merchant.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.ui.theme.*
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import java.util.Calendar
import java.util.Locale

internal enum class ShopClosureMode {
    UNTIL_REOPEN,
    SCHEDULED_DATE_TIME
}

internal enum class ShopClosureWindow(val label: String, val startMin: Int, val endMin: Int) {
    FULL_DAY("Cả ngày (08:30 - 21:00)", 0, 24 * 60),
    MORNING("Buổi sáng (08:30 - 12:00)", 0, 12 * 60),
    AFTERNOON("Buổi chiều - tối (13:00 - 21:00)", 12 * 60, 24 * 60)
}

internal enum class ShopClosureStep {
    MODE_SELECTION,
    CONFIRM_UNTIL_REOPEN,
    CLOSE_REASON,
    CLOSE_SUCCESS,
    SELECT_DATE_TIME,
    BOOKINGS_IN_WINDOW,
    CANCEL_BOOKINGS_REASON,
    CONFIRM_CANCEL_BOOKINGS,
    CANCEL_SUCCESS
}

private val CLOSE_REASONS = listOf(
    "Cửa hàng bận đột xuất",
    "Nghỉ lễ / Sự kiện cá nhân",
    "Bảo trì / Sửa chữa cửa hàng",
    "Thiếu nhân sự tạm thời",
    "Lý do khác"
)

private val CANCEL_BOOKING_REASONS = listOf(
    "Khách hàng đổi lịch",
    "Cửa hàng đóng cửa đột xuất",
    "Bảo trì / Nâng cấp tiệm",
    "Kỹ thuật viên bận đột xuất",
    "Lý do khác"
)

private val ACTIVE_BOOKING_STATUSES = setOf(
    MerchantConstants.BOOKING_STATUS_PENDING,
    MerchantConstants.BOOKING_STATUS_CONFIRMED,
    MerchantConstants.BOOKING_STATUS_IN_SERVICE
)

/**
 * Sơ đồ Luồng đi Đóng cửa & Huỷ lịch hẹn (10 bước theo đặc tả):
 * - Nhánh 1 ("Chọn đến khi tôi mở lại"): Bước 2 -> 3 -> 4 -> 5.
 * - Nhánh 2 ("Chọn thời gian đóng cửa"): Bước 2 -> 6 -> 7 -> 8 -> 9 -> 10.
 */
@Composable
internal fun ShopClosureFlowDialog(
    bookings: List<Booking>,
    onConfirmUntilReopen: (reason: String, note: String, onDone: () -> Unit) -> Unit,
    onConfirmScheduledClosureAndCancel: (
        date: String,
        bookingIds: List<String>,
        cancelReason: String,
        proposalNote: String,
        onDone: () -> Unit
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(ShopClosureStep.MODE_SELECTION) }
    var selectedMode by remember { mutableStateOf(ShopClosureMode.UNTIL_REOPEN) }

    // Nhánh A state
    var closeReason by remember { mutableStateOf(CLOSE_REASONS.first()) }
    var closeNote by remember { mutableStateOf("") }

    // Nhánh B state
    var selectedYear by remember { mutableIntStateOf(2026) }
    var selectedMonth by remember { mutableIntStateOf(10) } // 1..12
    var selectedDay by remember { mutableIntStateOf(29) }
    var selectedWindow by remember { mutableStateOf(ShopClosureWindow.FULL_DAY) }
    var selectedBookingIds by remember { mutableStateOf(setOf<String>()) }
    var cancelReason by remember { mutableStateOf(CANCEL_BOOKING_REASONS.first()) }
    var cancelProposal by remember { mutableStateOf("") }

    val selectedIsoDate = remember(selectedYear, selectedMonth, selectedDay) {
        String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
    }
    val selectedDisplayDate = remember(selectedYear, selectedMonth, selectedDay) {
        String.format(Locale.US, "%02d/%02d/%04d", selectedDay, selectedMonth, selectedYear)
    }

    val bookingsOnDate = remember(bookings, selectedIsoDate, selectedWindow) {
        filterActiveBookingsInWindow(
            bookings = bookings,
            isoDate = selectedIsoDate,
            window = selectedWindow
        )
    }

    when (step) {
        ShopClosureStep.MODE_SELECTION -> {
            ClosureModeSelectionDialog(
                selectedMode = selectedMode,
                onSelectMode = { selectedMode = it },
                onConfirm = {
                    step = if (selectedMode == ShopClosureMode.UNTIL_REOPEN) {
                        ShopClosureStep.CONFIRM_UNTIL_REOPEN
                    } else {
                        ShopClosureStep.SELECT_DATE_TIME
                    }
                },
                onDismiss = onDismiss
            )
        }

        ShopClosureStep.CONFIRM_UNTIL_REOPEN -> {
            DangerConfirmationDialog(
                message = "Bạn có chắc chắn muốn đóng cửa không?",
                confirmText = "OK",
                dismissText = "Huỷ",
                onConfirm = { step = ShopClosureStep.CLOSE_REASON },
                onDismiss = { step = ShopClosureStep.MODE_SELECTION },
                onClose = onDismiss
            )
        }

        ShopClosureStep.CLOSE_REASON -> {
            ReasonSelectionDialog(
                title = "Lý do đóng cửa",
                reasons = CLOSE_REASONS,
                selectedReason = closeReason,
                onSelectReason = { closeReason = it },
                noteLabel = "Ghi chú thêm (không bắt buộc):",
                notePlaceholder = "Nhập ghi chú...",
                noteValue = closeNote,
                onNoteChange = { closeNote = it },
                confirmText = "Xác nhận",
                dismissText = null,
                onConfirm = {
                    onConfirmUntilReopen(closeReason, closeNote) {
                        step = ShopClosureStep.CLOSE_SUCCESS
                    }
                },
                onDismiss = onDismiss
            )
        }

        ShopClosureStep.CLOSE_SUCCESS -> {
            SuccessNoticeDialog(
                title = "Đóng cửa thành công!",
                subtitle = "Cửa hàng đã được đặt ở trạng thái đóng cửa.",
                onOk = onDismiss
            )
        }

        ShopClosureStep.SELECT_DATE_TIME -> {
            ClosureCalendarDialog(
                year = selectedYear,
                month = selectedMonth,
                day = selectedDay,
                selectedDisplayDate = selectedDisplayDate,
                selectedWindow = selectedWindow,
                activeBookingDates = bookings
                    .filter { it.status in ACTIVE_BOOKING_STATUSES }
                    .map { it.bookingDate.ifBlank { "2026-10-29" } }
                    .toSet(),
                onChangeMonth = { delta ->
                    val cal = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth - 1, 1)
                        add(Calendar.MONTH, delta)
                    }
                    selectedYear = cal.get(Calendar.YEAR)
                    selectedMonth = cal.get(Calendar.MONTH) + 1
                    val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                    if (selectedDay > maxDay) selectedDay = maxDay
                },
                onSelectDay = { selectedDay = it },
                onSelectWindow = { selectedWindow = it },
                onOk = {
                    selectedBookingIds = emptySet()
                    step = ShopClosureStep.BOOKINGS_IN_WINDOW
                },
                onCancel = onDismiss
            )
        }

        ShopClosureStep.BOOKINGS_IN_WINDOW -> {
            BookingsInClosureWindowDialog(
                selectedDisplayDate = selectedDisplayDate,
                selectedWindow = selectedWindow,
                bookings = bookingsOnDate,
                selectedIds = selectedBookingIds,
                onToggleAll = {
                    selectedBookingIds = if (selectedBookingIds.size == bookingsOnDate.size) {
                        emptySet()
                    } else {
                        bookingsOnDate.map { it.id }.toSet()
                    }
                },
                onToggleItem = { id ->
                    selectedBookingIds = if (id in selectedBookingIds) {
                        selectedBookingIds - id
                    } else {
                        selectedBookingIds + id
                    }
                },
                onProceedCancel = { step = ShopClosureStep.CANCEL_BOOKINGS_REASON },
                onCloseWithoutBookings = {
                    onConfirmScheduledClosureAndCancel(
                        selectedIsoDate,
                        emptyList(),
                        "Đóng cửa ngày $selectedDisplayDate (${selectedWindow.label})",
                        "",
                        { step = ShopClosureStep.CLOSE_SUCCESS }
                    )
                },
                onDismiss = onDismiss
            )
        }

        ShopClosureStep.CANCEL_BOOKINGS_REASON -> {
            ReasonSelectionDialog(
                title = "Lý do huỷ lịch hẹn",
                reasons = CANCEL_BOOKING_REASONS,
                selectedReason = cancelReason,
                onSelectReason = { cancelReason = it },
                noteLabel = "Đề xuất (không bắt buộc):",
                notePlaceholder = "Nhập đề xuất của bạn...",
                noteValue = cancelProposal,
                onNoteChange = { cancelProposal = it },
                confirmText = "Xác nhận",
                dismissText = "Huỷ",
                onConfirm = { step = ShopClosureStep.CONFIRM_CANCEL_BOOKINGS },
                onDismiss = { step = ShopClosureStep.BOOKINGS_IN_WINDOW }
            )
        }

        ShopClosureStep.CONFIRM_CANCEL_BOOKINGS -> {
            DangerConfirmationDialog(
                message = "Bạn có chắc chắn muốn huỷ các lịch hẹn đã chọn?",
                detailText = "${selectedBookingIds.size} lịch hẹn ngày $selectedDisplayDate sẽ được hủy và hoàn tiền cọc cho khách.",
                confirmText = "OK",
                dismissText = "Huỷ",
                onConfirm = {
                    onConfirmScheduledClosureAndCancel(
                        selectedIsoDate,
                        selectedBookingIds.toList(),
                        cancelReason,
                        cancelProposal
                    ) {
                        step = ShopClosureStep.CANCEL_SUCCESS
                    }
                },
                onDismiss = { step = ShopClosureStep.CANCEL_BOOKINGS_REASON },
                onClose = onDismiss
            )
        }

        ShopClosureStep.CANCEL_SUCCESS -> {
            SuccessNoticeDialog(
                title = "Huỷ lịch hẹn thành công!",
                subtitle = "Các lịch hẹn đã chọn đã được huỷ.",
                onOk = onDismiss
            )
        }
    }
}

@Composable
private fun ClosureModeSelectionDialog(
    selectedMode: ShopClosureMode,
    onSelectMode: (ShopClosureMode) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = "Đóng cửa",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MerchantTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClosureModeOptionButton(
                        label = "Chọn đến khi\ntôi mở lại",
                        icon = Icons.Default.AccessTime,
                        selected = selectedMode == ShopClosureMode.UNTIL_REOPEN,
                        onClick = { onSelectMode(ShopClosureMode.UNTIL_REOPEN) },
                        modifier = Modifier.weight(1f)
                    )
                    ClosureModeOptionButton(
                        label = "Chọn thời gian\nđóng cửa",
                        icon = Icons.Default.CalendarToday,
                        selected = selectedMode == ShopClosureMode.SCHEDULED_DATE_TIME,
                        onClick = { onSelectMode(ShopClosureMode.SCHEDULED_DATE_TIME) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MerchantPrimary.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = MerchantPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MerchantError)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Text(
                    text = "Bạn có thể chọn cách đóng cửa phù hợp với cửa hàng của bạn.",
                    fontSize = 13.sp,
                    color = MerchantTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = "Xác nhận", fontWeight = FontWeight.Bold, color = Color.White)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MerchantBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(42.dp)
                ) {
                    Text(text = "Huỷ", fontWeight = FontWeight.SemiBold, color = MerchantTextPrimary)
                }
            }
        }
    )
}

@Composable
private fun ClosureModeOptionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) MerchantPrimary else MerchantBackground)
            .border(
                width = 1.dp,
                color = if (selected) MerchantPrimary else MerchantBorder,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else MerchantPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else MerchantTextPrimary
        )
    }
}

@Composable
private fun DangerConfirmationDialog(
    message: String,
    detailText: String? = null,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = Color.White,
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MerchantTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MerchantError),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = message,
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary,
                    textAlign = TextAlign.Center
                )
                if (!detailText.isNullOrBlank()) {
                    Text(
                        text = detailText,
                        fontSize = 12.5.sp,
                        color = MerchantTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    border = BorderStroke(1.dp, MerchantBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(text = dismissText, color = MerchantTextPrimary, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(text = confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun ReasonSelectionDialog(
    title: String,
    reasons: List<String>,
    selectedReason: String,
    onSelectReason: (String) -> Unit,
    noteLabel: String,
    notePlaceholder: String,
    noteValue: String,
    onNoteChange: (String) -> Unit,
    confirmText: String,
    dismissText: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MerchantTextPrimary)
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MerchantTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Vui lòng chọn lý do:",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MerchantTextSecondary
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MerchantBorder, RoundedCornerShape(10.dp))
                            .clickable { dropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedReason,
                            fontSize = 13.sp,
                            color = MerchantTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Chọn lý do",
                            tint = MerchantTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        reasons.forEach { reason ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = reason,
                                        fontSize = 13.sp,
                                        fontWeight = if (reason == selectedReason) FontWeight.Bold else FontWeight.Normal,
                                        color = if (reason == selectedReason) MerchantPrimary else MerchantTextPrimary
                                    )
                                },
                                onClick = {
                                    onSelectReason(reason)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = noteLabel,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MerchantTextSecondary
                )

                OutlinedTextField(
                    value = noteValue,
                    onValueChange = onNoteChange,
                    placeholder = { Text(notePlaceholder, fontSize = 13.sp, color = MerchantTextHint) },
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            if (dismissText != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, MerchantBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(text = dismissText, color = MerchantTextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(42.dp)
                    ) {
                        Text(text = confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = confirmText, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun ClosureCalendarDialog(
    year: Int,
    month: Int,
    day: Int,
    selectedDisplayDate: String,
    selectedWindow: ShopClosureWindow,
    activeBookingDates: Set<String>,
    onChangeMonth: (Int) -> Unit,
    onSelectDay: (Int) -> Unit,
    onSelectWindow: (ShopClosureWindow) -> Unit,
    onOk: () -> Unit,
    onCancel: () -> Unit
) {
    val firstDayCal = remember(year, month) {
        Calendar.getInstance().apply { set(year, month - 1, 1) }
    }
    val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startOffset = remember(year, month) {
        // Calendar.MONDAY = 2 .. Calendar.SUNDAY = 1 -> 0..6 (T2..CN)
        val dow = firstDayCal.get(Calendar.DAY_OF_WEEK)
        if (dow == Calendar.SUNDAY) 6 else dow - Calendar.MONDAY
    }
    val dayHeaders = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

    AlertDialog(
        onDismissRequest = onCancel,
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chọn thời gian đóng cửa",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                IconButton(onClick = onCancel, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MerchantTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MerchantPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(text = "Chọn ngày", fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = MerchantTextPrimary)
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, MerchantBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = selectedDisplayDate,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantPrimary
                        )
                    }
                }

                // Header tháng + mũi tên chuyển tháng
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onChangeMonth(-1) }, modifier = Modifier.size(28.dp)) {
                        GlowIcon(icon = GlowIcons.ChevronLeft, tint = MerchantTextPrimary, size = 16.dp)
                    }
                    Text(
                        text = "Tháng $month, $year",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                    IconButton(onClick = { onChangeMonth(1) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Tháng sau", tint = MerchantTextPrimary)
                    }
                }

                // Lịch các thứ trong tuần
                Row(modifier = Modifier.fillMaxWidth()) {
                    dayHeaders.forEach { d ->
                        Text(
                            text = d,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                val totalCells = startOffset + daysInMonth
                val rowCount = (totalCells + 6) / 7
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (r in 0 until rowCount) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (c in 0..6) {
                                val cellIdx = r * 7 + c
                                val cellDay = cellIdx - startOffset + 1
                                if (cellDay in 1..daysInMonth) {
                                    val cellIso = String.format(Locale.US, "%04d-%02d-%02d", year, month, cellDay)
                                    val isSelected = cellDay == day
                                    val hasBooking = cellIso in activeBookingDates
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) MerchantPrimary else Color.Transparent)
                                            .clickable { onSelectDay(cellDay) }
                                            .padding(vertical = 5.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = cellDay.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MerchantTextPrimary
                                        )
                                        if (hasBooking) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else MerchantError)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MerchantDivider)

                Text(
                    text = "Khung giờ đóng cửa:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MerchantTextSecondary
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShopClosureWindow.entries.forEach { win ->
                        val picked = win == selectedWindow
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (picked) MerchantPrimary.copy(alpha = 0.08f) else MerchantBackground)
                                .border(
                                    width = 1.dp,
                                    color = if (picked) MerchantPrimary else MerchantBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectWindow(win) }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = win.label,
                                fontSize = 11.5.sp,
                                fontWeight = if (picked) FontWeight.Bold else FontWeight.Normal,
                                color = if (picked) MerchantPrimary else MerchantTextPrimary
                            )
                            if (picked) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MerchantPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOk,
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(text = "OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onCancel,
                    border = BorderStroke(1.dp, MerchantBorder),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(42.dp)
                ) {
                    Text(text = "Huỷ", color = MerchantTextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    )
}

@Composable
private fun BookingsInClosureWindowDialog(
    selectedDisplayDate: String,
    selectedWindow: ShopClosureWindow,
    bookings: List<Booking>,
    selectedIds: Set<String>,
    onToggleAll: () -> Unit,
    onToggleItem: (String) -> Unit,
    onProceedCancel: () -> Unit,
    onCloseWithoutBookings: () -> Unit,
    onDismiss: () -> Unit
) {
    val allSelected = bookings.isNotEmpty() && selectedIds.size == bookings.size

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Danh sách lịch hẹn (trong giờ đóng cửa)",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MerchantTextSecondary)
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedDisplayDate · ${selectedWindow.label}",
                        fontSize = 11.sp,
                        color = MerchantTextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    if (bookings.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (allSelected) MerchantPrimary else MerchantPrimary.copy(alpha = 0.12f))
                                .clickable { onToggleAll() }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Tất cả",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (allSelected) Color.White else MerchantPrimary
                            )
                        }
                    }
                }
            }
        },
        text = {
            if (bookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MerchantBackground)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Không có lịch hẹn nào đang hoạt động trong khung giờ đóng cửa ngày $selectedDisplayDate.",
                        fontSize = 12.5.sp,
                        color = MerchantTextSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bookings.forEach { bk ->
                        val checked = bk.id in selectedIds
                        val slotTimeLabel = resolveBookingDisplaySlotTime(bk)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (checked) MerchantPrimary.copy(alpha = 0.08f) else MerchantBackground)
                                .border(
                                    width = 1.dp,
                                    color = if (checked) MerchantPrimary else MerchantBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onToggleItem(bk.id) }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(if (checked) MerchantPrimary else Color.Transparent)
                                    .border(
                                        width = 1.5.dp,
                                        color = if (checked) MerchantPrimary else MerchantTextHint,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (checked) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$slotTimeLabel | Khách hàng ${bk.customerName}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MerchantTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "#${bk.id} · ${bk.serviceNames.joinToString(", ")}",
                                    fontSize = 11.sp,
                                    color = MerchantTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (bookings.isNotEmpty()) {
                Button(
                    onClick = onProceedCancel,
                    enabled = selectedIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = "Huỷ lịch hẹn (${selectedIds.size})", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onCloseWithoutBookings,
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text(text = "Xác nhận đóng cửa", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    )
}

@Composable
private fun SuccessNoticeDialog(
    title: String,
    subtitle: String,
    onOk: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onOk,
        containerColor = Color.White,
        title = {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onOk,
                    modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Đóng", tint = MerchantTextSecondary)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MerchantTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onOk,
                colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(text = "OK", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    )
}

internal fun filterActiveBookingsInWindow(
    bookings: List<Booking>,
    isoDate: String,
    window: ShopClosureWindow
): List<Booking> {
    return bookings.filter { bk ->
        if (bk.status !in ACTIVE_BOOKING_STATUSES) return@filter false
        val matchesDate = if (bk.bookingDate.isNotBlank()) {
            bk.bookingDate == isoDate
        } else {
            isoDate == "2026-10-29"
        }
        if (!matchesDate) return@filter false
        if (window == ShopClosureWindow.FULL_DAY) return@filter true
        val startMin = resolveBookingStartMinutes(bk)
        startMin in window.startMin until window.endMin
    }
}

private fun resolveBookingStartMinutes(booking: Booking): Int {
    val bareSlot = booking.slotId.removePrefix("${booking.bookingDate}_")
    return when (bareSlot) {
        "slot_01" -> 9 * 60
        "slot_02" -> 10 * 60 + 30
        "slot_03" -> 13 * 60 + 30
        "slot_04" -> 15 * 60
        "slot_05" -> 16 * 60 + 30
        "slot_06" -> 18 * 60
        "slot_07" -> 19 * 60 + 30
        else -> 9 * 60
    }
}

private fun resolveBookingDisplaySlotTime(booking: Booking): String {
    val bareSlot = booking.slotId.removePrefix("${booking.bookingDate}_")
    return when (bareSlot) {
        "slot_01" -> "09:00 - 10:00"
        "slot_02" -> "10:30 - 11:30"
        "slot_03" -> "13:30 - 14:30"
        "slot_04" -> "15:00 - 16:00"
        "slot_05" -> "16:30 - 17:30"
        "slot_06" -> "18:00 - 19:00"
        "slot_07" -> "19:30 - 20:30"
        else -> if (booking.startAt > 0L && booking.endAt > booking.startAt) {
            "${Formatters.formatTime(booking.startAt)} - ${Formatters.formatTime(booking.endAt)}"
        } else {
            "Trong ngày"
        }
    }
}

