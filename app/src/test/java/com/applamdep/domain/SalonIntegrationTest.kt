package com.applamdep.domain

import com.applamdep.customer.data.repository.FakeBookingRepository
import com.applamdep.customer.data.repository.FakeReviewRepository
import com.applamdep.customer.data.repository.FakeShopRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Kiểm thử toàn diện tính nhất quán & không có xung đột dữ liệu:
 * 1. 11 Salon từ Firebase RTDB tồn tại đầy đủ, thông tin địa chỉ & số điện thoại Đà Nẵng chuẩn xác.
 * 2. Cơ chế Alias tương thích ngược (shop_lamour <-> salon_01, shop_anyen <-> salon_09) hoạt động trơn tru.
 * 3. Dịch vụ, slots, reviews và booking của từng salon không bị xung đột chéo.
 */
class SalonIntegrationTest {

    private lateinit var shopRepository: FakeShopRepository
    private lateinit var bookingRepository: FakeBookingRepository
    private lateinit var reviewRepository: FakeReviewRepository

    @Before
    fun setUp() {
        shopRepository = FakeShopRepository()
        bookingRepository = FakeBookingRepository()
        reviewRepository = FakeReviewRepository()
    }

    @Test
    fun testAllElevenFirebaseSalonsExist() = runBlocking {
        val shops = shopRepository.getApprovedShops().first()

        // 11 salons from salon_01 to salon_11
        val salonIds = (1..11).map { String.format("salon_%02d", it) }

        for (id in salonIds) {
            val found = shops.find { it.id == id }
            assertNotNull("Salon $id must exist in repository", found)
            assertTrue("Salon $id name must not be blank", found!!.name.isNotBlank())
            assertTrue("Salon $id address must not be blank", found.address.isNotBlank())
            assertTrue("Salon $id must be in Da Nang area", found.lat > 15.0 && found.lng > 107.0)
        }
    }

    @Test
    fun testShopDetailAliasResolution() = runBlocking {
        // Querying salon_01 directly
        val shop01 = shopRepository.getShopById("salon_01").getOrThrow()
        assertEquals("salon_01", shop01.id)
        assertEquals("1994 Nailsroom", shop01.name)
        assertEquals("0762792671", shop01.phone)

        // Querying via legacy alias shop_lamour
        val aliasShop = shopRepository.getShopById("shop_lamour").getOrThrow()
        assertEquals("salon_01", aliasShop.id)
        assertEquals("1994 Nailsroom", aliasShop.name)

        // Querying salon_09 vs shop_anyen
        val shop09 = shopRepository.getShopById("salon_09").getOrThrow()
        assertEquals("salon_09", shop09.id)
        assertTrue(shop09.name.contains("Thì Là SPA", ignoreCase = true))

        val alias09 = shopRepository.getShopById("shop_anyen").getOrThrow()
        assertEquals("salon_09", alias09.id)
    }

    @Test
    fun testShopServicesNoConflicts() = runBlocking {
        val services01 = shopRepository.getShopServices("salon_01").first()
        assertFalse("salon_01 must have services", services01.isEmpty())

        val services02 = shopRepository.getShopServices("salon_02").first()
        assertFalse("salon_02 (BONNIE NAIL) must have services", services02.isEmpty())

        val services09 = shopRepository.getShopServices("salon_09").first()
        assertFalse("salon_09 (Thì Là SPA) must have services", services09.isEmpty())

        // Services of salon_01 and salon_02 must be separate
        val ids01 = services01.map { it.id }.toSet()
        val ids02 = services02.map { it.id }.toSet()
        val intersection = ids01.intersect(ids02)
        assertTrue("Services between salon_01 and salon_02 must not conflict", intersection.isEmpty())
    }

    @Test
    fun testShopBookingsAliasSupport() = runBlocking {
        val bookings01 = bookingRepository.getShopBookings("salon_01").first()
        assertFalse("salon_01 must have active bookings", bookings01.isEmpty())

        // Legacy query for shop_lamour must retrieve the same bookings without dropping
        val bookingsAlias = bookingRepository.getShopBookings("shop_lamour").first()
        assertEquals("Bookings queried by alias must match salon_01 count", bookings01.size, bookingsAlias.size)
    }

    @Test
    fun testReviewsSupportBothSalonIdAndAlias() = runBlocking {
        val reviews01 = reviewRepository.getShopReviews("salon_01").first()
        assertFalse("salon_01 must have reviews", reviews01.isEmpty())

        val reviewsAlias = reviewRepository.getShopReviews("shop_lamour").first()
        assertEquals("Reviews queried by alias must match salon_01 count", reviews01.size, reviewsAlias.size)
    }

