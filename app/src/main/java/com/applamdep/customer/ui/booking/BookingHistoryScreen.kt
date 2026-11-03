package com.applamdep.customer.ui.booking

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.core.util.CurrencyFormatter
import com.applamdep.customer.domain.model.Booking
import com.applamdep.customer.domain.policy.CancellationDecision
import com.applamdep.customer.ui.components.GrabLoadingState
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.*

/**
 * MÀN HÌNH LỊCH SỬ ĐẶT LỊCH & QUẢN LÝ HỦY LỊCH (GLOW PRESTIGE - HAUTE COUTURE)
 * - Hiển thị các trạng thái: PENDING, CONFIRMED, IN_SERVICE, COMPLETED, CANCELLED
 * - Bảo chứng cọc sàn HELD an tâm
 * - Quy tắc hủy lịch trước 2 tiếng: Hiển thị hoàn cọc về ví qua CancellationPolicy
 * - Dưới 2 tiếng: Cảnh báo quá hạn hủy
 * - Giữ nguyên 100% logic kết nối backend
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(
    onBack: (() -> Unit)? = null,
    onNavigateToReview: ((bookingId: String) -> Unit)? = null,
    onViewCustomerReviews: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: BookingHistoryViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()

    var bookingToCancel by remember { mutableStateOf<Pair<Booking, CancellationDecision>?>(null) }
    var cancelReasonText by remember { mutableStateOf("Bận việc đột xuất") }

    LaunchedEffect(Unit) {
        viewModel.loadCustomerBookings("user_cust_01")
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmWhite,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lịch hẹn của tôi",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextNoir
                        )
                        Text(
                            text = "Theo dõi tiến trình & bảo chứng cọc HELD",
                            fontSize = 11.5.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextNoir
                            )
                        }
                    }
                },
                actions = {
                    if (onViewCustomerReviews != null) {
                        Surface(
                            onClick = onViewCustomerReviews,
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFFFFBF4),
                            border = BorderStroke(1.dp, GoldSoft),
                            shadowElevation = 0.5.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlowIcon(
                                    icon = GlowIcons.StarFilled,
                                    tint = ChampagneGold,
                                    size = 13.dp
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Đánh giá của tôi",
                                    fontSize = 11.5.sp,
                                    color = DustyMerlot,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thanh chuyển tab bộ lọc: Segmented Pill sang trọng
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFEFE5DC))
                        .border(1.dp, Color(0xFFE2D4C8), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterTabButton(
                        label = "Tất cả",
                        isSelected = currentFilter == BookingFilterType.ALL,
                        onClick = { viewModel.setFilter(BookingFilterType.ALL) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        label = "Sắp tới",
                        isSelected = currentFilter == BookingFilterType.UPCOMING,
                        onClick = { viewModel.setFilter(BookingFilterType.UPCOMING) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterTabButton(
                        label = "Đã xong / Hủy",
                        isSelected = currentFilter == BookingFilterType.COMPLETED_OR_CANCELLED,
                        onClick = { viewModel.setFilter(BookingFilterType.COMPLETED_OR_CANCELLED) },
                        modifier = Modifier.weight(1.3f)
                    )
                }
            }

            // Danh sách lịch hẹn
            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is UiState.Idle, is UiState.Loading -> GrabLoadingState()
                    is UiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                GlowIcon(
                                    icon = GlowIcons.Alert,
                                    tint = Color(0xFFDC2626),
                                    size = 36.dp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 14.sp,
                                    color = Color(0xFFDC2626),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { viewModel.loadCustomerBookings("user_cust_01") },
                                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Thử lại", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    is UiState.Success -> {
                        val bookings = state.data
                        if (bookings.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFAF1EC))
                                            .border(1.dp, Color(0xFFEED7CD), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        GlowIcon(
                                            icon = GlowIcons.Calendar,
                                            tint = DustyMerlot,
                                            size = 34.dp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Chưa có lịch hẹn nào",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextNoir
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Bạn chưa có lịch hẹn nào trong danh mục này.\nCác cuộc hẹn sẽ được hiển thị và bảo vệ tiền cọc tại đây.",
                                        fontSize = 13.sp,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                items(
                                    items = bookings,
                                    key = { it.id }
                                ) { booking ->
                                    val decision = viewModel.checkCancellationEligibility(booking)

                                    BookingTicketItem(
                                        booking = booking,
                                        cancellationDecision = decision,
                                        onCancelClick = {
                                            bookingToCancel = Pair(booking, decision)
                                        },
                                        onReviewClick = {
                                            onNavigateToReview?.invoke(booking.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // HỘP THOẠI XÁC NHẬN HỦY LỊCH (QUY TẮC HOÀN CỌC VÍ TRƯỚC 2 TIẾNG)
            bookingToCancel?.let { (booking, decision) ->
                AlertDialog(
                    onDismissRequest = { bookingToCancel = null },
                    containerColor = Color.White,
                    shape = RoundedCornerShape(22.dp),
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEE2E2)),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowIcon(
                                    icon = GlowIcons.Alert,
                                    tint = Color(0xFFDC2626),
                                    size = 18.dp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Xác nhận hủy lịch hẹn",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextNoir
                            )
                        }
                    },
                    text = {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (decision.isRefundEligible) Color(0xFFEBF7EE) else Color(0xFFFFF4F4))
                                    .border(
                                        1.dp,
                                        if (decision.isRefundEligible) Color(0xFFC3E6CB) else Color(0xFFFBD0D0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = decision.message,
                                    fontSize = 12.5.sp,
                                    color = if (decision.isRefundEligible) Color(0xFF1E7E34) else Color(0xFFC81E1E),
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = cancelReasonText,
                                onValueChange = { cancelReasonText = it },
                                label = { Text("Lý do hủy lịch (để salon cải thiện)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DustyMerlot,
                                    unfocusedBorderColor = BorderSilk,
                                    focusedLabelColor = DustyMerlot
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val b = booking
                                bookingToCancel = null
                                viewModel.cancelBooking(
                                    booking = b,
                                    reason = cancelReasonText,
                                    onSuccess = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    },
                                    onError = { err ->
                                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC81E1E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Đồng ý hủy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { bookingToCancel = null }) {
                            Text("Giữ lại lịch", color = TextMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Thẻ hiển thị vé đặt lịch phong cách Luxury Concierge Boarding Pass
 */
