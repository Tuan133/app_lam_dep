package com.example.app_lam_dep.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowBackButton
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.icons.StarRating
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.TextMuted
import com.example.app_lam_dep.ui.theme.WarningOrange

data class CustomerReview(val customerName: String, val rating: Int, val comment: String, val date: String)

@Composable
fun PartnerReviewsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reviews = listOf(
        CustomerReview("Hà An", 5, "Không gian tuyệt vời, nhân viên tay nghề đỉnh cao. Sẽ quay lại dài dài!", "Hôm nay"),
        CustomerReview("Nguyễn Thị Mai", 1, "Làm không đúng giờ hẹn, nhân viên thái độ...", "Hôm qua"),
        CustomerReview("Khánh Linh", 5, "Móng sơn bền và bóng đẹp lắm shop ơi! Lên tay sang và tinh tế.", "3 ngày trước")
    )

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Đánh Giá Của Khách Hàng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 16.dp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews.size) { idx ->
                val rev = reviews[idx]
                var reported by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = rev.customerName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DustyMerlot)
                            StarRating(rating = rev.rating, size = 13.dp)
                        }
                        Text(text = rev.comment, fontSize = 13.sp, color = Color(0xFF24090F))
                        Text(text = rev.date, fontSize = 11.sp, color = TextMuted)

                        HorizontalDivider(color = Color(0xFFEBDCD3))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {}) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    GlowIcon(icon = GlowIcons.Chat, tint = DustyMerlot, size = 14.dp)
                                    Text("Trả lời", fontSize = 12.sp, color = DustyMerlot, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (reported) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    GlowIcon(icon = GlowIcons.Alert, tint = WarningOrange, size = 13.dp)
                                    Text(text = "Đã tố cáo Admin", fontSize = 11.sp, color = WarningOrange, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                TextButton(onClick = { reported = true }) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        GlowIcon(icon = GlowIcons.Flag, tint = Color.Red, size = 13.dp)
                                        Text("Tố cáo Admin", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
