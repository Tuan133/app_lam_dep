package com.example.app_lam_dep.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.*
import com.example.app_lam_dep.ui.theme.*

// Data Models for Shop Profile
data class ShopServiceItem(
    val id: String,
    val name: String,
    val duration: String,
    val price: String,
    val icon: ImageVector,
    val tag: String,
    val isHot: Boolean = false
)

data class GalleryAlbumItem(
    val id: String,
    val title: String,
    val category: String,
    val photoCount: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val icon: ImageVector
)

data class ReviewCommentItem(
    val id: String,
    val author: String,
    val avatarColor: Color,
    val rating: Int,
    val timeAgo: String,
    val serviceUsed: String,
    val comment: String,
    val photosCount: Int
)

@Composable
fun SalonDetailScreen(
    salonName: String = "Beauty House",
    onBack: () -> Unit,
    onSelectServiceForBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Displayed shop name (falls back to Beauty House if default or empty)
    val displayShopName = if (salonName.isBlank() || salonName.contains("L'Amour") || salonName.contains("An Yên")) {
        salonName
    } else {
        "Beauty House"
    }

    // State for Profile Actions
    var isFavorite by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var galleryFilter by remember { mutableStateOf("Tất cả") }
    var reviewFilter by remember { mutableStateOf("Tất cả") }

    // Service list matching Beauty House profile
    val services = remember {
        listOf(
            ShopServiceItem("1", "Gội đầu dưỡng sinh thảo dược", "30 phút", "Từ 80.000đ", GlowIcons.Lotus, "Dưỡng sinh", isHot = true),
            ShopServiceItem("2", "Cắt tóc tạo kiểu thời trang Nam/Nữ", "45 phút", "Từ 150.000đ", GlowIcons.Scissors, "Stylist Cut", isHot = true),
            ShopServiceItem("3", "Uốn tóc sóng lơi Hàn Quốc / Setting", "120 phút", "Từ 800.000đ", GlowIcons.Comb, "Hot Trend", isHot = true),
            ShopServiceItem("4", "Nhuộm tóc thời trang & Phục hồi", "90 phút", "Từ 700.000đ", GlowIcons.Sparkle, "Chính hãng"),
            ShopServiceItem("5", "Duỗi tóc Collagen / Phồng tự nhiên", "150 phút", "Từ 900.000đ", GlowIcons.Crown, "Cao cấp"),
            ShopServiceItem("6", "Phục hồi tóc hư tổn Keratin & Olaplex", "60 phút", "Từ 500.000đ", GlowIcons.Serum, "Dưỡng sâu"),
            ShopServiceItem("7", "Làm móng gel nghệ thuật & Đính đá", "50 phút", "Từ 250.000đ", GlowIcons.NailPolish, "Nail Art"),
            ShopServiceItem("8", "Chăm sóc da mặt chuyên sâu & Điện di", "60 phút", "Từ 350.000đ", GlowIcons.Serum, "Spa Retreat")
        )
    }

    // Gallery albums
    val albums = remember {
        listOf(
            GalleryAlbumItem("g1", "BST Uốn Sóng Lơi Hàn Quốc", "Uốn tóc", "32 ảnh", Color(0xFFFDE8E4), Color(0xFFF6C3BC), GlowIcons.Comb),
            GalleryAlbumItem("g2", "Màu Nhuộm Nâu Tây Tự Nhiên", "Nhuộm tóc", "28 ảnh", Color(0xFFFBECE1), Color(0xFFE8C8B3), GlowIcons.Sparkle),
            GalleryAlbumItem("g3", "Không Gian Salon 5 Sao VIP", "Không gian", "15 ảnh", Color(0xFFF2ECE6), Color(0xFFD9CDC2), GlowIcons.Crown),
            GalleryAlbumItem("g4", "Nail Art Mắt Mèo Cực Quang", "Làm móng", "45 ảnh", Color(0xFFFCE4EC), Color(0xFFF8BBD0), GlowIcons.NailPolish),
            GalleryAlbumItem("g5", "Cắt Tóc Layer Nữ Thiết Kế", "Cắt tóc", "24 ảnh", Color(0xFFF3E5F5), Color(0xFFE1BEE7), GlowIcons.Scissors),
            GalleryAlbumItem("g6", "Phục Hồi Tóc Keratin Chuyên Sâu", "Uốn tóc", "19 ảnh", Color(0xFFE8F5E9), Color(0xFFC8E6C9), GlowIcons.Serum),
            GalleryAlbumItem("g7", "Tẩy Nhuộm Balayage Khói Sữa", "Nhuộm tóc", "22 ảnh", Color(0xFFFFF3E0), Color(0xFFFFE0B2), GlowIcons.Sparkle),
            GalleryAlbumItem("g8", "Khu Gội Đầu Dưỡng Sinh Thảo Mộc", "Không gian", "12 ảnh", Color(0xFFE0F2F1), Color(0xFFB2DFDB), GlowIcons.Lotus)
        )
    }

    // Customer reviews list
    val reviews = remember {
        listOf(
            ReviewCommentItem(
                id = "r1",
                author = "Nguyễn Thanh Mai",
                avatarColor = Color(0xFFB85368),
                rating = 5,
                timeAgo = "2 ngày trước",
                serviceUsed = "Uốn tóc sóng lơi Hàn Quốc",
                comment = "Không gian tiệm cực kỳ xinh và thư giãn! Anh Stylist tư vấn rất kỹ dáng mặt trước khi cắt. Uốn sóng lơi xong tóc mềm mượt không bị khô ráp xíu nào. Sẽ quay lại dài dài!",
                photosCount = 3
            ),
            ReviewCommentItem(
                id = "r2",
                author = "Trần Minh Anh",
                avatarColor = Color(0xFF62202F),
                rating = 5,
                timeAgo = "1 tuần trước",
                serviceUsed = "Nhuộm tóc thời trang & Phục hồi",
                comment = "Màu nhuộm lên chuẩn đét như hình mẫu mình gửi. Thuốc nhuộm không hề có mùi hắc khó chịu, gội đầu massage siêu êm ru ngủ quên luôn.",
                photosCount = 3
            ),
            ReviewCommentItem(
                id = "r3",
                author = "Lê Thu Hà",
                avatarColor = Color(0xFF8C3A4A),
                rating = 5,
                timeAgo = "2 tuần trước",
                serviceUsed = "Gội đầu dưỡng sinh thảo dược",
                comment = "Đi làm về mệt ghé tiệm gội đầu dưỡng sinh là đúng bài. Kỹ thuật viên ấn huyệt cổ vai gáy đã ơi là đã, phòng ốc thơm mùi sả chanh thư thái.",
                photosCount = 2
            ),
            ReviewCommentItem(
                id = "r4",
                author = "Hoàng Phương Vy",
                avatarColor = Color(0xFFA64457),
                rating = 4,
                timeAgo = "3 tuần trước",
                serviceUsed = "Làm móng gel nghệ thuật",
                comment = "Mẫu vẽ mắt mèo cực quang xinh lung linh, thợ làm tỉ mỉ từng ngón. Điểm trừ nhẹ là cuối tuần tiệm hơi đông nên phải chờ khoảng 10 phút dù có đặt trước.",
                photosCount = 3
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        // ────────────────────────── 1. TOP APP BAR ──────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DustyMerlot)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            GlowBackButton(onClick = onBack)

            // Header title
            Text(
                text = displayShopName,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )

            // Right Actions: Favorite & Share
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Heart Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable {
                            isFavorite = !isFavorite
                            Toast.makeText(
                                context,
                                if (isFavorite) "Đã thêm $displayShopName vào danh sách yêu thích ❤️"
                                else "Đã xóa khỏi danh sách yêu thích",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    GlowIcon(
                        icon = if (isFavorite) GlowIcons.HeartFilled else GlowIcons.Heart,
                        tint = if (isFavorite) Color(0xFFFF4B6E) else Color.White,
                        size = 18.dp
                    )
                }

                // Share Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(Color.White.copy(alpha = 0.14f))
                        .clickable {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Khám phá không gian làm đẹp $displayShopName tại 123 Nguyễn Huệ, Quận 1 trên Glow Prestige: https://glowprestige.vn/salon/beauty-house"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ salon $displayShopName"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Đã sao chép liên kết salon!", Toast.LENGTH_SHORT).show()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    GlowIcon(icon = GlowIcons.Share, tint = Color.White, size = 18.dp)
                }
            }
        }

        // ────────────────────────── 2. SCROLLABLE BODY ──────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ───────────────── 2A. SHOP PROFILE OVERVIEW CARD ─────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Shop Avatar with Luxury Medallion Border
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(DustyMerlot, Color(0xFF3D121B))))
                                .border(
                                    width = 2.5.dp,
                                    brush = Brush.linearGradient(listOf(ChampagneGold, GoldSoft)),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                GlowIcon(icon = GlowIcons.Crown, tint = ChampagneGold, size = 26.dp)
                                Text(
                                    text = "BH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ChampagneGold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Shop Name
                        Text(
                            text = displayShopName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextNoir,
                            textAlign = TextAlign.Center
                        )

                        // Rating + Review Count + 4 Social Media Buttons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Star & Reviews
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 15.dp)
                                Text(text = "4.8", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                Text(text = "(320 đánh giá)", fontSize = 12.sp, color = TextMuted)
                            }

                            Text(text = "•", color = BorderSilk, fontWeight = FontWeight.Bold)

                            // 4 Circular Social Media Buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SocialButton(icon = GlowIcons.Facebook, label = "Facebook") {
                                    Toast.makeText(context, "Mở Facebook: facebook.com/beautyhouse.vn", Toast.LENGTH_SHORT).show()
                                }
                                SocialButton(icon = GlowIcons.Instagram, label = "Instagram") {
                                    Toast.makeText(context, "Mở Instagram: @beautyhouse.vietnam", Toast.LENGTH_SHORT).show()
                                }
                                SocialButton(icon = GlowIcons.TikTok, label = "TikTok") {
                                    Toast.makeText(context, "Mở TikTok: @beautyhouse_official", Toast.LENGTH_SHORT).show()
                                }
                                SocialButton(icon = GlowIcons.Zalo, label = "Zalo") {
                                    Toast.makeText(context, "Mở Zalo OA: Beauty House Care", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        // Category Pill with dropdown chevron
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFFFBF1EE))
                                .border(1.dp, PinkPanther, RoundedCornerShape(20.dp))
                                .clickable {
                                    Toast.makeText(context, "Chuyên khoa: Hair Salon, Spa & Móng Nghệ Thuật", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Scissors, tint = DustyMerlot, size = 13.dp)
                                Text(
                                    text = "Hair Salon & Spa VIP",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DustyMerlot
                                )
                                GlowIcon(icon = GlowIcons.ChevronDown, tint = DustyMerlot, size = 11.dp)
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF3ECE6), thickness = 0.8.dp)

                        // Info List: Address, Phone with copy, Working Hours
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Address
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.MapPin, tint = DustyMerlot, size = 16.dp)
                                Text(
                                    text = "123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh",
                                    fontSize = 12.5.sp,
                                    color = TextNoir,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Phone with Copy Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Phone, tint = DustyMerlot, size = 16.dp)
                                Text(
                                    text = "0901 234 567",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextNoir,
                                    modifier = Modifier.weight(1f)
                                )

                                // Copy Button with visual feedback
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF5EBE6))
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString("0901234567"))
                                            Toast.makeText(context, "Đã sao chép: 0901 234 567", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        GlowIcon(icon = GlowIcons.Copy, tint = DustyMerlot, size = 12.dp)
                                        Text(text = "Sao chép", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                    }
                                }
                            }

                            // Working Hours
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GlowIcon(icon = GlowIcons.Clock, tint = DustyMerlot, size = 16.dp)
                                Text(
                                    text = "08:00 - 21:00 (Thứ 2 - Chủ nhật)",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Đang mở cửa",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }

            // ───────────────── 2B. 4-TAB NAVIGATION BAR ─────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    val tabs = listOf("Dịch vụ", "Giới thiệu", "Hình ảnh", "Đánh giá")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) DustyMerlot else Color.Transparent)
                                    .clickable { selectedTab = index }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // ───────────────── 2C. TAB CONTENTS ─────────────────
            when (selectedTab) {
                // TAB 0: DỊCH VỤ (SERVICES)
                0 -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 16.dp)
                                Text(text = "Bảng Giá Dịch Vụ Beauty House", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                            }
                            Text(text = "${services.size} dịch vụ", fontSize = 11.5.sp, color = TextMuted)
                        }
                    }

                    items(services) { serv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectServiceForBooking(serv.name) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Service Category Icon Medallion
                                IconMedallion(
                                    icon = serv.icon,
                                    size = 46.dp,
                                    iconSize = 22.dp,
                                    tint = DustyMerlot
                                )

                                // Service Details
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = serv.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            color = TextNoir,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            GlowIcon(icon = GlowIcons.Clock, tint = TextMuted, size = 12.dp)
                                            Text(text = serv.duration, fontSize = 11.sp, color = TextMuted)
                                        }

                                        Text(text = "•", color = BorderSilk)

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PinkPanther.copy(alpha = 0.35f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = serv.tag,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DustyMerlot
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = serv.price,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DustyMerlot
                                    )
                                }

                                // Right Arrow / Book Button
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DustyMerlot),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GlowIcon(icon = GlowIcons.ChevronRight, tint = Color.White, size = 16.dp)
                                }
                            }
                        }
                    }
                }

                // TAB 1: GIỚI THIỆU (ABOUT)
                1 -> {
                    // Về Chúng Tôi Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 18.dp)
                                    Text(text = "Về chúng tôi", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                }
                                Text(
                                    text = "Beauty House là không gian làm đẹp và thư giãn cao cấp tọa lạc tại trung tâm Quận 1. Với hơn 8 năm đồng hành cùng nhan sắc Việt, chúng tôi mang đến những trải nghiệm chăm sóc tóc, móng và da hoàn hảo nhất.\n\nĐội ngũ kỹ thuật viên tay nghề cao cùng mỹ phẩm nhập khẩu chuẩn Châu Âu cam kết mang lại sự hài lòng và tự tin tuyệt đối cho quý khách.",
                                    fontSize = 12.5.sp,
                                    lineHeight = 19.sp,
                                    color = Color(0xFF4A343A)
                                )
                            }
                        }
                    }

                    // 4 Feature Badges (Grid 2x2)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FeatureBadgeCard(
                                    icon = GlowIcons.Shield,
                                    title = "Sản phẩm chính hãng",
                                    subtitle = "100% Kérastase & Olaplex",
                                    modifier = Modifier.weight(1f)
                                )
                                FeatureBadgeCard(
                                    icon = GlowIcons.Award,
                                    title = "Đội ngũ chuyên nghiệp",
                                    subtitle = "Master Stylist > 5 năm",
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FeatureBadgeCard(
                                    icon = GlowIcons.Lotus,
                                    title = "Không gian thư giãn",
                                    subtitle = "Phòng VIP & Trà thảo mộc",
                                    modifier = Modifier.weight(1f)
                                )
                                FeatureBadgeCard(
                                    icon = GlowIcons.Sparkle,
                                    title = "Tư vấn cá nhân hóa",
                                    subtitle = "Kiểu tóc hợp dáng mặt",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Menu Dịch Vụ Preview Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Menu dịch vụ nổi bật", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                    Text(
                                        text = "Xem tất cả >",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DustyMerlot,
                                        modifier = Modifier.clickable { selectedTab = 0 }
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    services.take(4).forEach { serv ->
                                        Box(
                                            modifier = Modifier
                                                .width(130.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(WarmWhite)
                                                .border(1.dp, BorderSilk, RoundedCornerShape(12.dp))
                                                .clickable { onSelectServiceForBooking(serv.name) }
                                                .padding(10.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconMedallion(icon = serv.icon, size = 34.dp, iconSize = 16.dp, tint = DustyMerlot)
                                                Text(text = serv.name, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextNoir, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                Text(text = serv.price, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = DustyMerlot)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Không Gian Tiệm Preview Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Không gian tiệm sang trọng", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                    Text(
                                        text = "Xem tất cả >",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DustyMerlot,
                                        modifier = Modifier.clickable { selectedTab = 2 }
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    listOf(
                                        "Khu đón khách VIP" to Color(0xFFFDE8E4),
                                        "Phòng gội dưỡng sinh" to Color(0xFFE8F5E9),
                                        "Khu tạo kiểu cao cấp" to Color(0xFFFBECE1),
                                        "Quầy trà thảo dược" to Color(0xFFFFF3E0)
                                    ).forEach { (spaceName, bgCol) ->
                                        Box(
                                            modifier = Modifier
                                                .width(135.dp)
                                                .height(90.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(bgCol)
                                                .border(1.dp, BorderSilk, RoundedCornerShape(12.dp))
                                                .clickable { selectedTab = 2 }
                                                .padding(10.dp),
                                            contentAlignment = Alignment.BottomStart
                                        ) {
                                            Text(
                                                text = spaceName,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DustyMerlot
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: HÌNH ẢNH (GALLERY)
                2 -> {
                    // Filter Chips Row
                    item {
                        val filters = listOf("Tất cả", "Cắt tóc", "Uốn tóc", "Nhuộm tóc", "Làm móng", "Không gian")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filters.forEach { filter ->
                                val isSelected = galleryFilter == filter
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) DustyMerlot else SurfaceLight)
                                        .border(1.dp, if (isSelected) DustyMerlot else BorderSilk, RoundedCornerShape(20.dp))
                                        .clickable { galleryFilter = filter }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextNoir
                                    )
                                }
                            }
                        }
                    }

                    // 2-Column Photo Albums Grid
                    val filteredAlbums = if (galleryFilter == "Tất cả") albums else albums.filter { it.category == galleryFilter }
                    val chunkedAlbums = filteredAlbums.chunked(2)

                    items(chunkedAlbums) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { album ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            Toast.makeText(context, "Mở album: ${album.title} (${album.photoCount})", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column {
                                        // Visual Showcase Thumbnail Box
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(115.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(album.primaryColor, album.secondaryColor)
                                                    )
                                                )
                                                .padding(8.dp)
                                        ) {
                                            // Top photo count tag
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black.copy(alpha = 0.45f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(text = album.photoCount, fontSize = 9.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }

                                            // Ảnh bìa album thực tế
                                            val albumImgRes = when {
                                                album.title.contains("Nail", true) -> com.example.app_lam_dep.R.drawable.srv_nail_art
                                                album.title.contains("Cắt", true) -> com.example.app_lam_dep.R.drawable.srv_hair_cut
                                                album.title.contains("Uốn", true) || album.title.contains("Nhuộm", true) || album.title.contains("Keratin", true) || album.title.contains("Balayage", true) -> com.example.app_lam_dep.R.drawable.srv_hair_wave
                                                album.title.contains("Gội", true) || album.title.contains("Massage", true) -> com.example.app_lam_dep.R.drawable.srv_spa_massage
                                                album.title.contains("Không Gian", true) || album.title.contains("Salon", true) -> com.example.app_lam_dep.R.drawable.salon_luxury_interior
                                                else -> com.example.app_lam_dep.R.drawable.srv_nail_gel
                                            }
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = albumImgRes),
                                                contentDescription = album.title,
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        // Title & Category
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = album.title,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextNoir,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(text = album.category, fontSize = 10.sp, color = TextMuted)
                                        }
                                    }
                                }
                            }
                            // Empty spacer if odd number
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // TAB 3: ĐÁNH GIÁ (REVIEWS)
                3 -> {
                    // Overall Score Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Big Score Block
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(text = "4.8", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = DustyMerlot)
                                        StarRating(rating = 5, size = 15.dp)
                                        Text(text = "320 đánh giá", fontSize = 11.sp, color = TextMuted)
                                    }

                                    // Star Breakdown Progress Bars
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        RatingBarRow(stars = "5★", progress = 0.88f, count = "280")
                                        RatingBarRow(stars = "4★", progress = 0.10f, count = "32")
                                        RatingBarRow(stars = "3★", progress = 0.02f, count = "6")
                                        RatingBarRow(stars = "2★", progress = 0.00f, count = "1")
                                        RatingBarRow(stars = "1★", progress = 0.00f, count = "1")
                                    }
                                }
                            }
                        }
                    }

                    // Review Filter Pills
                    item {
                        val reviewFilters = listOf("Tất cả (320)", "5 sao (280)", "4 sao (32)", "Có hình ảnh (145)")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            reviewFilters.forEach { f ->
                                val isSelected = reviewFilter == f
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) DustyMerlot else SurfaceLight)
                                        .border(1.dp, if (isSelected) DustyMerlot else BorderSilk, RoundedCornerShape(20.dp))
                                        .clickable { reviewFilter = f }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = f,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextNoir
                                    )
                                }
                            }
                        }
                    }

                    // Customer Reviews List
                    items(reviews) { r ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Author Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Author Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(r.avatarColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = r.author.take(1),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = r.author, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            StarRating(rating = r.rating, size = 11.dp)
                                            Text(text = "• ${r.timeAgo}", fontSize = 10.5.sp, color = TextMuted)
                                        }
                                    }

                                    // Service badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PinkPanther.copy(alpha = 0.35f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = r.serviceUsed, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DustyMerlot)
                                    }
                                }

                                // Comment Content
                                Text(
                                    text = r.comment,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Color(0xFF3C262C)
                                )

                                // Attached Result Photos Preview Row
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    repeat(r.photosCount) { idx ->
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFFBECE1))
                                                .border(0.8.dp, BorderSilk, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    Toast.makeText(context, "Xem ảnh kết quả của ${r.author}", Toast.LENGTH_SHORT).show()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GlowIcon(icon = GlowIcons.Image, tint = DustyMerlot.copy(alpha = 0.7f), size = 18.dp)
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
}

// ────────────────────────── SUB-COMPONENTS ──────────────────────────

@Composable
private fun SocialButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFFFBF1EE))
            .border(0.8.dp, PinkPanther, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        GlowIcon(icon = icon, tint = DustyMerlot, size = 14.dp, contentDescription = label)
    }
}

@Composable
private fun FeatureBadgeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconMedallion(icon = icon, size = 34.dp, iconSize = 16.dp, tint = DustyMerlot)
            Column {
                Text(text = title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                Text(text = subtitle, fontSize = 9.5.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun RatingBarRow(
    stars: String,
    progress: Float,
    count: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(text = stars, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted, modifier = Modifier.width(18.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = ChampagneGold,
            trackColor = Color(0xFFF0E5DF)
        )
        Text(text = count, fontSize = 9.5.sp, color = TextMuted, modifier = Modifier.width(22.dp), textAlign = TextAlign.End)
    }
}
