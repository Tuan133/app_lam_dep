package com.example.app_lam_dep.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.app_lam_dep.model.BookingTicket
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.icons.IconMedallion
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted

@Composable
fun CustomerBookingsScreen(
    bookings: List<BookingTicket> = emptyList(),
    modifier: Modifier = Modifier
) {
    // Default sample ticket if list is empty
    val displayBookings = if (bookings.isEmpty()) {
        listOf(
            BookingTicket(
                id = "#GLOW-8921",
                salonName = "L’Amour Haute Nail & Lash Lounge",
                serviceName = "Sơn Gel Hữu Cơ Pháp + Dưỡng Móng (45p)",
                servicePrice = "180.000 đ",
                date = "Hôm nay",
                timeSlot = "15:30",
                specialist = "",
                customerName = "Khánh Linh",
                customerPhone = "0903 123 456",
                note = "Khách yêu cầu sơn tone hồng thạch tự nhiên",
                requiresDeposit = true,
                depositAmount = 50000L,
                status = "ĐÃ XÁC NHẬN & ĐẶT CỌC"
            )
        )
    } else {
        bookings
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Page Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Lịch Hẹn Của Bạn",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3A101A)
                )
                Text(
                    text = "Quản lý vé booking, mã QR check-in, thông tin cọc và nhắc hẹn",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        // 2. Smart Reminder Alert Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF6EE)),
                border = BorderStroke(1.dp, PinkPanther)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconMedallion(icon = GlowIcons.Bell, size = 38.dp, iconSize = 18.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Hệ thống nhắc hẹn: Đã đồng bộ với Salon",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DustyMerlot
                        )
                        Text(
                            text = "Thông tin đặt lịch của bạn đã được chuyển về salon đối tác và xác nhận thành công.",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. Dynamic Active Booking Tickets
        items(displayBookings) { ticket ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column {
                    // Card Top Status Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (ticket.requiresDeposit) DustyMerlot else SuccessGreen)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            GlowIcon(icon = GlowIcons.Check, tint = Color.White, size = 14.dp)
                            Text(
                                text = ticket.status,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = ticket.id,
                            color = if (ticket.requiresDeposit) PinkPanther else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Card Body
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Salon & Service Name
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = ticket.salonName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3A101A)
                            )
                            Text(
                                text = ticket.serviceName,
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }

                        // Customer Info Row (Form Data Received)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFBF7E8))
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "KHÁCH HÀNG:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(text = ticket.customerName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "SỐ ĐIỆN THOẠI:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(text = ticket.customerPhone, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                }
                                if (ticket.note.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "GHI CHÚ:", fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                        Text(text = ticket.note, fontSize = 11.sp, color = TextMuted, maxLines = 1)
                                    }
                                }
                            }
                        }

                        // Time & Service Price Info Boxes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFBF7E8))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(text = "THỜI GIAN HẸN", fontSize = 9.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(text = "${ticket.timeSlot} • ${ticket.date}", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFBF7E8))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(text = "GIÁ DỊCH VỤ", fontSize = 9.5.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                    Text(text = ticket.servicePrice, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                }
                            }
                        }

                        // QR Code Container (Dotted Border)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, PinkPanther, RoundedCornerShape(14.dp))
                                .background(Color(0xFFFDF6EE))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(112.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, ChampagneGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlowIcon(icon = GlowIcons.QrCode, tint = Color(0xFF24090F), size = 88.dp, contentDescription = "Mã QR check-in")
                                }
                                Text(
                                    text = "Đưa mã QR này cho lễ tân tiệm ${ticket.salonName} để check-in",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DustyMerlot,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }

                        // Financial Summary
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Giá dịch vụ dự kiến:", fontSize = 12.5.sp, color = TextMuted)
                                Text(text = ticket.servicePrice, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF24090F))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (ticket.requiresDeposit) "Tiền cọc đã thanh toán (Đã nhận):" else "Chính sách cọc:",
                                    fontSize = 12.5.sp,
                                    color = TextMuted
                                )
                                Text(
                                    text = if (ticket.requiresDeposit) "-${formatVnd(ticket.depositAmount)}" else "0 đ (Miễn cọc)",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (ticket.requiresDeposit) DustyMerlot else SuccessGreen
                                )
                            }
                            HorizontalDivider(color = Color(0xFFEBDCD3))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Thanh toán còn lại tại Salon:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3A101A))
                                Text(
                                    text = if (ticket.requiresDeposit) "Theo hóa đơn tại quầy" else ticket.servicePrice,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DustyMerlot
                                )
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, DustyMerlot),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                            ) {
                                Text(text = "↺ Hủy & Hoàn cọc", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                            }

                            Button(
                                onClick = {},
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DustyMerlot)
                            ) {
                                GlowIcon(icon = GlowIcons.Calendar, tint = Color.White, size = 16.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Đổi giờ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatVnd(amount: Long): String {
    return String.format("%,d đ", amount).replace(',', '.')
}
