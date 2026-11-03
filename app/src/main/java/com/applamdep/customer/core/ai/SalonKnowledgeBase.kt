package com.applamdep.customer.core.ai

/**
 * TẬP DỮ LIỆU TRI THỨC SALON (KNOWLEDGE BASE CHO RAG)
 * Trích xuất từ tập tin: `dữ liệu AI/Untitled spreadsheet.xlsx`
 * Bao gồm đầy đủ thông tin của 11 Salon thực tế tại Đà Nẵng:
 * - Chuyên môn / dịch vụ chính
 * - Địa chỉ và phân khu quận huyện
 * - Số hotline liên hệ
 * - Khung giờ hoạt động
 * - Mạng xã hội (Facebook, TikTok, Instagram, Website)
 * - Chính sách đặt cọc (Có cọc / Miễn cọc)
 */
data class SalonKnowledgeDoc(
    val id: String,
    val name: String,
    val category: String, // "Nail", "Hair", "Spa", "Makeup", "Skincare"
    val address: String,
    val district: String, // "Hải Châu", "Sơn Trà", "Cẩm Lệ", "Ngũ Hành Sơn", "Thanh Khê"
    val phone: String,
    val openingHours: String,
    val openHourInt: Int,
    val closeHourInt: Int,
    val specialty: String,
    val highlightService: String,
    val priceRange: String,
    val rating: Double,
    val reviewCount: Int,
    val requireDeposit: Boolean,
    val depositInfo: String,
    val facebook: String = "",
    val tiktok: String = "",
    val instagram: String = "",
    val website: String = "",
    val searchKeywords: List<String> = emptyList()
)

object SalonKnowledgeBase {

