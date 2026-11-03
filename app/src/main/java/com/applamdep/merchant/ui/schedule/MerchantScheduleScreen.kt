package com.applamdep.merchant.ui.schedule

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.di.MerchantViewModelFactory
import com.applamdep.merchant.domain.model.SlotItem
import com.applamdep.merchant.domain.model.WorkSchedule
import com.applamdep.merchant.domain.rule.SlotFilterType
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI & TRỪU TƯỢNG
 * GIAI ĐOẠN 4: CHỨC NĂNG 4 & 5
 * - Chức năng 4: Quản lý ca làm việc & ngày nghỉ tuần / lễ tết (Merchant Requirement #4).
 * - Chức năng 5: Quản lý khung giờ trống (Slots), điều chỉnh sức chứa (Capacity) với quy tắc
 *   bảo vệ không thể giảm dưới số đơn khách đã đặt trước, khóa/mở khung giờ thủ công (Merchant Requirement #5).
 */
@Composable
fun MerchantScheduleScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantScheduleViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    shopId: String = "shop_01"
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val activeFilter by viewModel.slotFilter.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Khung giờ Slots, 1: Ca làm việc & Ngày nghỉ

    LaunchedEffect(shopId) {
        viewModel.loadScheduleAndSlots(shopId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MerchantBackground,
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_schedule_title),
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab chuyển đổi: Slots vs Ca làm việc
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MerchantSurface,
                contentColor = MerchantPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = MerchantPrimary
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Text(
                            text = "Khung giờ nhận khách",
                            fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            text = "Cấu hình ca làm việc",
                            fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }

            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> {
                    LoadingStateView(message = "Đang tải lịch & khung giờ...")
                }
                is UiState.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadScheduleAndSlots(shopId) }
                    )
                }
                is UiState.Success -> {
                    val data = state.data
                    if (activeTab == 0) {
                        // TAB 0: QUẢN LÝ KHUNG GIỜ SLOTS (Requirement #5)
                        SlotManagementTab(
                            slots = data.filteredSlots,
                            allSlots = data.slots,
                            selectedDate = selectedDate,
                            activeFilter = activeFilter,
                            onSelectDate = { viewModel.setSelectedDate(it, shopId) },
                            onSelectFilter = { viewModel.setSlotFilter(it) },
                            onUpdateCapacity = { slot, newCap ->
                                viewModel.updateSlotCapacity(
                                    slotId = slot.id,
                                    currentBooked = slot.booked,
                                    newCapacity = newCap,
                                    onSuccess = {
                                        Toast.makeText(context, "Đã cập nhật sức chứa: $newCap khách", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            },
                            onToggleBlock = { slot ->
                                val willBlock = !slot.isBlocked
                                viewModel.toggleSlotBlock(
                                    slotId = slot.id,
                                    isBlocked = willBlock,
                                    onSuccess = {
                                        val msg = if (willBlock) "Đã khóa nhận khách lúc ${slot.startTime}" else "Đã mở lại slot lúc ${slot.startTime}"
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            },
                            onGenerateSlots = {
                                viewModel.generateSlotsFromSchedule(
                                    shopId = shopId,
                                    date = selectedDate,
                                    onSuccess = { count ->
                                        Toast.makeText(context, "Đã sinh thành công $count khung giờ cho ngày $selectedDate", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        )
                    } else {
                        // TAB 1: CẤU HÌNH CA LÀM VIỆC (Requirement #4)
                        ScheduleConfigurationTab(
                            schedule = data.schedule,
                            onSave = { updated ->
                                viewModel.updateSchedule(
                                    schedule = updated,
                                    onSuccess = {
                                        Toast.makeText(context, "Lưu cấu hình ca làm việc thành công!", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                )
                            }
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}

/**
 * Tab 0: Quản lý Slots nhận khách (Requirement #5)
 */
@Composable
private fun SlotManagementTab(
    slots: List<SlotItem>,
    allSlots: List<SlotItem>,
    selectedDate: String,
    activeFilter: SlotFilterType,
    onSelectDate: (String) -> Unit,
    onSelectFilter: (SlotFilterType) -> Unit,
    onUpdateCapacity: (SlotItem, Int) -> Unit,
    onToggleBlock: (SlotItem) -> Unit,
    onGenerateSlots: () -> Unit
) {
    val dateOptions = listOf(
        "2026-10-29" to "Hôm nay (29/10)",
        "2026-10-30" to "Ngày mai (30/10)",
        "2026-10-31" to "Thứ Bảy (31/10)",
        "2026-11-01" to "Chủ nhật (01/11)",
        "2026-11-02" to "Thứ Hai (02/11)"
    )

    val availableCount = allSlots.count { !it.isBlocked && it.booked < it.capacity }
    val fullCount = allSlots.count { !it.isBlocked && it.booked >= it.capacity }
    val blockedCount = allSlots.count { it.isBlocked }

    Column(modifier = Modifier.fillMaxSize()) {
        // Thanh chọn ngày
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MerchantSurface)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Chọn ngày tiếp nhận khách:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MerchantTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dateOptions.forEach { (dateKey, label) ->
                    val isSelected = dateKey == selectedDate
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) MerchantPrimary else MerchantCard)
                            .border(1.dp, if (isSelected) MerchantPrimary else MerchantBorder, RoundedCornerShape(10.dp))
                            .clickable { onSelectDate(dateKey) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MerchantTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bộ lọc trạng thái slot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SlotFilterType.entries.forEach { f ->
                    val isSelected = f == activeFilter
                    val countBadge = when (f) {
                        SlotFilterType.ALL -> allSlots.size
                        SlotFilterType.AVAILABLE -> availableCount
                        SlotFilterType.FULL -> fullCount
                        SlotFilterType.BLOCKED -> blockedCount
                    }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectFilter(f) },
                        label = { Text("${f.displayName} ($countBadge)", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MerchantPrimary.copy(alpha = 0.15f),
                            selectedLabelColor = MerchantPrimary
                        )
                    )
                }
            }
        }

        // Action banner: Thống kê + Nút sinh lại khung giờ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${slots.size} khung giờ hiển thị",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MerchantTextPrimary
            )

            TextButton(
                onClick = onGenerateSlots,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp), tint = MerchantPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Tạo lại theo ca", fontSize = 12.sp, color = MerchantPrimary, fontWeight = FontWeight.Bold)
            }
        }

        if (slots.isEmpty()) {
            EmptyStateView(
                title = "Không có khung giờ nào",
                subtitle = "Bấm 'Tạo lại theo ca' ở trên để tự động phân bổ khung giờ từ ca làm việc."
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(slots, key = { it.id }) { slot ->
                    SlotItemCard(
                        slot = slot,
                        onUpdateCapacity = { newCap -> onUpdateCapacity(slot, newCap) },
                        onToggleBlock = { onToggleBlock(slot) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun SlotItemCard(
    slot: SlotItem,
    onUpdateCapacity: (Int) -> Unit,
    onToggleBlock: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (slot.isBlocked) Color(0xFFF9F9F9) else MerchantCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Khung giờ hiển thị
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    slot.isBlocked -> Color(0xFFE0E0E0)
                                    slot.booked >= slot.capacity -> MerchantError.copy(alpha = 0.15f)
                                    else -> MerchantPrimary.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = when {
                                slot.isBlocked -> MerchantTextHint
                                slot.booked >= slot.capacity -> MerchantError
                                else -> MerchantPrimary
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "${slot.startTime} - ${slot.endTime.ifBlank { "kết thúc" }}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (slot.isBlocked) MerchantTextHint else MerchantTextPrimary
                        )
                        Text(
                            text = "Đã đặt: ${slot.booked}/${slot.capacity} khách (Còn ${slot.remainingCapacity} chỗ)",
                            fontSize = 12.sp,
                            color = when {
                                slot.isBlocked -> MerchantTextHint
                                slot.booked >= slot.capacity -> MerchantError
                                else -> MerchantPrimary
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Trạng thái badge
                when {
                    slot.isBlocked -> {
                        StatusBadge(status = "ĐÃ KHÓA")
                    }
                    slot.booked >= slot.capacity -> {
                        StatusBadge(status = "ĐÃ ĐẦY")
                    }
                    else -> {
                        StatusBadge(status = "CÒN CHỖ")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Thanh tiến trình đặt chỗ
            val progress = if (slot.capacity > 0) (slot.booked.toFloat() / slot.capacity.toFloat()).coerceIn(0f, 1f) else 0f
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = when {
                    slot.isBlocked -> MerchantTextHint
                    progress >= 1f -> MerchantError
                    progress >= 0.7f -> MerchantWarning
                    else -> MerchantPrimary
                },
                trackColor = MerchantBorder
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action row: Điều chỉnh sức chứa (Stepper) + Nút Khóa / Mở slot
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sức chứa (Capacity) Stepper với quy tắc bảo vệ: không giảm thấp hơn booked
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Sức chứa:",
                        fontSize = 12.sp,
                        color = MerchantTextSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            if (slot.capacity - 1 < slot.booked) {
                                Toast.makeText(
                                    context,
                                    "Không thể giảm số lượng phục vụ thấp hơn số đơn đã được khách đặt (${slot.booked} khách).",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onUpdateCapacity(slot.capacity - 1)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircleOutline,
                            contentDescription = "Giảm sức chứa",
                            tint = if (slot.capacity > slot.booked) MerchantPrimary else MerchantTextHint
                        )
                    }

                    Text(
                        text = "${slot.capacity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    IconButton(
                        onClick = { onUpdateCapacity(slot.capacity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Tăng sức chứa",
                            tint = MerchantPrimary
                        )
                    }
                }

                // Nút Khóa / Mở khóa slot
                OutlinedButton(
                    onClick = onToggleBlock,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (slot.isBlocked) MerchantPrimary else MerchantError
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (slot.isBlocked) MerchantPrimary else MerchantError.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = if (slot.isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (slot.isBlocked) stringResource(R.string.merchant_slot_unblock) else stringResource(R.string.merchant_slot_block),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Tab 1: Cấu hình ca làm việc & ngày nghỉ tuần / lễ tết (Requirement #4)
 */
@Composable
private fun ScheduleConfigurationTab(
    schedule: WorkSchedule,
    onSave: (WorkSchedule) -> Unit
) {
    var workingDays by remember { mutableStateOf(schedule.workingDays) }
    var openTime by remember { mutableStateOf(schedule.openTime) }
    var closeTime by remember { mutableStateOf(schedule.closeTime) }
    var hasBreakTime by remember { mutableStateOf(!schedule.breakStartTime.isNullOrBlank()) }
    var breakStart by remember { mutableStateOf(schedule.breakStartTime ?: "12:00") }
    var breakEnd by remember { mutableStateOf(schedule.breakEndTime ?: "13:00") }
    var slotDuration by remember { mutableStateOf(schedule.slotDurationMinutes) }
    var defaultCapacity by remember { mutableStateOf(schedule.defaultCapacity) }
    var offDates by remember { mutableStateOf(schedule.offDates) }
    var newOffDateInput by remember { mutableStateOf("") }

    val daysOfWeek = listOf(
        2 to "Thứ 2",
        3 to "Thứ 3",
        4 to "Thứ 4",
        5 to "Thứ 5",
        6 to "Thứ 6",
        7 to "Thứ 7",
        8 to "Chủ nhật"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Ngày làm việc trong tuần
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.merchant_work_days),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Bật/tắt các ngày mở cửa hoạt động nhận khách trong tuần:",
                    fontSize = 12.sp,
                    color = MerchantTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    daysOfWeek.forEach { (dayInt, label) ->
                        val isSelected = workingDays.contains(dayInt)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                workingDays = if (isSelected) {
                                    workingDays.filterNot { it == dayInt }
                                } else {
                                    (workingDays + dayInt).sorted()
                                }
                            },
                            label = { Text(label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // 2. Giờ mở cửa & Đóng cửa
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.merchant_work_hours),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = openTime,
                        onValueChange = { openTime = it },
                        label = { Text("Giờ mở cửa (HH:mm)") },
                        placeholder = { Text("08:30") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = closeTime,
                        onValueChange = { closeTime = it },
                        label = { Text("Giờ đóng cửa (HH:mm)") },
                        placeholder = { Text("21:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Thời gian nghỉ giữa ca
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.merchant_break_time),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MerchantTextPrimary
                        )
                        Text(
                            text = "Tạm ngưng nhận lịch trong khoảng nghỉ trưa",
                            fontSize = 12.sp,
                            color = MerchantTextSecondary
                        )
                    }
                    Switch(
                        checked = hasBreakTime,
                        onCheckedChange = { hasBreakTime = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MerchantPrimary
                        )
                    )
                }

                if (hasBreakTime) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = breakStart,
                            onValueChange = { breakStart = it },
                            label = { Text("Bắt đầu nghỉ (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = breakEnd,
                            onValueChange = { breakEnd = it },
                            label = { Text("Kết thúc nghỉ (HH:mm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Thời lượng slot & Sức chứa mặc định
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Thời lượng & Sức chứa mặc định",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Thời lượng mỗi khung giờ:",
                    fontSize = 12.sp,
                    color = MerchantTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30, 45, 60, 90).forEach { dur ->
                        FilterChip(
                            selected = slotDuration == dur,
                            onClick = { slotDuration = dur },
                            label = { Text("$dur phút", fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Sức chứa tối đa mặc định (khách/slot):",
                    fontSize = 12.sp,
                    color = MerchantTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (defaultCapacity > 1) defaultCapacity-- },
                        enabled = defaultCapacity > 1
                    ) {
                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = MerchantPrimary)
                    }
                    Text(
                        text = "$defaultCapacity khách",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    IconButton(
                        onClick = { if (defaultCapacity < 20) defaultCapacity++ }
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = MerchantPrimary)
                    }
                }
            }
        }

        // 5. Ngày nghỉ lễ tết / đột xuất
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.merchant_special_holidays),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hệ thống sẽ tự động khóa toàn bộ slot trong những ngày này:",
                    fontSize = 12.sp,
                    color = MerchantTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newOffDateInput,
                        onValueChange = { newOffDateInput = it },
                        placeholder = { Text("YYYY-MM-DD (Ví dụ: 2026-11-20)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newOffDateInput.isNotBlank() && !offDates.contains(newOffDateInput.trim())) {
                                offDates = offDates + newOffDateInput.trim()
                                newOffDateInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary)
                    ) {
                        Text("Thêm")
                    }
                }

                if (offDates.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        offDates.forEach { offDay ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MerchantBackground)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = offDay, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MerchantTextPrimary)
                                IconButton(
                                    onClick = { offDates = offDates.filterNot { it == offDay } },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Xóa", tint = MerchantError, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                val updated = schedule.copy(
                    workingDays = workingDays,
                    openTime = openTime.trim(),
                    closeTime = closeTime.trim(),
                    breakStartTime = if (hasBreakTime) breakStart.trim() else null,
                    breakEndTime = if (hasBreakTime) breakEnd.trim() else null,
                    slotDurationMinutes = slotDuration,
                    defaultCapacity = defaultCapacity,
                    offDates = offDates
                )
                onSave(updated)
            },
            colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = "Lưu cấu hình ca làm việc", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
