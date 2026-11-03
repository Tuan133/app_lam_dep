package com.applamdep.customer.ui.shop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applamdep.customer.ui.components.GrabPrimaryButton
import com.applamdep.customer.ui.shop.ShopFilter
import com.applamdep.customer.ui.theme.*

/**
 * Bottom Sheet lọc tìm kiếm kiểu Grab:
 * - Đánh giá sao (Tất cả, 4.0+, 4.5+)
 * - Đối tác VIP
 * - Yêu cầu cọc (Tất cả, Cần cọc, Không cần cọc)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopFilterBottomSheet(
    currentFilter: ShopFilter,
    onApplyFilter: (ShopFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var tempFilter by remember { mutableStateOf(currentFilter) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = GrabSurface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Tiêu đề
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ lọc tìm kiếm",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrabTextPrimary
                )
                TextButton(
                    onClick = {
                        tempFilter = ShopFilter()
                    }
                ) {
                    Text(
                        text = "Đặt lại",
                        color = GrabTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Lọc theo đánh giá sao
            Text(
                text = "Đánh giá sao tối thiểu",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrabTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChoiceChip(
                    label = "Tất cả",
                    selected = tempFilter.minRating == 0.0,
                    onClick = { tempFilter = tempFilter.copy(minRating = 0.0) }
                )
                FilterChoiceChip(
                    label = "★ 4.0+",
                    selected = tempFilter.minRating == 4.0,
                    onClick = { tempFilter = tempFilter.copy(minRating = 4.0) }
                )
                FilterChoiceChip(
                    label = "★ 4.5+",
                    selected = tempFilter.minRating == 4.5,
                    onClick = { tempFilter = tempFilter.copy(minRating = 4.5) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Lọc theo yêu cầu đặt cọc
            Text(
                text = "Chính sách đặt cọc",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GrabTextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChoiceChip(
                    label = "Tất cả",
                    selected = tempFilter.requireDeposit == null,
                    onClick = { tempFilter = tempFilter.copy(requireDeposit = null) }
                )
                FilterChoiceChip(
                    label = "Không cần cọc",
                    selected = tempFilter.requireDeposit == false,
                    onClick = { tempFilter = tempFilter.copy(requireDeposit = false) }
                )
                FilterChoiceChip(
                    label = "Cần đặt cọc",
                    selected = tempFilter.requireDeposit == true,
                    onClick = { tempFilter = tempFilter.copy(requireDeposit = true) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Lọc chỉ Shop VIP
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        tempFilter = tempFilter.copy(onlyVip = !tempFilter.onlyVip)
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Chỉ xem Đối tác VIP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GrabTextPrimary
                    )
                    Text(
                        text = "Các salon cao cấp có chứng nhận sàn",
                        fontSize = 12.sp,
                        color = GrabTextSecondary
                    )
                }
                Switch(
                    checked = tempFilter.onlyVip,
                    onCheckedChange = { tempFilter = tempFilter.copy(onlyVip = it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GrabGreen
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Nút Áp dụng
            GrabPrimaryButton(
                text = "Áp dụng bộ lọc",
                onClick = {
                    onApplyFilter(tempFilter)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FilterChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) GrabGreenLight else GrabSurface)
            .border(
                width = 1.dp,
                color = if (selected) GrabGreen else GrabBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) GrabGreenDark else GrabTextPrimary
        )
    }
}
