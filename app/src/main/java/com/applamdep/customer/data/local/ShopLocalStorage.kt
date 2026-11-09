package com.applamdep.customer.data.local

import android.content.Context
import android.content.SharedPreferences
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Shop
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

/**
 * Quản lý lưu trữ trực tiếp dữ liệu cửa hàng vào bộ nhớ máy (Device Local Storage).
 * Hỗ trợ lưu trữ bền vững (Persistent) qua SharedPreferences và File Storage nội bộ của ứng dụng,
 * đảm bảo dữ liệu hoạt động Offline 100%, không bị mất khi đóng app hoặc tắt nguồn thiết bị.
 *
 * Dữ liệu được trích xuất và đồng bộ từ bảng tính Excel danh sách Salon Đà Nẵng:
 * 1. 1994 Nailsroom
 * 2. BONNIE NAIL
 * 3. GÀ SPA
 * 4. HIEN HAIR STUDIO
 * 5. KỲ DUYÊN TRỊ MỤN
 * 6. MNH THUY MAKEUP
 * 7. Nails 1996
 * 8. SI MÊ BEAUTY
 * 9. Thì Là SPA - Gội Đầu Dưỡng Sinh, Massage Thư Giãn
 * 10. Tun Beauty Salon & Spa
 * 11. 1m65.nail (𝟏𝐦𝟔𝟓.𝐧𝐚𝐢𝐥)
 */
