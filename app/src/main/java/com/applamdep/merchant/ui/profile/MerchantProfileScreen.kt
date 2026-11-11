package com.applamdep.merchant.ui.profile

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.app_lam_dep.R
import com.example.app_lam_dep.ui.screens.ShopMembershipPlansScreen
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.core.util.Formatters
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.ui.component.EmptyStateView
import com.applamdep.merchant.ui.component.LoadingStateView
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.merchant.core.di.MerchantViewModelFactory

/**
 * QUẢN LÝ HỒ SƠ CỬA HÀNG (MERCHANT REQUIREMENT #2 - P0)
 */
@Composable
fun MerchantProfileScreen(
    onBack: () -> Unit = {},
    onNavigateToRegistration: (() -> Unit)? = null,
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MerchantProfileViewModel = viewModel(factory = MerchantViewModelFactory.DefaultFactory),
    ownerUid: String = "merchant_user_01"
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialogInProfile by remember { mutableStateOf(false) }

    LaunchedEffect(ownerUid) {
        viewModel.loadProfile(ownerUid)
    }

    Scaffold(
        topBar = {
            MerchantTopBar(
                title = stringResource(R.string.merchant_profile_title),
                onBackClick = onBack,
                actions = {
                    IconButton(onClick = { showLogoutDialogInProfile = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Đăng xuất tài khoản cửa hàng",
                            tint = Color(0xFFDC2626)
                        )
                    }
                }
            )
        },
        containerColor = MerchantBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingStateView()
                is UiState.Error -> EmptyStateView(title = state.message)
                is UiState.Success -> {
                    ProfileContent(
                        shop = state.data,
                        viewModel = viewModel,
                        onSaved = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        },
                        onLogout = onLogout
                    )
                }
                is UiState.Idle -> LoadingStateView()
            }
        }
    }

    if (showLogoutDialogInProfile) {
        AlertDialog(
            onDismissRequest = { showLogoutDialogInProfile = false },
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
                        showLogoutDialogInProfile = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialogInProfile = false }) {
                    Text("Hủy", color = MerchantTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun ProfileContent(
    shop: ShopProfile,
    viewModel: MerchantProfileViewModel,
    onSaved: (String) -> Unit,
    onLogout: () -> Unit = {}
) {
    var name by remember(shop) { mutableStateOf(shop.name) }
    var ownerName by remember(shop) { mutableStateOf(shop.ownerName) }
    var phone by remember(shop) { mutableStateOf(shop.phone) }
    var email by remember(shop) { mutableStateOf(shop.email) }
    var address by remember(shop) { mutableStateOf(shop.address) }
    var hours by remember(shop) { mutableStateOf(shop.openingHours) }
    var description by remember(shop) { mutableStateOf(shop.description) }
    var requireDeposit by remember(shop) { mutableStateOf(shop.requireDeposit) }
    var depositInput by remember(shop) { mutableStateOf(shop.depositOptions.joinToString(", ")) }
    var imagesList by remember(shop) { mutableStateOf(shop.images.ifEmpty { listOf("https://images.unsplash.com/photo-1560066984-138dadb4c035") }) }

    var showWarningDialog by remember { mutableStateOf(false) }
    var pendingSaveProfile by remember { mutableStateOf<ShopProfile?>(null) }
    var showLogoutConfirmInContent by remember { mutableStateOf(false) }
    var showMembershipPlansDialog by remember { mutableStateOf(false) }
    var showClosureFlowDialog by remember { mutableStateOf(false) }
    val shopBookings by viewModel.shopBookings.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trạng thái hiện tại
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Trạng thái cơ sở trên sàn", fontSize = 12.sp, color = MerchantTextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = shop.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MerchantTextPrimary)
                    if (shop.lastEditedAt != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Lần sửa thông tin gần nhất: ${Formatters.formatDateTime(shop.lastEditedAt)}",
                            fontSize = 11.sp,
                            color = MerchantWarning
                        )
                    }
                    if (shop.isClosed) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val untilLabel = shop.closedUntilDate?.takeIf { it.isNotBlank() }
                            ?.let { " • Tạm đóng ngày $it" }
                            ?: " • Cho đến khi mở lại"
                        val reasonLabel = shop.closeReason?.takeIf { it.isNotBlank() } ?: "Tạm ngừng nhận khách"
                        Text(
                            text = "Lý do đóng cửa: $reasonLabel$untilLabel",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD32F2F)
                        )
                        shop.closeNote?.takeIf { it.isNotBlank() }?.let { note ->
                            Text(
                                text = "Ghi chú: $note",
                                fontSize = 11.sp,
                                color = MerchantTextSecondary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(status = shop.status)
            }
        }

        // KHỐI 1: THÔNG TIN CHÍNH
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Thông tin cơ sở", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MerchantTextPrimary)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên cửa hàng / Salon *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = ownerName,
                        onValueChange = { ownerName = it },
                        label = { Text("Chủ cơ sở") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Hotline") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email liên hệ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Địa chỉ cơ sở *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("Giờ phục vụ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả giới thiệu") },
                    modifier = Modifier.fillMaxWidth().height(80.dp)
                )
            }
        }

        // KHỐI 2: HÌNH ẢNH CỬA HÀNG (THÊM / XÓA)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Hình ảnh cơ sở (${imagesList.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MerchantTextPrimary)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(imagesList) { imgUrl ->
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MerchantSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            Text(text = "Ảnh Salon", fontSize = 11.sp, color = MerchantTextSecondary, modifier = Modifier.align(Alignment.Center))
                            IconButton(
                                onClick = { imagesList = imagesList.filterNot { it == imgUrl } },
                                modifier = Modifier.size(20.dp).align(Alignment.TopEnd)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", tint = MerchantError, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(MerchantPrimaryLight, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = {
                                imagesList = imagesList + "https://images.unsplash.com/photo-sample-${System.currentTimeMillis()}"
                                Toast.makeText(context, "Đã thêm ảnh mới", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm ảnh", tint = MerchantPrimary)
                            }
                        }
                    }
                }
            }
        }

        // KHỐI 3: CHÍNH SÁCH ĐẶT CỌC
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Yêu cầu khách cọc trước", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MerchantTextPrimary)
                    Switch(
                        checked = requireDeposit,
                        onCheckedChange = { requireDeposit = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MerchantPrimary)
                    )
                }

                if (requireDeposit) {
                    OutlinedTextField(
                        value = depositInput,
                        onValueChange = { depositInput = it },
                        label = { Text("5 mệnh giá cọc (VNĐ, cách nhau bằng dấu phẩy)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // KHỐI 4: Gói hội viên cửa hàng
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MerchantSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val isVipPlan = shop.plan.equals("VIP", ignoreCase = true)
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "4. Gói hội viên cửa hàng",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MerchantPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gói hiện tại",
                        fontSize = 13.sp,
                        color = MerchantTextSecondary
                    )
                    Surface(
                        color = if (isVipPlan) Color(0xFFFFF8E1) else MerchantPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isVipPlan) "Gói VIP" else "Gói Thường",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isVipPlan) Color(0xFFB45309) else MerchantPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Chu kỳ gói",
                        fontSize = 13.sp,
                        color = MerchantTextSecondary
                    )
                    Text(
                        text = "${shop.planTermMonths} tháng",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MerchantTextPrimary
                    )
                }
                if (shop.planExpiry > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hạn sử dụng",
                            fontSize = 13.sp,
                            color = MerchantTextSecondary
                        )
                        Text(
                            text = Formatters.formatDate(shop.planExpiry),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MerchantTextPrimary
                        )
                    }
                }
                OutlinedButton(
                    onClick = { showMembershipPlansDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MerchantPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MerchantPrimary),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đăng ký / Nâng cấp gói hội viên",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // NÚT LƯU THAY ĐỔI
        Button(
            onClick = {
                val parsedDeposit = depositInput.split(",").mapNotNull { it.trim().toLongOrNull() }
                val updated = shop.copy(
                    name = name.trim(),
                    ownerName = ownerName.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    address = address.trim(),
                    openingHours = hours.trim(),
                    description = description.trim(),
                    requireDeposit = requireDeposit,
                    depositOptions = parsedDeposit,
                    images = imagesList
                )

                if (viewModel.hasCriticalChanges(updated)) {
                    // Nếu sửa Tên hoặc Địa chỉ của shop đã APPROVED -> Hiển thị cảnh báo
                    pendingSaveProfile = updated
                    showWarningDialog = true
                } else {
                    viewModel.updateProfile(
                        updatedProfile = updated,
                        onSuccess = onSaved,
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(text = stringResource(R.string.merchant_profile_save_changes), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (shop.isClosed) {
            Button(
                onClick = {
                    viewModel.reopenShop(
                        onSuccess = {
                            Toast.makeText(context, "Cửa hàng đã mở cửa hoạt động lại!", Toast.LENGTH_SHORT).show()
                        },
                        onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B8A5A)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mở cửa hoạt động lại",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            OutlinedButton(
                onClick = { showClosureFlowDialog = true },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF8B1538).copy(alpha = 0.7f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF8B1538)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Chọn thời gian đóng / Huỷ lịch",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Button(
                onClick = { showClosureFlowDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1538)),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Đóng cửa",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        OutlinedButton(
            onClick = { showLogoutConfirmInContent = true },
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.6f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Đăng xuất tài khoản cửa hàng",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

    // DIALOG CẢNH BÁO SỬA THÔNG TIN QUAN TRỌNG
    if (showWarningDialog && pendingSaveProfile != null) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            icon = {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MerchantWarning, modifier = Modifier.size(36.dp))
            },
            title = {
                Text(text = "Cảnh báo thay đổi thông tin quan trọng", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Bạn đang thay đổi Tên hoặc Địa chỉ của cửa hàng đã được kiểm duyệt. Hệ thống sẽ ghi nhận mốc thời gian sửa đổi (lastEditedAt) để Ban Quản Trị nắm thông tin hậu kiểm.\n\nCửa hàng của bạn vẫn tiếp tục hoạt động bình thường trên sàn mà không bị ẩn đi.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = MerchantTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWarningDialog = false
                        viewModel.updateProfile(
                            updatedProfile = pendingSaveProfile!!,
                            onSuccess = onSaved,
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary)
                ) {
                    Text("Tôi hiểu & Lưu thay đổi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWarningDialog = false }) {
                    Text("Hủy bỏ")
                }
            }
        )
    }

    if (showLogoutConfirmInContent) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmInContent = false },
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
                        showLogoutConfirmInContent = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmInContent = false }) {
                    Text("Hủy", color = MerchantTextSecondary)
                }
            }
        )
    }

    if (showMembershipPlansDialog) {
        val currentPlanTitle = if (shop.plan.equals("VIP", ignoreCase = true)) {
            "Gói VIP Tăng Đề Xuất (${shop.planTermMonths} tháng)"
        } else {
            "Gói Thường Mở Sàn (${shop.planTermMonths} Tháng)"
        }
        Dialog(
            onDismissRequest = { showMembershipPlansDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ShopMembershipPlansScreen(
                currentPlanName = currentPlanTitle,
                onBack = { showMembershipPlansDialog = false },
                onPlanSubscribed = { selectedPlanTitle ->
                    val targetPlan = if (selectedPlanTitle.contains("VIP", ignoreCase = true)) "VIP" else "NORMAL"
                    val termMonths = Regex("\\d+").find(selectedPlanTitle)?.value?.toIntOrNull() ?: 1
                    showMembershipPlansDialog = false
                    viewModel.updateMembershipPlan(
                        plan = targetPlan,
                        termMonths = termMonths,
                        onSuccess = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            )
        }
    }

    if (showClosureFlowDialog) {
        ShopClosureFlowDialog(
            bookings = shopBookings,
            onConfirmUntilReopen = { reason, note, onDone ->
                viewModel.closeShopUntilReopen(
                    reason = reason,
                    note = note,
                    onSuccess = { onDone() },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onConfirmScheduledClosureAndCancel = { date, bookingIds, cancelReason, proposalNote, onDone ->
                viewModel.closeShopForScheduleAndCancelBookings(
                    date = date,
                    bookingIdsToCancel = bookingIds,
                    cancelReason = cancelReason,
                    proposalNote = proposalNote,
                    onSuccess = { _ -> onDone() },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    }
                )
            },
            onDismiss = { showClosureFlowDialog = false }
        )
    }
}
