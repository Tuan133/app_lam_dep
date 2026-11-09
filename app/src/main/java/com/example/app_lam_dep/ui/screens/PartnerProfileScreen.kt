package com.example.app_lam_dep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.icons.IconMedallion
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted
import com.example.app_lam_dep.ui.theme.WarningOrange

@Composable
fun PartnerProfileScreen(
    currentPlanName: String = "Gói VIP Sponsor (Còn 24 ngày)",
    onOpenMembershipPlans: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reported by remember { mutableStateOf(false) }

    // Shop Profile Info state
    var shopName by remember { mutableStateOf("L'Amour Haute Nail & Lash Lounge") }
    var shopAddress by remember { mutableStateOf("45 Đồng Khởi, Phường Bến Nghé, Quận 1, TP.HCM") }
    var shopPhone by remember { mutableStateOf("0908 888 999") }
    var shopHours by remember { mutableStateOf("09:00 - 21:00 (Mở cả tuần)") }
    var shopDesc by remember { mutableStateOf("Salon phong cách Haute Couture Châu Âu, chuyên Nail Art đính đá Swarovski, Spa dưỡng sinh & Chăm sóc da chuyên sâu.") }
    
    // Deposit settings (Shop set 5 mệnh giá)
    var requiresDeposit by remember { mutableStateOf(true) }
    var depositTiers by remember { mutableStateOf(listOf("50.000đ", "100.000đ", "200.000đ", "300.000đ", "500.000đ")) }

    var showEditDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ──────────────── 1. SALON PROFILE TOP CARD ────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, ChampagneGold.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconMedallion(
                            icon = GlowIcons.Crown,
                            size = 56.dp,
                            iconSize = 26.dp,
                            tint = ChampagneGold,
                            contentDescription = "Salon Crown"
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = shopName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3A101A))
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                GlowIcon(icon = GlowIcons.MapPin, tint = DustyMerlot.copy(alpha = 0.7f), size = 12.dp)
                                Text(text = shopAddress, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                GlowIcon(icon = GlowIcons.Phone, tint = SuccessGreen, size = 12.dp)
                                Text(text = "Hotline: $shopPhone", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF24090F))
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF0E5DE))

                    // Description & Amenities
                    Text(text = shopDesc, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)

                    val amenities = listOf("Wifi 5G", "Bãi đỗ ô tô", "Phòng VIP riêng", "Nước detox miễn phí")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(amenities) { am ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF5EFEB))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = "✓ $am", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = DustyMerlot)
                            }
                        }
                    }

                    // Button Chỉnh sửa thông tin cơ sở
                    OutlinedButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, DustyMerlot)
                    ) {
                        GlowIcon(icon = GlowIcons.Settings, tint = DustyMerlot, size = 14.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chỉnh sửa thông tin quán & Cài đặt cọc", fontSize = 12.sp, color = DustyMerlot, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ──────────────── 2. CÀI ĐẶT CỌC CỦA SHOP (5 MỆNH GIÁ) ────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFEBDCD3))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Cài Đặt Nhận Cọc Của Shop", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DustyMerlot)
                            Text(
                                text = if (requiresDeposit) "Shop đang yêu cầu đặt cọc trước" else "Shop đang cho giữ chỗ miễn phí (0đ cọc)",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = requiresDeposit,
                            onCheckedChange = { requiresDeposit = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DustyMerlot, checkedTrackColor = PinkPanther)
                        )
                    }

                    if (requiresDeposit) {
                        HorizontalDivider(color = Color(0xFFF0E5DE))
                        Text(text = "Mức tiền cọc shop quy định (khách cọc theo mức này):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(depositTiers) { tier ->
                                val isChosen = tier == "50.000đ"
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isChosen) DustyMerlot else Color(0xFFF5EFEB))
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = if (isChosen) "$tier (Đang áp dụng)" else tier,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChosen) Color.White else DustyMerlot
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ──────────────── 3. QUẢN LÝ GÓI MEMBERSHIP (3, 6, 12 THÁNG) ────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.5.dp, ChampagneGold),
                elevation = CardDefaults.cardElevation(3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GlowIcon(icon = GlowIcons.Crown, tint = ChampagneGold, size = 18.dp)
                            Text(text = "Gói Dịch Vụ Mở Sàn & Đề Xuất", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DustyMerlot)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SuccessGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Free tháng đầu", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFBF7E8))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Gói hiện tại của shop: $currentPlanName", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF24090F))
                            Text(
                                text = "Bao gồm: Phí thường niên duy trì gian hàng + Phí quảng cáo vị trí Sponsored Top 1 trang chủ.",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    Button(
                        onClick = onOpenMembershipPlans,
                        colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GlowIcon(icon = GlowIcons.Crown, tint = ChampagneGold, size = 16.dp)
                            Text("Xem Bảng Giá Gói 3, 6, 12 Tháng & Ưu Đãi", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // ──────────────── 4. KIỂM DUYỆT ĐÁNH GIÁ CỦA KHÁCH ────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Đánh giá của khách hàng (1★)", fontWeight = FontWeight.Bold, color = DustyMerlot)
                        Text(text = "Hôm nay", fontSize = 12.sp, color = TextMuted)
                    }
                    Text(
                        text = "Nội dung: 'Nhân viên thái độ không tốt, làm không đúng hẹn...' (Đánh giá tiêu cực / spam)",
                        fontSize = 12.5.sp,
                        color = TextMuted
                    )

                    if (reported) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(WarningOrange.copy(alpha = 0.2f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Đã tố cáo lên Admin (Chờ Admin kiểm duyệt xóa)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = WarningOrange
                            )
                        }
                    } else {
                        Button(
                            onClick = { reported = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.align(Alignment.End).height(36.dp)
                        ) {
                            Text("Tố cáo cho Admin xem xét", fontSize = 11.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Modal Edit Shop Profile
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                Button(
                    onClick = { showEditDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                ) {
                    Text("Lưu Thay Đổi", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Hủy", color = TextMuted)
                }
            },
            title = {
                Text(text = "Chỉnh Sửa Thông Tin Quán", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DustyMerlot)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Tên Salon / Spa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shopPhone,
                        onValueChange = { shopPhone = it },
                        label = { Text("Số điện thoại Hotline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shopAddress,
                        onValueChange = { shopAddress = it },
                        label = { Text("Địa chỉ cơ sở") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shopHours,
                        onValueChange = { shopHours = it },
                        label = { Text("Giờ mở cửa") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}
