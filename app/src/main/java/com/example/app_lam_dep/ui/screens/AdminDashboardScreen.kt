package com.example.app_lam_dep.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app_lam_dep.ui.icons.GlowIcon
import com.example.app_lam_dep.ui.icons.GlowIcons
import com.example.app_lam_dep.ui.theme.ChampagneGold
import com.example.app_lam_dep.ui.theme.DustyMerlot
import com.example.app_lam_dep.ui.theme.GoldSoft
import com.example.app_lam_dep.ui.theme.PinkPanther
import com.example.app_lam_dep.ui.theme.SuccessGreen
import com.example.app_lam_dep.ui.theme.TextMuted
import com.example.app_lam_dep.ui.theme.WarningOrange
import com.applamdep.customer.data.local.UserLocalStorage
import com.applamdep.customer.domain.model.AccountStatus
import com.applamdep.customer.domain.model.UserRole
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

// Data models for Admin Management
data class AdminShopItem(
    val id: String,
    val name: String,
    val code: String,
    val phone: String,
    val representative: String,
    val address: String,
    val regDate: String,
    var status: String, // "Chờ duyệt", "Đang hoạt động", "Tạm khóa", "Từ chối"
    val legalDoc: String,
    val verifiedDate: String,
    val servicesCount: Int,
    val bookingsCount: Int,
    val rating: Double,
    val violationHistory: String = "Không có vi phạm"
)

data class AdminCustomerItem(
    val id: String,
    val name: String,
    val code: String,
    val phone: String,
    val email: String,
    val joinDate: String,
    val bookingsCount: Int,
    val cancelCount: Int,
    val totalSpent: String,
    val depositHeld: String,
    var status: String // "Đang hoạt động", "Tạm khóa"
)

data class AdminBookingItem(
    val code: String,
    val time: String,
    val salon: String,
    val customer: String,
    val service: String,
    val value: String,
    val deposit: String,
    var status: String, // "Chờ salon xác nhận", "Đã xác nhận", "Hoàn thành", "Đã hủy"
    var paymentStatus: String // "Đã đặt cọc qua sàn", "Chờ hoàn cọc", "Đã hoàn cọc", "Đã thanh toán đủ"
)

data class AdminRefundRequest(
    val id: String,
    val bookingCode: String,
    val customer: String,
    val salon: String,
    val depositAmount: String,
    val reason: String,
    var status: String // "Chờ duyệt", "Đã duyệt hoàn", "Từ chối"
)


data class AdminViolationReport(
    val id: String,
    val title: String,
    val target: String,
    val reporter: String,
    val time: String,
    val content: String,
    var status: String
)

data class AdminNotificationItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val type: String,
    var isRead: Boolean = false
)

