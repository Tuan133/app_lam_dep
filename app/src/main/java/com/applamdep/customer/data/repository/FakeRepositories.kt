package com.applamdep.customer.data.repository

import com.applamdep.common.BookingSyncGateway
import com.applamdep.customer.core.base.BaseRepository
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.core.network.FirebaseApiClient
import com.applamdep.customer.domain.model.*
import com.applamdep.customer.domain.repository.BookingRepository
import com.applamdep.customer.domain.repository.ReviewRepository
import com.applamdep.customer.domain.repository.ShopRepository
import com.applamdep.customer.domain.repository.WalletRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Kế thừa BaseRepository)
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Cài đặt các Repository Interface)
 *
 * Cung cấp dữ liệu 11 Cửa hàng từ Firebase Realtime Database:
 * Endpoint: https://applamdep-default-rtdb.asia-southeast1.firebasedatabase.app/salons.json
 * - Đồng bộ trực tiếp từ Firebase RTDB
 * - 11 Salon thực tế tại Đà Nẵng (Hải Châu, Sơn Trà, Cẩm Lệ, Ngũ Hành Sơn)
 * - Tích hợp đầy đủ dịch vụ, khung giờ, liên kết mạng xã hội (Facebook, Instagram, TikTok, Website)
 */
class FakeShopRepository(
    private val context: android.content.Context? = null
) : BaseRepository(), ShopRepository {

    private val localStorage = context?.let { com.applamdep.customer.data.local.ShopLocalStorage(it) }
    private val sampleShops = com.applamdep.customer.data.local.ShopLocalStorage.getExcelSalons()
    companion object {
        val sampleServices = listOf(
        // salon_01: 1994 Nailsroom
        ServiceItem("srv_01", "salon_01", "Sơn Gel Thạch Hàn Quốc + Cắt Da", "Sơn gel cao cấp kèm tẩy da chết và dưỡng ẩm móng", 160000L, 45, "Làm Nail"),
        ServiceItem("srv_02", "salon_01", "Nail Art Mắt Mèo Cực Quang VIP", "Hiệu ứng mắt mèo đổi màu kết hợp đính đá Swarovski", 250000L, 60, "Làm Nail"),
        ServiceItem("srv_03", "salon_01", "Nối Mi Lụa Thiết Kế Tự Nhiên", "Mi lụa mềm tự nhiên không cộm, không cay mắt", 220000L, 50, "Nối Mi"),
        ServiceItem("srv_04", "salon_01", "Chăm Sóc & Dưỡng Móng Organic", "Cắt da, ủ dưỡng móng chuyên sâu collagen", 120000L, 30, "Làm Nail"),

        // salon_02: BONNIE NAIL
        ServiceItem("srv_05", "salon_02", "Vẽ Móng Nghệ Thuật Ombre Pastel", "Vẽ cọ nét hoa văn ombre chuyển màu tự nhiên", 180000L, 45, "Làm Nail"),
        ServiceItem("srv_06", "salon_02", "Đắp Móng Gel Form Hạnh Nhân", "Đắp móng gel bền đẹp kèm tạo form thanh thoát", 280000L, 60, "Làm Nail"),
        ServiceItem("srv_07", "salon_02", "Tháo Móng & Dưỡng Keratin", "Tháo móng nhẹ nhàng không xước, phục hồi keratin", 100000L, 30, "Làm Nail"),

        // salon_03: GÀ SPA
        ServiceItem("srv_08", "salon_03", "Lấy Nhân Mụn Y Khoa Chuẩn Da Liễu", "Lấy sạch nhân mụn tăm bông vô trùng không để lại sẹo", 250000L, 60, "Spa Trị Liệu"),
        ServiceItem("srv_09", "salon_03", "Điện Di Vitamin C Sáng Mịn Da", "Đưa tinh chất vitamin C sâu vào biểu bì mờ thâm", 300000L, 45, "Chăm sóc da"),
        ServiceItem("srv_10", "salon_03", "Thải Độc Da CO2 Thải Chì Kháng Khuẩn", "Mặt nạ CO2 thải độc da kích thích tăng sinh tế bào", 350000L, 60, "Spa Trị Liệu"),

        // salon_04: HIEN HAIR STUDIO
        ServiceItem("srv_11", "salon_04", "Cắt Tạo Kiểu Layer / Wolf Cut Chuẩn Form", "Stylist tư vấn dáng mặt và cắt tạo kiểu thời thượng", 150000L, 40, "Hair Salon"),
        ServiceItem("srv_12", "salon_04", "Uốn Sóng Lơi Hàn Quốc Bồng Bềnh", "Uốn sóng lơi tự nhiên giữ nếp bền lâu", 450000L, 90, "Hair Salon"),
        ServiceItem("srv_13", "salon_04", "Nhuộm Nâu Tây / Balayage Tôn Da", "Nhuộm màu chuẩn tone không tẩy tóc hư tổn", 550000L, 90, "Hair Salon"),
        ServiceItem("srv_14", "salon_04", "Phục Hồi Tóc Keratin Chuyên Sâu", "Ủ nhiệt phục hồi các sợi tóc khô xơ chẻ ngọn", 350000L, 60, "Hair Salon"),

        // salon_05: KỲ DUYÊN TRỊ MỤN
        ServiceItem("srv_15", "salon_05", "Trị Mụn Thảo Dược Đông Y Tận Gốc", "Liệu trình thuốc nam đông y lành tính kháng khuẩn", 200000L, 60, "Spa Trị Liệu"),
        ServiceItem("srv_16", "salon_05", "Trị Thâm Sẹo Tế Bào Gốc Phục Hồi", "Cấy tinh chất tế bào gốc làm đầy vùng sẹo rỗ", 400000L, 75, "Spa Trị Liệu"),

        // salon_06: MNH THUY MAKEUP
        ServiceItem("srv_17", "salon_06", "Trang Điểm Cô Dâu Đón Dâu & Tiệc Cưới", "Trang điểm cô dâu tone trong trẻo kèm tạo kiểu tóc", 600000L, 75, "Makeup Tiệc"),
        ServiceItem("srv_18", "salon_06", "Trang Điểm & Làm Tóc Dạ Hội Cao Cấp", "Tone Tây / Douyin tôn đường nét gương mặt", 300000L, 50, "Makeup Tiệc"),
        ServiceItem("srv_19", "salon_06", "Makeup Chụp Ảnh Ngoại Cảnh / Kỷ Yếu", "Giữ lớp nền lâu trôi suốt cả ngày chụp ảnh", 250000L, 45, "Makeup Tiệc"),

        // salon_07: Nails 1996
        ServiceItem("srv_20", "salon_07", "Sơn Gel Thiết Kế Phong Cách Châu Âu", "Tone màu nude và gel kim tuyến sang chảnh", 220000L, 50, "Làm Nail"),
        ServiceItem("srv_21", "salon_07", "Đắp Bột Móng Nghệ Thuật Đính Đá", "Kỹ thuật đắp bột chắc chắn, đính đá tỉ mỉ", 350000L, 75, "Làm Nail"),

        // salon_08: SI MÊ BEAUTY
        ServiceItem("srv_22", "salon_08", "Chăm Sóc Da Căng Bóng Thủy Điểm", "Massage nâng cơ và cấp ẩm sâu cho da", 320000L, 60, "Chăm sóc da"),
        ServiceItem("srv_23", "salon_08", "Cấy Collagen Tươi Trẻ Hóa Tầng Sâu", "Bổ sung collagen tươi đàn hồi và săn chắc", 450000L, 75, "Chăm sóc da"),
        ServiceItem("srv_24", "salon_08", "Triệt Lông Vĩnh Viễn Diode Laser", "Công nghệ triệt lạnh không đau rát", 180000L, 30, "Chăm sóc da"),

        // salon_09: Thì Là SPA
        ServiceItem("srv_25", "salon_09", "Gội Đầu Dưỡng Sinh Thảo Dược 7 Bước", "Nước gội bồ kết cô đặc, bấm huyệt cổ vai gáy", 150000L, 45, "Dưỡng sinh"),
        ServiceItem("srv_26", "salon_09", "Massage Trị Liệu Cổ Vai Gáy Chuyên Sâu", "Đả thông kinh lạc, giải tỏa căng cứng mỏi cơ", 250000L, 50, "Dưỡng sinh"),
        ServiceItem("srv_27", "salon_09", "Massage Body Đá Nóng & Ngâm Chân Thảo Mộc", "Thư giãn các khối cơ, hồi phục năng lượng toàn thân", 380000L, 75, "Dưỡng sinh"),

        // salon_10: Tun Beauty Salon & Spa
        ServiceItem("srv_28", "salon_10", "Cắt Tóc & Gội Massage Thư Giãn", "Cắt tóc tạo kiểu kèm gội đầu thư giãn", 120000L, 40, "Hair Salon"),
        ServiceItem("srv_29", "salon_10", "Uốn Nhuộm Combo Chăm Sóc Tóc Cao Cấp", "Combo uốn nhuộm phục hồi trọn gói", 480000L, 90, "Hair Salon"),

        // salon_11: 1m65.nail
        ServiceItem("srv_30", "salon_11", "Vẽ Hoạt Hình Anime & Gắn Charm Theo Yêu Cầu", "Vẽ móng nhân vật anime siêu chi tiết kèm charm xinh", 220000L, 60, "Làm Nail"),
        ServiceItem("srv_31", "salon_11", "Sơn Gel Thạch Trong Trẻo Kiểu Nhật", "Màu sắc pastel trong trẻo tự nhiên chuẩn Nhật", 180000L, 45, "Làm Nail"),
        ServiceItem("srv_32", "salon_11", "Nail Box Thiết Kế Thủ Công Theo Size Tay", "Mẫu nail box thiết kế đo ni vừa vặn tiện lợi", 150000L, 30, "Làm Nail")
    )
    }

    private val shopsFlow = MutableStateFlow(localStorage?.loadOrInitializeShops() ?: sampleShops)

    init {
        // Đồng bộ dữ liệu mới nhất từ Firebase Realtime Database
        fetchSalonsFromFirebase()
    }

    private fun fetchSalonsFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remoteSalons = FirebaseApiClient.salonService.getSalons()
                if (remoteSalons.isNotEmpty()) {
                    val currentList = shopsFlow.value
                    val updated = currentList.map { shop ->
                        val remoteDto = remoteSalons[shop.id]
                        if (remoteDto != null) {
                            shop.copy(
                                name = remoteDto.name?.takeIf { it.isNotBlank() } ?: shop.name,
                                address = if (remoteDto.address.isNullOrBlank() || remoteDto.address.contains("Đang cập nhật", ignoreCase = true)) {
                                    shop.address
                                } else {
                                    remoteDto.address
                                },
                                phone = remoteDto.phone?.takeIf { it.isNotBlank() } ?: shop.phone,
                                openingHours = remoteDto.hours?.takeIf { it.isNotBlank() } ?: shop.openingHours,
                                facebook = remoteDto.facebook ?: shop.facebook,
                                instagram = remoteDto.instagram ?: shop.instagram,
                                tiktok = remoteDto.tiktok ?: shop.tiktok,
                                website = remoteDto.website ?: shop.website
                            )
                        } else {
                            shop
                        }
                    }
                    shopsFlow.value = updated
                    localStorage?.saveShopsToDevice(updated)
                }
            } catch (e: Exception) {
                // Sử dụng dữ liệu cục bộ 11 salon nếu không có kết nối internet
            }
        }
    }

    override fun getApprovedShops(): Flow<List<Shop>> {
        return shopsFlow.map { list ->
            // Lấy shop APPROVED hoặc CLOSED (để hiển thị rõ trạng thái Đã đóng cửa) và còn hạn, ưu tiên VIP lên đầu
            list.filter {
                (it.status == AppConstants.SHOP_STATUS_APPROVED || it.status == AppConstants.SHOP_STATUS_CLOSED) &&
                    it.isPlanValid
            }.sortedWith(
                compareByDescending<Shop> { it.isVipActive }
                    .thenByDescending { it.rating }
            )
        }
    }

    override fun searchShops(query: String): Flow<List<Shop>> {
        return shopsFlow.map { list ->
            val cleanQuery = query.trim().lowercase()
            list.filter { shop ->
                (shop.status == AppConstants.SHOP_STATUS_APPROVED || shop.status == AppConstants.SHOP_STATUS_CLOSED) &&
                    shop.isPlanValid &&
                    (shop.name.lowercase().contains(cleanQuery) ||
                        shop.address.lowercase().contains(cleanQuery) ||
                        sampleServices.any { (it.shopId == shop.id || (shop.id == "salon_01" && it.shopId == "shop_lamour")) && it.name.lowercase().contains(cleanQuery) })
            }.sortedWith(
                compareByDescending<Shop> { it.isVipActive }
                    .thenByDescending { it.rating }
            )
        }
    }

    fun updateShopStatusFromSync(customerShopId: String, status: String) {
        val current = shopsFlow.value
        val updated = current.map { shop ->
            if (shop.id == customerShopId) shop.copy(status = status) else shop
        }
        shopsFlow.value = updated
    }

    override suspend fun getShopById(shopId: String): Result<Shop> = safeCall {
        val targetId = when (shopId) {
            "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> shopId
        }
        shopsFlow.value.find { it.id == targetId || it.id == shopId }
            ?: sampleShops.find { it.id == targetId || it.id == shopId }
            ?: sampleShops.first()
    }

    override fun getShopServices(shopId: String): Flow<List<ServiceItem>> {
        val targetId = when (shopId) {
            "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> shopId
        }
        val list = sampleServices.filter { (it.shopId == targetId || it.shopId == shopId) && it.isActive }
        return MutableStateFlow(if (list.isEmpty()) sampleServices.take(4) else list)
    }

    override fun getShopSlots(shopId: String, date: String): Flow<List<ShopSlot>> {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val isInitialDay = date == todayStr || date == "2026-10-29"
        val sampleSlots = if (isInitialDay) {
            listOf(
                ShopSlot("slot_01", shopId, date, "09:00", maxCapacity = 1, bookedCount = 1, isAvailable = false),
                ShopSlot("slot_02", shopId, date, "10:30", maxCapacity = 1, bookedCount = 1, isAvailable = false),
                ShopSlot("slot_03", shopId, date, "13:30", maxCapacity = 1, bookedCount = 1, isAvailable = false),
                ShopSlot("slot_04", shopId, date, "15:00", maxCapacity = 1, bookedCount = 1, isAvailable = false),
                ShopSlot("slot_05", shopId, date, "16:30", maxCapacity = 1, bookedCount = 0, isAvailable = false),
                ShopSlot("slot_06", shopId, date, "18:00", maxCapacity = 1, bookedCount = 1, isAvailable = false),
                ShopSlot("slot_07", shopId, date, "19:30", maxCapacity = 1, bookedCount = 0, isAvailable = true)
            )
        } else {
            listOf(
                ShopSlot("${date}_slot_01", shopId, date, "09:00", maxCapacity = 1, bookedCount = 0, isAvailable = true),
                ShopSlot("${date}_slot_02", shopId, date, "10:30", maxCapacity = 1, bookedCount = 0, isAvailable = true),
                ShopSlot("${date}_slot_03", shopId, date, "13:30", maxCapacity = 1, bookedCount = 0, isAvailable = true),
                ShopSlot("${date}_slot_04", shopId, date, "15:00", maxCapacity = 1, bookedCount = 0, isAvailable = true),
                ShopSlot("${date}_slot_05", shopId, date, "16:30", maxCapacity = 1, bookedCount = 0, isAvailable = true),
                ShopSlot("${date}_slot_06", shopId, date, "18:00", maxCapacity = 1, bookedCount = 0, isAvailable = true),
                ShopSlot("${date}_slot_07", shopId, date, "19:30", maxCapacity = 1, bookedCount = 0, isAvailable = true)
            )
        }
        return MutableStateFlow(sampleSlots)
    }

    override suspend fun saveShop(shop: Shop): Result<Unit> = safeCall {
        val updatedList = localStorage?.updateShop(shop) ?: run {
            val list = shopsFlow.value.toMutableList()
            val idx = list.indexOfFirst { it.id == shop.id }
            if (idx != -1) list[idx] = shop else list.add(shop)
            list
        }
        shopsFlow.value = updatedList
    }

    override suspend fun resetToDefault(): Result<Unit> = safeCall {
        val defaults = localStorage?.resetToExcelDefaults() ?: sampleShops
        shopsFlow.value = defaults
    }
}

