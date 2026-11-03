package com.applamdep.customer.ui.shop

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.di.ViewModelFactory
import com.applamdep.customer.core.util.CurrencyFormatter
import com.applamdep.customer.core.util.ShopImageAssets
import com.applamdep.customer.domain.model.ServiceItem
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.ui.components.*
import com.applamdep.customer.ui.theme.*
import com.example.app_lam_dep.ui.theme.*
import com.example.app_lam_dep.R

// Data Models for Gallery
private data class AlbumItem(
    val title: String,
    val countText: String,
    val category: String,
    val imageRes: Int
)

// BẢNG MÀU CHUẨN THƯƠNG HIỆU HAUTE COUTURE CỦA TOÀN APP
private val LuxuryWine = DustyMerlot // Color(0xFF62202F) - Đỏ rượu Noble Burgundy Noir
private val LuxuryBlushBg = Color(0xFFFDF4F2) // Nền hồng phấn cho bảo chứng an tâm
private val LuxuryGold = ChampagneGold // Color(0xFFC6A15B) - Vàng champagne
private val LuxuryCreamBg = WarmWhite // Color(0xFFFBF7E8) - Lụa nhung tơ tằm

/**
 * GIAO DIỆN CỬA HÀNG CHUẨN GRAB (FOOD/BEAUTY)
 * - Cover Photo tràn viền với các nút thao tác nổi
 * - Floating Card bo góc 20dp đè lên ảnh bìa
 * - Hàng nút chức năng (Hotline, Bản đồ, Zalo, Chia sẻ)
 * - Khối Voucher & Ưu đãi trượt ngang kiểu Grab
 * - Giữ nguyên 4 Danh mục: [Dịch vụ | Giới thiệu | Hình ảnh | Đánh giá]
 * - Trình bày dịch vụ chuẩn danh sách món Grab: ảnh vuông bên trái, tên, mô tả, giá và nút tròn xanh lá (+)
 * - Thanh Sticky Bottom "Mở khoá ưu đãi" + nút "Chọn lịch ngay"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailScreen(
    shopId: String,
    onBack: () -> Unit,
    onSelectServiceForBooking: (shopId: String, serviceId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShopDetailViewModel = viewModel(factory = ViewModelFactory.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var isFavorite by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Dịch vụ, 1: Giới thiệu, 2: Hình ảnh, 3: Đánh giá
    var serviceSearchQuery by remember { mutableStateOf("") }
    var selectedServiceCategory by remember { mutableStateOf("Tất cả") }
    var galleryFilter by remember { mutableStateOf("Tất cả") }

    LaunchedEffect(shopId) {
        viewModel.loadShopDetail(shopId)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        when (val state = uiState) {
            is UiState.Idle, is UiState.Loading -> GrabLoadingState()
            is UiState.Error -> GrabEmptyState(
                message = state.message,
                onRetry = { viewModel.loadShopDetail(shopId) }
            )
            is UiState.Success -> {
                val detail = state.data
                val shop = detail.shop
                val services = detail.services
                val reviews = detail.reviews

                val bannerRes = ShopImageAssets.getShopDrawableRes(shop.id)

                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 20.dp)
                    ) {
                        // ───────────────── 1. COVER PHOTO TRÀN VIỀN PHONG CÁCH GRAB ─────────────────
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = bannerRes),
                                    contentDescription = shop.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Gradient tối trên cùng để nổi bật nút Back & Action
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
                                            )
                                        )
                                )

                                // Hàng nút điều hướng nổi trên Cover
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .statusBarsPadding()
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Nút Back tròn trắng
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.92f))
                                            .clickable { onBack() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color(0xFF1F2937),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Nhóm nút phải (Yêu thích & Đặt lịch / Chia sẻ)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Nút Yêu thích
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.92f))
                                                .clickable {
                                                    isFavorite = !isFavorite
                                                    Toast.makeText(context, if (isFavorite) "Đã lưu vào danh sách yêu thích ❤️" else "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Favorite",
                                                tint = if (isFavorite) Color(0xFFE11D48) else Color(0xFF1F2937),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        // Nút Pill "Đặt lịch hẹn" kiểu Grab "Giao hàng"
                                        Surface(
                                            shape = RoundedCornerShape(19.dp),
                                            color = Color.White.copy(alpha = 0.92f),
                                            shadowElevation = 2.dp,
                                            modifier = Modifier.clickable {
                                                val firstService = services.firstOrNull()
                                                onSelectServiceForBooking(shop.id, firstService?.id ?: "srv_01")
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = null,
                                                    tint = LuxuryWine,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Đặt lịch hẹn",
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1F2937)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ───────────────── 2. FLOATING WHITE CARD ĐÈ LÊN ẢNH BÌA (CHUẨN GRAB) ─────────────────
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                                    .offset(y = (-24).dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Thumbnail vuông bo góc của tiệm (giống ảnh gà nướng Cô Sa trên Grab)
                                        Box(
                                            modifier = Modifier
                                                .size(76.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                        ) {
                                            Image(
                                                painter = painterResource(id = bannerRes),
                                                contentDescription = shop.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Badge deal nhỏ góc dưới ảnh
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .background(Color(0xFFE11D48))
                                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "HOT VIP",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        // Tên tiệm + Đánh giá + Khoảng cách
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            Text(
                                                text = shop.name,
                                                fontSize = 18.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextNoir,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // Row: ★ 4.9 (142)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = LuxuryGold,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Text(
                                                    text = "${shop.rating}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextNoir
                                                )
                                                Text(
                                                    text = "(${shop.reviewCount})",
                                                    fontSize = 12.sp,
                                                    color = TextMuted
                                                )
                                            }

                                            // Row: Cọc + Thời gian phục vụ
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                                            ) {
                                                Text(
                                                    text = if (shop.requireDeposit) "Cọc từ 50.000đ" else "Miễn cọc 0đ",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (shop.requireDeposit) Color(0xFFE11D48) else LuxuryWine
                                                )
                                                Text(text = "•", fontSize = 11.sp, color = Color(0xFF9CA3AF))
                                                Text(
                                                    text = "0.8 km · 15 phút",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF4B5563)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ───────────────── 3. HÀNG NÚT THAO TÁC NHANH (GIỐNG GRAB) ─────────────────
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-14).dp)
                                    .padding(horizontal = 14.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 1. Gọi hotline
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}")))
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(19.dp),
                                    border = BorderStroke(1.dp, BorderSilk),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextNoir),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = LuxuryWine, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Hotline", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // 2. Bản đồ & Chỉ đường
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val gmmIntentUri = Uri.parse("geo:${shop.lat},${shop.lng}?q=${Uri.encode(shop.address)}")
                                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(19.dp),
                                    border = BorderStroke(1.dp, BorderSilk),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextNoir),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Chỉ đường", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // 3. Zalo Tư vấn
                                OutlinedButton(
                                    onClick = {
                                        val digits = shop.phone.replace(Regex("[^0-9]"), "")
                                        val url = if (digits.isNotBlank()) "https://zalo.me/$digits" else "https://zalo.me"
                                        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(19.dp),
                                    border = BorderStroke(1.dp, BorderSilk),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextNoir),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Zalo", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // 4. Chia sẻ
                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, "Khám phá ${shop.name} tại ${shop.address} trên Glow Prestige!")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ tiệm"))
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(19.dp),
                                    border = BorderStroke(1.dp, BorderSilk),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextNoir),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF4B5563), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Chia sẻ", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        // ───────────────── 4. THANH 4 DANH MỤC: GIỮ NGUYÊN THEO YÊU CẦU ─────────────────: GIỮ NGUYÊN THEO YÊU CẦU ─────────────────
                        // [Dịch vụ | Giới thiệu | Hình ảnh | Đánh giá] (Đúng như ảnh 4 người dùng gửi)
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = SurfaceLight,
                                border = BorderStroke(1.dp, BorderSilk),
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val tabItems = listOf(
                                        Triple("Dịch vụ", Icons.Default.GridView, 0),
                                        Triple("Giới thiệu", Icons.Default.Description, 1),
                                        Triple("Hình ảnh", Icons.Default.Image, 2),
                                        Triple("Đánh giá", Icons.Default.Star, 3)
                                    )

                                    tabItems.forEach { (title, icon, index) ->
                                        val isSelected = selectedTab == index
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) LuxuryWine else Color.Transparent)
                                                .clickable { selectedTab = index }
                                                .padding(vertical = 9.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = icon,
                                                    contentDescription = title,
                                                    tint = if (isSelected) Color.White else TextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = title,
                                                    fontSize = 11.5.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextMuted
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // ───────────────── 6. NỘI DUNG CHI TIẾT THEO 4 TAB ─────────────────
                        when (selectedTab) {
                            // ══════════════ TAB 0: DỊCH VỤ (TRÌNH BÀY CHUẨN MENU GRAB) ══════════════
                            0 -> {
                                // Tiêu đề nhóm dịch vụ kiểu Grab "Combo"
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Combo & Dịch Vụ Nổi Bật",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextNoir
                                        )
                                        Text(
                                            text = "${services.size} món dịch vụ",
                                            fontSize = 12.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                // Danh sách món / dịch vụ (Layout chuẩn Grab như ảnh 1 & ảnh 2)
                                items(
                                    items = services,
                                    key = { it.id }
                                ) { service ->
                                    val srvImgRes = ShopImageAssets.getServiceDrawableRes(service.id)
                                    val formattedPrice = CurrencyFormatter.formatVnd(service.price)
                                        .replace(" đ", "")
                                        .replace(" VND", "")

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectServiceForBooking(shop.id, service.id) }
                                            .background(Color.White)
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // 1. Ảnh vuông món/dịch vụ bo góc bên trái (giống hệt đĩa gà nướng Grab)
                                        Box(
                                            modifier = Modifier
                                                .size(96.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color(0xFFF3F4F6))
                                        ) {
                                            Image(
                                                painter = painterResource(id = srvImgRes),
                                                contentDescription = service.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        // 2. Cột thông tin món/dịch vụ ở giữa
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = service.name,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextNoir,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = service.description.ifEmpty { "Liệu trình chăm sóc cao cấp mang đến vẻ đẹp tự nhiên hoàn hảo." },
                                                fontSize = 12.sp,
                                                color = TextMuted,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                lineHeight = 16.sp
                                            )

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Schedule,
                                                    contentDescription = null,
                                                    tint = Color(0xFF9CA3AF),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(3.dp))
                                                Text(
                                                    text = "${service.durationMinutes} phút",
                                                    fontSize = 11.5.sp,
                                                    color = TextMuted
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(2.dp))

                                            Text(
                                                text = formattedPrice,
                                                fontSize = 16.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextNoir
                                            )
                                        }

                                        // 3. Nút tròn xanh lá (+) của Grab ở góc phải
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(LuxuryWine)
                                                .clickable { onSelectServiceForBooking(shop.id, service.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Chọn dịch vụ",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    // Đường kẻ mờ phân tách món
                                    HorizontalDivider(
                                        color = Color(0xFFF3F4F6),
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            }

                            // ══════════════ TAB 1: GIỚI THIỆU (ABOUT US & CONTACT) ══════════════
                            1 -> {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(text = "Về chúng tôi", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                            Text(
                                                text = shop.description.ifEmpty {
                                                    "Tổ hợp làm đẹp uy tín hàng đầu với đội ngũ chuyên viên tay nghề cao, không gian hiện đại và sản phẩm chính hãng cao cấp."
                                                },
                                                fontSize = 13.sp,
                                                color = Color(0xFF4B5563),
                                                lineHeight = 20.sp
                                            )
                                        }
                                    }
                                }

                                // Kênh liên hệ & Mạng xã hội từ Excel
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text(text = "Thông tin liên hệ & Kênh truyền thông", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextNoir)

                                            // Hotline
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF0FDF4)).padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = LuxuryWine, modifier = Modifier.size(18.dp))
                                                    Text(text = "Hotline: ${shop.phone}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                                }
                                                Text(
                                                    text = "Gọi ngay >",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = LuxuryWine,
                                                    modifier = Modifier.clickable {
                                                        try { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shop.phone}"))) } catch (_: Exception) {}
                                                    }
                                                )
                                            }

                                            // Địa chỉ
                                            Row(
                                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFEF2F2)).padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(18.dp))
                                                    Text(text = shop.address, fontSize = 12.5.sp, color = Color(0xFF991B1B), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                                Text(
                                                    text = "Chỉ đường >",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE11D48),
                                                    modifier = Modifier.clickable {
                                                        try {
                                                            val gmmIntentUri = Uri.parse("geo:${shop.lat},${shop.lng}?q=${Uri.encode(shop.address)}")
                                                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                                                        } catch (_: Exception) {}
                                                    }
                                                )
                                            }

                                            // Giờ làm việc
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(16.dp))
                                                Text(text = "Giờ mở cửa: ${shop.openingHours} (Thứ 2 - Chủ nhật)", fontSize = 12.5.sp, color = Color(0xFF374151))
                                            }

                                            // Các nút link mạng xã hội
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                if (!shop.facebook.isNullOrBlank() && shop.facebook != "-") {
                                                    OutlinedButton(
                                                        onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shop.facebook))) } catch (_: Exception) {} },
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Facebook", fontSize = 11.5.sp, color = Color(0xFF1877F2), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (!shop.tiktok.isNullOrBlank() && shop.tiktok != "-") {
                                                    OutlinedButton(
                                                        onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shop.tiktok))) } catch (_: Exception) {} },
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("TikTok", fontSize = 11.5.sp, color = Color(0xFF111111), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (!shop.instagram.isNullOrBlank() && shop.instagram != "-") {
                                                    OutlinedButton(
                                                        onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shop.instagram))) } catch (_: Exception) {} },
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Instagram", fontSize = 11.5.sp, color = Color(0xFFE1306C), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (!shop.website.isNullOrBlank() && shop.website != "-") {
                                                    OutlinedButton(
                                                        onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(shop.website))) } catch (_: Exception) {} },
                                                        modifier = Modifier.weight(1f).height(36.dp),
                                                        shape = RoundedCornerShape(8.dp),
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Website", fontSize = 11.5.sp, color = Color(0xFF0284C7), fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Tiện ích tiệm
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Text(text = "Tiện ích & Dịch vụ đi kèm", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                items(listOf("Máy lạnh", "Trà thảo mộc miễn phí", "Wifi tốc độ cao", "Phòng VIP riêng tư", "Chỗ đậu xe máy", "Thanh toán VietQR")) { am ->
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(Color(0xFFF3F4F6))
                                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    ) {
                                                        Text(text = am, fontSize = 11.5.sp, color = Color(0xFF374151))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // ══════════════ TAB 2: HÌNH ẢNH (GALLERY) ══════════════
                            2 -> {
                                item {
                                    val filters = listOf("Tất cả", "Cắt tóc", "Uốn tóc", "Làm móng", "Chăm sóc da", "Không gian")
                                    LazyRow(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(filters) { f ->
                                            val isSelected = galleryFilter == f
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(if (isSelected) LuxuryWine else SurfaceLight)
                                                    .border(1.dp, if (isSelected) LuxuryWine else BorderSilk, RoundedCornerShape(16.dp))
                                                    .clickable { galleryFilter = f }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = f,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else TextNoir
                                                )
                                            }
                                        }
                                    }
                                }

                                val albums = listOf(
                                    AlbumItem("Cắt tóc tạo kiểu", "32 ảnh >", "Cắt tóc", R.drawable.srv_hair_cut),
                                    AlbumItem("Uốn sóng lơi Hàn", "28 ảnh >", "Uốn tóc", R.drawable.srv_hair_wave),
                                    AlbumItem("Nail Art Mắt Mèo", "35 ảnh >", "Làm móng", R.drawable.srv_nail_art),
                                    AlbumItem("Sơn Gel Hữu Cơ", "40 ảnh >", "Làm móng", R.drawable.srv_nail_gel),
                                    AlbumItem("Chăm sóc da & Trị mụn", "20 ảnh >", "Chăm sóc da", R.drawable.srv_spa_facial),
                                    AlbumItem("Gội đầu & Massage", "26 ảnh >", "Không gian", R.drawable.srv_spa_massage),
                                    AlbumItem("Makeup Cô Dâu VIP", "18 ảnh >", "Cắt tóc", R.drawable.srv_makeup_bride),
                                    AlbumItem("Không gian sang trọng", "25 ảnh >", "Không gian", R.drawable.salon_luxury_interior)
                                )
                                val filteredAlbums = if (galleryFilter == "Tất cả") albums else albums.filter { it.category == galleryFilter || it.title.contains(galleryFilter, true) }
                                val pairs = filteredAlbums.chunked(2)

                                items(pairs) { pair ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        pair.forEach { album ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(115.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable {
                                                        Toast.makeText(context, "Mở BST: ${album.title}", Toast.LENGTH_SHORT).show()
                                                    }
                                            ) {
                                                Image(
                                                    painter = painterResource(id = album.imageRes),
                                                    contentDescription = album.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                                                            )
                                                        )
                                                        .padding(10.dp),
                                                    contentAlignment = Alignment.BottomStart
                                                ) {
                                                    Column {
                                                        Text(text = album.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        Text(text = album.countText, fontSize = 10.sp, color = Color(0xFFF3CEC5))
                                                    }
                                                }
                                            }
                                        }
                                        if (pair.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }

                            // ══════════════ TAB 3: ĐÁNH GIÁ (REVIEWS) ══════════════
                            3 -> {
                                item {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = "${shop.rating}", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = TextNoir)
                                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                    repeat(5) {
                                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(14.dp))
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(text = "${shop.reviewCount} đánh giá", fontSize = 11.5.sp, color = TextMuted)
                                            }

                                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                GrabRatingProgressRow("5★", 0.88f, "125")
                                                GrabRatingProgressRow("4★", 0.10f, "14")
                                                GrabRatingProgressRow("3★", 0.02f, "3")
                                                GrabRatingProgressRow("2★", 0.00f, "0")
                                                GrabRatingProgressRow("1★", 0.00f, "0")
                                            }
                                        }
                                    }
                                }

                                val activeReviews = reviews.filter { it.status != "REMOVED" }
                                val displayReviews = if (activeReviews.isNotEmpty()) {
                                    activeReviews.map { 
                                        Triple(it.customerName.ifBlank { "Khách hàng thân thiết" }, "${it.rating}★", it.comment)
                                    }
                                } else {
                                    listOf(
                                        Triple("Trần Thu Trang", "5★", "Tiệm làm móng cực kỳ tỉ mỉ và xinh, giữ màu hơn 1 tháng vẫn không bị tróc! Sơn gel hữu cơ không hề có mùi nồng, phòng ốc máy lạnh thơm mát rất thư giãn."),
                                        Triple("Nguyễn Bảo Ngọc", "5★", "Gội đầu dưỡng sinh bồ kết nấu tươi rất thơm, nhân viên massage bấm huyệt cổ vai gáy hết nhức mỏi. Đặt hẹn qua app được giảm ngay 30K rất tiện!"),
                                        Triple("Lê Hoàng Yến", "5★", "Làm mi lụa mềm tự nhiên nhẹ tênh không hề cộm hay ngứa mắt. Giá cả niêm yết rõ ràng không phát sinh phụ phí.")
                                    )
                                }

                                items(displayReviews) { (author, rating, comment) ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    Box(
                                                        modifier = Modifier.size(32.dp).clip(CircleShape).background(PinkPanther.copy(alpha = 0.5f)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(text = author.take(1), fontWeight = FontWeight.Bold, color = LuxuryWine, fontSize = 13.sp)
                                                    }
                                                    Text(text = author, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = TextNoir)
                                                }
                                                Row {
                                                    repeat(5) {
                                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(12.dp))
                                                    }
                                                }
                                            }
                                            Text(text = comment, fontSize = 12.5.sp, color = Color(0xFF4B5563), lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ───────────────── 7. STICKY BOTTOM BAR (CHUẨN GRAB NHƯ ẢNH 1 & ẢNH 2) ─────────────────
                    Surface(
                        color = Color.White,
                        shadowElevation = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Mini banner bảo chứng lịch hẹn sang trọng chuẩn thương hiệu
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LuxuryBlushBg)
                                    .border(1.dp, BorderSilk, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = LuxuryWine,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Bảo chứng lịch hẹn: Giữ chỗ 100% · Cam kết an tâm",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LuxuryWine
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            // Nút bấm chính "Chọn lịch ngay" (màu Grab Green)
                            Button(
                                onClick = {
                                    val firstService = services.firstOrNull()
                                    onSelectServiceForBooking(shop.id, firstService?.id ?: "srv_01")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LuxuryWine)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Chọn lịch ngay",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
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

// ───────────────── CÁC COMPONENT PHỤ TRỢ ─────────────────


@Composable
private fun GrabRatingProgressRow(label: String, progress: Float, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4B5563), modifier = Modifier.width(20.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = LuxuryGold,
            trackColor = Color(0xFFE5E7EB)
        )
        Text(text = count, fontSize = 10.sp, color = TextMuted, modifier = Modifier.width(24.dp), textAlign = TextAlign.End)
    }
}
