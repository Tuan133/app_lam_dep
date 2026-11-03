package com.applamdep.customer.ui.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.core.util.CurrencyFormatter
import com.applamdep.customer.ui.components.*
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.*

/**
 * MÀN HÌNH "CHỌN LỊCH" HẸN (GLOW PRESTIGE - HAUTE COUTURE)
 * - RÀNG BUỘC: Địa chỉ quán hiển thị trên ĐẦU TRANG
 * - Tiền cọc do salon/shop set cho dịch vụ, khách KHÔNG chọn mệnh giá
 * - Ẩn khung giờ trùng/đã qua/hết chỗ bằng SlotConflictResolver
 * - Form thông tin khách có validate
 * - Đồng bộ với bảng màu Haute Couture DustyMerlot & ChampagneGold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    shopId: String,
    serviceId: String,
    onBack: () -> Unit,
    onBookingSuccess: (bookingId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookingViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formData by viewModel.formData.collectAsStateWithLifecycle()

    LaunchedEffect(shopId, serviceId) {
        viewModel.initBooking(shopId, serviceId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmWhite,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chọn lịch hẹn",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextNoir
                        )
                        Text(
                            text = "Xác nhận thời gian & bảo chứng giữ chỗ",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextNoir
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is UiState.Loading -> GrabLoadingState()
                is UiState.Error -> GrabEmptyState(
                    message = (uiState as UiState.Error).message,
                    onRetry = { viewModel.initBooking(shopId, serviceId) }
                )
                else -> {
                    val data = formData
                    if (data != null) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // 1. RÀNG BUỘC BẮT BUỘC: ĐỊA CHỈ QUÁN HIỂN THỊ TRÊN ĐẦU TRANG
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 4.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0xFFFAF1EC))
                                            .border(1.dp, Color(0xFFEED7CD), RoundedCornerShape(16.dp))
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFFF6F0))
                                                    .border(1.dp, Color(0xFFF3DEC8), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(
                                                    icon = GlowIcons.MapPin,
                                                    tint = ChampagneGold,
                                                    size = 18.dp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = data.shop.name,
                                                    fontSize = 14.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextNoir
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = data.shop.address,
                                                    fontSize = 12.sp,
                                                    color = TextMuted,
                                                    maxLines = 2
                                                )
                                            }
                                        }
                                    }
                                }

                                // 2. Dịch vụ đã chọn
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEBDCD3)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Dịch vụ đã chọn",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextNoir
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFFFAF2EE))
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = "⏱ ${data.selectedService.durationMinutes} phút",
                                                        fontSize = 11.5.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = DustyMerlot
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 0.8.dp)
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = data.selectedService.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = TextNoir,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = CurrencyFormatter.formatVnd(data.selectedService.price),
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = DustyMerlot
                                                )
                                            }
                                        }
                                    }
                                }

                                // 3. Chọn ngày hẹn
                                item {
                                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                        Text(
                                            text = "Chọn ngày làm đẹp",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextNoir
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(data.availableDates) { date ->
                                                val isSelected = date == data.selectedDate
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(if (isSelected) DustyMerlot else Color.White)
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) DustyMerlot else Color(0xFFEBDCD3),
                                                            shape = RoundedCornerShape(14.dp)
                                                        )
                                                        .clickable { viewModel.onDateSelected(date) }
                                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = if (date == data.availableDates.first()) "Hôm nay" else "Ngày",
                                                            fontSize = 11.sp,
                                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextMuted
                                                        )
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        Text(
                                                            text = date.takeLast(5),
                                                            fontSize = 13.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) Color.White else TextNoir
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // 4. Chọn khung giờ hẹn (Đã lọc qua SlotConflictResolver)
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEBDCD3)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Khung giờ hẹn có sẵn",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextNoir
                                            )
                                            Text(
                                                text = "Hệ thống tự động ẩn các khung giờ trùng hoặc đã hết chỗ",
                                                fontSize = 11.5.sp,
                                                color = TextMuted
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))

                                            if (data.availableSlots.isEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFFFF4F4))
                                                        .border(1.dp, Color(0xFFFBD0D0), RoundedCornerShape(10.dp))
                                                        .padding(12.dp)
                                                ) {
                                                    Text(
                                                        text = "Rất tiếc, ngày này không còn khung giờ trống. Vui lòng chọn ngày khác.",
                                                        fontSize = 12.5.sp,
                                                        color = Color(0xFFC81E1E),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            } else {
                                                // Lưới các khung giờ
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    data.availableSlots.chunked(3).forEach { rowSlots ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            rowSlots.forEach { slot ->
                                                                val isSlotSelected = slot.id == data.selectedSlot?.id
                                                                Box(
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .clip(RoundedCornerShape(10.dp))
                                                                        .background(if (isSlotSelected) Color(0xFFFDE8EE) else Color.White)
                                                                        .border(
                                                                            width = if (isSlotSelected) 1.5.dp else 1.dp,
                                                                            color = if (isSlotSelected) DustyMerlot else Color(0xFFEBDCD3),
                                                                            shape = RoundedCornerShape(10.dp)
                                                                        )
                                                                        .clickable { viewModel.onSlotSelected(slot) }
                                                                        .padding(vertical = 10.dp),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = slot.startTime,
                                                                        fontSize = 14.sp,
                                                                        fontWeight = if (isSlotSelected) FontWeight.Bold else FontWeight.Medium,
                                                                        color = if (isSlotSelected) DustyMerlot else TextNoir
                                                                    )
                                                                }
                                                            }
                                                            // Bù khoảng trống nếu hàng không đủ 3
                                                            repeat(3 - rowSlots.size) {
                                                                Spacer(modifier = Modifier.weight(1f))
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            if (data.slotError != null) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = data.slotError,
                                                    color = Color(0xFFC81E1E),
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                // 5. THÔNG TIN TIỀN CỌC GIỮ CHỖ (DO SHOP QUY ĐỊNH - KHÁCH KHÔNG CẦN CHỌN MỆNH GIÁ)
                                if (data.shop.requireDeposit && data.selectedDepositAmount > 0) {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp),
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            border = BorderStroke(1.dp, Color(0xFFEBDCD3)),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(CircleShape)
                                                                .background(Color(0xFFFFF6F0))
                                                                .border(1.dp, Color(0xFFF3DEC8), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            GlowIcon(
                                                                icon = GlowIcons.Shield,
                                                                tint = ChampagneGold,
                                                                size = 17.dp
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Column {
                                                            Text(
                                                                text = "Tiền cọc giữ chỗ",
                                                                fontSize = 14.5.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = TextNoir
                                                            )
                                                            Text(
                                                                text = "Mức cọc do salon quy định",
                                                                fontSize = 11.5.sp,
                                                                color = TextMuted
                                                            )
                                                        }
                                                    }

                                                    // Mức cọc cố định do salon ấn định cho dịch vụ
                                                    Text(
                                                        text = CurrencyFormatter.formatVnd(data.selectedDepositAmount),
                                                        fontSize = 16.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = DustyMerlot
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))
                                                HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 0.8.dp)
                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Cam kết bảo chứng sàn HELD
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFFFF8F2))
                                                        .border(1.dp, Color(0xFFF6DFD0), RoundedCornerShape(10.dp))
                                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    GlowIcon(
                                                        icon = GlowIcons.Check,
                                                        tint = SuccessGreen,
                                                        size = 13.dp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Tiền cọc được Sàn bảo hộ (HELD). Hoàn 100% về ví trong ứng dụng nếu hủy trước ít nhất 2 tiếng.",
                                                        fontSize = 11.5.sp,
                                                        color = Color(0xFF8B1538),
                                                        fontWeight = FontWeight.Medium,
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // 6. Form thông tin người đặt (Có validate)
                                item {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.dp, Color(0xFFEBDCD3)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Thông tin khách hẹn",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextNoir
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Họ tên
                                            OutlinedTextField(
                                                value = data.customerName,
                                                onValueChange = viewModel::onNameChanged,
                                                label = { Text("Họ và tên") },
                                                isError = data.nameError != null,
                                                supportingText = data.nameError?.let { { Text(it, color = Color(0xFFC81E1E)) } },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = DustyMerlot,
                                                    unfocusedBorderColor = Color(0xFFEBDCD3),
                                                    focusedLabelColor = DustyMerlot
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Số điện thoại
                                            OutlinedTextField(
                                                value = data.customerPhone,
                                                onValueChange = viewModel::onPhoneChanged,
                                                label = { Text("Số điện thoại liên hệ") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                isError = data.phoneError != null,
                                                supportingText = data.phoneError?.let { { Text(it, color = Color(0xFFC81E1E)) } },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = DustyMerlot,
                                                    unfocusedBorderColor = Color(0xFFEBDCD3),
                                                    focusedLabelColor = DustyMerlot
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(6.dp))

                                            // Ghi chú
                                            OutlinedTextField(
                                                value = data.customerNote,
                                                onValueChange = viewModel::onNoteChanged,
                                                label = { Text("Ghi chú đặc biệt (tùy chọn)") },
                                                placeholder = { Text("Ví dụ: da tay nhạy cảm, làm nhẹ nhàng...", color = TextMuted) },
                                                maxLines = 3,
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = DustyMerlot,
                                                    unfocusedBorderColor = Color(0xFFEBDCD3),
                                                    focusedLabelColor = DustyMerlot
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // 7. CHÂN TRANG TỔNG HỢP & NÚT XÁC NHẬN
                            Surface(
                                color = Color.White,
                                shadowElevation = 10.dp,
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                border = BorderStroke(0.8.dp, Color(0xFFEBDCD3)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = if (data.shop.requireDeposit) "Tiền cọc giữ chỗ (Sàn tạm giữ)" else "Tổng tiền thanh toán tại tiệm",
                                                fontSize = 12.sp,
                                                color = TextMuted
                                            )
                                            val amountDisplay = if (data.shop.requireDeposit) {
                                                CurrencyFormatter.formatVnd(data.selectedDepositAmount)
                                            } else {
                                                CurrencyFormatter.formatVnd(data.selectedService.price)
                                            }
                                            Text(
                                                text = amountDisplay,
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (data.shop.requireDeposit) Color(0xFF8B1538) else DustyMerlot
                                            )
                                        }

                                        if (data.selectedSlot != null) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFFAF1EC))
                                                    .border(1.dp, Color(0xFFEED7CD), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = "${data.selectedSlot.startTime} · ${data.selectedDate.takeLast(5)}",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DustyMerlot
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            viewModel.submitBooking(
                                                customerId = "user_cust_01",
                                                onSuccess = { bookingId ->
                                                    com.applamdep.customer.core.notification.NotificationHelper.scheduleBookingReminder(
                                                        context = context,
                                                        bookingId = bookingId,
                                                        shopName = data.shop.name,
                                                        bookingTime = data.selectedSlot?.startTime ?: "10:00",
                                                        appointmentTimestamp = System.currentTimeMillis() + (4 * 3600000L)
                                                    )
                                                    onBookingSuccess(bookingId)
                                                }
                                            )
                                        },
                                        enabled = !data.isSubmitting,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = DustyMerlot,
                                            disabledContainerColor = Color(0xFFEBDCD3)
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        if (data.isSubmitting) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(
                                                text = "Xác nhận đặt lịch",
                                                color = Color.White,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
