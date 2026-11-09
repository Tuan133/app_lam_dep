package com.applamdep.merchant.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.R
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.ui.component.MerchantTopBar
import com.applamdep.merchant.ui.component.StatusBadge
import com.applamdep.merchant.ui.theme.*

/**
 * Màn hình theo dõi trạng thái hồ sơ cửa hàng:
 * PENDING (Chờ duyệt) / APPROVED (Đã duyệt) / REJECTED (Bị từ chối kèm lý do và cho sửa gửi lại)
 */
@Composable
fun ShopStatusTrackingScreen(
    profile: ShopProfile,
    onEnterPortal: () -> Unit,
    onEditAndResubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            MerchantTopBar(
                title = "Trạng thái Hồ sơ Cơ sở",
                onBackClick = onBack
            )
        },
        containerColor = MerchantBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (profile.status) {
                MerchantConstants.SHOP_STATUS_APPROVED -> {
                    StatusIconBox(
                        icon = Icons.Default.Check,
                        iconTint = MerchantSuccess,
                        bgTint = Color(0xFFEDF7ED)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hồ sơ đã được phê duyệt!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Cửa hàng '${profile.name}' của bạn đã chính thức hoạt động trên sàn và hiển thị tới khách hàng.",
                        fontSize = 14.sp,
                        color = MerchantTextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    StatusBadge(status = profile.status)
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onEnterPortal,
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Truy cập Trang Quản trị Cửa hàng", fontWeight = FontWeight.Bold)
                    }
                }

                MerchantConstants.SHOP_STATUS_REJECTED -> {
                    StatusIconBox(
                        icon = Icons.Default.Close,
                        iconTint = MerchantError,
                        bgTint = Color(0xFFFDECEA)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hồ sơ cần bổ sung / Bị từ chối",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lý do từ chối từ Ban Quản Trị:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MerchantTextPrimary
                    )
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = profile.rejectReason ?: "Thông tin hình ảnh hoặc địa chỉ cơ sở chưa đủ rõ ràng. Vui lòng cập nhật lại giấy phép hoặc ảnh mặt tiền salon.",
                            fontSize = 13.sp,
                            color = MerchantError,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onEditAndResubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = MerchantPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = stringResource(R.string.merchant_resubmit_registration), fontWeight = FontWeight.Bold)
                    }
                }

                else -> { // PENDING
                    StatusIconBox(
                        icon = Icons.Default.HourglassTop,
                        iconTint = Color(0xFFB76E00),
                        bgTint = Color(0xFFFFF4E5)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Hồ sơ đang chờ xét duyệt",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MerchantTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Đội ngũ kiểm duyệt sàn làm đẹp đang thẩm định hồ sơ cửa hàng '${profile.name}'. Thời gian xử lý thông thường từ 2 - 24 giờ làm việc.",
                        fontSize = 14.sp,
                        color = MerchantTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StatusBadge(status = MerchantConstants.SHOP_STATUS_PENDING)
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = onEditAndResubmit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(text = "Chỉnh sửa thông tin hồ sơ", color = MerchantPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIconBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    bgTint: Color
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(bgTint),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(40.dp)
        )
    }
}
