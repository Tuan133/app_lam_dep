package com.applamdep.merchant.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.merchant.core.constants.MerchantConstants
import com.applamdep.merchant.ui.theme.MerchantPrimary
import com.applamdep.merchant.ui.theme.MerchantVipGold

/**
 * StatusBadge: Huy hiệu trạng thái chuẩn phong cách Grab (chữ đậm nét, nền nhạt tương ứng)
 */
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        MerchantConstants.BOOKING_STATUS_PENDING -> Triple(
            Color(0xFFFFF4E5), Color(0xFFB76E00), "Chờ duyệt"
        )
        MerchantConstants.BOOKING_STATUS_CONFIRMED -> Triple(
            Color(0xFFE8F4FD), Color(0xFF1976D2), "Đã xác nhận"
        )
        MerchantConstants.BOOKING_STATUS_IN_SERVICE -> Triple(
            Color(0xFFE0F2F1), MerchantPrimary, "Đang phục vụ"
        )
        MerchantConstants.BOOKING_STATUS_COMPLETED -> Triple(
            Color(0xFFEDF7ED), Color(0xFF2E7D32), "Hoàn thành"
        )
        MerchantConstants.BOOKING_STATUS_CANCELLED -> Triple(
            Color(0xFFFDECEA), Color(0xFFD32F2F), "Đã hủy"
        )
        MerchantConstants.SHOP_STATUS_APPROVED -> Triple(
            Color(0xFFEDF7ED), Color(0xFF2E7D32), "Đang hoạt động"
        )
        MerchantConstants.SHOP_STATUS_PENDING -> Triple(
            Color(0xFFFFF4E5), Color(0xFFB76E00), "Chờ admin duyệt"
        )
        MerchantConstants.SHOP_STATUS_REJECTED -> Triple(
            Color(0xFFFDECEA), Color(0xFFD32F2F), "Bị từ chối"
        )
        MerchantConstants.PLAN_VIP -> Triple(
            Color(0xFFFFF9E6), MerchantVipGold, "ĐỐI TÁC VIP"
        )
        MerchantConstants.PLAN_NORMAL -> Triple(
            Color(0xFFEEF1F4), Color(0xFF4B5563), "TIÊU CHUẨN"
        )
        MerchantConstants.TX_STATUS_HELD -> Triple(
            Color(0xFFFFF4E5), Color(0xFFB76E00), "Sàn đang giữ"
        )
        MerchantConstants.TX_STATUS_RELEASED_TO_SHOP -> Triple(
            Color(0xFFEDF7ED), Color(0xFF2E7D32), "Đã giải ngân"
        )
        MerchantConstants.TX_STATUS_REFUNDED -> Triple(
            Color(0xFFE8F4FD), Color(0xFF1976D2), "Đã hoàn khách"
        )
        else -> Triple(Color(0xFFF3F4F6), Color(0xFF4B5563), status)
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}
