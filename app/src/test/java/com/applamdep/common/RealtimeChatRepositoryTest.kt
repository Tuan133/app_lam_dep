package com.applamdep.common

import com.applamdep.customer.data.repository.RealtimeChatRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Kiểm thử logic hội thoại, định tuyến từng khách hàng/salon và đồng bộ trạng thái chưa đọc
 * giữa Tài khoản Khách hàng (CUSTOMER) và Tài khoản Cửa hàng (MERCHANT).
 */
class RealtimeChatRepositoryTest {

    @Before
    fun setUp() {
        RealtimeChatRepository.resetForTesting()
    }

    @After
    fun tearDown() {
        RealtimeChatRepository.resetForTesting()
    }

    @Test
    fun getConversationId_routesEachCustomerAndSalonIndependently() {
        val khanhLinhId = RealtimeChatRepository.getConversationId("Khách hàng: Khánh Linh")
        val nailsroomId = RealtimeChatRepository.getConversationId("1994 Nailsroom")
        val maiPhuongId = RealtimeChatRepository.getConversationId("Mai Phương")
        val thuHaId = RealtimeChatRepository.getConversationId("Thu Hà")
        val thaoMyId = RealtimeChatRepository.getConversationId("Thảo My")
        val bonnieId = RealtimeChatRepository.getConversationId("BONNIE NAIL")
        val thiLaId = RealtimeChatRepository.getConversationId("Thì Là SPA")
        val glamourId = RealtimeChatRepository.getConversationId("The Glamour Hair Salon")
        val customVietId = RealtimeChatRepository.getConversationId("Ngọc Ánh")

        assertEquals("salon_01", khanhLinhId)
        assertEquals(nailsroomId, khanhLinhId)
        assertEquals("cust_mai_phuong", maiPhuongId)
        assertEquals("cust_thu_ha", thuHaId)
        assertEquals("cust_thao_my", thaoMyId)
        assertEquals("salon_02", bonnieId)
        assertEquals("salon_09", thiLaId)
        assertEquals("salon_glamour", glamourId)
        assertEquals("conv_ngoc_anh", customVietId)

        val uniqueIds = setOf(
            khanhLinhId,
            maiPhuongId,
            thuHaId,
            thaoMyId,
            bonnieId,
            thiLaId,
            glamourId,
            customVietId
        )
        assertEquals(8, uniqueIds.size)
        assertNotEquals(maiPhuongId, khanhLinhId)
    }

    @Test
    fun unreadCount_persistsReadStatePerRoleAndUpdatesOnOppositeReply() {
        // Ban đầu salon_01 có 1 tin chưa đọc phía MERCHANT (tin từ Khánh Linh)
        assertEquals(1, RealtimeChatRepository.getUnreadCount("salon_01", "MERCHANT"))
        assertEquals(0, RealtimeChatRepository.getUnreadCount("salon_01", "CUSTOMER"))

        // Khi Merchant mở chat -> đánh dấu đã đọc -> giữ 0 ngay cả khi quay lại danh sách
        RealtimeChatRepository.markAsRead("salon_01", "MERCHANT")
        assertEquals(0, RealtimeChatRepository.getUnreadCount("salon_01", "MERCHANT"))

        // Khi Merchant gửi tin mới cho Khánh Linh -> Khách hàng thấy 1 chưa đọc, Merchant vẫn 0
        RealtimeChatRepository.sendMessage(
            conversationId = "salon_01",
            senderRole = "MERCHANT",
            senderName = "1994 Nailsroom",
            text = "Dạ tiệm đã giữ ghế sẵn cho bạn rồi ạ!",
            autoReply = false
        )
        assertEquals(0, RealtimeChatRepository.getUnreadCount("salon_01", "MERCHANT"))
        assertEquals(1, RealtimeChatRepository.getUnreadCount("salon_01", "CUSTOMER"))
        assertTrue(
            RealtimeChatRepository.getLatestMessage("salon_01")?.text == "Dạ tiệm đã giữ ghế sẵn cho bạn rồi ạ!"
        )

        // Khi Khách hàng mở lại -> đánh dấu đã đọc -> về 0
        RealtimeChatRepository.markAsRead("salon_01", "CUSTOMER")
        assertEquals(0, RealtimeChatRepository.getUnreadCount("salon_01", "CUSTOMER"))
    }

    @Test
    fun sendMessage_toDifferentCustomer_doesNotOverwriteOtherThreads() {
        val linhLatestBefore = RealtimeChatRepository.getLatestMessage("salon_01")?.text

        RealtimeChatRepository.sendMessage(
            conversationId = "cust_mai_phuong",
            senderRole = "MERCHANT",
            senderName = "1994 Nailsroom",
            text = "Dạ khung 16:30 hôm nay còn slot bạn nha!",
            autoReply = false
        )

        assertEquals(
            "Dạ khung 16:30 hôm nay còn slot bạn nha!",
            RealtimeChatRepository.getLatestMessage("cust_mai_phuong")?.text
        )
        assertEquals(linhLatestBefore, RealtimeChatRepository.getLatestMessage("salon_01")?.text)
    }
}

