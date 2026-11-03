package com.example.app_lam_dep.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.R
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ApplamdepTheme
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.GoldSoft

data class PartnerServiceItem(
    val name: String,
    val duration: String,
    val deposit: String,
    val price: String,
    val imageRes: Int
)

@Composable
fun PartnerDashboardScreen(
    onNavigateToReviews: () -> Unit,
    modifier: Modifier = Modifier
) {
    val services = listOf(
        PartnerServiceItem(
            name = "Sơn Gel Hữu Cơ Pháp +\nDưỡng Móng",
            duration = "45 phút",
            deposit = "Cọc 50k",
            price = "180.000 đ",
            imageRes = R.drawable.nail_french_gel
        ),
        PartnerServiceItem(
            name = "Nail Art Mắt Mèo Cực\nQuang VIP",
            duration = "60 phút",
            deposit = "Cọc 84k",
            price = "280.000 đ",
            imageRes = R.drawable.nail_cateye_aurora
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Luxury Salon Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3D0C18),
                                    Color(0xFF290610),
                                    Color(0xFF1E030B)
                                )
                            )
                        )
                        .padding(top = 22.dp, bottom = 22.dp, start = 20.dp, end = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Information Column
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Crown Circular Badge
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFF9F5))
                                    .border(1.5.dp, Brush.linearGradient(listOf(ChampagneGold, GoldSoft)), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowIcon(
                                    icon = GlowIcons.Crown,
                                    tint = Color(0xFFB8863A),
                                    size = 30.dp,
                                    contentDescription = "Salon Crown"
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Salon Name in Haute Couture Serif
                            Text(
                                text = "L'Amour Luxury\nSpa & Nail",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 30.sp
                            )

                            // Location and Hours
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.MapPin, tint = Color(0xFFF3C2CA), size = 12.dp)
                                Text(
                                    text = "Quận 1, TP. Hồ Chí Minh · Mở cửa: 09:00 - 21:00",
                                    color = Color(0xFFF3C2CA),
                                    fontSize = 11.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Status Pill Button
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFFB58451), Color(0xFFC79863))
                                        )
                                    )
                                    .border(1.dp, Color(0xFFFFDFB0).copy(alpha = 0.5f), CircleShape)
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    GlowIcon(icon = GlowIcons.ChevronRight, tint = Color.White, size = 12.dp)
                                    Text(
                                        text = "Đang hoạt động",
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Right: Arched Luxury Spa Portal
                        Box(
                            modifier = Modifier
                                .width(118.dp)
                                .height(172.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 64.dp,
                                        topEnd = 64.dp,
                                        bottomStart = 24.dp,
                                        bottomEnd = 24.dp
                                    )
                                )
                                .border(
                                    2.dp,
                                    Brush.linearGradient(
                                        listOf(
                                            ChampagneGold,
                                            GoldSoft,
                                            Color(0xFF8B5A2B),
                                            ChampagneGold
                                        )
                                    ),
                                    RoundedCornerShape(
                                        topStart = 64.dp,
                                        topEnd = 64.dp,
                                        bottomStart = 24.dp,
                                        bottomEnd = 24.dp
                                    )
                                )
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.shop_hero_spa),
                                contentDescription = "Spa Portal",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // 2. Metrics Card: Booking hôm nay & Doanh thu ngày
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left half: Booking hôm nay
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF9EFEF)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlowIcon(icon = GlowIcons.Calendar, tint = Color(0xFF5A1426), size = 22.dp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = "Booking hôm nay", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                            Text(text = "12", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .width(1.dp)
                            .background(Color(0xFFF0E5DE))
                    )

                    // Right half: Doanh thu ngày
                    Row(
                        modifier = Modifier
                            .weight(1.25f)
                            .padding(start = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF9EFEF)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlowIcon(icon = GlowIcons.Coins, tint = Color(0xFF5A1426), size = 22.dp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = "Doanh thu ngày", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                            Text(text = "4.850.000 đ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }
                    }
                }
            }
        }

        // 3. Reviews Card: Đánh giá của khách hàng (Clickable)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReviews() },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF9EFEF)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlowIcon(icon = GlowIcons.StarFilled, tint = Color(0xFF882C42), size = 22.dp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(text = "Đánh giá của khách hàng", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "4.9",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF24090F)
                                )
                                Text(
                                    text = " (1.8k+ reviews)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF24090F)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "Xem tất cả", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5A1426))
                        GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF5A1426), size = 14.dp)
                    }
                }
            }
        }

        // 4. Section Title: Danh Mục Dịch Vụ Của Shop with Gold Star Line
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowIcon(icon = GlowIcons.ShoppingBag, tint = Color(0xFF6B1228), size = 24.dp)
                    Text(
                        text = "Danh Mục Dịch Vụ Của Shop",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B1228)
                    )
                }

                // Gold decorative line with sparkle ✦
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, Color(0xFFBA8B55))
                                )
                            )
                    )
                    Text(
                        text = "✦",
                        fontSize = 12.sp,
                        color = Color(0xFFBA8B55),
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }

        // 5. Service Cards List
        items(services.size) { idx ->
            val serv = services[idx]
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Thumbnail photo
                    Image(
                        painter = painterResource(id = serv.imageRes),
                        contentDescription = serv.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    // Title & info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = serv.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF2B0A13)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            GlowIcon(icon = GlowIcons.Clock, tint = Color(0xFF7A686D), size = 12.dp)
                            Text(
                                text = "${serv.duration} · ${serv.deposit}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF7A686D)
                            )
                        }
                    }

                    // Price & chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = serv.price,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF6B1228)
                        )
                        GlowIcon(
                            icon = GlowIcons.ChevronRight,
                            tint = Color(0xFF6B1228),
                            size = 14.dp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
fun PartnerDashboardScreenPreview() {
    ApplamdepTheme {
        PartnerDashboardScreen(onNavigateToReviews = {})
    }
}