/**
 * Fake Booking Repository
 */
class FakeBookingRepository(
    private val syncGateway: BookingSyncGateway? = null
) : BaseRepository(), BookingRepository {
    private val bookings = MutableStateFlow<List<Booking>>(
        listOf(
            Booking(
                id = "BK-8921",
                customerId = "user_cust_01",
                customerName = "Khánh Linh",
                customerPhone = "0903 123 456",
                customerNote = "Sơn tone hồng thạch, da tay nhạy cảm",
                shopId = "salon_01",
                shopName = "1994 Nailsroom",
                shopAddress = "172/5 Trần Phú, Hải Châu, Đà Nẵng",
                serviceId = "srv_01",
                serviceName = "Sơn Gel Hữu Cơ + Dưỡng Móng",
                servicePrice = 180000L,
                bookingDate = "2026-10-23",
                bookingTime = "15:30",
                appointmentTimestamp = System.currentTimeMillis() + (4 * 60 * 60 * 1000L), // Còn 4 tiếng (hợp lệ hủy hoàn cọc)
                durationMinutes = 45,
                requiresDeposit = true,
                depositAmount = 50000L,
                depositTransactionId = "TX-DEP-001",
                status = AppConstants.BOOKING_STATUS_CONFIRMED
            ),
            // Thêm booking đã hoàn thành để khách hàng trải nghiệm ngay chức năng ĐÁNH GIÁ (Nhóm 5)
            Booking(
                id = "BK-8840",
                customerId = "user_cust_01",
                customerName = "Khánh Linh",
                customerPhone = "0903 123 456",
                customerNote = "Gội đầu dưỡng sinh thảo dược",
                shopId = "salon_09",
                shopName = "Thì Là SPA - Gội Đầu Dưỡng Sinh",
                shopAddress = "57 Nguyễn Thị Minh Khai, Hải Châu, Đà Nẵng",
                serviceId = "srv_03",
                serviceName = "Gội Đầu Dưỡng Sinh Thảo Dược 60p",
                servicePrice = 250000L,
                bookingDate = "2026-10-22",
                bookingTime = "14:00",
                appointmentTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000L),
                durationMinutes = 60,
                requiresDeposit = true,
                depositAmount = 50000L,
                depositTransactionId = "TX-DEP-000",
                status = AppConstants.BOOKING_STATUS_COMPLETED,
                isReviewed = false
            ),
            // Thêm booking của khách khác tại shop để Merchant Portal kiểm thử quản lý booking
            Booking(
                id = "BK-9012",
                customerId = "user_cust_02",
                customerName = "Phương Thảo",
                customerPhone = "0912 888 999",
                customerNote = "Khách làm móng cô dâu phong cách Hàn Quốc",
                shopId = "shop_lamour",
                shopName = "L'Amour Haute Nail & Lash Lounge",
                shopAddress = "128 Pasteur, Bến Nghé, Quận 1, TP.HCM",
                serviceId = "srv_01",
                serviceName = "Sơn Gel Hữu Cơ Pháp + Dưỡng Móng",
                servicePrice = 180000L,
                bookingDate = "2026-10-23",
                bookingTime = "10:30",
                appointmentTimestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000L), // Quá 30p sau hẹn -> Tự động hoàn thành
                durationMinutes = 45,
                requiresDeposit = true,
                depositAmount = 50000L,
                depositTransactionId = "TX-DEP-002",
                status = AppConstants.BOOKING_STATUS_CONFIRMED
            )
        )
    )

    override suspend fun createBooking(booking: Booking): Result<String> = safeCall {
        val currentList = bookings.value.toMutableList()
        currentList.add(0, booking)
        bookings.value = currentList
        syncGateway?.onCustomerBookingCreated(booking)
        booking.id
    }

    override fun getCustomerBookings(customerId: String): Flow<List<Booking>> {
        return bookings.map { list -> list.filter { it.customerId == customerId } }
    }

    override fun getShopBookings(shopId: String): Flow<List<Booking>> {
        val targetId = when (shopId) {
            "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> shopId
        }
        return bookings.map { list ->
            list.filter { it.shopId == targetId || it.shopId == shopId || (targetId == "salon_01" && it.shopId == "shop_lamour") }
        }
    }

    override suspend fun getBookingById(bookingId: String): Result<Booking> = safeCall {
        bookings.value.find { it.id == bookingId } ?: throw NoSuchElementException("Không tìm thấy booking: $bookingId")
    }

    override suspend fun updateBookingStatus(bookingId: String, status: String): Result<Unit> = safeCall {
        val updated = bookings.value.map { b ->
            if (b.id == bookingId) {
                b.copy(status = status)
            } else b
        }
        bookings.value = updated
    }

    override suspend fun markBookingReviewed(bookingId: String): Result<Unit> = safeCall {
        val updated = bookings.value.map { b ->
            if (b.id == bookingId) {
                b.copy(isReviewed = true)
            } else b
        }
        bookings.value = updated
        syncGateway?.onCustomerBookingReviewed(bookingId)
    }

    override suspend fun cancelBooking(bookingId: String, reason: String): Result<Unit> = safeCall {
        val updated = bookings.value.map { b ->
            if (b.id == bookingId) {
                b.copy(
                    status = AppConstants.BOOKING_STATUS_CANCELLED,
                    cancelReason = reason,
                    cancelledAt = System.currentTimeMillis()
                )
            } else b
        }
        bookings.value = updated
        syncGateway?.onCustomerBookingCancelled(bookingId, reason)
    }

    fun syncFromMerchant(booking: Booking, createIfMissing: Boolean) {
        val currentList = bookings.value
        val exists = currentList.any { it.id == booking.id }
        if (exists) {
            bookings.value = currentList.map { existing ->
                if (existing.id == booking.id) {
                    existing.copy(
                        status = booking.status,
                        cancelReason = booking.cancelReason ?: existing.cancelReason,
                        cancelledAt = booking.cancelledAt ?: existing.cancelledAt,
                        isReviewed = existing.isReviewed || booking.isReviewed
                    )
                } else existing
            }
        } else if (createIfMissing) {
            bookings.value = listOf(booking) + currentList
        }
    }
}