    val salons: List<SalonKnowledgeDoc> = listOf(
        SalonKnowledgeDoc(
            id = "salon_01",
            name = "1994 Nailsroom",
            category = "Nail",
            address = "172/5 Trần Phú, Hải Châu, Đà Nẵng",
            district = "Hải Châu",
            phone = "0762792671",
            openingHours = "9h - 20h (Thứ 2 - Chủ nhật)",
            openHourInt = 9,
            closeHourInt = 20,
            specialty = "Làm móng nghệ thuật phong cách Hàn Quốc, sơn gel thạch tự nhiên, cắt da dưỡng móng cao cấp",
            highlightService = "Sơn Gel Thạch Hàn Quốc + Dưỡng Móng",
            priceRange = "160.000đ - 350.000đ",
            rating = 4.9,
            reviewCount = 142,
            requireDeposit = true,
            depositInfo = "Có cọc (50.000đ giữ chỗ 100%)",
            facebook = "https://www.facebook.com/profile.php?id=100083563112383",
            instagram = "https://www.instagram.com/1998_nails.room",
            searchKeywords = listOf("nail", "móng", "gel", "thạch", "hải châu", "trần phú", "1994", "hàn quốc", "sơn gel", "cắt da")
        ),
        SalonKnowledgeDoc(
            id = "salon_02",
            name = "BONNIE NAIL",
            category = "Nail",
            address = "260/18 Dương Đình Nghệ, Sơn Trà, Đà Nẵng",
            district = "Sơn Trà",
            phone = "0912154434",
            openingHours = "8h - 20h (Thứ 2 - Chủ nhật)",
            openHourInt = 8,
            closeHourInt = 20,
            specialty = "Vẽ móng Ombre nghệ thuật, đắp móng gel sang trọng, nối mi tự nhiên, vị trí gần biển Mỹ Khê",
            highlightService = "Vẽ Móng Ombre Tự Nhiên & Nối Mi",
            priceRange = "180.000đ - 420.000đ",
            rating = 4.8,
            reviewCount = 98,
            requireDeposit = true,
            depositInfo = "Có cọc (50.000đ giữ chỗ)",
            facebook = "https://www.facebook.com/profile.php?id=61590247413035",
            tiktok = "https://www.tiktok.com/@bonnie.nail",
            searchKeywords = listOf("nail", "bonnie", "sơn trà", "dương đình nghệ", "mỹ khê", "ombre", "đắp gel", "nối mi")
        ),
        SalonKnowledgeDoc(
            id = "salon_03",
            name = "GÀ SPA",
            category = "Skincare",
            address = "Hải Châu, Đà Nẵng",
            district = "Hải Châu",
            phone = "0901 311 113",
            openingHours = "8h30 - 19h30 (Thứ 2 - Chủ nhật)",
            openHourInt = 8,
            closeHourInt = 19,
            specialty = "Chăm sóc da & điều trị mụn chuẩn y khoa hàng đầu, thải độc Oxy tươi, massage thư giãn da mặt",
            highlightService = "Thải Độc Da Oxy Tươi & Trị Mụn Chuẩn Y Khoa",
            priceRange = "250.000đ - 650.000đ",
            rating = 4.8,
            reviewCount = 215,
            requireDeposit = false,
            depositInfo = "Miễn cọc 0đ (Đặt giữ chỗ miễn phí)",
            searchKeywords = listOf("spa", "gà spa", "mụn", "trị mụn", "chăm sóc da", "oxy tươi", "y khoa", "hải châu", "thải độc")
        ),
        SalonKnowledgeDoc(
            id = "salon_04",
            name = "HIEN HAIR STUDIO",
            category = "Hair",
            address = "183 Tố Hữu, Cẩm Lệ, Đà Nẵng",
            district = "Cẩm Lệ",
            phone = "0332 678 156",
            openingHours = "8h30 - 19h30 (Thứ 2 - Chủ nhật)",
            openHourInt = 8,
            closeHourInt = 19,
            specialty = "Cắt tạo kiểu tóc nam nữ thời thượng, uốn sóng lơi Hàn Quốc, nhuộm Balayage thời trang, phục hồi keratin",
            highlightService = "Uốn Sóng Lơi Hàn Quốc + Cắt Tạo Kiểu",
            priceRange = "150.000đ - 850.000đ",
            rating = 4.9,
            reviewCount = 168,
            requireDeposit = false,
            depositInfo = "Miễn cọc 0đ (Đặt giữ chỗ miễn phí)",
            facebook = "https://www.facebook.com/p/Hienz-Hair-Studio-61554487566304",
            tiktok = "https://www.tiktok.com/@hienz_hair_studio",
            searchKeywords = listOf("tóc", "hair", "cắt tóc", "uốn tóc", "sóng lơi", "cẩm lệ", "tố hữu", "hien", "hienz", "nhuộm", "balayage")
        ),
        SalonKnowledgeDoc(
            id = "salon_05",
            name = "KỲ DUYÊN TRỊ MỤN",
            category = "Skincare",
            address = "282 Nguyễn Phước Nguyên, Thanh Khê, Đà Nẵng",
            district = "Thanh Khê",
            phone = "0905 926 017",
            openingHours = "8h00 - 19h00 (Thứ 2 - Chủ nhật)",
            openHourInt = 8,
            closeHourInt = 19,
            specialty = "Chuyên gia điều trị dứt điểm các loại mụn bọc, mụn viêm, phục hồi da nhiễm corticoid, peel da sinh học an toàn",
            highlightService = "Liệu Trình Điều Trị Mụn Chuyên Sâu & Phục Hồi",
            priceRange = "200.000đ - 500.000đ",
            rating = 4.9,
            reviewCount = 189,
            requireDeposit = false,
            depositInfo = "Miễn cọc 0đ (Đặt giữ chỗ miễn phí)",
            facebook = "https://www.facebook.com/ky.duyen.478645",
            tiktok = "https://www.tiktok.com/@kyduyen_29",
            searchKeywords = listOf("trị mụn", "mụn", "kỳ duyên", "nguyễn phước nguyên", "thanh khê", "peel da", "da liễu", "skincare")
        ),
        SalonKnowledgeDoc(
            id = "salon_06",
            name = "MNH THUY MAKEUP",
            category = "Makeup",
            address = "01 Đinh Văn Chấp, Cẩm Lệ, Đà Nẵng",
            district = "Cẩm Lệ",
            phone = "077 9417 168",
            openingHours = "8h - 20h (Thứ 2 - Chủ nhật)",
            openHourInt = 8,
            closeHourInt = 20,
            specialty = "Trang điểm cô dâu VIP, makeup dự tiệc phong cách trong trẻo Hàn Quốc, Douyin sắc sảo, tạo kiểu tóc cô dâu",
            highlightService = "Makeup Dự Tiệc Tone Hàn Trong Trẻo & Làm Tóc",
            priceRange = "300.000đ - 1.500.000đ",
            rating = 4.8,
            reviewCount = 112,
            requireDeposit = true,
            depositInfo = "Có cọc (50.000đ - 100.000đ)",
            facebook = "https://www.facebook.com/mnh.thuy.2025",
            tiktok = "https://www.tiktok.com/@mnhthuy.makeup",
            searchKeywords = listOf("makeup", "trang điểm", "cô dâu", "dự tiệc", "cẩm lệ", "đinh văn chấp", "mnh thuy", "thủy", "tóc cô dâu")
        ),
        SalonKnowledgeDoc(
            id = "salon_07",
            name = "Nails 1996",
            category = "Nail",
            address = "3 Dương Tụ Quán, Ngũ Hành Sơn, Đà Nẵng",
            district = "Ngũ Hành Sơn",
            phone = "083.383.5555",
            openingHours = "9h - 20h (Thứ 2 - Chủ nhật)",
            openHourInt = 9,
            closeHourInt = 20,
            specialty = "Nail Art mắt mèo kim cương, sơn gel organic cao cấp an toàn, nối móng úp thiết kế riêng, đính đá sang trọng",
            highlightService = "Nail Art Mắt Mèo Kim Cương & Nối Móng Úp",
            priceRange = "170.000đ - 450.000đ",
            rating = 4.9,
            reviewCount = 156,
            requireDeposit = false,
            depositInfo = "Miễn cọc 0đ (Đặt giữ chỗ miễn phí)",
            facebook = "https://www.facebook.com/1996.NailStudio",
            website = "https://1996nailstudio.com/vi",
            searchKeywords = listOf("nail", "móng", "1996", "ngũ hành sơn", "dương tụ quán", "mắt mèo", "móng úp", "gel", "đính đá")
        ),
        SalonKnowledgeDoc(
            id = "salon_08",
            name = "SI MÊ BEAUTY",
            category = "Spa",
            address = "58 Nguyễn Đôn Tiết, Hải Châu, Đà Nẵng",
            district = "Hải Châu",
            phone = "0777603852",
            openingHours = "8h - 21h (Thứ 2 - Chủ nhật - Mở cửa muộn nhất)",
            openHourInt = 8,
            closeHourInt = 21,
            specialty = "Nối mi tơ tằm mềm nhẹ, uốn mi collagen phủ nano, triệt lông diode laser công nghệ mới, chăm sóc da mặt",
            highlightService = "Nối Mi Lụa Tơ Tằm Tự Nhiên & Uốn Mi Collagen",
            priceRange = "150.000đ - 480.000đ",
            rating = 4.8,
            reviewCount = 134,
            requireDeposit = true,
            depositInfo = "Có cọc (50.000đ)",
            facebook = "https://www.facebook.com/simebeauty.haichau.danang",
            tiktok = "https://www.tiktok.com/@simebeauty81",
            searchKeywords = listOf("mi", "nối mi", "si mê", "si me", "nguyễn đôn tiết", "hải châu", "uốn mi", "triệt lông", "mở muộn", "21h")
        ),
        SalonKnowledgeDoc(
            id = "salon_09",
            name = "Thì Là SPA - Gội Đầu Dưỡng Sinh, Massage Thư Giãn",
            category = "Spa",
            address = "57 Nguyễn Thị Minh Khai, Hải Châu, Đà Nẵng",
            district = "Hải Châu",
            phone = "0773622699",
            openingHours = "9h15 - 20h30 (Thứ 2 - Chủ nhật)",
            openHourInt = 9,
            closeHourInt = 20,
            specialty = "Gội đầu dưỡng sinh thảo dược nấu từ bồ kết cô đặc, bấm huyệt cổ vai gáy giải tỏa căng thẳng, ngâm chân thảo mộc",
            highlightService = "Gội Đầu Dưỡng Sinh Thảo Dược & Massage Cổ Vai Gáy",
            priceRange = "150.000đ - 380.000đ",
            rating = 5.0,
            reviewCount = 260,
            requireDeposit = true,
            depositInfo = "Có cọc (50.000đ giữ chỗ 100%)",
            facebook = "https://www.facebook.com/thilaspa.danang",
            searchKeywords = listOf("spa", "gội đầu", "dưỡng sinh", "thì là", "thi la", "massage", "cổ vai gáy", "hải châu", "minh khai", "thư giãn", "thảo dược")
        ),
        SalonKnowledgeDoc(
            id = "salon_10",
            name = "Tun Beauty Salon & Spa",
            category = "Spa",
            address = "238 Trần Hưng Đạo, Sơn Trà, Đà Nẵng",
            district = "Sơn Trà",
            phone = "083.383.5555",
            openingHours = "10h - 21h (Thứ 2 - Chủ nhật)",
            openHourInt = 10,
            closeHourInt = 21,
            specialty = "Tổ hợp cắt tạo kiểu tóc thời trang, chăm sóc spa phục hồi da, massage thư giãn view sông Hàn lung linh",
            highlightService = "Cắt Gội Tạo Kiểu & Massage Thư Giãn",
            priceRange = "180.000đ - 650.000đ",
            rating = 4.8,
            reviewCount = 145,
            requireDeposit = false,
            depositInfo = "Miễn cọc 0đ (Đặt giữ chỗ miễn phí)",
            facebook = "https://www.facebook.com/TunBeautySalonSpa",
            searchKeywords = listOf("tun beauty", "sơn trà", "trần hưng đạo", "sông hàn", "tóc", "spa", "massage", "mở muộn", "21h")
        ),
        SalonKnowledgeDoc(
            id = "salon_11",
            name = "𝟏𝐦𝟔𝟓.𝐧𝐚𝐢𝐥",
            category = "Nail",
            address = "48 Lê Đình Dương, Hải Châu, Đà Nẵng",
            district = "Hải Châu",
            phone = "0935043679",
            openingHours = "9h - 20h (Thứ 2 - Chủ nhật)",
            openHourInt = 9,
            closeHourInt = 20,
            specialty = "Nail box thiết kế theo số đo móng riêng, đính đá sang chảnh, vẽ hoạt hình nghệ thuật, sơn gel hữu cơ bền đẹp",
            highlightService = "Nail Box Thiết Kế Theo Yêu Cầu & Đính Đá VIP",
            priceRange = "150.000đ - 400.000đ",
            rating = 4.9,
            reviewCount = 175,
            requireDeposit = false,
            depositInfo = "Miễn cọc 0đ (Đặt giữ chỗ miễn phí)",
            facebook = "https://www.facebook.com/profile.php?id=100083393916454",
            instagram = "https://www.instagram.com/1m65.nail",
            searchKeywords = listOf("nail", "1m65", "lê đình dương", "hải châu", "nail box", "thiết kế", "đính đá", "vẽ móng", "gel")
        )
    )

    /**
     * Tìm salon theo ID
     */
    fun getById(id: String): SalonKnowledgeDoc? {
        return salons.firstOrNull { it.id == id }
    }

    /**
     * Tìm salon theo tên tương đối
     */
    fun findByName(name: String): SalonKnowledgeDoc? {
        val lower = name.lowercase().trim()
        return salons.firstOrNull { 
            it.name.lowercase().contains(lower) || lower.contains(it.name.lowercase()) 
        }
    }
}