@Composable
private fun BookingTicketItem(
    booking: Booking,
    cancellationDecision: CancellationDecision,
    onCancelClick: () -> Unit,
    onReviewClick: () -> Unit
) {
    val isConfirmed = booking.status == AppConstants.BOOKING_STATUS_CONFIRMED

    val salonIcon = when {
        booking.shopName.contains("Nail", ignoreCase = true) -> GlowIcons.NailPolish
        booking.shopName.contains("Spa", ignoreCase = true) || booking.shopName.contains("Gội Đầu", ignoreCase = true) -> GlowIcons.Lotus
        booking.shopName.contains("Hair", ignoreCase = true) || booking.shopName.contains("Tóc", ignoreCase = true) -> GlowIcons.Scissors
        else -> GlowIcons.Sparkle
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            1.dp,
            if (isConfirmed) Color(0xFFE8D3C8) else Color(0xFFEBDCD3)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Dòng 1: Mã vé + Trạng thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mã booking phong cách Concierge Pass
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFAF2EE))
                        .border(1.dp, Color(0xFFEFE2DB), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlowIcon(
                        icon = GlowIcons.Tag,
                        tint = ChampagneGold,
                        size = 12.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "#${booking.id}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNoir
                    )
                }

                BookingStatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Dòng 2: Salon Info kèm Icon Medallion
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFAF1EC))
                        .border(1.dp, Color(0xFFEED7CD), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GlowIcon(
                        icon = salonIcon,
                        tint = DustyMerlot,
                        size = 20.dp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.shopName,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextNoir,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlowIcon(
                            icon = GlowIcons.MapPin,
                            tint = ChampagneGold,
                            size = 11.dp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = booking.shopAddress,
                            fontSize = 12.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dòng 3: Khối dịch vụ & Thời gian (Highlight Capsule)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFAF5EE))
                    .border(1.dp, Color(0xFFEFE2D6), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                // Tên dịch vụ & Giá tiền
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = booking.serviceName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextNoir,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = CurrencyFormatter.formatVnd(booking.servicePrice),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DustyMerlot
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color(0xFFEFE2D6), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(8.dp))

                // Thời gian hẹn
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlowIcon(
                        icon = GlowIcons.Clock,
                        tint = DustyMerlot,
                        size = 14.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val displayDate = if (booking.bookingDate == "2026-10-23" || booking.bookingDate == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())) "Hôm nay" else booking.bookingDate
                    Text(
                        text = "${booking.bookingTime}  ·  $displayDate",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A101E)
                    )
                    if (booking.durationMinutes > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${booking.durationMinutes} phút)",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Dòng 4: Khối bảo chứng cọc HELD (nếu có)
            if (booking.requiresDeposit && booking.depositAmount > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFFF7F2))
                        .border(1.dp, Color(0xFFF6DFD0), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlowIcon(
                        icon = GlowIcons.Shield,
                        tint = ChampagneGold,
                        size = 14.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đã cọc: ${CurrencyFormatter.formatVnd(booking.depositAmount)} (Sàn giữ HELD)",
                        fontSize = 12.sp,
                        color = Color(0xFF8B1538),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFFDF8))
                            .border(0.8.dp, Color(0xFFE8D3A5), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "BẢO CHỨNG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ChampagneGold
                        )
                    }
                }
            }

            // Dòng 5: Hành động - Hủy lịch hoặc Đánh giá
            val isCancellableStatus = booking.status == AppConstants.BOOKING_STATUS_CONFIRMED || booking.status == AppConstants.BOOKING_STATUS_PENDING

            if (isCancellableStatus) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!cancellationDecision.canCancel) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            GlowIcon(
                                icon = GlowIcons.Alert,
                                tint = Color(0xFFDC2626),
                                size = 13.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Quá hạn hủy (< 2h)",
                                fontSize = 12.sp,
                                color = Color(0xFFDC2626),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            GlowIcon(
                                icon = GlowIcons.Check,
                                tint = SuccessGreen,
                                size = 13.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Được hoàn 100% cọc về ví",
                                fontSize = 12.sp,
                                color = Color(0xFF1E7E34),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // NÚT HỦY LỊCH
                    OutlinedButton(
                        onClick = onCancelClick,
                        enabled = cancellationDecision.canCancel,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (cancellationDecision.canCancel) Color(0xFFE8B2BC) else Color(0xFFE5DCDD)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (cancellationDecision.canCancel) Color(0xFFFFF5F6) else Color(0xFFF8F5F5),
                            contentColor = Color(0xFFC81E1E),
                            disabledContentColor = Color(0xFFA59B9E)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Hủy lịch",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (booking.status == AppConstants.BOOKING_STATUS_COMPLETED && !booking.isReviewed) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onReviewClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    GlowIcon(
                        icon = GlowIcons.StarFilled,
                        tint = ChampagneGold,
                        size = 15.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Viết đánh giá dịch vụ",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (booking.status == AppConstants.BOOKING_STATUS_COMPLETED && booking.isReviewed) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onReviewClick,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ChampagneGold),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFFDF6)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    GlowIcon(
                        icon = GlowIcons.StarFilled,
                        tint = ChampagneGold,
                        size = 15.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Đã gửi đánh giá · Xem nhận xét",
                        color = DustyMerlot,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (booking.status == AppConstants.BOOKING_STATUS_CANCELLED && !booking.cancelReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFF2E6DF), thickness = 1.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFAF2F2))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lý do hủy: ${booking.cancelReason}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF8B5E66)
                    )
                }
            }
        }
    }
}

