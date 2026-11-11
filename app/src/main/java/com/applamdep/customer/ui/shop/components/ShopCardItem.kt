package com.applamdep.customer.ui.shop.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.ui.components.GrabDepositBadge
import com.applamdep.customer.ui.components.GrabVipBadge
import com.applamdep.customer.ui.theme.*

/**
 * Item hiển thị Shop trong danh sách: Phong cách Grab với card bo góc 16dp
 */
@Composable
fun ShopCardItem(
    shop: Shop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GrabSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // Ảnh bìa shop kèm các nhãn nổi bật
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val imageRes = com.applamdep.customer.core.util.ShopImageAssets.getShopDrawableRes(shop.id)
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = imageRes),
                    contentDescription = shop.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Hàng nhãn góc trên
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (shop.isVipActive) {
                        GrabVipBadge()
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    GrabDepositBadge(requiresDeposit = shop.requireDeposit)
                }
            }

            // Thông tin shop
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = shop.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GrabTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Điểm đánh giá sao
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = GrabAccentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${shop.rating}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GrabTextPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = " (${shop.reviewCount})",
                            fontSize = 12.sp,
                            color = GrabTextSecondary,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Địa chỉ
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = GrabTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = shop.address,
                        fontSize = 13.sp,
                        color = GrabTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Giờ mở cửa / Trạng thái đóng cửa
                val isClosed = shop.status == AppConstants.SHOP_STATUS_CLOSED
                Text(
                    text = if (isClosed) "Đã đóng cửa" else "Giờ mở cửa: ${shop.openingHours}",
                    fontSize = 12.sp,
                    color = if (isClosed) Color(0xFFD32F2F) else GrabGreenDark,
                    fontWeight = if (isClosed) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
