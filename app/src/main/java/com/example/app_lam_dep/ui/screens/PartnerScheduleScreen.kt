package com.example.app_lam_dep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ApplamdepTheme
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted
import com.example.app_lam_dep.ui.theme.WarningOrange
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale

data class SlotItem(val time: String, var status: String, var customer: String, var maxSlots: Int = 2, var bookedSlots: Int = 1)

@Composable
fun PartnerScheduleScreen(
    modifier: Modifier = Modifier
) {
    var selectedCalendarView by remember { mutableStateOf(0) } // 0: Lịch Tuần, 1: Lịch Tháng
    var selectedDayIdx by remember { mutableStateOf(0) }
    
    // Quản lý trạng thái mở Overlay Sửa/Thêm Lịch
    var showEditSchedule by remember { mutableStateOf(false) }
    var editInitialDateIdx by remember { mutableStateOf(0) }
    var editInitialTime by remember { mutableStateOf("09:00 - 10:15") }
    
    // Auto-generate 7 days from today (Vietnam Time)
    val days = remember {
        val timeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        val calendar = Calendar.getInstance(timeZone)
        val format = SimpleDateFormat("dd/MM", Locale.getDefault())
        format.timeZone = timeZone
        
        (0..6).map { offset ->
            if (offset > 0) calendar.add(Calendar.DAY_OF_YEAR, 1)
            val dateStr = format.format(calendar.time)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 (Sun) - 7 (Sat)
            
            val shortName = when(dayOfWeek) {
                Calendar.MONDAY -> "T2"
                Calendar.TUESDAY -> "T3"
                Calendar.WEDNESDAY -> "T4"
                Calendar.THURSDAY -> "T5"
                Calendar.FRIDAY -> "T6"
                Calendar.SATURDAY -> "T7"
                Calendar.SUNDAY -> "CN"
                else -> ""
            }
            val fullName = when(dayOfWeek) {
                Calendar.MONDAY -> "Thứ Hai"
                Calendar.TUESDAY -> "Thứ Ba"
                Calendar.WEDNESDAY -> "Thứ Tư"
                Calendar.THURSDAY -> "Thứ Năm"
                Calendar.FRIDAY -> "Thứ Sáu"
                Calendar.SATURDAY -> "Thứ Bảy"
                Calendar.SUNDAY -> "Chủ Nhật"
                else -> ""
            }
            
            if (offset == 0) "$shortName (Hôm nay)\n$dateStr" else "$fullName\n$dateStr"
        }
    }

    val slotsByDay = remember {
        mutableStateMapOf(
            0 to mutableStateListOf(
                SlotItem("09:00 - 10:15", "booked", "Hà An (Đã cọc 50k)", 2, 1),
                SlotItem("10:30 - 11:45", "available", "", 3, 0),
                SlotItem("13:00 - 14:15", "available", "", 2, 0),
                SlotItem("15:30 - 16:45", "booked", "Khánh Linh (Đã cọc 50k)", 2, 1),
                SlotItem("17:00 - 18:15", "available", "", 2, 0),
                SlotItem("18:30 - 19:45", "locked", "", 2, 0)
            ),
            1 to mutableStateListOf(
                SlotItem("09:00 - 10:15", "available", "", 2, 0),
                SlotItem("10:30 - 11:45", "booked", "Mai Phương", 2, 1)
            )
            // Các ngày 2, 3, 4, 5, 6 tạm thời chưa có dữ liệu (trống)
        )
    }

    // Lấy danh sách slot của ngày đang chọn, nếu không có thì trả về rỗng (để hiện màn hình trống)
    val currentSlots = slotsByDay[selectedDayIdx] ?: emptyList()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFBF7E8))
        ) {
            // Sub-tabs: Lịch Tuần | Lịch Tháng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarTabButton("Lịch Tuần", selectedCalendarView == 0, GlowIcons.Calendar) { selectedCalendarView = 0 }
                CalendarTabButton("Lịch Tháng", selectedCalendarView == 1, GlowIcons.Calendar) { selectedCalendarView = 1 }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Content based on selected calendar view
            if (selectedCalendarView == 0) {
                // Lịch Tuần (Weekly Matrix)
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp), // padding cho nút FAB
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column {
                                    Text(text = "Lịch Hoạt Động (7 Ngày x Khung Giờ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A101A))
                                    Text(text = "Chạm vào ô để Khóa/Mở hoặc sửa slot. Nhấn (+) để thêm.", fontSize = 10.5.sp, color = TextMuted)
                                }

                                // Legend
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    LegendItem("Mở nhận khách", SuccessGreen)
                                    LegendItem("Đã khóa (Bận)", Color.Gray)
                                    LegendItem("Khách đã cọc", WarningOrange)
                                }

                                // 7-Day Day Selector
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(days.size) { idx ->
                                        val isSel = idx == selectedDayIdx
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) DustyMerlot else Color(0xFFFBF7E8))
                                                .clickable { selectedDayIdx = idx }
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = days[idx],
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else Color(0xFF3A101A),
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Empty State (Khi ngày chưa có lịch nào)
                    if (currentSlots.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Calendar, tint = Color.LightGray, size = 52.dp)
                                Text(
                                    text = "Chưa có khung giờ nào được mở\ntrong ngày này", 
                                    fontSize = 13.sp, 
                                    color = TextMuted, 
                                    fontWeight = FontWeight.Medium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    // Slot Grid items
                    items(currentSlots.size) { sIdx ->
                        val slot = currentSlots[sIdx]
                        val isBooked = slot.status == "booked"
                        val isLocked = slot.status == "locked"

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editInitialDateIdx = selectedDayIdx
                                    editInitialTime = slot.time
                                    showEditSchedule = true 
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = slot.time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A101A))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isBooked -> Color(0xFFFFE8D6)
                                                    isLocked -> Color(0xFFF3F4F6)
                                                    else -> Color(0xFFD1FAE5)
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = when {
                                                isBooked -> "Đã cọc 30% (${slot.bookedSlots}/${slot.maxSlots} slots)"
                                                isLocked -> "Đang KHÓA (${slot.maxSlots} slots)"
                                                else -> "Mở nhận khách (${slot.maxSlots} slots)"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isBooked -> Color(0xFFD97706)
                                                isLocked -> Color(0xFF6B7280)
                                                else -> Color(0xFF059669)
                                            }
                                        )
                                    }
                                }

                                if (isBooked) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        GlowIcon(icon = GlowIcons.User, tint = DustyMerlot, size = 13.dp)
                                        Text(text = slot.customer, fontSize = 12.sp, color = DustyMerlot, fontWeight = FontWeight.Medium)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Chạm để cấu hình sửa slot / khóa giờ", fontSize = 10.sp, color = TextMuted)
                                    Button(
                                        onClick = {
                                            if (slot.status == "available") slot.status = "locked"
                                            else if (slot.status == "locked") slot.status = "available"
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLocked) SuccessGreen else DustyMerlot
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text(
                                            text = if (isLocked) "Mở giờ này" else "Khóa giờ",
                                            fontSize = 10.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } // Close Box from Lịch Tuần
        } else {
            // Lịch Tháng (Monthly View)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "Lịch Tháng", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                            GlowIcon(icon = GlowIcons.Calendar, tint = ChampagneGold, size = 16.dp)
                        }
                        Text(text = "Hiển thị tổng quan mật độ booking trong tháng. Chọn ngày để xem chi tiết slot.", fontSize = 12.sp, color = TextMuted)

                        val daysInMonth = (1..31).toList()
                        val weeks = daysInMonth.chunked(7)
                        weeks.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                week.forEach { day ->
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (day == 7 || day == 15) DustyMerlot else Color(0xFFFBF7E8))
                                            .clickable { },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$day",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (day == 7 || day == 15) Color.White else Color(0xFF3A101A)
                                        )
                                    }
                                }
                                repeat(7 - week.size) {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Nút FAB nổi góc dưới để Thêm lịch
        if (selectedCalendarView == 0) {
            FloatingActionButton(
                onClick = { 
                    editInitialDateIdx = selectedDayIdx
                    editInitialTime = "" // Để rỗng để người dùng tự chọn
                    showEditSchedule = true 
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = ChampagneGold,
                contentColor = Color.White
            ) {
                GlowIcon(icon = GlowIcons.Check, tint = Color.White) // Dùng tạm icon Check hoặc đổi icon Plus nếu đã sync được
            }
        }
    }

    if (showEditSchedule) {
        EditScheduleOverlay(
            days = days,
            initialDateIdx = editInitialDateIdx,
            initialTime = editInitialTime,
            onDismiss = { showEditSchedule = false },
            onSave = { dateIdx, timeList, slots, isLocked ->
                // Logic xử lý khi Lưu Lịch: 
                // Cập nhật trạng thái slot vào bộ nhớ map (slotsByDay)
                val daySlots = slotsByDay.getOrPut(dateIdx) { mutableStateListOf() }
                
                timeList.forEach { time ->
                    val existingSlot = daySlots.find { it.time == time }
                    
                    if (existingSlot != null) {
                        existingSlot.maxSlots = slots
                        if (isLocked) {
                            existingSlot.status = "locked"
                        } else if (existingSlot.status == "locked") {
                            // Khôi phục lại trạng thái cũ nếu mở khóa
                            existingSlot.status = if (existingSlot.bookedSlots > 0) "booked" else "available"
                        }
                    } else {
                        // Nếu là slot tạo mới
                        daySlots.add(SlotItem(
                            time = time,
                            status = if (isLocked) "locked" else "available",
                            customer = "",
                            maxSlots = slots,
                            bookedSlots = 0
                        ))
                    }
                }
                
                // Sắp xếp lại theo thời gian
                daySlots.sortBy { it.time }
                showEditSchedule = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EditScheduleOverlay(
    days: List<String>,
    initialDateIdx: Int,
    initialTime: String,
    onDismiss: () -> Unit,
    onSave: (dateIdx: Int, timeList: List<String>, slots: Int, isLocked: Boolean) -> Unit
) {
    var selectedDateIdx by remember { mutableStateOf(initialDateIdx) }
    
    // Đổi sang mutableStateListOf để lưu nhiều khung giờ được chọn cùng lúc
    val selectedTimes = remember { mutableStateListOf(initialTime) }
    
    // Tách thời gian bắt đầu và kết thúc cho chức năng thêm giờ thủ công
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:15") }
    
    var maxSlots by remember { mutableStateOf("2") }
    var isLocked by remember { mutableStateOf(false) }
    
    // Danh sách khung giờ gợi ý, lưu bằng SnapshotStateList để UI tự update khi thêm mới
    val timeOptions = remember {
        mutableStateListOf(
            "09:00 - 10:15", "10:30 - 11:45", "13:00 - 14:15", 
            "15:30 - 16:45", "17:00 - 18:15", "18:30 - 19:45"
        ).apply {
            // Nếu initialTime không nằm trong danh sách gợi ý, thêm nó vào đầu
            if (!contains(initialTime)) add(0, initialTime)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Cấu Hình Lịch", fontSize = 16.sp, color = DustyMerlot, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            GlowIcon(icon = GlowIcons.ChevronLeft, tint = DustyMerlot)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color(0xFFFBF7E8)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Chọn ngày
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Chọn Ngày Áp Dụng", fontWeight = FontWeight.Bold, color = DustyMerlot)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(days.size) { idx ->
                            val isSel = idx == selectedDateIdx
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) DustyMerlot else Color.White)
                                    .border(1.dp, if (isSel) Color.Transparent else Color.LightGray, RoundedCornerShape(12.dp))
                                    .clickable { selectedDateIdx = idx }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = days[idx].replace("\n", " "),
                                    fontSize = 12.sp,
                                    fontWeight = if(isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) Color.White else Color(0xFF3A101A)
                                )
                            }
                        }
                    }
                }
                
                // Chọn khung giờ
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("2. Chọn Khung Giờ (Có thể chọn nhiều)", fontWeight = FontWeight.Bold, color = DustyMerlot)
                            Text("Đã chọn ${selectedTimes.size} khung giờ", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    
                    // Box nhập thời gian tùy chỉnh
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Từ (VD: 09:00)", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Text("-", fontWeight = FontWeight.Bold, color = TextMuted)
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Đến (VD: 10:15)", fontSize = 12.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = { 
                                val newTimeStr = "$startTime - $endTime"
                                if (newTimeStr.isNotBlank() && !timeOptions.contains(newTimeStr)) {
                                    timeOptions.add(0, newTimeStr)
                                    if (!selectedTimes.contains(newTimeStr)) {
                                        selectedTimes.add(newTimeStr)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.width(60.dp)
                        ) {
                            Text("Thêm", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Sử dụng FlowRow (từ thư viện ExperimentalLayoutApi) để wrap các giờ
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timeOptions.forEach { time ->
                            val isSel = selectedTimes.contains(time)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSel) DustyMerlot.copy(alpha = 0.15f) else Color.White)
                                    .border(1.dp, if (isSel) DustyMerlot else Color.LightGray, RoundedCornerShape(10.dp))
                                    .clickable { 
                                        if (isSel) {
                                            // Cho phép bỏ chọn, nhưng ít nhất phải giữ lại 1 giờ
                                            if (selectedTimes.size > 1) selectedTimes.remove(time)
                                        } else {
                                            selectedTimes.add(time)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = time, 
                                    fontSize = 13.sp, 
                                    fontWeight = if(isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) DustyMerlot else Color.DarkGray
                                )
                            }
                        }
                    }
                }
                
                // Cấu hình Slot
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("3. Cài Đặt Slot Nhận Khách", fontWeight = FontWeight.Bold, color = DustyMerlot)
                        
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Tạm Khóa ${selectedTimes.size} Khung Giờ Này", fontSize = 14.sp, color = Color.DarkGray)
                            Switch(
                                checked = isLocked,
                                onCheckedChange = { isLocked = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DustyMerlot)
                            )
                        }
                        
                        if (!isLocked) {
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text("Số khách tối đa", fontSize = 14.sp, color = Color.DarkGray)
                                    Text("Áp dụng chung cho ${selectedTimes.size} khung giờ đã chọn", fontSize = 11.sp, color = TextMuted)
                                }
                                OutlinedTextField(
                                    value = maxSlots,
                                    onValueChange = { maxSlots = it },
                                    singleLine = true,
                                    modifier = Modifier.width(80.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color.LightGray,
                                        focusedBorderColor = DustyMerlot
                                    ),
                                    textStyle = androidx.compose.ui.text.TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { 
                        val slots = maxSlots.toIntOrNull() ?: 2
                        onSave(selectedDateIdx, selectedTimes.toList(), slots, isLocked)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Lưu ${selectedTimes.size} Khung Giờ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CalendarTabButton(text: String, isSelected: Boolean, icon: ImageVector? = null, onClick: () -> Unit) {
    val bg = if (isSelected) DustyMerlot else Color.White
    val fg = if (isSelected) Color.White else DustyMerlot

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                GlowIcon(icon = icon, tint = fg, size = 13.dp)
            }
            Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}

@Composable
private fun LegendItem(text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = text, fontSize = 10.sp, color = TextMuted)
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun PartnerScheduleScreenPreview() {
    ApplamdepTheme {
        PartnerScheduleScreen()
    }
}