data class AdminStaffAccount(
    val id: String,
    val name: String,
    val email: String,
    val roleTitle: String,
    val permissions: String,
    var status: String = "Hoạt động"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    selectedTab: Int = 0,
    onTabChange: (Int) -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Sub-view navigation when in Tab 3 ("Thêm")
    var activeMoreSubView by remember { mutableStateOf<String?>(null) } // "bookings", "finance", "violations", "settings"
    var showAdminLogoutConfirm by remember { mutableStateOf(false) }
    var showAddStaffDialog by remember { mutableStateOf(false) }

    // Violations data state
    val violationReports = remember {
        mutableStateListOf(
            AdminViolationReport("REP-801", "Nghi vấn spam đánh giá 1 sao hạ uy tín đối thủ", "Thì Là SPA", "Chủ cơ sở (0773***)", "10:15 Hôm nay", "Nội dung đánh giá lặp lại nhiều lần từ các tài khoản mới tạo cùng dải IP.", "Chờ xử lý"),
            AdminViolationReport("REP-802", "Phản ánh cơ sở phụ thu giá ngoài khác trên ứng dụng", "1994 Nailsroom", "Khánh Linh (0903***)", "Hôm qua", "Cơ sở tính thêm 30k phụ thu cắt da dù gói đã bao gồm cắt da dưỡng móng.", "Chờ xử lý"),
            AdminViolationReport("REP-803", "Bình luận chứa ngôn từ khiếm nhã, thô tục", "BONNIE NAIL", "Hệ thống tự động", "11/08", "Từ ngữ xúc phạm nhân viên tiệm, vi phạm tiêu chuẩn cộng đồng Glow.", "Đã gỡ bỏ")
        )
    }

    // Notifications data state
    val adminNotifications = remember {
        mutableStateListOf(
            AdminNotificationItem("NOTIF-01", "Hồ sơ đối tác mới chờ duyệt", "Cơ sở 'Glow Boutique Nail' vừa đăng ký tài khoản cần thẩm định hồ sơ", "5 phút trước", "SHOP", false),
            AdminNotificationItem("NOTIF-02", "Khoản cọc mới 50.000 ₫", "Đơn #GLOW-8921 đã thanh toán cọc thành công vào quỹ ký quỹ Escrow", "25 phút trước", "PAYMENT", false),
            AdminNotificationItem("NOTIF-03", "Cảnh báo yêu cầu hoàn cọc", "Khách hàng Hoàng Kim Oanh yêu cầu hoàn cọc 84.000 ₫ do salon đổi lịch", "1 giờ trước", "ALERT", false),
            AdminNotificationItem("NOTIF-04", "Sao lưu dữ liệu tự động", "Toàn bộ 11 salon và danh sách khách hàng đã được đồng bộ vào bộ nhớ an toàn", "3 giờ trước", "SYSTEM", true),
            AdminNotificationItem("NOTIF-05", "Báo cáo doanh thu ngày", "Doanh thu phí sàn hôm nay đạt 4.850.000 ₫ (+12% so với hôm qua)", "Hôm qua", "PAYMENT", true)
        )
    }

    // Staff accounts data state
    val staffAccounts = remember {
        mutableStateListOf(
            AdminStaffAccount("STAFF-01", "Trần Tuấn (Admin Trưởng)", "admin@glowprestige.com", "Super Administrator", "Toàn quyền quản trị hệ thống, phê duyệt đối tác, đối soát tài chính", "Hoạt động"),
            AdminStaffAccount("STAFF-02", "Nguyễn Thị Mai (Kế toán)", "finance.admin@glowprestige.com", "Finance & Escrow Admin", "Quản lý quỹ cọc ký quỹ Escrow, duyệt hoàn cọc, xuất đối soát Excel", "Hoạt động"),
            AdminStaffAccount("STAFF-03", "Lê Hoàng Yến (Kiểm duyệt)", "moderator@glowprestige.com", "Content Moderator", "Kiểm duyệt hồ sơ salon mới, duyệt bảng giá dịch vụ, xử lý vi phạm", "Hoạt động"),
            AdminStaffAccount("STAFF-04", "Phạm Minh Đức (CSKH)", "support@glowprestige.com", "Customer Support", "Hỗ trợ khách hàng đặt lịch, giải đáp khiếu nại dịch vụ 24/7", "Hoạt động")
        )
    }

    // System settings state
    var commissionRate by remember { mutableStateOf("10") }
    var vipMonthlyFee by remember { mutableStateOf("499.000") }
    var refundDeadlineHours by remember { mutableStateOf("24") }
    var autoCompleteMinutes by remember { mutableStateOf("30") }
    var autoApproveDocShop by remember { mutableStateOf(false) }
    var enableSmsReminder by remember { mutableStateOf(true) }
    var notifyOnNewShop by remember { mutableStateOf(true) }
    var financeActiveTab by remember { mutableStateOf(0) } // 0: Doanh thu sàn, 1: Tiền cọc

    // State for modals
    var selectedShopDetail by remember { mutableStateOf<AdminShopItem?>(null) }
    var selectedCustomerDetail by remember { mutableStateOf<AdminCustomerItem?>(null) }
    var selectedBookingDetail by remember { mutableStateOf<AdminBookingItem?>(null) }
    var showAddCustomerDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userStorage = remember { UserLocalStorage(context) }

    // Đồng bộ danh sách tài khoản Cửa hàng đăng ký từ bộ nhớ máy
    val registeredMerchants = remember {
        userStorage.getAllUsers().filter { it.role == UserRole.MERCHANT }
    }

    // Sample Shop Data kết hợp Cửa hàng người dùng đăng ký
    val shops = remember {
        mutableStateListOf<AdminShopItem>().apply {
            // Nạp các tài khoản Cửa hàng do người dùng đăng ký bằng Gmail
            registeredMerchants.forEach { m ->
                val statusStr = when (m.status) {
                    AccountStatus.APPROVED -> "Đang hoạt động"
                    AccountStatus.PENDING_APPROVAL -> "Chờ duyệt"
                    AccountStatus.REJECTED -> "Từ chối"
                    AccountStatus.LOCKED -> "Tạm khóa"
                }
                add(
                    AdminShopItem(
                        id = "merchant_${m.gmail.hashCode()}",
                        name = m.salonName.ifBlank { "Salon (${m.gmail.substringBefore('@')})" },
                        code = "SALON-M${Math.abs(m.gmail.hashCode()) % 1000}",
                        phone = m.phone.ifBlank { "0905 123 456" },
                        representative = m.gmail,
                        address = "Đà Nẵng",
                        regDate = "Hôm nay",
                        status = statusStr,
                        legalDoc = "Tài khoản Gmail: ${m.gmail}",
                        verifiedDate = if (m.status == AccountStatus.APPROVED) "Đã duyệt" else "Chưa duyệt",
                        servicesCount = 6,
                        bookingsCount = 0,
                        rating = 5.0
                    )
                )
            }
            add(
                AdminShopItem(
                    id = "1",
                    name = "L'Amour Luxury Spa & Nail",
                    code = "SALON-001",
                    phone = "0908 123 456",
                    representative = "Trần Minh Anh",
                    address = "45 Đồng Khởi, P. Bến Nghé, Quận 1, TP.HCM",
                    regDate = "10/01/2025",
                    status = "Đang hoạt động",
                    legalDoc = "GPKD số 0314892182 do Sở KHĐT TP.HCM cấp",
                    verifiedDate = "12/01/2025",
                    servicesCount = 18,
                    bookingsCount = 342,
                    rating = 4.9
                )
            )
            add(
                AdminShopItem(
                    id = "2",
                    name = "An Yên Thảo Dược Retreat",
                    code = "SALON-002",
                    phone = "0912 345 678",
                    representative = "Lê Hoàng Yến",
                    address = "12 Tú Xương, Phường 7, Quận 3, TP.HCM",
                    regDate = "15/02/2025",
                    status = "Đang hoạt động",
                    legalDoc = "GPKD số 0317823901 do Sở KHĐT TP.HCM cấp",
                    verifiedDate = "17/02/2025",
                    servicesCount = 12,
                    bookingsCount = 186,
                    rating = 4.95
                )
            )
            add(
                AdminShopItem(
                    id = "3",
                    name = "The Royal Nail & Lash Lounge",
                    code = "SALON-003",
                    phone = "0933 888 999",
                    representative = "Nguyễn Ngọc Bích",
                    address = "88 Pasteur, Phường Bến Nghé, Quận 1, TP.HCM",
                    regDate = "18/08/2025",
                    status = "Chờ duyệt",
                    legalDoc = "GPKD số 0319203810 - Đang xác minh hồ sơ",
                    verifiedDate = "Chưa duyệt",
                    servicesCount = 10,
                    bookingsCount = 0,
                    rating = 5.0
                )
            )
            add(
                AdminShopItem(
                    id = "4",
                    name = "Aura Beauty & Spa",
                    code = "SALON-004",
                    phone = "0944 555 666",
                    representative = "Phạm Thu Hương",
                    address = "102 Lê Thị Riêng, Quận 1, TP.HCM",
                    regDate = "19/08/2025",
                    status = "Chờ duyệt",
                    legalDoc = "Hồ sơ đăng ký kèm giấy phép hành nghề",
                    verifiedDate = "Chưa duyệt",
                    servicesCount = 8,
                    bookingsCount = 0,
                    rating = 0.0
                )
            )
            add(
                AdminShopItem(
                    id = "5",
                    name = "Glow Paris Nails",
                    code = "SALON-005",
                    phone = "0977 111 222",
                    representative = "Đặng Vũ Hải",
                    address = "15 Nguyễn Đình Chiểu, Quận 3, TP.HCM",
                    regDate = "19/08/2025",
                    status = "Chờ duyệt",
                    legalDoc = "Hồ sơ xác minh căn cước & mặt bằng",
                    verifiedDate = "Chưa duyệt",
                    servicesCount = 14,
                    bookingsCount = 0,
                    rating = 0.0
                )
            )
        }
    }

    // Sample Customer Data
    val customers = remember {
        mutableStateListOf(
            AdminCustomerItem("1", "Khánh Linh", "CUST-001", "0901 888 777", "khanhlinh@glow.vn", "05/01/2025", 14, 0, "4.850.000 đ", "100.000 đ", "Đang hoạt động"),
            AdminCustomerItem("2", "Nguyễn Thị Mai", "CUST-002", "0902 333 444", "mainguyen@gmail.com", "12/02/2025", 8, 1, "2.150.000 đ", "50.000 đ", "Đang hoạt động"),
            AdminCustomerItem("3", "Trần Thảo Anh", "CUST-003", "0909 555 666", "thaoanh@outlook.com", "20/03/2025", 5, 0, "1.480.000 đ", "84.000 đ", "Đang hoạt động"),
            AdminCustomerItem("4", "Lê Minh Trang", "CUST-004", "0918 777 999", "minhtrang@gmail.com", "14/04/2025", 3, 0, "850.000 đ", "0 đ", "Đang hoạt động")
        )
    }

    // Sample Refund Requests
    val refundRequests = remember {
        mutableStateListOf(
            AdminRefundRequest("REF-101", "BK-9021", "Nguyễn Thị Mai", "L'Amour Luxury", "50.000 đ", "Khách báo hủy trước 24h theo đúng chính sách hoàn tiền 100%", "Chờ duyệt"),
            AdminRefundRequest("REF-102", "BK-8842", "Hoàng Kim Oanh", "An Yên Thảo Dược", "84.000 đ", "Salon đổi lịch đột xuất không báo trước", "Chờ duyệt")
        )
    }

    // Sample Bookings Data
    val bookings = remember {
        mutableStateListOf(
            AdminBookingItem("BK-9021", "15:30 · 12/08", "L'Amour Luxury", "Nguyễn Thị Mai", "Sơn Gel Hữu Cơ Pháp", "180.000 đ", "50.000 đ", "Chờ salon xác nhận", "Đã đặt cọc qua sàn"),
            AdminBookingItem("BK-9022", "16:00 · 12/08", "L'Amour Luxury", "Khánh Linh", "Nail Art Mắt Mèo Cực Quang", "280.000 đ", "84.000 đ", "Đã xác nhận", "Đã đặt cọc qua sàn"),
            AdminBookingItem("BK-8842", "10:00 · 11/08", "An Yên Thảo Dược", "Hoàng Kim Oanh", "Massage Thảo Dược Cung Đình", "350.000 đ", "100.000 đ", "Đã hủy", "Chờ hoàn cọc"),
            AdminBookingItem("BK-8830", "14:00 · 10/08", "L'Amour Luxury", "Trần Thảo Anh", "Đắp Móng Hoàng Gia Swarovski", "450.000 đ", "135.000 đ", "Hoàn thành", "Đã thanh toán đủ")
        )
    }

    // Search and filter states
    var shopSearchQuery by remember { mutableStateOf("") }
    var shopStatusFilter by remember { mutableStateOf("Tất cả") }
    var customerSearchQuery by remember { mutableStateOf("") }
    var bookingSearchQuery by remember { mutableStateOf("") }
    var bookingStatusFilter by remember { mutableStateOf("Tất cả") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF7E8))
            .statusBarsPadding()
    ) {
        when (selectedTab) {
            0 -> {
                // ==================== SECTION A: TRANG CHỦ ADMIN (MATCHING IMAGE 3) ====================
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card 1: Tổng quan hôm nay (Image 3)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // Header row: "Tổng quan hôm nay" & Date selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Tổng quan hôm nay",
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF24090F)
                                        )
                                        Text(
                                            text = "Cập nhật lúc 15:56 · 12/08/2025",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7A686D)
                                        )
                                    }

                                    // Filter pill
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFFFBF7E8))
                                            .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(14.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        GlowIcon(icon = GlowIcons.Calendar, tint = Color(0xFF6B1228), size = 12.dp)
                                        Text(
                                            text = "Hôm nay",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF24090F)
                                        )
                                        GlowIcon(icon = GlowIcons.ChevronDown, tint = Color(0xFF24090F), size = 10.dp)
                                    }
                                }

                                // 3 Metric Cards in 1 Row (Image 3 exact metrics - Bố cục cân đối, chống tràn chữ)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // 1. Doanh thu sàn
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFC)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE6))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFCEEEB)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.Coins, tint = Color(0xFF6B1228), size = 15.dp)
                                            }
                                            Text(
                                                text = "Doanh thu sàn",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF7A686D),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = "125.680.000đ",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF24090F),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    GlowIcon(icon = GlowIcons.TrendingUp, tint = SuccessGreen, size = 10.dp)
                                                    Text(
                                                        text = "+12%",
                                                        fontSize = 10.sp,
                                                        color = SuccessGreen,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                                Text(
                                                    text = "so với hôm qua",
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF7A686D),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }

                                    // 2. Tổng tiền cọc tạm giữ (TÁCH RIÊNG)
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFC)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE6))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFCEEEB)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.Shield, tint = Color(0xFF6B1228), size = 15.dp)
                                            }
                                            Text(
                                                text = "Tiền cọc giữ",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF7A686D),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = "28.400.000đ",
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF24090F),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Text(
                                                    text = "3 đơn",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF6B1228),
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                                Text(
                                                    text = "đang giữ cọc",
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF7A686D),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }

                                    // 3. Tổng đơn hàng
                                    Card(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDFC)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3ECE6))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFFCEEEB)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.Wallet, tint = Color(0xFF6B1228), size = 15.dp)
                                            }
                                            Text(
                                                text = "Tổng đơn hàng",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF7A686D),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Text(
                                                text = "48 đơn",
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF24090F),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    GlowIcon(icon = GlowIcons.TrendingUp, tint = SuccessGreen, size = 10.dp)
                                                    Text(
                                                        text = "+8%",
                                                        fontSize = 10.sp,
                                                        color = SuccessGreen,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        softWrap = false
                                                    )
                                                }
                                                Text(
                                                    text = "so với hôm qua",
                                                    fontSize = 8.sp,
                                                    color = Color(0xFF7A686D),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section: Việc cần xử lý (Spec A: 3 hồ sơ cửa hàng & 2 yêu cầu hoàn cọc)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(text = "Việc cần xử lý", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFFE11D48))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(text = "5 chờ xử lý", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                // Row 1: Hồ sơ cửa hàng chờ duyệt
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF9F7))
                                        .clickable { onTabChange(1) } // Navigate to Shops tab
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFCEEEB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GlowIcon(icon = GlowIcons.Store, tint = Color(0xFF6B1228), size = 18.dp)
                                        }
                                        Column {
                                            Text(text = "Hồ sơ cửa hàng chờ duyệt", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            Text(text = "Kiểm tra thông tin và giấy phép", fontSize = 11.sp, color = Color(0xFF7A686D))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFCEEEB))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "3 hồ sơ", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                    }
                                }

                                // Row 2: Yêu cầu hoàn cọc
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF9F7))
                                        .clickable {
                                            activeMoreSubView = "finance"
                                            financeActiveTab = 1
                                            onTabChange(3) // Navigate to Finance Tab
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFCEEEB)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GlowIcon(icon = GlowIcons.Coins, tint = Color(0xFF6B1228), size = 18.dp)
                                        }
                                        Column {
                                            Text(text = "Yêu cầu hoàn cọc", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            Text(text = "Kiểm tra lịch hẹn và giao dịch", fontSize = 11.sp, color = Color(0xFF7A686D))
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFFCEEEB))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "2 yêu cầu", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                    }
                                }
                            }
                        }
                    }

                    // Section: Chức năng chính (Image 3: 4 large cards in 2x2 grid)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "Chức năng chính", fontSize = 17.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                Text(text = "Quản lý dễ dàng hơn >", fontSize = 12.sp, color = Color(0xFF7A686D))
                            }

                            // Row 1: Quản lý cửa hàng & Quản lý khách hàng
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Card 1: Quản lý cửa hàng
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable { onTabChange(1) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFFCEEEB)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.Store, tint = Color(0xFF6B1228), size = 20.dp)
                                            }
                                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF8E7A80), size = 16.dp)
                                        }
                                        Column {
                                            Text(text = "Quản lý cửa hàng", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Thêm / Sửa / Xóa cửa hàng, quản lý thông tin & lịch.", fontSize = 10.sp, color = Color(0xFF7A686D), lineHeight = 13.sp)
                                        }
                                    }
                                }

                                // Card 2: Quản lý khách hàng
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable { onTabChange(2) },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFFCEEEB)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.Users, tint = Color(0xFF6B1228), size = 20.dp)
                                            }
                                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF8E7A80), size = 16.dp)
                                        }
                                        Column {
                                            Text(text = "Quản lý khách hàng", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Tra cứu, xem chi tiết, thêm / sửa / khóa khách hàng.", fontSize = 10.sp, color = Color(0xFF7A686D), lineHeight = 13.sp)
                                        }
                                    }
                                }
                            }

                            // Row 2: Doanh thu & Tổng tiền cọc tạm giữ
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Card 3: Doanh thu
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable {
                                            activeMoreSubView = "finance"
                                            financeActiveTab = 0
                                            onTabChange(3)
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFF3E8FF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.FileText, tint = Color(0xFF7E22CE), size = 20.dp)
                                            }
                                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF8E7A80), size = 16.dp)
                                        }
                                        Column {
                                            Text(text = "Doanh thu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Theo ngày / tuần / tháng. Báo cáo đối soát hoa hồng.", fontSize = 10.sp, color = Color(0xFF7A686D), lineHeight = 13.sp)
                                        }
                                    }
                                }

                                // Card 4: Tổng tiền cọc tạm giữ
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp)
                                        .clickable {
                                            activeMoreSubView = "finance"
                                            financeActiveTab = 1
                                            onTabChange(3)
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFE6F4EA)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.Coins, tint = Color(0xFF137333), size = 20.dp)
                                            }
                                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF8E7A80), size = 16.dp)
                                        }
                                        Column {
                                            Text(text = "Tổng tiền cọc tạm giữ", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(text = "Xem và quản lý các khoản cọc, duyệt hoàn tiền.", fontSize = 10.sp, color = Color(0xFF7A686D), lineHeight = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // ==================== SECTION B: QUẢN LÝ CỬA HÀNG (SPEC B) ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(text = "Quản lý Cửa Hàng (Đối tác Salon)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))

                    // Search box (tên, mã, SĐT)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(22.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowIcon(icon = GlowIcons.Search, tint = Color(0xFF7A686D), size = 18.dp)
                        Box(modifier = Modifier.weight(1f)) {
                            if (shopSearchQuery.isEmpty()) {
                                Text(text = "Tìm theo tên, mã cửa hàng, SĐT...", color = Color(0xFF8E7A80), fontSize = 12.5.sp)
                            }
                            TextField(
                                value = shopSearchQuery,
                                onValueChange = { shopSearchQuery = it },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }

                    val syncedStatuses by com.applamdep.common.AppBookingSyncBridge.shopStatusFlow.collectAsState()
                    val effectiveStatusOf: (AdminShopItem) -> String = { item ->
                        if (item.code == "SALON-001" &&
                            syncedStatuses["shop_01"] == com.applamdep.merchant.core.constants.MerchantConstants.SHOP_STATUS_CLOSED
                        ) {
                            "Đã đóng cửa"
                        } else {
                            item.status
                        }
                    }

                    // Status filters: Tất cả, Chờ duyệt, Đang hoạt động, Đã đóng cửa, Tạm khóa, Từ chối
                    val shopFilterTabs = listOf("Tất cả", "Chờ duyệt", "Đang hoạt động", "Đã đóng cửa", "Tạm khóa", "Từ chối")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(shopFilterTabs) { fTab ->
                            val isSel = fTab == shopStatusFilter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSel) Color(0xFF62202F) else Color.White)
                                    .border(1.dp, if (isSel) Color.Transparent else Color(0xFFEBDCD3), RoundedCornerShape(16.dp))
                                    .clickable { shopStatusFilter = fTab }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = fTab,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else Color(0xFF7A686D)
                                )
                            }
                        }
                    }

                    // Shops list
                    val filteredShops = shops.filter { s ->
                        val matchesSearch = shopSearchQuery.isEmpty() ||
                                s.name.contains(shopSearchQuery, ignoreCase = true) ||
                                s.code.contains(shopSearchQuery, ignoreCase = true) ||
                                s.phone.contains(shopSearchQuery)
                        val matchesFilter = when (shopStatusFilter) {
                            "Tất cả" -> true
                            else -> effectiveStatusOf(s) == shopStatusFilter
                        }
                        matchesSearch && matchesFilter
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredShops) { shop ->
                            val displayStatus = effectiveStatusOf(shop)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedShopDetail = shop },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        ) {
                                            Text(
                                                text = shop.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = Color(0xFF24090F),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Mã: ${shop.code} · Ngày ĐK: ${shop.regDate}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7A686D),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        // Status badge
                                        val (bgColor, textColor) = when (displayStatus) {
                                            "Đang hoạt động" -> Color(0xFFE6F4EA) to Color(0xFF137333)
                                            "Chờ duyệt" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                                            "Đã đóng cửa", "Tạm khóa" -> Color(0xFFFCEEEB) to Color(0xFFE11D48)
                                            else -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(bgColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = displayStatus,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFF5EFEF))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "SĐT: ${shop.phone} · ĐD: ${shop.representative}", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(text = "Hồ sơ chi tiết", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF6B1228), size = 12.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // ==================== SECTION C: QUẢN LÝ KHÁCH HÀNG (SPEC C) ====================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Quản lý Khách Hàng (Tài khoản)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        Button(
                            onClick = { showAddCustomerDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF62202F)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            GlowIcon(icon = GlowIcons.Plus, tint = Color.White, size = 12.dp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Thêm KH", fontSize = 11.sp, color = Color.White)
                        }
                    }

                    // Search box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(22.dp))
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlowIcon(icon = GlowIcons.Search, tint = Color(0xFF7A686D), size = 18.dp)
                        Box(modifier = Modifier.weight(1f)) {
                            if (customerSearchQuery.isEmpty()) {
                                Text(text = "Tìm theo tên, SĐT, mã khách hàng...", color = Color(0xFF8E7A80), fontSize = 12.5.sp)
                            }
                            TextField(
                                value = customerSearchQuery,
                                onValueChange = { customerSearchQuery = it },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true
                            )
                        }
                    }

                    // Customer list
                    val filteredCustomers = customers.filter { c ->
                        customerSearchQuery.isEmpty() ||
                                c.name.contains(customerSearchQuery, ignoreCase = true) ||
                                c.code.contains(customerSearchQuery, ignoreCase = true) ||
                                c.phone.contains(customerSearchQuery)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredCustomers) { cust ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCustomerDetail = cust },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFF9EFEF)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                GlowIcon(icon = GlowIcons.User, tint = Color(0xFF6B1228), size = 20.dp)
                                            }
                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = cust.name,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp,
                                                    color = Color(0xFF24090F),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "Mã: ${cust.code} · Ngày tham gia: ${cust.joinDate}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF7A686D),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (cust.status == "Đang hoạt động") Color(0xFFE6F4EA) else Color(0xFFFCEEEB))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = cust.status,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (cust.status == "Đang hoạt động") Color(0xFF137333) else Color(0xFFE11D48),
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Color(0xFFF5EFEF))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Số lịch hẹn: ${cust.bookingsCount} (Hủy: ${cust.cancelCount})", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(text = "Xem hồ sơ", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF6B1228), size = 12.dp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // ==================== SECTION F: THÊM (LỊCH, TÀI CHÍNH, BÁO CÁO, CÀI ĐẶT) ====================
                if (activeMoreSubView == "bookings") {
                    // SUB-VIEW: QUẢN LÝ ĐƠN ĐẶT LỊCH (SECTION D)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { activeMoreSubView = null }) {
                                GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color(0xFF24090F), size = 20.dp)
                            }
                            Text(text = "Quản lý Đơn Đặt Lịch", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }

                        // Search
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.White)
                                .border(1.dp, Color(0xFFEBDCD3), RoundedCornerShape(22.dp))
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlowIcon(icon = GlowIcons.Search, tint = Color(0xFF7A686D), size = 18.dp)
                            Box(modifier = Modifier.weight(1f)) {
                                if (bookingSearchQuery.isEmpty()) {
                                    Text(text = "Tìm theo mã lịch, tên khách, salon...", color = Color(0xFF8E7A80), fontSize = 12.5.sp)
                                }
                                TextField(
                                    value = bookingSearchQuery,
                                    onValueChange = { bookingSearchQuery = it },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                            }
                        }

                        // Bookings list
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(bookings) { bk ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedBookingDetail = bk },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${bk.code} · ${bk.time}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp,
                                                color = Color(0xFF24090F),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFFCEEEB))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = bk.status,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF6B1228),
                                                    maxLines = 1,
                                                    softWrap = false
                                                )
                                            }
                                        }
                                        Text(text = "${bk.salon} → ${bk.customer}", fontSize = 12.sp, color = Color(0xFF7A686D))
                                        Text(text = "Dịch vụ: ${bk.service}", fontSize = 11.5.sp, color = Color(0xFF24090F))
                                        HorizontalDivider(color = Color(0xFFF5EFEF))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = "Tổng: ${bk.value} (Cọc: ${bk.deposit})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                            Text(text = bk.paymentStatus, fontSize = 11.sp, color = Color(0xFF137333), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activeMoreSubView == "finance") {
                    // SUB-VIEW: TÀI CHÍNH (SECTION E: 2 TABS - DOANH THU SÀN & TIỀN CỌC)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { activeMoreSubView = null }) {
                                GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color(0xFF24090F), size = 20.dp)
                            }
                            Text(text = "Tài chính — Doanh thu & Tiền cọc", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }

                        // 2 Tabs: Doanh thu sàn | Tiền cọc
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFEBDCD3))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (financeActiveTab == 0) Color.White else Color.Transparent)
                                    .clickable { financeActiveTab = 0 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Doanh thu sàn",
                                    fontSize = 13.sp,
                                    fontWeight = if (financeActiveTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (financeActiveTab == 0) Color(0xFF6B1228) else Color(0xFF7A686D)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (financeActiveTab == 1) Color.White else Color.Transparent)
                                    .clickable { financeActiveTab = 1 }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tiền cọc tạm giữ",
                                    fontSize = 13.sp,
                                    fontWeight = if (financeActiveTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (financeActiveTab == 1) Color(0xFF6B1228) else Color(0xFF7A686D)
                                )
                            }
                        }

                        if (financeActiveTab == 0) {
                            // Tab 1: Doanh thu sàn
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "Báo cáo doanh thu phí sàn", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                    Text(text = "Bộ lọc: Hôm nay · 7 ngày · Tháng này · Tùy chỉnh", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    HorizontalDivider(color = Color(0xFFF5EFEF))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Tổng phí/hoa hồng sàn thu:", fontSize = 12.sp, color = Color(0xFF7A686D))
                                        Text(text = "125.680.000 ₫", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Giao dịch thành công:", fontSize = 12.sp, color = Color(0xFF7A686D))
                                        Text(text = "348 giao dịch", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Phí hoàn trả/điều chỉnh:", fontSize = 12.sp, color = Color(0xFF7A686D))
                                        Text(text = "- 1.250.000 ₫", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Doanh thu ròng sàn:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                        Text(text = "124.430.000 ₫", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                                    }
                                    Button(
                                        onClick = { },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF62202F)),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(text = "Xuất báo cáo đối soát Excel", fontSize = 12.sp, color = Color.White)
                                    }
                                }
                            }
                        } else {
                            // Tab 2: Tiền cọc tạm giữ & Phê duyệt hoàn cọc
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(text = "Quỹ tiền cọc đang tạm giữ", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                    Text(text = "Tách riêng hoàn toàn, không tính vào doanh thu sàn", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = "Tổng tiền cọc đang giữ:", fontSize = 12.sp, color = Color(0xFF7A686D))
                                        Text(text = "28.400.000 ₫", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                                    }
                                    Text(text = "3 khoản cọc hợp lệ chờ hoàn thành dịch vụ", fontSize = 11.sp, color = Color(0xFF137333))
                                }
                            }

                            Text(text = "Yêu cầu hoàn cọc chờ xử lý (2)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(refundRequests) { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = "${req.id} · ${req.bookingCode}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF24090F))
                                                Text(text = req.depositAmount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF6B1228))
                                            }
                                            Text(text = "Khách hàng: ${req.customer} · Salon: ${req.salon}", fontSize = 12.sp, color = Color(0xFF7A686D))
                                            Text(text = "Lý do: ${req.reason}", fontSize = 11.5.sp, color = Color(0xFF24090F))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { req.status = "Đã duyệt hoàn" },
                                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Text(text = if (req.status == "Đã duyệt hoàn") "Đã duyệt ✓" else "Duyệt hoàn cọc", fontSize = 11.5.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = { req.status = "Từ chối" },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Text(text = "Từ chối", fontSize = 11.5.sp, color = Color(0xFFE11D48))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activeMoreSubView == "violations") {
                    // ==================== SUB-VIEW: ĐÁNH GIÁ & BÁO CÁO VI PHẠM ====================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { activeMoreSubView = null }) {
                                GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color(0xFF24090F), size = 20.dp)
                            }
                            Text(text = "Đánh giá & Báo cáo Vi phạm", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }

                        // Metric Cards
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Chờ xử lý", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    Text(text = "${violationReports.count { it.status == "Chờ xử lý" }} báo cáo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE11D48))
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Đã gỡ bỏ", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    Text(text = "18 đánh giá", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                                }
                            }
                        }

                        Text(text = "Danh sách báo cáo khiếu nại (${violationReports.size})", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(violationReports) { rep ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                GlowIcon(icon = GlowIcons.Alert, tint = if (rep.status == "Chờ xử lý") Color(0xFFE11D48) else Color(0xFF7A686D), size = 16.dp)
                                                Text(text = rep.id, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF24090F))
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (rep.status == "Chờ xử lý") Color(0xFFFCEEEB) else Color(0xFFE8F5E9))
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = rep.status,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (rep.status == "Chờ xử lý") Color(0xFFE11D48) else Color(0xFF137333)
                                                )
                                            }
                                        }

                                        Text(text = rep.title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                        Text(text = "Cơ sở: ${rep.target} • Người gửi: ${rep.reporter} • ${rep.time}", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                                        Text(text = rep.content, fontSize = 12.sp, color = Color(0xFF4A3E42), lineHeight = 16.sp)

                                        if (rep.status == "Chờ xử lý") {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        rep.status = "Đã gỡ bỏ"
                                                        android.widget.Toast.makeText(context, "Đã gỡ bỏ đánh giá vi phạm và gửi cảnh cáo!", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228)),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Text(text = "Gỡ bỏ đánh giá", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        rep.status = "Đã bác bỏ"
                                                        android.widget.Toast.makeText(context, "Đã bác bỏ khiếu nại (Đánh giá hợp lệ)!", android.widget.Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(vertical = 4.dp)
                                                ) {
                                                    Text(text = "Bác khiếu nại", fontSize = 11.5.sp, color = Color(0xFF7A686D))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activeMoreSubView == "notifications") {
                    // ==================== SUB-VIEW: THÔNG BÁO HỆ THỐNG ====================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { activeMoreSubView = null }) {
                                    GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color(0xFF24090F), size = 20.dp)
                                }
                                Text(text = "Thông báo Hệ thống", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                            }
                            TextButton(
                                onClick = {
                                    adminNotifications.forEach { it.isRead = true }
                                    android.widget.Toast.makeText(context, "Đã đánh dấu đọc tất cả!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text(text = "Đọc tất cả", fontSize = 12.sp, color = Color(0xFF6B1228), fontWeight = FontWeight.Bold)
                            }
                        }

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(adminNotifications) { notif ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { notif.isRead = true },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (notif.isRead) Color.White else Color(0xFFFFF9F5)
                                    ),
                                    border = if (!notif.isRead) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3CEC5)) else null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (notif.type) {
                                                        "SHOP" -> Color(0xFFE8F5E9)
                                                        "PAYMENT" -> Color(0xFFFFF7ED)
                                                        "ALERT" -> Color(0xFFFCEEEB)
                                                        else -> Color(0xFFF3F4F6)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GlowIcon(
                                                icon = when (notif.type) {
                                                    "SHOP" -> GlowIcons.Store
                                                    "PAYMENT" -> GlowIcons.Coins
                                                    "ALERT" -> GlowIcons.Alert
                                                    else -> GlowIcons.Bell
                                                },
                                                tint = when (notif.type) {
                                                    "SHOP" -> Color(0xFF137333)
                                                    "PAYMENT" -> Color(0xFFB45309)
                                                    "ALERT" -> Color(0xFFE11D48)
                                                    else -> Color(0xFF6B1228)
                                                },
                                                size = 18.dp
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF24090F))
                                                if (!notif.isRead) {
                                                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFE11D48)))
                                                }
                                            }
                                            Text(text = notif.subtitle, fontSize = 12.sp, color = Color(0xFF6B5C60), lineHeight = 16.sp)
                                            Text(text = notif.time, fontSize = 10.5.sp, color = Color(0xFF9E8E92))
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (activeMoreSubView == "accounts") {
                    // ==================== SUB-VIEW: TÀI KHOẢN & PHÂN QUYỀN ====================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(onClick = { activeMoreSubView = null }) {
                                    GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color(0xFF24090F), size = 20.dp)
                                }
                                Text(text = "Tài khoản & Phân quyền", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                            }

                            Button(
                                onClick = { showAddStaffDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "+ Thêm nhân sự", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Text(text = "Danh sách quản trị viên & phân quyền (${staffAccounts.size})", fontSize = 13.5.sp, color = Color(0xFF7A686D))

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(staffAccounts) { staff ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFCEEEB)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = staff.name.firstOrNull()?.toString() ?: "A",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF6B1228)
                                                    )
                                                }
                                                Column {
                                                    Text(text = staff.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Color(0xFF24090F))
                                                    Text(text = staff.email, fontSize = 11.5.sp, color = Color(0xFF7A686D))
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color(0xFFE8F5E9))
                                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                                            ) {
                                                Text(text = staff.status, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF5EFEF))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Vai trò: ${staff.roleTitle}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF6B1228)
                                            )
                                            TextButton(
                                                onClick = {
                                                    staff.status = if (staff.status == "Hoạt động") "Tạm khóa" else "Hoạt động"
                                                    android.widget.Toast.makeText(context, "Đã cập nhật trạng thái: ${staff.status}", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Text(
                                                    text = if (staff.status == "Hoạt động") "Tạm khóa" else "Kích hoạt",
                                                    fontSize = 11.sp,
                                                    color = if (staff.status == "Hoạt động") Color(0xFFE11D48) else Color(0xFF137333)
                                                )
                                            }
                                        }

                                        Text(text = "Quyền hạn: ${staff.permissions}", fontSize = 11.5.sp, color = Color(0xFF6B5C60), lineHeight = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                } else if (activeMoreSubView == "settings") {
                    // ==================== SUB-VIEW: CÀI ĐẶT HỆ THỐNG ====================
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { activeMoreSubView = null }) {
                                GlowIcon(icon = GlowIcons.ChevronLeft, tint = Color(0xFF24090F), size = 20.dp)
                            }
                            Text(text = "Cài đặt Hệ thống Sàn", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }

                        // KHỐI 1: CẤU HÌNH PHÍ SÀN & HOA HỒNG
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Hoa hồng nền tảng & Gói hội viên", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))

                                OutlinedTextField(
                                    value = commissionRate,
                                    onValueChange = { commissionRate = it },
                                    label = { Text("Mức phí hoa hồng thu trên mỗi đơn hoàn tất (%)") },
                                    suffix = { Text("%", fontWeight = FontWeight.Bold, color = Color(0xFF6B1228)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = vipMonthlyFee,
                                    onValueChange = { vipMonthlyFee = it },
                                    label = { Text("Phí duy trì gian hàng VIP Sponsor (₫/tháng)") },
                                    suffix = { Text("₫/tháng", fontWeight = FontWeight.Bold, color = Color(0xFF6B1228)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        // KHỐI 2: CHÍNH SÁCH ĐẶT CỌC & HỦY HOÀN
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Chính sách Đặt cọc & Hoàn tiền (Escrow)", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))

                                OutlinedTextField(
                                    value = refundDeadlineHours,
                                    onValueChange = { refundDeadlineHours = it },
                                    label = { Text("Thời hạn cho phép khách hủy hoàn cọc 100% (Giờ)") },
                                    suffix = { Text("giờ trước", color = Color(0xFF7A686D)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = autoCompleteMinutes,
                                    onValueChange = { autoCompleteMinutes = it },
                                    label = { Text("Tự động chuyển hoàn thành sau khi hết giờ dịch vụ") },
                                    suffix = { Text("phút", color = Color(0xFF7A686D)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        // KHỐI 3: TỰ ĐỘNG HÓA & THÔNG BÁO
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(text = "Tự động hóa & Cảnh báo", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Nhắc lịch hẹn khách trước 30 phút", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "Tự động gửi thông báo nhắc khách đúng giờ", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    }
                                    Switch(
                                        checked = enableSmsReminder,
                                        onCheckedChange = { enableSmsReminder = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6B1228), checkedTrackColor = Color(0xFFF3CEC5))
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFF5EFEF))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Báo Admin khi có Salon mới đăng ký", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "Nhận chuông thông báo ngay khi có tài khoản đối tác", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    }
                                    Switch(
                                        checked = notifyOnNewShop,
                                        onCheckedChange = { notifyOnNewShop = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6B1228), checkedTrackColor = Color(0xFFF3CEC5))
                                    )
                                }

                                HorizontalDivider(color = Color(0xFFF5EFEF))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "Tự động duyệt đối tác có GPKD hợp lệ", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                        Text(text = "Tự động cấp quyền hoạt động ngay sau khi đối chiếu giấy tờ", fontSize = 11.sp, color = Color(0xFF7A686D))
                                    }
                                    Switch(
                                        checked = autoApproveDocShop,
                                        onCheckedChange = { autoApproveDocShop = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF6B1228), checkedTrackColor = Color(0xFFF3CEC5))
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                android.widget.Toast.makeText(context, "Đã lưu cài đặt cấu hình sàn thành công!", android.widget.Toast.LENGTH_LONG).show()
                                activeMoreSubView = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(text = "Lưu Cài Đặt Hệ Thống", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // MAIN "THÊM" MENU (SPEC F & 4 NAVIGATION SHORTCUTS)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(text = "Chức năng Mở rộng (Admin Hub)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                        }

                        // Shortcut 1: Quản lý đơn đặt lịch
                        item {
                            AdminMoreMenuCard(
                                title = "Quản lý Đơn Đặt Lịch",
                                subtitle = "Tra cứu mã lịch, đối soát trạng thái cọc và thanh toán",
                                icon = GlowIcons.Calendar,
                                onClick = { activeMoreSubView = "bookings" }
                            )
                        }

                        // Shortcut 2: Tài chính & Tiền cọc
                        item {
                            AdminMoreMenuCard(
                                title = "Tài chính — Doanh thu và Tiền cọc",
                                subtitle = "Phí sàn 125,68tr, kiểm soát 28,4tr tiền cọc đang giữ",
                                icon = GlowIcons.Coins,
                                onClick = { activeMoreSubView = "finance" }
                            )
                        }

                        // Shortcut 3: Đánh giá & Báo cáo vi phạm
                        item {
                            AdminMoreMenuCard(
                                title = "Đánh giá & Báo cáo vi phạm",
                                subtitle = "Duyệt báo cáo spam, nội dung không phù hợp và xử lý khiếu nại",
                                icon = GlowIcons.Alert,
                                onClick = { activeMoreSubView = "violations" }
                            )
                        }

                        // Shortcut 4: Thông báo hệ thống
                        item {
                            AdminMoreMenuCard(
                                title = "Thông báo hệ thống",
                                subtitle = "Hồ sơ đối tác mới, giao dịch bất thường cần xử lý",
                                icon = GlowIcons.Bell,
                                onClick = { activeMoreSubView = "notifications" }
                            )
                        }

                        // Shortcut 5: Tài khoản & Phân quyền
                        item {
                            AdminMoreMenuCard(
                                title = "Tài khoản & Phân quyền",
                                subtitle = "Quản lý tài khoản nhân viên quản trị, vai trò và quyền truy cập",
                                icon = GlowIcons.Shield,
                                onClick = { activeMoreSubView = "accounts" }
                            )
                        }

                        // Shortcut 6: Cài đặt hệ thống
                        item {
                            AdminMoreMenuCard(
                                title = "Cài đặt hệ thống",
                                subtitle = "Cấu hình hoa hồng nền tảng, chính sách hủy & hoàn cọc",
                                icon = GlowIcons.Settings,
                                onClick = { activeMoreSubView = "settings" }
                            )
                        }

                        // Shortcut 7: Đăng xuất
                        item {
                            AdminMoreMenuCard(
                                title = "Đăng xuất tài khoản Admin",
                                subtitle = "Thoát phiên làm việc quản trị và quay về màn hình đăng nhập",
                                icon = Icons.AutoMirrored.Filled.Logout,
                                onClick = { showAdminLogoutConfirm = true }
                            )
                        }
                    }
                }
            }
        }

        // ==================== DETAIL MODALS (SPEC B: 4 NHÓM HỒ SƠ SALON) ====================
        if (selectedShopDetail != null) {
            val shop = selectedShopDetail!!
            val detailSyncedStatuses by com.applamdep.common.AppBookingSyncBridge.shopStatusFlow.collectAsState()
            val detailStatus = if (shop.code == "SALON-001" &&
                detailSyncedStatuses["shop_01"] == com.applamdep.merchant.core.constants.MerchantConstants.SHOP_STATUS_CLOSED
            ) {
                "Đã đóng cửa"
            } else {
                shop.status
            }
            val (detailBgColor, detailTextColor) = when (detailStatus) {
                "Đang hoạt động" -> Color(0xFFE6F4EA) to Color(0xFF137333)
                "Chờ duyệt" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                "Đã đóng cửa", "Tạm khóa" -> Color(0xFFFCEEEB) to Color(0xFFE11D48)
                else -> Color(0xFFF3F4F6) to Color(0xFF6B7280)
            }
            ModalBottomSheet(
                onDismissRequest = { selectedShopDetail = null },
                containerColor = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(text = shop.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                Text(text = "Mã cơ sở: ${shop.code}", fontSize = 12.sp, color = Color(0xFF7A686D))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(detailBgColor)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = detailStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = detailTextColor)
                            }
                        }
                    }

                    item { HorizontalDivider(color = Color(0xFFF5EFEF)) }

                    // Nhóm 1: Thông tin chung
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "1. THÔNG TIN CHUNG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Người đại diện: ${shop.representative}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Số điện thoại: ${shop.phone}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Địa chỉ hoạt động: ${shop.address}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Nhóm 2: Hồ sơ pháp lý
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "2. HỒ SƠ PHÁP LÝ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Giấy phép KD: ${shop.legalDoc}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Trạng thái xác minh: Đã kiểm tra CCCD & Hợp đồng thuê", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Ngày phê duyệt: ${shop.verifiedDate}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Nhóm 3: Dịch vụ
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "3. DỊCH VỤ & BẢNG GIÁ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Tổng số dịch vụ niêm yết: ${shop.servicesCount} dịch vụ", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Khung giá: 180.000 đ – 450.000 đ", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Thiết lập cọc: Mở nhận cọc từ 50k", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Nhóm 4: Hoạt động
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "4. HOẠT ĐỘNG VÀ ĐỐI SOÁT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Tổng lượt booking hoàn thành: ${shop.bookingsCount}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Đánh giá trung bình: ${shop.rating} ★ (1.8k reviews)", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Lịch sử vi phạm: ${shop.violationHistory}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Thao tác quản trị (Duyệt, Khóa, Từ chối)
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (shop.status == "Chờ duyệt") {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            shop.status = "Đang hoạt động"
                                            userStorage.approveMerchant(shop.representative)
                                            selectedShopDetail = null
                                            Toast.makeText(context, "🎉 Đã duyệt tài khoản Cửa hàng (${shop.representative}) thành công!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Duyệt hồ sơ mở sàn")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            shop.status = "Từ chối"
                                            userStorage.rejectMerchant(shop.representative)
                                            selectedShopDetail = null
                                            Toast.makeText(context, "Đã từ chối hồ sơ (${shop.representative})", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Từ chối hồ sơ", color = Color(0xFFE11D48))
                                    }
                                }
                            } else if (detailStatus == "Đã đóng cửa") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFCEEEB))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Cửa hàng hiện đang tạm đóng cửa theo thiết lập từ đối tác.",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE11D48)
                                    )
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            shop.status = if (shop.status == "Đang hoạt động") "Tạm khóa" else "Đang hoạt động"
                                            selectedShopDetail = null
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (shop.status == "Đang hoạt động") Color(0xFFE11D48) else SuccessGreen
                                        ),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(if (shop.status == "Đang hoạt động") "Tạm khóa gian hàng" else "Mở khóa gian hàng")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== DETAIL MODALS (SPEC C: 4 NHÓM HỒ SƠ KHÁCH HÀNG) ====================
        if (selectedCustomerDetail != null) {
            val cust = selectedCustomerDetail!!
            ModalBottomSheet(
                onDismissRequest = { selectedCustomerDetail = null },
                containerColor = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = cust.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                                Text(text = "Mã KH: ${cust.code}", fontSize = 12.sp, color = Color(0xFF7A686D))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE6F4EA))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = cust.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF137333))
                            }
                        }
                    }

                    item { HorizontalDivider(color = Color(0xFFF5EFEF)) }

                    // Nhóm 1: Thông tin tài khoản & liên hệ
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "1. THÔNG TIN LIÊN HỆ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Số điện thoại: ${cust.phone}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Email: ${cust.email}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Ngày đăng ký tài khoản: ${cust.joinDate}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Nhóm 2: Lịch sử đặt lịch & hủy
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "2. LỊCH SỬ ĐẶT LỊCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Tổng số đơn đã đặt: ${cust.bookingsCount} lịch hẹn", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Số lần hủy lịch: ${cust.cancelCount} lần (Hủy trước 24h hợp lệ)", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Nhóm 3: Thanh toán, tiền cọc & hoàn tiền
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "3. LỊCH SỬ THANH TOÁN VÀ TIỀN CỌC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B1228))
                            Text(text = "• Tổng chi tiêu tích lũy: ${cust.totalSpent}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                            Text(text = "• Tiền cọc đang tạm giữ: ${cust.depositHeld}", fontSize = 12.5.sp, color = Color(0xFF24090F))
                        }
                    }

                    // Thao tác: Khóa / Mở khóa tài khoản
                    item {
                        Button(
                            onClick = {
                                cust.status = if (cust.status == "Đang hoạt động") "Tạm khóa" else "Đang hoạt động"
                                selectedCustomerDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (cust.status == "Đang hoạt động") Color(0xFFE11D48) else SuccessGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (cust.status == "Đang hoạt động") "Khóa tài khoản khách hàng" else "Mở khóa tài khoản")
                        }
                    }
                }
            }
        }
    }

    // DIALOG XÁC NHẬN ĐĂNG XUẤT ADMIN
    if (showAdminLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showAdminLogoutConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Đăng xuất Admin",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF24090F)
                )
            },
            text = {
                Text(
                    text = "Bạn có chắc chắn muốn đăng xuất khỏi phiên làm việc Quản trị viên và quay về màn hình đăng nhập?",
                    fontSize = 14.sp,
                    color = Color(0xFF7A686D)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdminLogoutConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Đăng xuất", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminLogoutConfirm = false }) {
                    Text("Hủy", color = Color(0xFF7A686D))
                }
            }
        )
    }

    // DIALOG THÊM NHÂN SỰ ADMIN
    if (showAddStaffDialog) {
        var newStaffName by remember { mutableStateOf("") }
        var newStaffEmail by remember { mutableStateOf("") }
        var newStaffRole by remember { mutableStateOf("Content Moderator") }

        AlertDialog(
            onDismissRequest = { showAddStaffDialog = false },
            title = {
                Text(text = "Thêm nhân sự quản trị mới", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newStaffName,
                        onValueChange = { newStaffName = it },
                        label = { Text("Họ và tên nhân sự") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStaffEmail,
                        onValueChange = { newStaffEmail = it },
                        label = { Text("Địa chỉ Gmail nhân viên") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStaffRole,
                        onValueChange = { newStaffRole = it },
                        label = { Text("Vai trò / Chức vụ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newStaffName.isNotBlank() && newStaffEmail.isNotBlank()) {
                            staffAccounts.add(
                                AdminStaffAccount(
                                    id = "STAFF-0${staffAccounts.size + 1}",
                                    name = newStaffName.trim(),
                                    email = newStaffEmail.trim(),
                                    roleTitle = newStaffRole.trim(),
                                    permissions = "Quyền thao tác nghiệp vụ theo vai trò $newStaffRole"
                                )
                            )
                            showAddStaffDialog = false
                            android.widget.Toast.makeText(context, "Đã thêm nhân sự $newStaffName thành công!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B1228))
                ) {
                    Text("Tạo tài khoản")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStaffDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

}

@Composable
private fun AdminMoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFCEEEB)),
                    contentAlignment = Alignment.Center
                ) {
                    GlowIcon(icon = icon, tint = Color(0xFF6B1228), size = 20.dp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF24090F))
                    Text(text = subtitle, fontSize = 11.5.sp, color = Color(0xFF7A686D), lineHeight = 15.sp)
                }
            }
            GlowIcon(icon = GlowIcons.ChevronRight, tint = Color(0xFF7A686D), size = 16.dp)
        }
    }
}
