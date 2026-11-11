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
import androidx.compose.foundation.verticalScroll
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
import com.applamdep.merchant.domain.rule.MerchantAutoApprovalPolicy
import com.applamdep.merchant.domain.rule.MerchantAutoCompletionPolicy
import com.applamdep.merchant.domain.rule.MerchantCancellationPolicy
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*
import kotlinx.coroutines.delay

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
    val completedDepositRevenue by viewModel.completedDepositRevenue.collectAsState()

    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var cancelReasonInput by remember { mutableStateOf("") }
    var cancelReasonError by remember { mutableStateOf(false) }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val autoApprovalPolicy = remember { MerchantAutoApprovalPolicy() }
    val autoCompletionPolicy = remember { MerchantAutoCompletionPolicy() }
    val cancellationPolicy = remember { MerchantCancellationPolicy() }

    LaunchedEffect(shopId) {
        viewModel.loadBookings(shopId)
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    LaunchedEffect(currentTimeMillis, bookingToCancel) {
        val activeBooking = bookingToCancel
        if (activeBooking != null &&
            activeBooking.status == MerchantConstants.BOOKING_STATUS_CONFIRMED &&
            !cancellationPolicy.canCancel(activeBooking, currentTimeMillis)
        ) {
            bookingToCancel = null
            cancelReasonError = false
            Toast.makeText(
                context,
                "Đã trong vòng 30 phút trước giờ hẹn, nút Hủy lịch đã bị khóa.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(currentTimeMillis, uiState) {
        val currentList = (uiState as? UiState.Success)?.data.orEmpty()
        val hasExpiredPending = currentList.any { autoApprovalPolicy.shouldAutoApprove(it, currentTimeMillis) }
        val hasExpiredCompletion = currentList.any { autoCompletionPolicy.shouldAutoComplete(it, currentTimeMillis) }
        if (hasExpiredPending || hasExpiredCompletion) {
            viewModel.triggerAutoPolicies(shopId) { _, _ -> }
        }
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
                            viewModel.triggerAutoPolicies(shopId) { approvedCount, completedCount ->
                                when {
                                    approvedCount > 0 && completedCount > 0 -> {
                                        Toast.makeText(
                                            context,
                                            "Đã tự động duyệt $approvedCount đơn quá 30p và hoàn tất $completedCount lịch hẹn!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    approvedCount > 0 -> {
                                        Toast.makeText(
                                            context,
                                            "Đã tự động duyệt $approvedCount đơn hàng quá 30p chờ duyệt!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    completedCount > 0 -> {
                                        Toast.makeText(
                                            context,
                                            "Đã tự động chuyển $completedCount lịch hẹn quá 30p sang HOÀN THÀNH!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Không có đơn nào đến hạn tự động duyệt hoặc hoàn tất.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Kiểm tra tự động duyệt và hoàn thành",
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
                        text = stringResource(R.string.merchant_booking_auto_policy_note),
                        fontSize = 11.sp,
                        color = MerchantPrimaryDark,
                        lineHeight = 15.sp
                    )
                }
            }

            // Thanh hiển thị doanh thu cọc đã cộng dồn từ các đơn Hoàn thành
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MerchantSurface)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MerchantPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Doanh thu cọc đã nhận:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MerchantTextSecondary
                    )
                }
                Text(
                    text = Formatters.formatCurrency(completedDepositRevenue),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantPrimary
                )
            }
            HorizontalDivider(thickness = 0.5.dp, color = MerchantDivider)

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
                    val dueForAutoApproveIds = remember(bookings, currentTimeMillis) {
                        bookings.filter { autoApprovalPolicy.shouldAutoApprove(it, currentTimeMillis) }
                            .map { it.id }
                    }
                    LaunchedEffect(dueForAutoApproveIds) {
                        if (dueForAutoApproveIds.isNotEmpty()) {
                            if (bookingToCancel != null && bookingToCancel!!.id in dueForAutoApproveIds) {
                                bookingToCancel = null
                            }
                            viewModel.triggerAutoApprove(shopId) { approvedCount ->
                                if (approvedCount > 0) {
                                    Toast.makeText(
                                        context,
                                        "Đã tự động duyệt $approvedCount đơn hàng sau 30 phút chờ duyệt.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }

                    val dueForAutoCompleteIds = remember(bookings, currentTimeMillis) {
                        bookings.filter { autoCompletionPolicy.shouldAutoComplete(it, currentTimeMillis) }
                            .map { it.id }
                    }
                    LaunchedEffect(dueForAutoCompleteIds) {
                        if (dueForAutoCompleteIds.isNotEmpty()) {
                            if (bookingToCancel != null && bookingToCancel!!.id in dueForAutoCompleteIds) {
                                bookingToCancel = null
                                cancelReasonError = false
                            }
                            viewModel.triggerAutoComplete(shopId) { completedCount ->
                                if (completedCount > 0) {
                                    Toast.makeText(
                                        context,
                                        "Đã tự động duyệt hoàn thành $completedCount đơn sau 30 phút và cộng cọc vào doanh thu shop.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }

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
                                    currentTimeMillis = currentTimeMillis,
                                    autoApprovalPolicy = autoApprovalPolicy,
                                    autoCompletionPolicy = autoCompletionPolicy,
                                    cancellationPolicy = cancellationPolicy,
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
                                        cancelReasonError = false
                                    }
                                )
                            }
                        }
                    }
                }
                else -> Unit
            }
        }

        // Hộp thoại xác nhận Từ chối (PENDING) / Hủy (CONFIRMED) đơn đặt hẹn
        bookingToCancel?.let { bk ->
            val isPendingReject = bk.status == MerchantConstants.BOOKING_STATUS_PENDING
            AlertDialog(
                onDismissRequest = {
                    bookingToCancel = null
                    cancelReasonError = false
                },
                title = {
                    Text(
                        text = if (isPendingReject) "Xác nhận từ chối đơn hàng" else "Hủy lịch hẹn",
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isPendingReject) {
                                "Bạn có chắc chắn muốn từ chối đơn hàng #${bk.id} của khách ${bk.customerName} hay không?"
                            } else {
                                "Bạn có chắc chắn muốn hủy đơn hẹn #${bk.id} của khách ${bk.customerName} không?"
                            },
                            fontSize = 14.sp,
                            color = MerchantTextPrimary
                        )
                        if (bk.depositAmount > 0L) {
                            Text(
                                text = "Tiền cọc ${Formatters.formatCurrency(bk.depositAmount)} sẽ được hoàn lại cho khách hàng.",
                                fontSize = 12.sp,
                                color = MerchantTextSecondary
                            )
                        }
                        OutlinedTextField(
                            value = cancelReasonInput,
                            onValueChange = {
                                cancelReasonInput = it
                                if (it.isNotBlank()) cancelReasonError = false
                            },
                            isError = !isPendingReject && cancelReasonError,
                            label = {
                                Text(
                                    if (isPendingReject) "Lý do từ chối (tùy chọn)"
                                    else "Lý do hủy (gửi tới khách hàng)"
                                )
                            },
                            placeholder = { Text("Nhập lý do hoặc chọn nhanh gợi ý bên dưới...") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isPendingReject && cancelReasonError) {
                            Text(
                                text = "Vui lòng nhập lý do hoặc chạm vào một lý do đề xuất bên dưới.",
                                fontSize = 11.5.sp,
                                color = MerchantError
                            )
                        }
                        if (!isPendingReject) {
                            Text(
                                text = "Lý do đề xuất (chạm để điền nhanh):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MerchantTextSecondary
                            )
                            @OptIn(ExperimentalLayoutApi::class)
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MerchantConstants.SUGGESTED_CANCEL_REASONS.forEach { suggestion ->
                                    val isPicked = cancelReasonInput == suggestion
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(
                                                if (isPicked) MerchantPrimary.copy(alpha = 0.12f) else MerchantBackground
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isPicked) MerchantPrimary else MerchantBorder,
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clickable {
                                                cancelReasonInput = suggestion
                                                cancelReasonError = false
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = suggestion,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isPicked) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isPicked) MerchantPrimary else MerchantTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!isPendingReject && cancelReasonInput.isBlank()) {
                                cancelReasonError = true
                                return@Button
                            }
                            val finalReason = cancelReasonInput.ifBlank { "Cửa hàng từ chối lịch hẹn" }
                            viewModel.updateStatus(
                                bookingId = bk.id,
                                newStatus = MerchantConstants.BOOKING_STATUS_CANCELLED,
                                currentStatus = bk.status,
                                reason = finalReason,
                                onSuccess = { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    bookingToCancel = null
                                    cancelReasonError = false
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    bookingToCancel = null
                                    cancelReasonError = false
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantError)
                    ) {
                        Text(
                            text = if (isPendingReject) "Xác nhận từ chối" else "Xác nhận hủy",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            bookingToCancel = null
                            cancelReasonError = false
                        }
                    ) {
                        Text(
                            text = if (isPendingReject) "Không" else "Quay lại",
                            color = MerchantTextSecondary
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun BookingItemCard(
    booking: Booking,
    currentTimeMillis: Long = System.currentTimeMillis(),
    autoApprovalPolicy: MerchantAutoApprovalPolicy = MerchantAutoApprovalPolicy(),
    autoCompletionPolicy: MerchantAutoCompletionPolicy = MerchantAutoCompletionPolicy(),
    cancellationPolicy: MerchantCancellationPolicy = MerchantCancellationPolicy(),
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
            // Header: Mã đơn + Huy hiệu trạng thái + Tag tự động duyệt / tự động hoàn tất
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
                    if (booking.autoApproved) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE8F4FD))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚡ Tự động duyệt",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
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

            // Thời gian hẹn dịch vụ & Cọc ký quỹ
            val appointmentTimeText = when {
                booking.startAt > 0L && booking.endAt > booking.startAt ->
                    "${Formatters.formatTime(booking.startAt)} - ${Formatters.formatTime(booking.endAt)} · ${Formatters.formatDate(booking.startAt)}"
                booking.startAt > 0L ->
                    Formatters.formatDateTime(booking.startAt)
                booking.slotId.isNotBlank() ->
                    "Slot: ${booking.slotId}"
                else -> "Hôm nay"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MerchantTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = appointmentTimeText,
                        fontSize = 12.sp,
                        color = MerchantTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (booking.depositAmount > 0L) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tiền cọc: ${Formatters.formatCurrency(booking.depositAmount)}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantPrimary
                    )
                }
            }

            // Thời gian khách đặt đơn
            if (booking.createdAt > 0L) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MerchantTextSecondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Đặt lúc: ${Formatters.formatDateTime(booking.createdAt)}",
                        fontSize = 12.sp,
                        color = MerchantTextSecondary
                    )
                }
            }

            // Thời hạn 30 phút để từ chối khi đơn ở trạng thái Chờ duyệt
            if (booking.status == MerchantConstants.BOOKING_STATUS_PENDING && booking.createdAt > 0L) {
                val remainingMillis = autoApprovalPolicy.remainingRejectMillis(booking, currentTimeMillis)
                val deadlineTime = Formatters.formatTime(autoApprovalPolicy.autoApproveDeadline(booking))
                val isUrgent = remainingMillis in 1L..(5 * 60 * 1000L)
                val stripBg = if (isUrgent) Color(0xFFFDECEA) else Color(0xFFFFF4E5)
                val stripText = if (isUrgent) Color(0xFFD32F2F) else Color(0xFFB76E00)

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(stripBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = stripText,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Còn ${Formatters.formatCountdown(remainingMillis)} để từ chối · Tự động duyệt lúc $deadlineTime",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = stripText
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

            // Lý do hủy / từ chối (nếu có)
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
                        text = "Lý do: ${booking.cancelReason}",
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
                            Text(
                                text = stringResource(R.string.merchant_btn_reject),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
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
                    val canCancel = cancellationPolicy.canCancel(booking, currentTimeMillis)
                    val canComplete = autoCompletionPolicy.canComplete(booking, currentTimeMillis)
                    val lockTimeText = if (booking.startAt > 0L) {
                        Formatters.formatTime(cancellationPolicy.cancelDeadline(booking))
                    } else ""
                    val completeTimeText = if (booking.endAt > 0L) {
                        Formatters.formatTime(booking.endAt)
                    } else ""

                    Spacer(modifier = Modifier.height(10.dp))

                    if (booking.endAt > 0L || booking.startAt > 0L) {
                        val stripSpec = when {
                            canComplete && booking.endAt > 0L -> {
                                val remAutoComplete = autoCompletionPolicy.remainingBeforeAutoCompleteMillis(booking, currentTimeMillis)
                                StatusBannerSpec(
                                    bg = Color(0xFFFDECEA),
                                    color = Color(0xFFD32F2F),
                                    icon = Icons.Default.Timer,
                                    text = "Đã đến giờ hoàn thành ($completeTimeText) · Sàn tự động duyệt sau ${Formatters.formatCountdown(remAutoComplete)}"
                                )
                            }
                            !canCancel && booking.startAt > 0L -> {
                                val completeNote = if (completeTimeText.isNotEmpty()) " · Mở Hoàn thành lúc $completeTimeText" else ""
                                StatusBannerSpec(
                                    bg = Color(0xFFF3F4F6),
                                    color = Color(0xFF6B7280),
                                    icon = Icons.Default.Lock,
                                    text = "Khóa hủy lịch (dưới 30p trước giờ hẹn)$completeNote"
                                )
                            }
                            else -> {
                                val completeNote = if (completeTimeText.isNotEmpty()) " · Mở Hoàn thành lúc $completeTimeText" else ""
                                StatusBannerSpec(
                                    bg = Color(0xFFE8F4FD),
                                    color = Color(0xFF1976D2),
                                    icon = Icons.Default.Info,
                                    text = "Hủy lịch trước $lockTimeText$completeNote"
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(stripSpec.bg)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = stripSpec.icon,
                                contentDescription = null,
                                tint = stripSpec.color,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stripSpec.text,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = stripSpec.color
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onRequestCancel,
                            enabled = canCancel,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MerchantError,
                                disabledContainerColor = Color(0xFFF3F4F6),
                                disabledContentColor = Color(0xFF9CA3AF)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (canCancel) MerchantError.copy(alpha = 0.5f) else Color(0xFFD1D5DB)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Hủy lịch",
                                fontSize = 12.sp,
                                fontWeight = if (canCancel) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }

                        Button(
                            onClick = onComplete,
                            enabled = canComplete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD32F2F),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE5E7EB),
                                disabledContentColor = Color(0xFF9CA3AF)
                            ),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text(
                                text = stringResource(R.string.merchant_btn_complete_short),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                MerchantConstants.BOOKING_STATUS_IN_SERVICE -> {
                    val canComplete = autoCompletionPolicy.canComplete(booking, currentTimeMillis)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onComplete,
                        enabled = canComplete,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE5E7EB),
                            disabledContentColor = Color(0xFF9CA3AF)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.merchant_btn_complete_short), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                MerchantConstants.BOOKING_STATUS_COMPLETED -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = MerchantSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (booking.depositAmount > 0L) {
                                    "Dịch vụ đã hoàn tất · +${Formatters.formatCurrency(booking.depositAmount)} tiền cọc vào doanh thu shop"
                                } else {
                                    "Dịch vụ đã hoàn tất"
                                },
                                fontSize = 12.sp,
                                color = MerchantSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MerchantGoldDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (booking.isReviewed) {
                                    "Khách hàng đã gửi đánh giá dịch vụ"
                                } else {
                                    "Khách hàng đã có thể đánh giá dịch vụ này"
                                },
                                fontSize = 11.5.sp,
                                color = MerchantTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

private data class StatusBannerSpec(
    val bg: Color,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val text: String
)