class ShopLocalStorage(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val shopListType = object : TypeToken<List<Shop>>() {}.type

    companion object {
        private const val PREFS_NAME = "glow_prive_local_shops_pref"
        private const val KEY_SHOPS_JSON = "key_shops_json_data"
        private const val BACKUP_FILE_NAME = "local_shops_data.json"

        /**
         * Danh sách 11 cửa hàng thực tế trích xuất trực tiếp từ file Untitled spreadsheet.xlsx
         */
        fun getExcelSalons(): List<Shop> {
            val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000L
            val sixtyDaysMillis = 60L * 24 * 60 * 60 * 1000L
            val ninetyDaysMillis = 90L * 24 * 60 * 60 * 1000L
            val currentTime = System.currentTimeMillis()

            return listOf(
                Shop(
                    id = "salon_01",
                    ownerId = "merchant_01",
                    name = "1994 Nailsroom",
                    address = "172/5 Trần Phú, Hải Châu, Đà Nẵng",
                    lat = 16.0658,
                    lng = 108.2230,
                    phone = "0762792671",
                    openingHours = "9h - 20h",
                    description = "Tiệm làm móng nghệ thuật phong cách Hàn Quốc và dưỡng móng cao cấp tại trung tâm Hải Châu, Đà Nẵng.",
                    images = listOf("salon_01_1994_nailsroom", "nail_french_gel", "salon_luxury_interior"),
                    rating = 4.9,
                    reviewCount = 142,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + thirtyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(50000L, 100000L, 200000L, 300000L, 500000L),
                    amenities = listOf("Máy lạnh", "Trà thảo mộc", "Wifi tốc độ cao", "Phòng VIP riêng", "Thanh toán VietQR"),
                    facebook = "https://www.facebook.com/profile.php?id=100083563112383",
                    instagram = "https://www.instagram.com/1998_nails.room"
                ),
                Shop(
                    id = "salon_02",
                    ownerId = "merchant_02",
                    name = "BONNIE NAIL",
                    address = "260/18 Dương Đình Nghệ, Sơn Trà, Đà Nẵng",
                    lat = 16.0682,
                    lng = 108.2395,
                    phone = "0912154434",
                    openingHours = "8h - 20h",
                    description = "Chuyên vẽ móng Ombre nghệ thuật, đắp móng gel sang trọng, nối mi tự nhiên gần bãi biển Mỹ Khê.",
                    images = listOf("salon_02_bonnie_nail", "nail_cateye_aurora", "salon_luxury_interior"),
                    rating = 4.8,
                    reviewCount = 98,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + (45L * 24 * 60 * 60 * 1000L),
                    requireDeposit = true,
                    depositOptions = listOf(50000L, 100000L, 200000L),
                    amenities = listOf("Wifi", "Nước uống", "Chỗ đậu xe máy", "Máy lạnh"),
                    facebook = "https://www.facebook.com/profile.php?id=61590247413035",
                    tiktok = "https://www.tiktok.com/@bonnie.nail"
                ),
                Shop(
                    id = "salon_03",
                    ownerId = "merchant_03",
                    name = "GÀ SPA",
                    address = "Hải Châu, Đà Nẵng",
                    lat = 16.0630,
                    lng = 108.2225,
                    phone = "0901 311 113",
                    openingHours = "8h30 - 19h30",
                    description = "Thương hiệu chăm sóc da và điều trị mụn chuẩn y khoa uy tín hàng đầu, liệu trình thải độc Oxy tươi chuyên sâu.",
                    images = listOf("salon_03_ga_spa", "shop_hero_spa"),
                    rating = 4.8,
                    reviewCount = 215,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_NORMAL,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = false,
                    depositOptions = emptyList(),
                    amenities = listOf("Bác sĩ tư vấn", "Thiết bị y khoa", "Điều hòa"),
                    facebook = "https://www.facebook.com/gaspa.vn",
                    website = "https://gaspa.vn"
                ),
                Shop(
                    id = "salon_04",
                    ownerId = "merchant_04",
                    name = "HIEN HAIR STUDIO",
                    address = "183 Tố Hữu, Cẩm Lệ, Đà Nẵng",
                    lat = 16.0280,
                    lng = 108.2090,
                    phone = "0332 678 156",
                    openingHours = "8h30 - 19h30",
                    description = "Salon tạo mẫu tóc chuyên nghiệp, uốn sóng lơi chuẩn Hàn, nhuộm Balayage thời thượng và phục hồi tóc hư tổn.",
                    images = listOf("salon_04_hien_hair_studio", "salon_luxury_interior"),
                    rating = 4.9,
                    reviewCount = 180,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(100000L, 200000L, 500000L),
                    amenities = listOf("Ghế massage gội đầu", "Wifi 5G", "Trà & Cafe", "Chỗ để xe"),
                    facebook = "https://www.facebook.com/p/Hienz-Hair-Studio-61554487566304/",
                    tiktok = "https://www.tiktok.com/@hienz_hair_studio"
                ),
                Shop(
                    id = "salon_05",
                    ownerId = "merchant_05",
                    name = "KỲ DUYÊN TRỊ MỤN",
                    address = "282 Nguyễn Phước Nguyên, Đà Nẵng",
                    lat = 16.0590,
                    lng = 108.1820,
                    phone = "0905 926 017",
                    openingHours = "8h00 - 19h00",
                    description = "Cơ sở chuyên sâu điều trị các loại mụn viêm, mụn ẩn, sẹo rỗ đông y, giúp tái sinh làn da mịn màng.",
                    images = listOf("salon_05_ky_duyen_tri_mun", "shop_hero_spa"),
                    rating = 4.7,
                    reviewCount = 112,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_NORMAL,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = false,
                    depositOptions = emptyList(),
                    amenities = listOf("Dược mỹ phẩm chính hãng", "Tư vấn 1:1"),
                    facebook = "https://www.facebook.com/ky.duyen.478645",
                    tiktok = "https://www.tiktok.com/@kyduyen_29"
                ),
                Shop(
                    id = "salon_06",
                    ownerId = "merchant_06",
                    name = "MNH THUY MAKEUP",
                    address = "01 Đinh Văn Chấp, Cẩm Lệ, Đà Nẵng",
                    lat = 16.0240,
                    lng = 108.2130,
                    phone = "077 9417 168",
                    openingHours = "8h - 20h",
                    description = "Studio trang điểm cô dâu, dạ tiệc, kỷ yếu phong cách trong trẻo Douyin & phương Tây quyến rũ.",
                    images = listOf("salon_06_mnh_thuy_makeup", "banner_glowprive_vip"),
                    rating = 4.9,
                    reviewCount = 156,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + ninetyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(100000L, 200000L, 300000L),
                    amenities = listOf("Mỹ phẩm High-end", "Phòng thay đồ", "Ánh sáng Studio chuyên nghiệp"),
                    facebook = "https://www.facebook.com/mnh.thuy.2025",
                    tiktok = "https://www.tiktok.com/@mnhthuy.makeup"
                ),
                Shop(
                    id = "salon_07",
                    ownerId = "merchant_07",
                    name = "Nails 1996",
                    address = "3 Dương Tụ Quán, Ngũ Hành Sơn, Đà Nẵng",
                    lat = 16.0370,
                    lng = 108.2430,
                    phone = "083.383.5555",
                    openingHours = "9h - 20h",
                    description = "Studio móng nghệ thuật cao cấp, không gian sang trọng, dịch vụ chuyên nghiệp hàng đầu tại Ngũ Hành Sơn.",
                    images = listOf("salon_07_nails_1996", "nail_french_gel", "salon_luxury_interior"),
                    rating = 4.8,
                    reviewCount = 89,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(50000L, 100000L, 200000L),
                    amenities = listOf("Sơn hữu cơ an toàn", "Bảo hành 7 ngày", "Trà hoa cúc"),
                    facebook = "https://www.facebook.com/1996.NailStudio",
                    website = "https://1996nailstudio.com/vi"
                ),
                Shop(
                    id = "salon_08",
                    ownerId = "merchant_08",
                    name = "SI MÊ BEAUTY",
                    address = "58 Nguyễn Đôn Tiết, Hải Châu, Đà Nẵng",
                    lat = 16.0820,
                    lng = 108.2190,
                    phone = "0777603852",
                    openingHours = "8h - 21h",
                    description = "Trung tâm làm đẹp toàn diện, triệt lông diode laser thế hệ mới, cấy collagen căng bóng tầng sâu.",
                    images = listOf("salon_08_si_me_beauty", "shop_hero_spa"),
                    rating = 4.8,
                    reviewCount = 94,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_NORMAL,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = false,
                    depositOptions = emptyList(),
                    amenities = listOf("Phòng riêng tư", "Máy lạnh", "Tư vấn da miễn phí"),
                    facebook = "https://www.facebook.com/simebeauty.haichau.danang",
                    tiktok = "https://www.tiktok.com/@simebeauty81"
                ),
                Shop(
                    id = "salon_09",
                    ownerId = "merchant_09",
                    name = "Thì Là SPA - Gội Đầu Dưỡng Sinh, Massage Thư Giãn",
                    address = "57 Nguyễn Thị Minh Khai, Hải Châu, Đà Nẵng",
                    lat = 16.0710,
                    lng = 108.2170,
                    phone = "0773622699",
                    openingHours = "9h15 - 20h30",
                    description = "Không gian dưỡng sinh thư thái với nước gội bồ kết nấu tươi cô đặc, massage trị liệu cổ vai gáy và ngâm chân thảo mộc đá nóng.",
                    images = listOf("salon_09_thi_la_spa", "shop_hero_spa"),
                    rating = 5.0,
                    reviewCount = 260,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + ninetyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(50000L, 100000L, 200000L),
                    amenities = listOf("Bồ kết nấu tươi", "Phòng xông tinh dầu", "Trà gừng mật ong", "Phòng VIP đôi"),
                    facebook = "https://www.facebook.com/thilaspa.danang"
                ),
                Shop(
                    id = "salon_10",
                    ownerId = "merchant_10",
                    name = "Tun Beauty Salon & Spa",
                    address = "238 Trần Hưng Đạo, Sơn Trà, Đà Nẵng",
                    lat = 16.0610,
                    lng = 108.2310,
                    phone = "083.383.5555",
                    openingHours = "10h - 21h",
                    description = "Tổ hợp chăm sóc tóc và spa thư giãn bên bờ sông Hàn thơ mộng, ngắm trọn cầu Rồng Đà Nẵng.",
                    images = listOf("salon_10_tun_beauty_salon", "salon_luxury_interior"),
                    rating = 4.8,
                    reviewCount = 130,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(50000L, 100000L, 200000L),
                    amenities = listOf("View sông Hàn", "Chỗ đỗ ô tô", "Điều hòa"),
                    facebook = "https://www.facebook.com/TunBeautySalonSpa/"
                ),
                Shop(
                    id = "salon_11",
                    ownerId = "merchant_11",
                    name = "1m65.nail",
                    address = "48 Lê Đình Dương, Hải Châu, Đà Nẵng",
                    lat = 16.0615,
                    lng = 108.2205,
                    phone = "0935043679",
                    openingHours = "9h - 20h",
                    description = "Tiệm nail phong cách Nhật - Hàn, chuyên vẽ hoạt hình anime tỉ mỉ và phụ kiện charm xinh độc quyền.",
                    images = listOf("salon_11_1m65_nail", "nail_cateye_aurora"),
                    rating = 4.9,
                    reviewCount = 175,
                    status = AppConstants.SHOP_STATUS_APPROVED,
                    plan = AppConstants.PLAN_VIP,
                    planExpiryMillis = currentTime + sixtyDaysMillis,
                    requireDeposit = true,
                    depositOptions = listOf(50000L, 100000L, 200000L),
                    amenities = listOf("Sơn gel cao cấp Nhật", "Kho charm hơn 1000 mẫu", "Điều hòa"),
                    facebook = "https://www.facebook.com/profile.php?id=100083393916454",
                    instagram = "https://www.instagram.com/1m65.nail"
                )
            )
        }
    }

    /**
     * Tải danh sách cửa hàng từ bộ nhớ máy (SharedPreferences hoặc File nội bộ).
     * Nếu chưa có trong bộ nhớ, tự động khởi tạo 11 cửa hàng Excel và lưu ngay vào bộ nhớ.
     */
    fun loadOrInitializeShops(): List<Shop> {
        val jsonFromPrefs = prefs.getString(KEY_SHOPS_JSON, null)
        if (!jsonFromPrefs.isNullOrBlank()) {
            try {
                val shops: List<Shop>? = gson.fromJson(jsonFromPrefs, shopListType)
                if (!shops.isNullOrEmpty() && shops.size >= 11 && shops.firstOrNull()?.images?.firstOrNull()?.startsWith("salon_") == true) {
                    return shops
                }
            } catch (e: Exception) {
                // Thử đọc từ file nội bộ
            }
        }

        // Kiểm tra file nội bộ
        val file = File(context.filesDir, BACKUP_FILE_NAME)
        if (file.exists()) {
            try {
                val fileContent = file.readText()
                val shops: List<Shop>? = gson.fromJson(fileContent, shopListType)
                if (!shops.isNullOrEmpty()) {
                    // Cập nhật lại vào SharedPreferences để truy xuất nhanh hơn
                    prefs.edit().putString(KEY_SHOPS_JSON, fileContent).apply()
                    return shops
                }
            } catch (e: Exception) {
                // Fallback
            }
        }

        // Lần đầu chạy: Lưu thẳng 11 cửa hàng vào bộ nhớ máy
        val initialShops = getExcelSalons()
        saveShopsToDevice(initialShops)
        return initialShops
    }

    /**
     * Ghi danh sách cửa hàng trực tiếp vào bộ nhớ máy (SharedPreferences + File System nội bộ)
     */
    fun saveShopsToDevice(shops: List<Shop>) {
        try {
            val jsonString = gson.toJson(shops)
            // 1. Lưu vào SharedPreferences (RAM Cache & XML Storage)
            prefs.edit().putString(KEY_SHOPS_JSON, jsonString).apply()

            // 2. Lưu đồng thời vào internal storage file để đảm bảo an toàn dữ liệu
            val file = File(context.filesDir, BACKUP_FILE_NAME)
            file.writeText(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Cập nhật thông tin một cửa hàng cụ thể vào bộ nhớ máy
     */
    fun updateShop(updatedShop: Shop): List<Shop> {
        val currentShops = loadOrInitializeShops().toMutableList()
        val index = currentShops.indexOfFirst { it.id == updatedShop.id }
        if (index != -1) {
            currentShops[index] = updatedShop
        } else {
            currentShops.add(updatedShop)
        }
        saveShopsToDevice(currentShops)
        return currentShops
    }

    /**
     * Khôi phục danh sách cửa hàng về mặc định ban đầu từ Excel và lưu lại vào bộ nhớ máy
     */
    fun resetToExcelDefaults(): List<Shop> {
        val defaults = getExcelSalons()
        saveShopsToDevice(defaults)
        return defaults
    }
}

