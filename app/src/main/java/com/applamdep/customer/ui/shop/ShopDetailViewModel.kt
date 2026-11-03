package com.applamdep.customer.ui.shop

import androidx.lifecycle.viewModelScope
import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.domain.model.Review
import com.applamdep.customer.domain.model.ServiceItem
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.domain.repository.ReviewRepository
import com.applamdep.customer.domain.repository.ShopRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI
 * Trạng thái dữ liệu chi tiết của một Shop
 */
data class ShopDetailData(
    val shop: Shop,
    val services: List<ServiceItem>,
    val reviews: List<Review>
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Kế thừa BaseViewModel)
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Chỉ tương tác qua ShopRepository và ReviewRepository interface)
 */
class ShopDetailViewModel(
    private val shopRepository: ShopRepository,
    private val reviewRepository: ReviewRepository
) : BaseViewModel<ShopDetailData>() {

    fun loadShopDetail(shopId: String) {
        launchWithLoading {
            val shopResult = shopRepository.getShopById(shopId)
            val shop = shopResult.getOrThrow()

            // Kết hợp realtime stream của dịch vụ và đánh giá
            combine(
                shopRepository.getShopServices(shopId),
                reviewRepository.getShopReviews(shopId)
            ) { services, reviews ->
                ShopDetailData(
                    shop = shop,
                    services = services,
                    reviews = reviews
                )
            }.collect { detailData ->
                updateState(UiState.Success(detailData))
            }
        }
    }
}
