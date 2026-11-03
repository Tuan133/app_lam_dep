package com.applamdep.customer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import com.applamdep.customer.ui.theme.*

/**
 * Thanh tìm kiếm nổi bật kiểu Grab (bo tròn 24dp, nền trắng)
 */
@Composable
fun GrabSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Tìm kiếm salon, spa, nail gần bạn...",
    onFilterClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(GrabSurface)
            .border(1.dp, GrabBorder, RoundedCornerShape(25.dp))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = GrabTextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = GrabTextHint,
                    fontSize = 14.sp
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.weight(1f)
        )
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = GrabTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Card bo góc 16dp chuẩn Grab
 */
@Composable
fun GrabCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GrabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(GrabBorder))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            content = content
        )
    }
}

/**
 * Nút bấm phong cách Grab (Màu xanh chủ đạo bo góc 12dp)
 */
@Composable
fun GrabPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GrabGreen,
            disabledContainerColor = GrabBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                color = if (enabled) Color.White else GrabTextHint,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Badge đối tác VIP
 */
@Composable
fun GrabVipBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(GrabVipBadge)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "★ ĐỐI TÁC VIP",
            color = GrabVipText,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Badge cọc / không cọc
 */
@Composable
fun GrabDepositBadge(requiresDeposit: Boolean) {
    val bgColor = if (requiresDeposit) Color(0xFFFFF0F0) else GrabGreenLight
    val textColor = if (requiresDeposit) GrabStatusError else GrabGreenDark
    val text = if (requiresDeposit) "Cần đặt cọc" else "Không cần cọc"

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Trạng thái Loading
 */
@Composable
fun GrabLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = GrabGreen)
    }
}

/**
 * Nút bấm chuẩn Grab (Alias cho GrabPrimaryButton)
 */
@Composable
fun GrabButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    GrabPrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isLoading = isLoading
    )
}

/**
 * Trạng thái Trống (Empty) - Hỗ trợ cả message đơn và cấu trúc icon, title, subtitle
 */
@Composable
fun GrabEmptyState(
    message: String = "",
    title: String? = null,
    subtitle: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val displayTitle = title ?: if (message.isNotBlank()) message else "Chưa có dữ liệu"
    val displaySubtitle = subtitle

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(GrabGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GrabGreenDark,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = displayTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = GrabTextPrimary,
            textAlign = TextAlign.Center
        )

        if (!displaySubtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = displaySubtitle,
                fontSize = 13.sp,
                color = GrabTextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = GrabGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Tải lại", color = Color.White)
            }
        }
    }
}

