package com.example.app_lam_dep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowBackButton
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.GoldSoft
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted

@Composable
fun ShopMembershipPlansScreen(
    currentPlanName: String = "Gói VIP Sponsor (Còn 24 ngày)",
    onBack: () -> Unit,
    onPlanSubscribed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 0: 1 Tháng, 1: 3 Tháng, 2: 6 Tháng, 3: 12 Tháng
    var selectedCycleIdx by remember { mutableStateOf(2) } // Default 6 months
    var showSuccessDialog by remember { mutableStateOf(false) }
    var registeredPlanTitle by remember { mutableStateOf("") }

    val cycles = listOf(
        CycleOption(1, "1 Tháng", "Theo tháng"),
        CycleOption(3, "3 Tháng", "Khởi đầu chuẩn"),
        CycleOption(6, "6 Tháng", "Tiết kiệm 10%"),
        CycleOption(12, "12 Tháng", "Tiết kiệm 20% + Tặng 1 tháng")
    )

    val currentCycle = cycles[selectedCycleIdx]

    // Pricing calculation
    val standardFee = when (currentCycle.months) {
        1 -> 220000L
        3 -> 590000L
        6 -> 1090000L
        else -> 1990000L
    }

    val vipRegularFee = standardFee
    val vipAdFee = when (currentCycle.months) {
        1 -> 350000L
        3 -> 900000L
        6 -> 1700000L
        else -> 3000000L
    }
    val vipTotalFee = vipRegularFee + vipAdFee

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8))
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DustyMerlot)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlowBackButton(onClick = onBack)
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Gói Dịch Vụ Mở Sàn & Đề Xuất", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    GlowIcon(icon = GlowIcons.Crown, tint = ChampagneGold, size = 16.dp)
                }
                Text(text = "Đang áp dụng: $currentPlanName", fontSize = 11.sp, color = PinkPanther)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Free First Month Special Promo Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DustyMerlot),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF2E0913), DustyMerlot, Color(0xFF7E2A3E))
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ChampagneGold)
                                    .padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Sparkle, tint = DustyMerlot, size = 12.dp)
                                Text(
                                    text = "ĐẶC QUYỀN ĐỐI TÁC MỚI",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DustyMerlot
                                )
                            }

                            Text(
                                text = "MIỄN PHÍ (FREE) THÁNG ĐẦU TIÊN",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                text = "Áp dụng cho mọi gói đăng ký 3, 6 hoặc 12 tháng. Bạn sẽ được dùng thử 30 ngày hoàn toàn miễn phí trước khi bắt đầu tính chu kỳ cước!",
                                fontSize = 12.sp,
                                color = PinkPanther,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Duration Cycle Selector (3, 6, 12 Tháng)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Chọn Chu Kỳ Thanh Toán Phí",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DustyMerlot
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cycles.forEachIndexed { idx, cycle ->
                            val isSel = idx == selectedCycleIdx
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCycleIdx = idx },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSel) DustyMerlot else Color.White
                                ),
                                border = if (isSel) BorderStroke(1.5.dp, ChampagneGold) else BorderStroke(1.dp, Color(0xFFEBDCD3))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = cycle.label,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Color(0xFF24090F)
                                    )
                                    Text(
                                        text = cycle.sub,
                                        fontSize = 9.5.sp,
                                        color = if (isSel) PinkPanther else TextMuted,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // GÓI 1: GÓI CỬA HÀNG BÌNH THƯỜNG MỞ SÀN (Phí thường niên)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFEBDCD3)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFEFECE6))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("GÓI CỬA HÀNG BÌNH THƯỜNG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gói Cửa Hàng Thường (Mở Sàn)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF24090F)
                                )
                                Text(text = "Phí thường niên duy trì mở sàn (${currentCycle.label})", fontSize = 11.5.sp, color = TextMuted)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatVnd(standardFee),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DustyMerlot
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Free tháng đầu", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF0E5DE))

                        // Features
                        val standardFeatures = listOf(
                            "Mở gian hàng kinh doanh chính thức trên sàn Glow Prestige",
                            "Đăng tải tối đa 15 dịch vụ làm đẹp & menu bảng giá",
                            "Quản lý lịch hẹn 7 ngày & Khóa/Mở khung giờ linh hoạt",
                            "Cài đặt nhận cọc: Tùy chọn Bật/Tắt cọc & Set 5 mệnh giá",
                            "Tặng 01 tháng đầu tiên trải nghiệm 0đ"
                        )

                        standardFeatures.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Check, tint = SuccessGreen, size = 14.dp)
                                Text(text = feat, fontSize = 12.sp, color = Color(0xFF3A101A))
                            }
                        }

                        Button(
                            onClick = {
                                registeredPlanTitle = "Gói Thường Mở Sàn (${currentCycle.label})"
                                showSuccessDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Đăng Ký Gói Thường (${currentCycle.label})", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // GÓI 2: GÓI CỬA HÀNG VIP ĐỂ TĂNG ĐỀ XUẤT (Phí thường niên + Phí quảng cáo)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.5.dp, ChampagneGold),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Brush.linearGradient(listOf(ChampagneGold, GoldSoft)))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    GlowIcon(icon = GlowIcons.Crown, tint = DustyMerlot, size = 11.dp)
                                    Text("TĂNG ĐỀ XUẤT TOP 1 MARKETPLACE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Gói Cửa Hàng VIP (Tăng Đề Xuất)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DustyMerlot
                                )
                                Text(text = "Phí thường niên + Phí quảng cáo đẩy top (${currentCycle.label})", fontSize = 11.sp, color = TextMuted)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatVnd(vipTotalFee),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    color = DustyMerlot
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SuccessGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Free tháng đầu", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                                }
                            }
                        }

                        // Transparent fee breakdown box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFBF7E8))
                                .border(1.dp, ChampagneGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(text = "Bóc tách cấu trúc chi phí gói VIP:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("1. Phí thường niên mở sàn:", fontSize = 11.sp, color = TextMuted)
                                    Text(formatVnd(vipRegularFee), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("2. Phí quảng cáo đề xuất Top 1:", fontSize = 11.sp, color = TextMuted)
                                    Text(formatVnd(vipAdFee), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF0E5DE))

                        // VIP Features
                        val vipFeatures = listOf(
                            "Tất cả quyền lợi của Gói Thường Mở Sàn",
                            "Huy hiệu Vàng \"SPONSORED TOP\" trên đầu trang chủ và danh mục",
                            "Ưu tiên Top 1 thuật toán gợi ý tìm kiếm theo bán kính quận",
                            "Chatbox AI tự động ưu tiên đề xuất shop khi khách tìm dịch vụ",
                            "Đẩy thông báo Deal độc quyền đến 50.000+ khách hàng thành viên",
                            "Được FREE 01 tháng đầu tiên trải nghiệm không mất phí"
                        )

                        vipFeatures.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Crown, tint = ChampagneGold, size = 14.dp)
                                Text(text = feat, fontSize = 12.sp, color = Color(0xFF3A101A), fontWeight = FontWeight.Medium)
                            }
                        }

                        Button(
                            onClick = {
                                registeredPlanTitle = "Gói VIP Tăng Đề Xuất (${currentCycle.months} tháng)"
                                showSuccessDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Crown, tint = DustyMerlot, size = 16.dp)
                                Text("Đăng Ký Gói VIP (${currentCycle.label}) - Free Tháng Đầu", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPlanSubscribed(registeredPlanTitle)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                ) {
                    Text("Đồng ý & Bắt đầu kinh doanh", color = Color.White)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 20.dp)
                    Text("Kích Hoạt Gói Thành Công!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DustyMerlot)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Đã kích hoạt: $registeredPlanTitle", fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                    Text(
                        text = "🎉 Quý shop được áp dụng ưu đãi MIỄN PHÍ 01 THÁNG ĐẦU TIÊN (30 ngày không trừ phí). Hệ thống đã cập nhật trạng thái hiển thị của cơ sở lên sàn!",
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

private data class CycleOption(val months: Int, val label: String, val sub: String)

private fun formatVnd(amount: Long): String {
    return String.format("%,d đ", amount).replace(',', '.')
}
