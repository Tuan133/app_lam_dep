package com.example.app_lam_dep.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import com.applamdep.customer.data.repository.FakeShopRepository
import com.applamdep.customer.data.repository.FakeWalletRepository
import androidx.compose.ui.text.style.TextOverflow
import com.applamdep.customer.core.util.ShopImageAssets
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.data.local.ShopLocalStorage
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.R
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.GoldSoft
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.TextMuted

data class BeautyCategoryItem(val name: String, val icon: ImageVector)
data class FavoriteServiceItem(val name: String, val price: String, val imageRes: Int, val salon: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHomeScreen(
    onNavigateToSalon: (String) -> Unit,
    onSelectServiceForBooking: (String) -> Unit,
    onSelectFavoriteService: (FavoriteServiceItem) -> Unit = {},
    onOpenWallet: () -> Unit = {},
    onOpenBookingSchedule: () -> Unit = {},
    onSelectCategory: (String) -> Unit = {},
    onOpenShopPlans: () -> Unit = {},
    onViewAllSalons: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val allShops = remember { ShopLocalStorage.getExcelSalons() }
    val allServices = remember { FakeShopRepository.sampleServices }
    val walletState by FakeWalletRepository.walletFlow.collectAsState()

    fun removeAccents(src: String): String {
        return java.text.Normalizer.normalize(src, java.text.Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace("đ", "d")
            .replace("Đ", "D")
    }

    val filteredShops = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            val qNoAccent = removeAccents(q)
            allShops.filter { shop ->
                val shopServices = allServices.filter { it.shopId == shop.id || (shop.id == "salon_01" && it.shopId == "shop_lamour") }
                val hasMatchingService = shopServices.any { srv ->
                    val sName = srv.name.lowercase()
                    val sCat = srv.category.lowercase()
                    val sDesc = srv.description.lowercase()
                    sName.contains(q) || removeAccents(sName).contains(qNoAccent) ||
                    sCat.contains(q) || removeAccents(sCat).contains(qNoAccent) ||
                    sDesc.contains(q) || removeAccents(sDesc).contains(qNoAccent)
                }
                hasMatchingService ||
                shop.name.lowercase().contains(q) ||
                removeAccents(shop.name.lowercase()).contains(qNoAccent) ||
                shop.address.lowercase().contains(q) ||
                removeAccents(shop.address.lowercase()).contains(qNoAccent) ||
                shop.description.lowercase().contains(q) ||
                removeAccents(shop.description.lowercase()).contains(qNoAccent) ||
                shop.amenities.any { 
                    it.lowercase().contains(q) || removeAccents(it.lowercase()).contains(qNoAccent) 
                } ||
                shop.phone.contains(q)
            }
        }
    }
    var activeCategoryModal by remember { mutableStateOf<String?>(null) }
    var isFavorite by remember { mutableStateOf(false) }

    // 8 categories matching client feedback
    val categories = listOf(
        BeautyCategoryItem("Làm Nail", GlowIcons.NailPolish),
        BeautyCategoryItem("Spa Trị Liệu", GlowIcons.Lotus),
        BeautyCategoryItem("Chăm sóc da", GlowIcons.Serum),
        BeautyCategoryItem("Nối Mi & Mày", GlowIcons.Sparkle),
        BeautyCategoryItem("Hair Salon", GlowIcons.Scissors),
        BeautyCategoryItem("Makeup Tiệc", GlowIcons.Lipstick),
        BeautyCategoryItem("Dưỡng sinh", GlowIcons.Leaf),
        BeautyCategoryItem("Khác", GlowIcons.MoreHorizontal)
    )

    val favoriteServices = listOf(
        FavoriteServiceItem("Sơn Gel Thạch Hàn Quốc", "160.000 đ", R.drawable.nail_french_gel, "1994 Nailsroom"),
        FavoriteServiceItem("Vẽ Móng Ombre Pastel", "180.000 đ", R.drawable.nail_cateye_aurora, "BONNIE NAIL"),
        FavoriteServiceItem("Gội Dưỡng Sinh Bồ Kết Tươi", "150.000 đ", R.drawable.shop_hero_spa, "Thì Là SPA"),
        FavoriteServiceItem("Uốn Sóng Lơi Chuẩn Hàn", "450.000 đ", R.drawable.salon_01_1994_nailsroom, "HIEN HAIR STUDIO")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8)),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Search Bar Top Row (Image 1 Header integration)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF360814), Color(0xFF2B0710))
                        )
                    )
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(24.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlowIcon(
                            icon = GlowIcons.Search,
                            tint = Color(0xFF7A686D),
                            size = 20.dp,
                            contentDescription = "Search"
                        )
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 13.5.sp,
                                color = Color(0xFF24090F),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Tìm salon, dịch vụ, quận...",
                                            color = Color(0xFF8E7A80),
                                            fontSize = 13.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Xóa tìm kiếm",
                                    tint = Color(0xFF7A686D),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable { onViewAllSalons() },
                            contentAlignment = Alignment.Center
                        ) {
                            GlowIcon(
                                icon = GlowIcons.Filter,
                                tint = Color(0xFF360814),
                                size = 18.dp,
                                contentDescription = "Bộ lọc"
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        val quickKeywords = listOf("1994 Nailsroom", "Sơn Gel Thạch", "Gội đầu dưỡng sinh", "Hải Châu", "Sơn Trà", "Nối Mi", "Spa Trị Liệu")
                        items(quickKeywords) { kw ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .clickable { searchQuery = kw }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = kw,
                                    color = Color(0xFFFBF7E8),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kết quả tìm kiếm cho \"$searchQuery\"",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3A101A)
                        )
                        Text(
                            text = "Tìm thấy ${filteredShops.size} salon phù hợp",
                            fontSize = 12.sp,
                            color = Color(0xFF7A686D)
                        )
                    }
                    TextButton(onClick = { searchQuery = "" }) {
                        Text("Xóa lọc", color = Color(0xFF6B1228), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (filteredShops.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBF7E8)),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowIcon(icon = GlowIcons.Search, tint = Color(0xFF7A686D), size = 26.dp)
                            }
                            Text(
                                text = "Không tìm thấy salon nào phù hợp",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF3A101A)
                            )
                            Text(
                                text = "Gợi ý: Bạn có thể tìm theo tên salon (1994, BONNIE, Thì Là, Gà Spa, Nails 1996, Tun...) hoặc theo quận (Hải Châu, Sơn Trà, Cẩm Lệ...)",
                                fontSize = 12.sp,
                                color = Color(0xFF7A686D),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(
                                onClick = { onViewAllSalons() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Xem toàn bộ 11 salon", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(
                    items = filteredShops,
                    key = { it.id }
                ) { shop ->
                    SearchShopResultCard(
                        shop = shop,
                        onClick = { onNavigateToSalon(shop.id) }
                    )
                }
            }
        } else {
        // 2. Section: Khám phá dịch vụ (Image 1 4x2 grid with soft rose medallions)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Khám phá dịch vụ",
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A101A)
                    )
                    Row(
                        modifier = Modifier.clickable { activeCategoryModal = "Tất cả danh mục dịch vụ làm đẹp" },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Tất cả",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B1228)
                        )
                        GlowIcon(
                            icon = GlowIcons.ChevronRight,
                            tint = Color(0xFF6B1228),
                            size = 12.dp
                        )
                    }
                }

                // 4x2 Grid of services
                val rows = categories.chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowItems.forEach { cat ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .width(76.dp)
                                        .clickable {
                                            if (cat.name == "Khác") {
                                                activeCategoryModal = "Tất cả danh mục dịch vụ làm đẹp"
                                            } else {
                                                onSelectCategory(cat.name)
                                            }
                                        }
                                ) {
                                    // Soft blush circular medallion matching Image 1
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0xFFFFF9F5),
                                                        Color(0xFFF9E7E2)
                                                    )
                                                )
                                            )
                                            .border(1.dp, Color(0xFFF3CEC5), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        GlowIcon(
                                            icon = cat.icon,
                                            tint = Color(0xFF6B1228),
                                            size = 24.dp,
                                            contentDescription = cat.name
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = cat.name,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF24090F),
                                        maxLines = 1
                                    )
                                }
                            }
                            repeat(4 - rowItems.size) {
                                Spacer(modifier = Modifier.width(76.dp))
                            }
                        }
                    }
                }
            }
        }

        // 3. Dual Quick Action Cards (Image 1: Ví GlowPrivé & Hôm nay 15:30)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Left Card: Ví GlowPrivé (Dark wine card)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp)
                        .clickable { onOpenWallet() },
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF3D0C18),
                                        Color(0xFF290610),
                                        Color(0xFF1F030B)
                                    )
                                )
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.12f))
                                    .border(1.dp, ChampagneGold.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowIcon(
                                    icon = GlowIcons.Crown,
                                    tint = ChampagneGold,
                                    size = 20.dp,
                                    contentDescription = "Crown"
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = "VÍ TÀI KHOẢN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE5B8C2),
                                        letterSpacing = 0.5.sp
                                    )
                                    GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFFE5B8C2), size = 9.dp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "%,d đ".format(walletState.balance),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Right Card: Hôm nay 15:30 · L'Amour (White card)
                Card(
                    modifier = Modifier
                        .weight(1.05f)
                        .height(88.dp)
                        .clickable { onOpenBookingSchedule() },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 11.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF9EFEF)),
                            contentAlignment = Alignment.Center
                        ) {
                            GlowIcon(icon = GlowIcons.Calendar, tint = Color(0xFF6B1228), size = 19.dp)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Hôm nay",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF7A686D),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE11D48))
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "15:30",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B1228)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "1994 Nailsroom",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF24090F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // 4. Suggested Stores Banner Card (Cửa Hàng Gợi Ý Dành Cho Bạn)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(185.dp)
                    .clickable { onViewAllSalons() },
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // High-res VIP image
                    Image(
                        painter = painterResource(id = R.drawable.banner_glowprive_vip),
                        contentDescription = "Cửa Hàng Gợi Ý Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dark luxury gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2C0610).copy(alpha = 0.95f),
                                        Color(0xFF2C0610).copy(alpha = 0.70f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            // Pill badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.15f))
                                    .border(0.75.dp, ChampagneGold.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 11.dp)
                                    Text(
                                        text = "CỬA HÀNG GỢI Ý DÀNH CHO BẠN",
                                        color = ChampagneGold,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Text(
                                text = "11 Salon Đà Nẵng\nĐã Kiểm Duyệt Độc Quyền",
                                color = Color.White,
                                fontFamily = FontFamily.Serif,
                                fontSize = 17.5.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 22.sp
                            )

                            Text(
                                text = "Tuyển chọn salon cao cấp với không gian sang trọng và phục vụ tận tâm",
                                color = Color(0xFFF3CEC5),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }

                        // Explore button
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .clickable { onViewAllSalons() }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text(
                                text = "Khám phá các Cửa hàng gợi ý",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF360814)
                            )
                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF360814), size = 12.dp)
                        }
                    }

                    // Carousel 3-dots indicator at bottom center
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 14.dp, height = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White)
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.45f))
                        )
                    }
                }
            }
        }

        // 5. Section: Salon nổi bật (Image 1 L'Amour Luxury Spa & Nail)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Salon nổi bật",
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A101A)
                    )
                    Row(
                        modifier = Modifier.clickable { onViewAllSalons() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Xem tất cả",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B1228)
                        )
                        GlowIcon(
                            icon = GlowIcons.ChevronRight,
                            tint = Color(0xFF6B1228),
                            size = 12.dp
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToSalon("salon_01") },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnail photo: salon_luxury_interior
                            Image(
                                painter = painterResource(id = R.drawable.salon_01_1994_nailsroom),
                                contentDescription = "Salon Interior",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(105.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val homeShopStatusMap by com.applamdep.common.AppBookingSyncBridge.shopStatusFlow.collectAsState()
                                    val is1994Closed = homeShopStatusMap["salon_01"] == com.applamdep.customer.core.constants.AppConstants.SHOP_STATUS_CLOSED
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "1994 Nailsroom",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp,
                                            color = Color(0xFF24090F)
                                        )
                                        if (is1994Closed) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFDECEA))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "Đã đóng cửa",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFD32F2F)
                                                )
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier.clickable { 
                                            isFavorite = !isFavorite 
                                            val msg = if (isFavorite) "Đã lưu 1994 Nailsroom vào danh sách yêu thích ❤️" else "Đã xóa khỏi danh sách yêu thích"
                                            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        GlowIcon(
                                            icon = if (isFavorite) GlowIcons.HeartFilled else GlowIcons.Heart,
                                            tint = if (isFavorite) Color(0xFFE11D48) else Color(0xFF8E7A80),
                                            size = 18.dp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 13.dp)
                                    Text(
                                        text = "4.9",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF24090F)
                                    )
                                    Text(
                                        text = "(142+ đánh giá)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A686D)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    GlowIcon(icon = GlowIcons.MapPin, tint = Color(0xFF7A686D), size = 12.dp)
                                    Text(
                                        text = "172/5 Trần Phú, Hải Châu · 0.8 km",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A686D),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Tags row
                                @OptIn(ExperimentalLayoutApi::class)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Nail Art", "Spa", "Chăm sóc da").forEach { tag ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color(0xFFF9EFEF))
                                                .padding(horizontal = 7.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = tag,
                                                fontSize = 10.sp,
                                                color = Color(0xFF6B1228),
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Voucher Salon discount bar clarifying shop promotion
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF1F2))
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GlowIcon(icon = GlowIcons.Tag, tint = Color(0xFFE11D48), size = 13.dp)
                            Text(
                                text = "Voucher: Giảm 30.000đ khi đặt lịch qua app",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF9F1239)
                            )
                        }
                    }
                }
            }
        }

        // 6. Section: Dịch vụ yêu thích (Image 1 bottom carousel)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dịch vụ yêu thích",
                        fontSize = 17.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A101A)
                    )
                    Row(
                        modifier = Modifier.clickable { onViewAllSalons() },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "Xem tất cả",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B1228)
                        )
                        GlowIcon(
                            icon = GlowIcons.ChevronRight,
                            tint = Color(0xFF6B1228),
                            size = 12.dp
                        )
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(favoriteServices) { fav ->
                        Card(
                            modifier = Modifier
                                .width(155.dp)
                                .clickable { onSelectFavoriteService(fav) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Image(
                                    painter = painterResource(id = fav.imageRes),
                                    contentDescription = fav.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                )
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = fav.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF24090F),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = fav.salon,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF7A686D)
                                    )
                                    Text(
                                        text = fav.price,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6B1228)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }

    // Category Bottom Sheet Modal
    if (activeCategoryModal != null) {
        val isAllCategories = activeCategoryModal == "Tất cả danh mục dịch vụ làm đẹp"
        val modalTitle = if (isAllCategories) "Khám phá danh mục dịch vụ" else "Bộ Sưu Tập $activeCategoryModal"

        ModalBottomSheet(
            onDismissRequest = { activeCategoryModal = null },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "DỊCH VỤ LÀM ĐẸP THƯỢNG LƯU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B1228),
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GlowIcon(icon = GlowIcons.Sparkle, tint = ChampagneGold, size = 18.dp)
                    Text(
                        text = modalTitle,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3A101A)
                    )
                }

                HorizontalDivider(color = Color(0xFFEBDCD3))

                val modalItems = if (isAllCategories) {
                    listOf(
                        Triple("Làm Nail", "Sơn gel thạch, móng úp, đắp bột ombre, móng nghệ thuật", "Từ 150.000 đ"),
                        Triple("Spa Trị Liệu", "Trị mụn y khoa, thải độc CO2, cấy tế bào gốc phục hồi", "Từ 200.000 đ"),
                        Triple("Chăm sóc da", "Điện di vitamin C, collagen tươi căng bóng trẻ hóa", "Từ 250.000 đ"),
                        Triple("Nối Mi & Mày", "Mi lụa mềm thiết kế tự nhiên, uốn mi collagen 6D", "Từ 180.000 đ"),
                        Triple("Hair Salon", "Cắt layer Hàn Quốc, uốn sóng lơi, nhuộm balayage tôn da", "Từ 150.000 đ"),
                        Triple("Makeup Tiệc", "Trang điểm cô dâu VIP, makeup tone Douyin/Tây sang chảnh", "Từ 250.000 đ"),
                        Triple("Dưỡng sinh đông y", "Gội đầu bồ kết nấu tươi, massage bấm huyệt cổ vai gáy", "Từ 120.000 đ"),
                        Triple("Triệt lông vĩnh viễn", "Công nghệ Diode Laser lạnh không đau rát", "Từ 180.000 đ"),
                        Triple("Massage body thư giãn", "Đá nóng muối khoáng Himalaya, ngâm chân thảo mộc", "Từ 280.000 đ")
                    )
                } else {
                    listOf(
                        Triple("Sơn Gel Hữu Cơ Pháp", "Dưỡng móng keratin, cắt da sạch sẽ chuẩn salon", "180.000 đ"),
                        Triple("Nail Art Mắt Mèo Cực Quang", "Hiệu ứng mắt mèo đổi màu kết hợp đính đá Swarovski", "280.000 đ"),
                        Triple("Đắp Móng Hoàng Gia", "Đắp gel/bột form thanh thoát, độ bền hơn 1 tháng", "450.000 đ")
                    )
                }

                modalItems.forEach { (catTitle, catDesc, catPrice) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                activeCategoryModal = null
                                onSelectCategory(catTitle)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF7E8)),
                        border = BorderStroke(1.dp, Color(0xFFEBDCD3))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF9EFEF)),
                                contentAlignment = Alignment.Center
                            ) {
                                GlowIcon(icon = GlowIcons.Sparkle, tint = Color(0xFF6B1228), size = 18.dp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = catTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF24090F),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = catDesc,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF7A686D),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 15.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFCEEEB)
                            ) {
                                Text(
                                    text = catPrice,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6B1228),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


@Composable
private fun SearchShopResultCard(
    shop: Shop,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ảnh đại diện salon
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(id = ShopImageAssets.getShopDrawableRes(shop.id)),
                        contentDescription = shop.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (shop.isVipActive || shop.plan == "VIP") {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(Color(0xFF6B1228))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "VIP GLOW",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChampagneGold
                            )
                        }
                    }
                }

                // Thông tin salon
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = shop.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF24090F),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Rating & Reviews
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GlowIcon(icon = GlowIcons.StarFilled, tint = ChampagneGold, size = 12.dp)
                        Text(
                            text = "${shop.rating}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B1228)
                        )
                        Text(
                            text = "(${shop.reviewCount} đánh giá)",
                            fontSize = 11.sp,
                            color = Color(0xFF7A686D)
                        )
                    }

                    // Địa chỉ
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GlowIcon(icon = GlowIcons.MapPin, tint = Color(0xFF7A686D), size = 12.dp)
                        Text(
                            text = shop.address,
                            fontSize = 11.5.sp,
                            color = Color(0xFF7A686D),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Giờ mở cửa & Cọc
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFBF7E8))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = shop.openingHours,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6B1228)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (shop.requireDeposit) Color(0xFFFCEEEB) else Color(0xFFE8F5E9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (shop.requireDeposit) "Cọc giữ chỗ 100%" else "Miễn cọc 0đ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (shop.requireDeposit) Color(0xFF6B1228) else Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nút bấm đặt lịch / xem salon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFBF7E8))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Xem chi tiết dịch vụ & Đặt lịch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B1228)
                )
                GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF6B1228), size = 14.dp)
            }
        }
    }
}