private data class StatusStyle(
    val bg: Color,
    val border: Color,
    val text: Color,
    val dot: Color,
    val label: String
)

@Composable
private fun BookingStatusChip(status: String) {
    val style = when (status) {
        AppConstants.BOOKING_STATUS_CONFIRMED -> StatusStyle(
            bg = Color(0xFFEBF7EE),
            border = Color(0xFFC3E6CB),
            text = Color(0xFF1E7E34),
            dot = Color(0xFF1E7E34),
            label = "ĐÃ XÁC NHẬN"
        )
        AppConstants.BOOKING_STATUS_PENDING -> StatusStyle(
            bg = Color(0xFFFFF7E6),
            border = Color(0xFFFFE3B0),
            text = Color(0xFFD97706),
            dot = Color(0xFFD97706),
            label = "CHỜ XỬ LÝ"
        )
        AppConstants.BOOKING_STATUS_IN_SERVICE -> StatusStyle(
            bg = Color(0xFFEFF6FF),
            border = Color(0xFFBFDBFE),
            text = Color(0xFF2563EB),
            dot = Color(0xFF2563EB),
            label = "ĐANG LÀM"
        )
        AppConstants.BOOKING_STATUS_COMPLETED -> StatusStyle(
            bg = Color(0xFFF3F4F6),
            border = Color(0xFFE5E7EB),
            text = Color(0xFF4B5563),
            dot = Color(0xFF6B7280),
            label = "HOÀN THÀNH"
        )
        AppConstants.BOOKING_STATUS_CANCELLED -> StatusStyle(
            bg = Color(0xFFFEE2E2),
            border = Color(0xFFFECACA),
            text = Color(0xFFDC2626),
            dot = Color(0xFFDC2626),
            label = "ĐÃ HỦY"
        )
        else -> StatusStyle(
            bg = Color(0xFFF3F4F6),
            border = Color(0xFFE5E7EB),
            text = Color(0xFF4B5563),
            dot = Color(0xFF6B7280),
            label = status
        )
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(style.bg)
            .border(1.dp, style.border, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(style.dot)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = style.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = style.text
        )
    }
}

@Composable
private fun FilterTabButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) DustyMerlot else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextMuted
        )
    }
}