/**
 * Fake Wallet Repository
 */
class FakeWalletRepository : BaseRepository(), WalletRepository {
    companion object {
        val walletFlow = MutableStateFlow(Wallet("user_cust_01", balance = 3450000L))
    }
    private val transactionsFlow = MutableStateFlow<List<Transaction>>(
        listOf(
            DepositTransaction(
                id = "TX-DEP-001",
                userId = "user_cust_01",
                amount = 50000L,
                status = AppConstants.TX_STATUS_HELD, // Sàn đang giữ cọc
                bookingId = "BK-8921",
                shopId = "salon_01"
            ),
            TopUpTransaction(
                id = "TX-TOP-002",
                userId = "user_cust_01",
                amount = 500000L,
                status = AppConstants.TX_STATUS_SUCCESS
            )
        )
    )

    override fun getWalletBalance(userId: String): Flow<Wallet?> = walletFlow

    override fun getTransactionHistory(userId: String): Flow<List<Transaction>> = transactionsFlow

    override suspend fun createTopUpRequest(userId: String, amount: Long): Result<Transaction> = safeCall {
        val txId = "TX-TOP-" + UUID.randomUUID().toString().take(8).uppercase()
        val tx = TopUpTransaction(
            id = txId,
            userId = userId,
            amount = amount,
            status = AppConstants.TX_STATUS_PENDING,
            qrUrl = "${AppConstants.DEFAULT_MOCK_PAYMENT_URL}?txId=$txId&amount=$amount"
        )
        val list = transactionsFlow.value.toMutableList()
        list.add(0, tx)
        transactionsFlow.value = list
        tx
    }

