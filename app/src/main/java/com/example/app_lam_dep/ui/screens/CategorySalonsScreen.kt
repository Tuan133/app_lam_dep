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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowBackButton
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.icons.IconMedallion
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.GoldSoft
import com.example.app_lam_dep.ui.theme.IconBlush
import com.example.app_lam_dep.ui.theme.IconCream
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted

// ─────────────────────────── Data Models (Chuẩn Layout Grab) ───────────────────────────

data class GrabFeaturedService(
    val id: String,
    val name: String,
    val price: String,
    val bgGradient: List<Color>
)

data class GrabSalon(
    val id: String,
    val name: String,
    val rating: Double,
    val reviewCount: String,
    val priceRange: String,
    val categoryTags: String,
    val distance: String,
    val eta: String,
    val promoVoucher: String,
    val isSignatureBadge: Boolean,
    val signatureBadgeText: String,
    val services: List<GrabFeaturedService>,
    val requiresDeposit: Boolean = true,
    val depositLabel: String = "Cọc 50.000đ giữ chỗ"
)

@Composable
fun CategorySalonsScreen(
    categoryName: String,
    onBack: () -> Unit,
    onNavigateToSalon: (String) -> Unit,
    onSelectServiceForBooking: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("Tất cả") }
    val displayCategoryTitle = if (categoryName.contains("Nail", true)) "Làm Nail" else categoryName

    // 11 Cửa hàng thực tế từ bảng tính Excel
    val allExcelSalons = remember {
        listOf(
            GrabSalon(
                id = "salon_01",
                name = "1994 Nailsroom",
                rating = 4.9,
                reviewCount = "142+",
                priceRange = "$$$",
                categoryTags = "172/5 Trần Phú, Hải Châu • Sơn Gel Hàn Quốc • Nối Mi Lụa",
                distance = "0.8 km",
                eta = "15 phút",
                promoVoucher = "Giảm 30.000đ khi đặt lịch qua app",
                isSignatureBadge = true,
                signatureBadgeText = "Salon VIP Glow",
                services = listOf(
                    GrabFeaturedService("srv_01", "Sơn Gel Thạch Hàn Quốc", "160.000đ", listOf(Color(0xFF5A1E2D), Color(0xFF8A2E44))),
                    GrabFeaturedService("srv_02", "Nail Mắt Mèo Cực Quang VIP", "250.000đ", listOf(Color(0xFF2E1A47), Color(0xFF6A3B82))),
                    GrabFeaturedService("srv_03", "Nối Mi Lụa Tự Nhiên", "220.000đ", listOf(Color(0xFF4A3B18), Color(0xFF9E7B34))),
                    GrabFeaturedService("srv_04", "Dưỡng Móng Organic", "120.000đ", listOf(Color(0xFF28483B), Color(0xFF4C7D69)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 50.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_02",
                name = "BONNIE NAIL",
                rating = 4.8,
                reviewCount = "98+",
                priceRange = "$$",
                categoryTags = "260/18 Dương Đình Nghệ, Sơn Trà • Ombre Nghệ Thuật • Móng Gel",
                distance = "1.5 km",
                eta = "18 phút",
                promoVoucher = "Giảm 10% đơn đầu tiên",
                isSignatureBadge = true,
                signatureBadgeText = "Gần Bãi Tắm Mỹ Khê",
                services = listOf(
                    GrabFeaturedService("srv_05", "Vẽ Móng Nghệ Thuật Ombre", "180.000đ", listOf(Color(0xFF6B2636), Color(0xFF964354))),
                    GrabFeaturedService("srv_06", "Đắp Móng Gel Form Hạnh Nhân", "280.000đ", listOf(Color(0xFF3C3C4D), Color(0xFF686882))),
                    GrabFeaturedService("srv_07", "Tháo Móng & Dưỡng Keratin", "100.000đ", listOf(Color(0xFF4D3844), Color(0xFF7A596C)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 50.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_03",
                name = "GÀ SPA",
                rating = 4.8,
                reviewCount = "215+",
                priceRange = "$$$",
                categoryTags = "Hải Châu, Đà Nẵng • Trị Mụn Chuẩn Y Khoa • Thải Độc Oxy",
                distance = "1.2 km",
                eta = "15 phút",
                promoVoucher = "Tặng buổi soi da 3D chuẩn Y khoa",
                isSignatureBadge = true,
                signatureBadgeText = "Thương Hiệu Toàn Quốc",
                services = listOf(
                    GrabFeaturedService("srv_08", "Lấy Nhân Mụn Y Khoa", "250.000đ", listOf(Color(0xFF1E3A2F), Color(0xFF386653))),
                    GrabFeaturedService("srv_09", "Điện Di Vitamin C Sáng Da", "300.000đ", listOf(Color(0xFF4D2B1C), Color(0xFF7D4A32))),
                    GrabFeaturedService("srv_10", "Thải Độc Da CO2 Thải Chì", "350.000đ", listOf(Color(0xFF303C24), Color(0xFF53663E)))
                ),
                requiresDeposit = false,
                depositLabel = "Không cần cọc • Giữ chỗ 0đ"
            ),
            GrabSalon(
                id = "salon_04",
                name = "HIEN HAIR STUDIO",
                rating = 4.9,
                reviewCount = "180+",
                priceRange = "$$$",
                categoryTags = "183 Tố Hữu, Cẩm Lệ • Uốn Sóng Lơi • Nhuộm Balayage",
                distance = "2.8 km",
                eta = "25 phút",
                promoVoucher = "Tặng gói ủ dưỡng phục hồi Keratin",
                isSignatureBadge = true,
                signatureBadgeText = "Master Stylist",
                services = listOf(
                    GrabFeaturedService("srv_11", "Cắt Tạo Kiểu Layer / Wolf Cut", "150.000đ", listOf(Color(0xFF2A423B), Color(0xFF446B5F))),
                    GrabFeaturedService("srv_12", "Uốn Sóng Lơi Hàn Quốc", "450.000đ", listOf(Color(0xFF4B3224), Color(0xFF754F3A))),
                    GrabFeaturedService("srv_13", "Nhuộm Balayage Tôn Da", "550.000đ", listOf(Color(0xFF541C2E), Color(0xFF85304B))),
                    GrabFeaturedService("srv_14", "Phục Hồi Tóc Keratin", "350.000đ", listOf(Color(0xFF4A3B18), Color(0xFF9E7B34)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 100.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_05",
                name = "KỲ DUYÊN TRỊ MỤN",
                rating = 4.7,
                reviewCount = "112+",
                priceRange = "$$",
                categoryTags = "282 Nguyễn Phước Nguyên • Đông Y • Trị Mụn Tận Gốc",
                distance = "3.2 km",
                eta = "28 phút",
                promoVoucher = "Tặng serum thảo dược trị mụn",
                isSignatureBadge = false,
                signatureBadgeText = "Đông Y Gia Truyền",
                services = listOf(
                    GrabFeaturedService("srv_15", "Trị Mụn Thảo Dược Đông Y", "200.000đ", listOf(Color(0xFF1E3A2F), Color(0xFF386653))),
                    GrabFeaturedService("srv_16", "Trị Thâm Sẹo Tế Bào Gốc", "400.000đ", listOf(Color(0xFF4D2B1C), Color(0xFF7D4A32)))
                ),
                requiresDeposit = false,
                depositLabel = "Không cần cọc • Giữ chỗ 0đ"
            ),
            GrabSalon(
                id = "salon_06",
                name = "MNH THUY MAKEUP",
                rating = 4.9,
                reviewCount = "156+",
                priceRange = "$$$$",
                categoryTags = "01 Đinh Văn Chấp, Cẩm Lệ • Makeup Cô Dâu • Dạ Tiệc Douyin",
                distance = "3.5 km",
                eta = "30 phút",
                promoVoucher = "Tặng hoa cài tóc và lens mắt",
                isSignatureBadge = true,
                signatureBadgeText = "Top Makeup Artist",
                services = listOf(
                    GrabFeaturedService("srv_17", "Makeup Cô Dâu & Làm Tóc", "600.000đ", listOf(Color(0xFF5A1E2D), Color(0xFF8A2E44))),
                    GrabFeaturedService("srv_18", "Makeup Dạ Hội Cao Cấp", "300.000đ", listOf(Color(0xFF2E1A47), Color(0xFF6A3B82))),
                    GrabFeaturedService("srv_19", "Makeup Chụp Ảnh Kỷ Yếu", "250.000đ", listOf(Color(0xFF4A3B18), Color(0xFF9E7B34)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 100.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_07",
                name = "Nails 1996",
                rating = 4.8,
                reviewCount = "89+",
                priceRange = "$$$",
                categoryTags = "3 Dương Tụ Quán, Ngũ Hành Sơn • Sơn Châu Âu • Đắp Bột Đá",
                distance = "2.6 km",
                eta = "22 phút",
                promoVoucher = "Bảo hành móng 7 ngày miễn phí",
                isSignatureBadge = true,
                signatureBadgeText = "Phong Cách Châu Âu",
                services = listOf(
                    GrabFeaturedService("srv_20", "Sơn Gel Thiết Kế Châu Âu", "220.000đ", listOf(Color(0xFF6B2636), Color(0xFF964354))),
                    GrabFeaturedService("srv_21", "Đắp Bột Móng Nghệ Thuật Đá", "350.000đ", listOf(Color(0xFF3C3C4D), Color(0xFF686882)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 50.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_08",
                name = "SI MÊ BEAUTY",
                rating = 4.8,
                reviewCount = "94+",
                priceRange = "$$",
                categoryTags = "58 Nguyễn Đôn Tiết, Hải Châu • Triệt Lông Diode • Cấy Collagen",
                distance = "1.8 km",
                eta = "18 phút",
                promoVoucher = "Giảm 20% khi mua gói liệu trình",
                isSignatureBadge = false,
                signatureBadgeText = "Công Nghệ Laser Mới",
                services = listOf(
                    GrabFeaturedService("srv_22", "Chăm Sóc Da Căng Bóng", "320.000đ", listOf(Color(0xFF1E3A2F), Color(0xFF386653))),
                    GrabFeaturedService("srv_23", "Cấy Collagen Tươi", "450.000đ", listOf(Color(0xFF4D2B1C), Color(0xFF7D4A32))),
                    GrabFeaturedService("srv_24", "Triệt Lông Diode Laser", "180.000đ", listOf(Color(0xFF303C24), Color(0xFF53663E)))
                ),
                requiresDeposit = false,
                depositLabel = "Không cần cọc • Giữ chỗ 0đ"
            ),
            GrabSalon(
                id = "salon_09",
                name = "Thì Là SPA - Gội Đầu Dưỡng Sinh, Massage Thư Giãn",
                rating = 5.0,
                reviewCount = "260+",
                priceRange = "$$$",
                categoryTags = "57 Nguyễn Thị Minh Khai, Hải Châu • Bồ Kết Nấu Tươi • Đá Nóng",
                distance = "0.9 km",
                eta = "12 phút",
                promoVoucher = "Tặng ngâm chân thảo mộc đá nóng",
                isSignatureBadge = true,
                signatureBadgeText = "5.0 Tuyệt Đối",
                services = listOf(
                    GrabFeaturedService("srv_25", "Gội Dưỡng Sinh Thảo Dược 7 Bước", "150.000đ", listOf(Color(0xFF1E3A2F), Color(0xFF386653))),
                    GrabFeaturedService("srv_26", "Massage Cổ Vai Gáy Chuyên Sâu", "250.000đ", listOf(Color(0xFF4D2B1C), Color(0xFF7D4A32))),
                    GrabFeaturedService("srv_27", "Massage Body Đá Nóng 75p", "380.000đ", listOf(Color(0xFF303C24), Color(0xFF53663E)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 50.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_10",
                name = "Tun Beauty Salon & Spa",
                rating = 4.8,
                reviewCount = "130+",
                priceRange = "$$$",
                categoryTags = "238 Trần Hưng Đạo, Sơn Trà • View Cầu Rồng • Cắt Tóc & Spa",
                distance = "1.4 km",
                eta = "15 phút",
                promoVoucher = "Giảm 15% combo uốn + nhuộm",
                isSignatureBadge = true,
                signatureBadgeText = "View Cầu Rồng Đẹp",
                services = listOf(
                    GrabFeaturedService("srv_28", "Cắt Tóc & Gội Massage", "120.000đ", listOf(Color(0xFF2A423B), Color(0xFF446B5F))),
                    GrabFeaturedService("srv_29", "Uốn Nhuộm Combo Cao Cấp", "480.000đ", listOf(Color(0xFF4B3224), Color(0xFF754F3A)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 50.000đ giữ chỗ"
            ),
            GrabSalon(
                id = "salon_11",
                name = "1m65.nail",
                rating = 4.9,
                reviewCount = "175+",
                priceRange = "$$",
                categoryTags = "48 Lê Đình Dương, Hải Châu • Nail Anime • Charm Xinh Độc Quyền",
                distance = "1.1 km",
                eta = "14 phút",
                promoVoucher = "Tặng charm đính móng tự chọn",
                isSignatureBadge = true,
                signatureBadgeText = "Hot Trend Giới Trẻ",
                services = listOf(
                    GrabFeaturedService("srv_30", "Vẽ Hoạt Hình Anime VIP", "220.000đ", listOf(Color(0xFF5A1E2D), Color(0xFF8A2E44))),
                    GrabFeaturedService("srv_31", "Sơn Gel Thạch Trong Trẻo", "180.000đ", listOf(Color(0xFF2E1A47), Color(0xFF6A3B82))),
                    GrabFeaturedService("srv_32", "Nail Box Thiết Kế Theo Size", "150.000đ", listOf(Color(0xFF4A3B18), Color(0xFF9E7B34)))
                ),
                requiresDeposit = true,
                depositLabel = "Cọc 50.000đ giữ chỗ"
            )
        )
    }

    val salons = remember(categoryName, allExcelSalons) {
        when {
            categoryName.contains("Nail", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_01", "salon_02", "salon_07", "salon_11") }
            categoryName.contains("Spa", ignoreCase = true) || categoryName.contains("Trị Liệu", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_03", "salon_05", "salon_08", "salon_09") }
            categoryName.contains("da", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_03", "salon_05", "salon_08") }
            categoryName.contains("Hair", ignoreCase = true) || categoryName.contains("Tóc", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_04", "salon_10") }
            categoryName.contains("Makeup", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_06", "salon_04", "salon_10") }
            categoryName.contains("Dưỡng sinh", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_09", "salon_10") }
            categoryName.contains("Mi", ignoreCase = true) || categoryName.contains("Mày", ignoreCase = true) ->
                allExcelSalons.filter { it.id in listOf("salon_01", "salon_02", "salon_11") }
            else -> allExcelSalons
        }
    }

    val filteredSalons = remember(salons, searchQuery, selectedFilterChip) {
        var result = salons
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            result = result.filter { salon ->
                salon.name.lowercase().contains(q) ||
                salon.categoryTags.lowercase().contains(q) ||
                salon.services.any { it.name.lowercase().contains(q) }
            }
        }
        when (selectedFilterChip) {
            "Dưới 200.000đ" -> result = result.filter { s ->
                s.services.any {
                    val digits = it.price.filter { c -> c.isDigit() }
                    val p = digits.toLongOrNull() ?: 0L
                    p in 1..200000
                }
            }
            "⚡ Đặt hẹn ngay" -> result = result.filter { !it.requiresDeposit || it.isSignatureBadge }
            "👑 Salon Trứ Danh" -> result = result.filter { it.isSignatureBadge }
            "★ 4.8+" -> result = result.filter { it.rating >= 4.8 }
            else -> Unit
        }
        result
    }

    val topHorizontalHighlights = remember(filteredSalons) { filteredSalons.take(3) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8))
    ) {
        // ─────────────────────────── 1. Header phong cách Grab ───────────────────────────
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Hàng 1: Nút Back + Vị trí / Địa chỉ phục vụ + Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlowBackButton(
                        onClick = onBack,
                        tint = DustyMerlot,
                        background = Color(0xFFFBF7E8)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { /* Mở chọn địa chỉ */ }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Salon gần bạn",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Medium
                            )
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SuccessGreen)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Đồng Khởi, Phường Bến Nghé, Quận 1",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF24090F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            GlowIcon(icon = GlowIcons.ChevronDown, tint = DustyMerlot, size = 14.dp)
                        }
                    }
                }

                // Hàng 2: Thanh tìm kiếm "Bạn đang cần làm đẹp gì?" (giống Grab)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF5EFEB))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowIcon(icon = GlowIcons.Search, tint = DustyMerlot.copy(alpha = 0.7f), size = 18.dp)
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Tìm kiếm tiệm $displayCategoryTitle, mẫu móng, dịch vụ...",
                                fontSize = 12.5.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF24090F),
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Hàng 3: Các nút Filter chips ngang (Lọc theo, Dưới 200k, Đặt hẹn ngay...)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        // Nút Icon Filter (như Grab)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .clickable { },
                            contentAlignment = Alignment.Center
                        ) {
                            GlowIcon(icon = GlowIcons.Filter, tint = DustyMerlot, size = 16.dp)
                        }
                    }

                    val filterChips = listOf("Lọc theo", "Dưới 200.000đ", "⚡ Đặt hẹn ngay", "👑 Salon Trứ Danh", "★ 4.8+")
                    items(filterChips) { chip ->
                        val isSelected = selectedFilterChip == chip
                        Row(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) DustyMerlot else Color(0xFFEBDCD3),
                                    RoundedCornerShape(18.dp)
                                )
                                .background(if (isSelected) PinkPanther.copy(alpha = 0.35f) else Color.White)
                                .clickable { selectedFilterChip = if (isSelected) "Tất cả" else chip }
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = chip,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DustyMerlot else Color(0xFF24090F)
                            )
                            if (chip == "Lọc theo") {
                                GlowIcon(icon = GlowIcons.ChevronDown, tint = DustyMerlot, size = 12.dp)
                            }
                        }
                    }
                }
            }
        }

        // ─────────────────────────── 2. Danh sách Salon (Feed chuẩn Grab) ───────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Phần 2A: Row ngang các Salon hàng đầu (như Cơm Thố Thiên Phúc, Jollibee, KFC trên Grab)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Tiệm $displayCategoryTitle Nổi Bật Được Yêu Thích",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A101A),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(topHorizontalHighlights) { salon ->
                            Card(
                                modifier = Modifier
                                    .width(170.dp)
                                    .clickable { onNavigateToSalon(salon.id) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column {
                                    // Thumbnail trên (Ảnh đại diện salon)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                    ) {
                                        val imgRes = com.applamdep.customer.core.util.ShopImageAssets.getShopDrawableRes(salon.id)
                                        androidx.compose.foundation.Image(
                                            painter = androidx.compose.ui.res.painterResource(id = imgRes),
                                            contentDescription = salon.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                                                    )
                                                )
                                        )

                                        // Badge góc trên bên trái (TRỨ DANH)
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(ChampagneGold)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                GlowIcon(icon = GlowIcons.Crown, tint = Color.White, size = 9.dp)
                                                Text(
                                                    text = "TRỨ DANH",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        // Badge cọc dưới cùng ảnh
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .padding(8.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DustyMerlot.copy(alpha = 0.9f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (salon.requiresDeposit) "ƯU ĐÃI CỌC 30K" else "MIỄN CỌC 0Đ",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PinkPanther
                                            )
                                        }
                                    }

                                    // Thông tin dưới
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = salon.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF24090F),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            GlowIcon(icon = GlowIcons.Bolt, tint = SuccessGreen, size = 11.dp)
                                            Text(
                                                text = "${salon.eta} • ${salon.distance}",
                                                fontSize = 10.5.sp,
                                                color = TextMuted
                                            )
                                        }

                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(PinkPanther.copy(alpha = 0.4f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            GlowIcon(icon = GlowIcons.Tag, tint = DustyMerlot, size = 9.dp)
                                            Text(
                                                text = "Giảm 30K cọc",
                                                fontSize = 9.sp,
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
            }

            // Phần 2B: "Chỉ có trên Glow Prestige" (Container cam nhạt/hồng nude như trên Grab)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBECE6)),
                    border = BorderStroke(1.dp, PinkPanther)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title header container
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GlowIcon(icon = GlowIcons.Sparkle, tint = DustyMerlot, size = 15.dp)
                            Text(
                                text = "Chỉ có trên Glow Prestige",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = DustyMerlot
                            )
                        }

                        // Danh sách từng Salon lớn có dịch vụ đính kèm bên dưới (ĐÚNG NHƯ ẢNH ÔNG NHỊ TRÊN GRAB)
                        filteredSalons.forEach { salon ->
                            GrabStyleSalonDetailCard(
                                salon = salon,
                                onSalonClick = { onNavigateToSalon(salon.id) },
                                onServiceClick = { sName -> onSelectServiceForBooking(sName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────── Card Salon chuẩn phong cách Grab (Kèm 3 dịch vụ) ───────────────────────────

@Composable
private fun GrabStyleSalonDetailCard(
    salon: GrabSalon,
    onSalonClick: () -> Unit,
    onServiceClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onSalonClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Phần Header Salon: Thumbnail vuông bên trái + Chi tiết bên phải
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Thumbnail vuông bo góc hiển thị ảnh salon thực tế
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3ECE6))
                        .border(1.dp, GoldSoft.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val imgRes = com.applamdep.customer.core.util.ShopImageAssets.getShopDrawableRes(salon.id)
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = imgRes),
                        contentDescription = salon.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Cột thông tin chi tiết
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = salon.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF24090F),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Đánh giá ★ 4.7 (1K+) • $$$$ • Danh mục
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 12.dp)
                        Text(
                            text = "${salon.rating} (${salon.reviewCount})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF24090F)
                        )
                        Text(text = "•", fontSize = 11.sp, color = TextMuted)
                        Text(text = salon.priceRange, fontSize = 11.sp, color = TextMuted)
                        Text(text = "•", fontSize = 11.sp, color = TextMuted)
                        Text(
                            text = salon.categoryTags,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Giá / Khuyến mãi & Khoảng cách (như Grab)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = salon.depositLabel,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (salon.requiresDeposit) DustyMerlot else SuccessGreen
                        )
                        Text(text = "•", fontSize = 11.sp, color = TextMuted)
                        Text(text = "${salon.distance} • ${salon.eta}", fontSize = 11.sp, color = TextMuted)
                    }

                    // Huy hiệu "Quán trứ danh Grab" -> "★ Salon Trứ Danh Glow >"
                    if (salon.isSignatureBadge) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFF7DF))
                                .border(0.75.dp, ChampagneGold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 10.dp)
                            Text(
                                text = salon.signatureBadgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B4800)
                            )
                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF6B4800), size = 10.dp)
                        }
                    }

                    // Voucher chip (Giảm 18.000đ đơn từ 80k...)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF7F2EE))
                            .border(0.75.dp, Color(0xFFEBDCD3), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconMedallion(
                            icon = GlowIcons.Tag,
                            size = 18.dp,
                            iconSize = 10.dp,
                            tint = DustyMerlot
                        )
                        Text(
                            text = salon.promoVoucher,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DustyMerlot,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0E5DE))

            // Phần Danh Sách Món / Dịch vụ tiêu biểu (3 hình ảnh ngang như món ăn Grab)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Dịch vụ được đặt nhiều nhất:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(salon.services) { s ->
                        Column(
                            modifier = Modifier
                                .width(108.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onServiceClick(s.name) },
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Ảnh dịch vụ thực tế
                            Box(
                                modifier = Modifier
                                    .size(108.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                val srvImg = com.applamdep.customer.core.util.ShopImageAssets.getServiceDrawableRes(s.id)
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = srvImg),
                                    contentDescription = s.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.9f))
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "+ Đặt cọc",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DustyMerlot
                                    )
                                }
                            }

                            // Giá tiền in đậm (như 145.000, 69.000 trên Grab)
                            Text(
                                text = s.price,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF24090F)
                            )

                            // Tên dịch vụ
                            Text(
                                text = s.name,
                                fontSize = 10.5.sp,
                                color = Color(0xFF3A101A),
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
