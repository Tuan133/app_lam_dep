package com.applamdep.merchant.ui.dashboard

import androidx.lifecycle.viewModelScope
import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.ShopRepositoryImpl
import com.applamdep.merchant.data.repository.StatsRepositoryImpl
import com.applamdep.merchant.domain.model.MerchantStats
import com.applamdep.merchant.domain.model.ShopProfile
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.repository.StatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MerchantDashboardData(
    val profile: ShopProfile,
    val stats: MerchantStats
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc ShopRepository, StatsRepository, BookingRepository
 * ViewModel Dashboard tổng quan cổng đối tác (Merchant Requirement #10)
 */
class MerchantDashboardViewModel(
    private val shopRepository: ShopRepository = ShopRepositoryImpl(),
    private val statsRepository: StatsRepository = StatsRepositoryImpl(),
    private val bookingRepository: BookingRepository = BookingRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<MerchantDashboardData>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        shopRepository = ShopRepositoryImpl(),
        statsRepository = StatsRepositoryImpl(),
        bookingRepository = BookingRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    private val _autoCompletedCount = MutableStateFlow(0)
    val autoCompletedCount: StateFlow<Int> = _autoCompletedCount.asStateFlow()

    fun loadDashboard(shopId: String = "shop_01", ownerUid: String = "merchant_user_01") {
        viewModelScope.launch {
            // Tự động kiểm tra và hoàn thành các ca làm đã kết thúc quá 30 phút theo Requirement #7
            val autoResult = bookingRepository.checkAndAutoCompleteBookings(shopId)
            autoResult.onSuccess { count ->
                _autoCompletedCount.value = count
            }

            launchWithLoading {
                shopRepository.getShopProfile(ownerUid).collect { prof ->
                    if (prof != null) {
                        statsRepository.getShopStats(shopId).collect { stats ->
                            updateState(UiState.Success(MerchantDashboardData(prof, stats)))
                        }
                    } else {
                        updateState(UiState.Error("Chưa tìm thấy hồ sơ cửa hàng."))
                    }
                }
            }
        }
    }
}