    @Test
    fun testSocialLinksPresence() = runBlocking {
        val shop01 = shopRepository.getShopById("salon_01").getOrThrow()
        assertNotNull("salon_01 Facebook must be present", shop01.facebook)
        assertNotNull("salon_01 Instagram must be present", shop01.instagram)

        val shop02 = shopRepository.getShopById("salon_02").getOrThrow()
        assertNotNull("salon_02 TikTok must be present", shop02.tiktok)

        val shop07 = shopRepository.getShopById("salon_07").getOrThrow()
        assertNotNull("salon_07 Website must be present", shop07.website)
    }

    @Test
    fun testAllElevenSalonsMatchExcelData() = runBlocking {
        val excelSalons = com.applamdep.customer.data.local.ShopLocalStorage.getExcelSalons()
        assertEquals(11, excelSalons.size)

        // 1. 1994 Nailsroom
        val s1 = excelSalons.find { it.id == "salon_01" }!!
        assertEquals("1994 Nailsroom", s1.name)
        assertTrue(s1.address.contains("Trần Phú", ignoreCase = true))
        assertEquals("0762792671", s1.phone)
        assertEquals("9h - 20h", s1.openingHours)

        // 2. BONNIE NAIL
        val s2 = excelSalons.find { it.id == "salon_02" }!!
        assertEquals("BONNIE NAIL", s2.name)
        assertTrue(s2.address.contains("Dương Đình Nghệ", ignoreCase = true))
        assertEquals("0912154434", s2.phone)

        // 3. GÀ SPA
        val s3 = excelSalons.find { it.id == "salon_03" }!!
        assertEquals("GÀ SPA", s3.name)

        // 4. HIEN HAIR STUDIO
        val s4 = excelSalons.find { it.id == "salon_04" }!!
        assertEquals("HIEN HAIR STUDIO", s4.name)
        assertTrue(s4.address.contains("Tố Hữu", ignoreCase = true))
        assertEquals("0332 678 156", s4.phone)

        // 5. KỲ DUYÊN TRỊ MỤN
        val s5 = excelSalons.find { it.id == "salon_05" }!!
        assertEquals("KỲ DUYÊN TRỊ MỤN", s5.name)
        assertTrue(s5.address.contains("Nguyễn Phước Nguyên", ignoreCase = true))

        // 6. MNH THUY MAKEUP
        val s6 = excelSalons.find { it.id == "salon_06" }!!
        assertEquals("MNH THUY MAKEUP", s6.name)
        assertTrue(s6.address.contains("Đinh Văn Chấp", ignoreCase = true))

        // 7. Nails 1996
        val s7 = excelSalons.find { it.id == "salon_07" }!!
        assertEquals("Nails 1996", s7.name)
        assertTrue(s7.address.contains("Dương Tụ Quán", ignoreCase = true))
        assertNotNull(s7.website)

        // 8. SI MÊ BEAUTY
        val s8 = excelSalons.find { it.id == "salon_08" }!!
        assertEquals("SI MÊ BEAUTY", s8.name)
        assertTrue(s8.address.contains("Nguyễn Đôn Tiết", ignoreCase = true))

        // 9. Thì Là SPA
        val s9 = excelSalons.find { it.id == "salon_09" }!!
        assertTrue(s9.name.contains("Thì Là SPA", ignoreCase = true))
        assertTrue(s9.address.contains("Nguyễn Thị Minh Khai", ignoreCase = true))

        // 10. Tun Beauty Salon & Spa
        val s10 = excelSalons.find { it.id == "salon_10" }!!
        assertEquals("Tun Beauty Salon & Spa", s10.name)
        assertTrue(s10.address.contains("Trần Hưng Đạo", ignoreCase = true))

        // 11. 1m65.nail
        val s11 = excelSalons.find { it.id == "salon_11" }!!
        assertEquals("1m65.nail", s11.name)
        assertTrue(s11.address.contains("Lê Đình Dương", ignoreCase = true))
    }

    @Test
    fun testSaveShopInRepository() = runBlocking {
        val currentS1 = shopRepository.getShopById("salon_01").getOrThrow()
        val updatedS1 = currentS1.copy(description = "Mô tả mới đã được cập nhật vào bộ nhớ máy")
        val result = shopRepository.saveShop(updatedS1)
        assertTrue(result.isSuccess)

        val retrieved = shopRepository.getShopById("salon_01").getOrThrow()
        assertEquals("Mô tả mới đã được cập nhật vào bộ nhớ máy", retrieved.description)
    }
}