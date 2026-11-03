package com.applamdep.customer.ui.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.customer.ui.components.GrabCard
import com.applamdep.customer.ui.components.GrabPrimaryButton
import com.applamdep.customer.ui.theme.*

/**
 * MÀN HÌNH XÁC NHẬN ĐẶT LỊCH THÀNH CÔNG (NHÓM 3 - P0)
 */
@Composable
fun BookingConfirmationScreen(
    bookingId: String,
    onViewHistory: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = GrabBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon thành công
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(GrabGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Đặt lịch thành công!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = GrabTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Hệ thống đã ghi nhận lịch hẹn và gửi thông báo tới cửa hàng để sẵn sàng phục vụ bạn.",
                fontSize = 13.sp,
                color = GrabTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Thẻ vé tóm tắt
            GrabCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Mã đặt lịch:",
                        fontSize = 13.sp,
                        color = GrabTextSecondary
                    )
                    Text(
                        text = bookingId,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = GrabBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Time",
                        tint = GrabGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Trạng thái: ĐÃ XÁC NHẬN",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabGreenDark
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Policy",
                        tint = GrabAccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Quy định: Hủy trước 2 tiếng hoàn 100% cọc về ví",
                        fontSize = 12.sp,
                        color = GrabTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Nút xem lịch sử
            GrabPrimaryButton(
                text = "Xem lịch sử đặt lịch",
                onClick = onViewHistory
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Nút về trang chủ
            OutlinedButton(
                onClick = onBackHome,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Về trang chủ",
                    color = GrabTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
