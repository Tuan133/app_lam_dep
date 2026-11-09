package com.applamdep.customer.domain.repository

import com.applamdep.customer.domain.model.ServiceItem
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.domain.model.ShopSlot
import kotlinx.coroutines.flow.Flow

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Interface ShopRepository che giấu hoàn toàn chi tiết nguồn dữ liệu (Firestore, REST hay Mock).
 * ViewModel chỉ phụ thuộc vào interface này.
 */
interface ShopRepository {
    /**
     * Lấy danh sách shop được duyệt (APPROVED) và còn hạn (planExpiry > now),
     * tự động sắp xếp VIP lên trước.
     */
    fun getApprovedShops(): Flow<List<Shop>>

    /**
     * Tìm kiếm shop theo từ khóa tên, dịch vụ hoặc khu vực
     */
    fun searchShops(query: String): Flow<List<Shop>>

    /**
     * Lấy chi tiết một cửa hàng
     */
    suspend fun getShopById(shopId: String): Result<Shop>

    /**
     * Lấy danh sách dịch vụ của shop
     */
    fun getShopServices(shopId: String): Flow<List<ServiceItem>>

    /**
     * Lấy các khung giờ hẹn của shop trong một ngày cụ thể
     */
    fun getShopSlots(shopId: String, date: String): Flow<List<ShopSlot>>

    /**
     * Cập nhật hoặc lưu cửa hàng trực tiếp vào bộ nhớ máy
     */
    suspend fun saveShop(shop: Shop): Result<Unit> = Result.success(Unit)

    /**
     * Khôi phục danh sách cửa hàng về mặc định ban đầu từ Excel và lưu lại vào bộ nhớ máy
     */
    suspend fun resetToDefault(): Result<Unit> = Result.success(Unit)
}