    override suspend fun holdDeposit(
        userId: String,
        bookingId: String,
        shopId: String,
        amount: Long
    ): Result<Transaction> = safeCall {
        val currentBalance = walletFlow.value.balance
        if (currentBalance < amount) {
            throw IllegalStateException("Số dư ví không đủ để thanh toán cọc ($amount đ). Vui lòng nạp thêm.")
        }
        // Trừ tiền số dư ví của khách
        walletFlow.value = walletFlow.value.copy(balance = currentBalance - amount)

        val txId = "TX-DEP-" + UUID.randomUUID().toString().take(8).uppercase()
        val tx = DepositTransaction(
            id = txId,
            userId = userId,
            amount = amount,
            status = AppConstants.TX_STATUS_HELD, // Sàn tạm giữ
            bookingId = bookingId,
            shopId = shopId
        )
        val list = transactionsFlow.value.toMutableList()
        list.add(0, tx)
        transactionsFlow.value = list
        tx
    }

    override suspend fun refundDepositToWallet(
        userId: String,
        bookingId: String,
        amount: Long,
        reason: String
    ): Result<Transaction> = safeCall {
        // Hoàn tiền cộng lại vào ví khách
        walletFlow.value = walletFlow.value.copy(balance = walletFlow.value.balance + amount)

        val txId = "TX-REF-" + UUID.randomUUID().toString().take(8).uppercase()
        val tx = RefundTransaction(
            id = txId,
            userId = userId,
            amount = amount,
            status = AppConstants.TX_STATUS_REFUNDED,
            originalDepositTxId = "TX-DEP-OLD",
            bookingId = bookingId,
            refundReason = reason
        )
        val list = transactionsFlow.value.toMutableList()
        list.add(0, tx)
        transactionsFlow.value = list
        tx
    }

