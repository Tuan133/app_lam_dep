package com.applamdep.merchant.ui.booking

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.di.MerchantViewModelFactory
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.domain.model.Booking
import com.applamdep.merchant.domain.rule.BookingFilterType
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI & TRỪU TƯỢNG
 * GIAI ĐOẠN: CHỨC NĂNG 6 & 7
 * - Chức năng 6: Quản lý lịch hẹn (Xác nhận, bắt đầu phục vụ, hủy đơn, hoàn thành dịch vụ).
 * - Chức năng 7: Quét và kích hoạt tự động hoàn tất dịch vụ sau 30 phút từ thời điểm kết thúc
 *   nhằm tự động giải ngân tiền cọc nếu cửa hàng quên bấm thủ công.
 */
@Composable
fun MerchantBookingListScreen(
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantBookingViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    shopId: String = "shop_01"
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var cancelReasonInput by remember { mutableStateOf("") }

    LaunchedEffect(shopId) {
        viewModel.loadBookings(shopId)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MerchantBackground,
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_booking_title),
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAutoComplete(shopId) { count ->
                                if (count > 0) {
                                    Toast.makeText(
                                        context,
                                        "Đã tự động chuyển $count lịch hẹn quá 30p sang HOÀN THÀNH!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Không có lịch hẹn nào quá hạn 30p chưa hoàn tất.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Quét tự động hoàn thành",
                            tint = MerchantPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thanh tìm kiếm và bộ lọc trạng thái
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MerchantSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Tìm theo tên khách, SĐT, mã đơn...", fontSize = 13.sp, color = MerchantTextHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MerchantTextHint) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = MerchantTextHint)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MerchantPrimary,
                        unfocusedBorderColor = MerchantBorder,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filter chips theo trạng thái
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BookingFilterType.entries.forEach { f ->
                        val isSelected = f == currentFilter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) MerchantPrimary else MerchantCard)
                                .border(1.dp, if (isSelected) MerchantPrimary else MerchantBorder, RoundedCornerShape(20.dp))
                                .clickable { viewModel.setFilter(f) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = f.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MerchantTextPrimary
                            )
                        }
                    }
                }
            }

            // Banner thông báo chính sách tự động hoàn thành sau 30 phút (Requirement #7)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MerchantPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MerchantPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.merchant_booking_auto_complete_note),
                        fontSize = 11.sp,
                        color = MerchantPrimaryDark,
                        lineHeight = 15.sp
                    )
                }
            }

            when (val state = uiState) {
                is UiState.Idle, is UiState.Loading -> {
                    LoadingStateView(message = "Đang tải danh sách lịch hẹn...")
                }
                is UiState.Error -> {
                    ErrorStateView(
                        message = state.message,
                        onRetry = { viewModel.loadBookings(shopId) }
                    )
                }
                is UiState.Success -> {
                    val bookings = state.data
                    if (bookings.isEmpty()) {
                        EmptyStateView(
                            title = "Không có lịch hẹn nào",
                            subtitle = if (searchQuery.isNotEmpty())
                                "Không tìm thấy đơn nào khớp với từ khóa '$searchQuery'"
                            else
                                "Chưa có lịch hẹn nào trong mục này."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookings, key = { it.id }) { booking ->
                                BookingItemCard(
                                    booking = booking,
                                    onConfirm = {
                                        viewModel.updateStatus(
                                            bookingId = booking.id,
                                            newStatus = MerchantConstants.BOOKING_STATUS_CONFIRMED,
                                            currentStatus = booking.status,
                                            onSuccess = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                    onStartService = {
                                        viewModel.updateStatus(
                                            bookingId = booking.id,
                                            newStatus = MerchantConstants.BOOKING_STATUS_IN_SERVICE,
                                            currentStatus = booking.status,
                                            onSuccess = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                    onComplete = {
                                        viewModel.updateStatus(
                                            bookingId = booking.id,
                                            newStatus = MerchantConstants.BOOKING_STATUS_COMPLETED,
                                            currentStatus = booking.status,
                                            onSuccess = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() },
                                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                                        )
                                    },
                                    onRequestCancel = {
                                        bookingToCancel = booking
                                        cancelReasonInput = ""
                                    }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }

        // Hộp thoại xác nhận Hủy / Từ chối đơn đặt hẹn kèm nhập lý do
        bookingToCancel?.let { bk ->
            AlertDialog(
                onDismissRequest = { bookingToCancel = null },
                title = {
                    Text(
                        text = if (bk.status == MerchantConstants.BOOKING_STATUS_PENDING) "Từ chối lịch hẹn" else "Hủy lịch hẹn",
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Bạn có chắc chắn muốn hủy đơn hẹn #${bk.id} của khách ${bk.customerName} không?",
                            fontSize = 14.sp,
                            color = MerchantTextPrimary
                        )
                        OutlinedTextField(
                            value = cancelReasonInput,
                            onValueChange = { cancelReasonInput = it },
                            label = { Text("Lý do hủy (gửi tới khách hàng)") },
                            placeholder = { Text("Ví dụ: Quán kín chỗ đột xuất, bảo trì thiết bị...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateStatus(
                                bookingId = bk.id,
                                newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
                                currentStatus = bk.status,
                                reason = cancelReasonInput.ifBlank { "Cửa hàng bận việc đột xuất" },
                                onSuccess = {
                                    Toast.makeText(context, "Đã hủy đơn hẹn.", Toast.LENGTH_SHORT).show()
                                    bookingToCancel = null
                                },
                                onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantError)
                    ) {
                        Text(text = "Xác nhận hủy", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { bookingToCancel = null }) {
                        Text(text = "Quay lại", color = MerchantTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun BookingItemCard(
    booking: Booking,
    onConfirm: () -> Unit,
    onStartService: () -> Unit,
    onComplete: () -> Unit,
    onRequestCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MerchantCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Mã đơn + Huy hiệu trạng thái + Tag tự động hoàn tất
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#${booking.id}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                    if (booking.autoCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MerchantVipBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ Tự động hoàn tất",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MerchantVipGold
                            )
                        }
                    }
                }

                StatusBadge(status = booking.status)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 0.5.dp,
                color = MerchantDivider
            )

            // Thông tin khách hàng
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MerchantPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = booking.customerName.ifBlank { "Khách hàng" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                if (booking.customerPhone.isNotBlank()) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "·  ${booking.customerPhone}",
                        fontSize = 13.sp,
                        color = MerchantTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dịch vụ đã đặt
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = MerchantGoldDark,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (booking.serviceNames.isNotEmpty()) booking.serviceNames.joinToString(", ") else "Dịch vụ làm đẹp",
                    fontSize = 13.sp,
                    color = MerchantTextPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Thời gian & Cọc ký quỹ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MerchantTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (booking.slotId.isNotBlank()) "Slot: ${booking.slotId}" else "Hôm nay",
                        fontSize = 12.sp,
                        color = MerchantTextSecondary
                    )
                }

                if (booking.depositAmount > 0L) {
                    Text(
                        text = "Tiền cọc: ${Formatters.formatCurrency(booking.depositAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantPrimary
                    )
                }
            }

            // Ghi chú của khách (nếu có)
            if (booking.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MerchantBackground)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Ghi chú: ${booking.note}",
                        fontSize = 12.sp,
                        color = MerchantTextSecondary
                    )
                }
            }

            // Lý do hủy (nếu có)
            if (booking.status == MerchantConstants.BOOKING_STATUS_CANCELLED && !booking.cancelReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MerchantError.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Lý do hủy: ${booking.cancelReason}",
                        fontSize = 12.sp,
                        color = MerchantError
                    )
                }
            }

            // Các nút thao tác theo trạng thái (State Machine)
            when (booking.status) {
                MerchantConstants.BOOKING_STATUS_PENDING -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestCancel,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MerchantError),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MerchantError.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = stringResource(R.string.merchant_btn_cancel), fontSize = 12.sp)
                        }

                        Button(
                            onClick = onConfirm,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(text = stringResource(R.string.merchant_btn_confirm), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                MerchantConstants.BOOKING_STATUS_CONFIRMED -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestCancel,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MerchantError),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MerchantError.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Hủy lịch", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onStartService,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(text = stringResource(R.string.merchant_btn_in_service), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                MerchantConstants.BOOKING_STATUS_IN_SERVICE -> {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onComplete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantSecondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.merchant_btn_complete), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                MerchantConstants.BOOKING_STATUS_COMPLETED -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MerchantSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dịch vụ đã hoàn tất · Tiền cọc đã giải ngân về ví tiệm",
                            fontSize = 12.sp,
                            color = MerchantSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                else -> Unit
            }
        }
    }
}
