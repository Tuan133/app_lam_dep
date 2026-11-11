package com.applamdep.merchant.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.di.MerchantViewModelFactory
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.ErrorStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

/**
 * DASHBOARD CỔNG ĐỐI TÁC CỬA HÀNG (MERCHANT PORTAL - 10 PHÂN HỆ QUY CHUẨN)
 * Điều hướng nhanh chóng đến tất cả các nghiệp vụ của salon
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MerchantDashboardScreen(
    onNavigateToBookings: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToReviews: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantDashboardViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    shopId: String = "shop_01",
    ownerUid: String = "merchant_user_01"
) {
    val uiState by viewModel.uiState.collectAsState()
    val autoCount by viewModel.autoCompletedCount.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(shopId, ownerUid) {
        viewModel.loadDashboard(shopId, ownerUid)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MerchantBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MerchantPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Merchant Portal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MerchantTextPrimary
                            )
                            Text(
                                text = "Kênh Quản Trị Cửa Hàng",
                                fontSize = 11.sp,
                                color = MerchantTextSecondary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Hồ sơ & Cài đặt",
                            tint = MerchantTextPrimary
                        )
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Đăng xuất tài khoản cửa hàng",
                            tint = Color(0xFFDC2626)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MerchantSurface)
            )
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is UiState.Idle, is UiState.Loading -> {
                LoadingStateView(message = "Đang tải tổng quan salon...")
            }
            is UiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.loadDashboard(shopId, ownerUid) }
                )
            }
            is UiState.Success -> {
                val data = state.data
                val profile = data.profile
                val stats = data.stats

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Thẻ hồ sơ đối tác & Huy hiệu gói
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MerchantCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MerchantPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = MerchantPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.shopName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MerchantTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "Chủ tiệm: ${profile.ownerName.ifBlank { "Đối tác" }} · ${profile.phone}",
                                    fontSize = 12.sp,
                                    color = MerchantTextSecondary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StatusBadge(status = profile.status)
                                }
                            }
                        }
                    }

                    // Thông báo tự động hoàn thành (Requirement #7)
                    if (autoCount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MerchantSecondary.copy(alpha = 0.1f))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MerchantSecondary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Đã tự động chuyển $autoCount lịch hẹn sang trạng thái HOÀN THÀNH (sau 30p kết thúc) và giải ngân tiền cọc.",
                                    fontSize = 12.sp,
                                    color = MerchantSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // 2. Các chỉ số thống kê kinh doanh (Requirement #10)
                    Text(
                        text = stringResource(R.string.merchant_stats_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardMetricCard(
                            title = "Lịch hôm nay",
                            value = "${stats.totalBookingsToday}",
                            subtitle = "Lượt khách",
                            icon = Icons.Default.CalendarToday,
                            color = MerchantPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        DashboardMetricCard(
                            title = "Chờ xác nhận",
                            value = "${stats.pendingBookings}",
                            subtitle = "Đơn hẹn mới",
                            icon = Icons.Default.PendingActions,
                            color = MerchantWarning,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardMetricCard(
                            title = "Đang phục vụ",
                            value = "${stats.inServiceBookings}",
                            subtitle = "Khách tại quán",
                            icon = Icons.Default.Face,
                            color = MerchantPrimaryDark,
                            modifier = Modifier.weight(1f)
                        )

                        DashboardMetricCard(
                            title = "Doanh thu hôm nay",
                            value = Formatters.formatCurrency(stats.estimatedRevenueToday),
                            subtitle = "Cọc sàn sau hoàn thành",
                            icon = Icons.Default.AttachMoney,
                            color = MerchantSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 3. Danh mục truy cập nhanh 6 phân hệ quản trị
                    Text(
                        text = "Phân hệ quản trị Salon",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FeatureRow(
                            icon = Icons.Default.EventNote,
                            title = "Quản lý Lịch hẹn & Trạng thái",
                            description = "Từ chối, hủy lịch, duyệt hoàn thành (+30p)",
                            badge = if (stats.pendingBookings > 0) "${stats.pendingBookings} mới" else null,
                            onClick = onNavigateToBookings
                        )

                        FeatureRow(
                            icon = Icons.Default.ContentCut,
                            title = "Quản lý Dịch vụ Salon",
                            description = "Bảng giá, thời lượng thực hiện, ẩn/xóa an toàn",
                            onClick = onNavigateToServices
                        )

                        FeatureRow(
                            icon = Icons.Default.AccessTime,
                            title = "Lịch làm việc & Khung giờ Slot",
                            description = "Cấu hình ca mở cửa, kiểm soát sức chứa slot",
                            onClick = onNavigateToSchedule
                        )

                        FeatureRow(
                            icon = Icons.Default.StarHalf,
                            title = "Đánh giá & Phản hồi khách",
                            description = "Xem sao, trả lời phản hồi, báo cáo vi phạm",
                            badge = "${stats.averageRating} ★",
                            onClick = onNavigateToReviews
                        )

                        FeatureRow(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Theo dõi tiền cọc",
                            description = "Theo dõi cọc sàn giữ, giải ngân, hoàn tiền",
                            onClick = onNavigateToFinance
                        )

                        FeatureRow(
                            icon = Icons.Default.Store,
                            title = "Hồ sơ Cửa hàng & Gói Hội viên",
                            description = "Thông tin cơ sở, chính sách 5 mức cọc, gói VIP",
                            onClick = onNavigateToProfile
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Đăng xuất tài khoản cửa hàng",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
            else -> Unit
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Xác nhận đăng xuất",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MerchantTextPrimary
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất tài khoản cửa hàng và quay về màn hình đăng nhập?",
                    fontSize = 14.sp,
                    color = MerchantTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Hủy", color = MerchantTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DashboardMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MerchantCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MerchantTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MerchantTextPrimary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                color = MerchantTextHint
            )
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MerchantCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MerchantPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MerchantPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantTextPrimary
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MerchantTextSecondary
                )
            }

            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MerchantVipBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantVipGold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MerchantTextHint,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