    override fun observeTransactionStatus(transactionId: String): Flow<String> {
        return transactionsFlow.map { list ->
            list.find { it.id == transactionId }?.status ?: AppConstants.TX_STATUS_PENDING
        }
    }

    override suspend fun confirmTransactionSuccess(transactionId: String): Result<Unit> = safeCall {
        var topUpAmount = 0L
        val updated = transactionsFlow.value.map { tx ->
            if (tx.id == transactionId) {
                if (tx is TopUpTransaction) {
                    topUpAmount = tx.amount
                    tx.copy(status = AppConstants.TX_STATUS_SUCCESS)
                } else tx
            } else tx
        }
        transactionsFlow.value = updated

        // Nếu là giao dịch nạp tiền, cộng số dư ví
        if (topUpAmount > 0) {
            walletFlow.value = walletFlow.value.copy(balance = walletFlow.value.balance + topUpAmount)
        }
    }
}

/**
 * Fake Review Repository
 */
class FakeReviewRepository(
    private val syncGateway: BookingSyncGateway? = null
) : BaseRepository(), ReviewRepository {
    private val reviewsFlow = MutableStateFlow(
        listOf(
            Review(
                id = "REV-01",
                bookingId = "BK-OLD-1",
                shopId = "salon_01",
                customerId = "user_cust_02",
                customerName = "Thảo Nhi",
                rating = 5,
                comment = "Tiệm làm móng rất tỉ mỉ, tone màu hồng thạch cực xinh. Chuyên viên nhẹ nhàng chu đáo!",
                status = "ACTIVE"
            ),
            Review(
                id = "REV-02",
                bookingId = "BK-OLD-2",
                shopId = "salon_01",
                customerId = "user_cust_03",
                customerName = "Bảo Trâm",
                rating = 5,
                comment = "Không gian sang trọng tại Đà Nẵng, phục vụ rất chu đáo. Sẽ quay lại nhiều lần.",
                status = "ACTIVE"
            )
        )
    )

    override fun getShopReviews(shopId: String): Flow<List<Review>> {
        val targetId = when (shopId) {
            "shop_lamour" -> "salon_01"
            "shop_anyen" -> "salon_09"
            else -> shopId
        }
        return reviewsFlow.map { list ->
            list.filter { (it.shopId == targetId || it.shopId == shopId || (targetId == "salon_01" && it.shopId == "shop_lamour")) && it.status != "REMOVED" }
        }
    }

    override fun getCustomerReviews(customerId: String): Flow<List<Review>> {
        return reviewsFlow.map { list ->
            list.filter { it.customerId == customerId }
        }
    }

    override suspend fun submitReview(review: Review): Result<Unit> = safeCall {
        val list = reviewsFlow.value.toMutableList()
        list.add(0, review)
        reviewsFlow.value = list
        syncGateway?.onCustomerReviewSubmitted(review)
    }

    override suspend fun replyReview(reviewId: String, reply: String): Result<Unit> = safeCall {
        val updated = reviewsFlow.value.map { r ->
            if (r.id == reviewId) {
                r.copy(
                    replyComment = reply,
                    repliedAt = System.currentTimeMillis()
                )
            } else r
        }
        reviewsFlow.value = updated
    }

    override suspend fun reportReview(reviewId: String, reason: String): Result<Unit> = safeCall {
        val updated = reviewsFlow.value.map { r ->
            if (r.id == reviewId) {
                r.copy(
                    status = "REMOVE_REQUESTED",
                    reportReason = reason,
                    reportedAt = System.currentTimeMillis()
                )
            } else r
        }
        reviewsFlow.value = updated
    }
}
