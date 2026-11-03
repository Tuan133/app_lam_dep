package com.applamdep.customer.ui.shop

import androidx.lifecycle.viewModelScope
import com.applamdep.customer.core.base.BaseViewModel
import com.applamdep.customer.core.base.UiState
import com.applamdep.customer.core.constants.AppConstants
import com.applamdep.customer.domain.model.Shop
import com.applamdep.customer.domain.repository.ShopRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 1. TÍNH CHẤT OOP: ĐÓNG GÓI (ENCAPSULATION)
 * Model chứa các tiêu chí lọc tìm kiếm
 */
data class ShopFilter(
    val minRating: Double = 0.0,
    val onlyVip: Boolean = false,
    val requireDeposit: Boolean? = null // null: tất cả, true: cần cọc, false: không cọc
)

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Kế thừa BaseViewModel)
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Chỉ phụ thuộc ShopRepository interface)
 */
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ShopListViewModel(
    private val shopRepository: ShopRepository
) : BaseViewModel<List<Shop>>() {

    // 1. TÍNH CHẤT OOP: ĐÓNG GÓI StateFlow tìm kiếm và bộ lọc
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(ShopFilter())
    val filterState: StateFlow<ShopFilter> = _filterState.asStateFlow()

    init {
        observeShops()
    }

    /**
     * Lắng nghe và kết hợp Tìm kiếm (Debounce bằng Flow) cùng Bộ lọc
     */
    private fun observeShops() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300).distinctUntilChanged(),
                _filterState
            ) { query, filter ->
                Pair(query, filter)
            }.flatMapLatest { (query, filter) ->
                updateState(UiState.Loading)
                if (query.isBlank()) {
                    shopRepository.getApprovedShops()
                } else {
                    shopRepository.searchShops(query)
                }.map { rawList ->
                    applyBusinessRulesAndFilters(rawList, filter)
                }
            }.catch { throwable ->
                handleError(throwable)
            }.collect { filteredList ->
                updateState(UiState.Success(filteredList))
            }
        }
    }

    /**
     * 1. TÍNH CHẤT OOP: ĐÓNG GÓI LOGIC NGHIỆP VỤ
     * - Quy tắc 1: Chỉ hiển thị shop đã đăng ký trên sàn (status = APPROVED) và gói còn hạn (planExpiry > now)
     * - Quy tắc 2: Shop gói VIP được xếp ưu tiên lên đầu (đọc trường plan)
     * - Áp dụng các tiêu chí lọc: số sao đánh giá, yêu cầu cọc
     */
    private fun applyBusinessRulesAndFilters(
        rawList: List<Shop>,
        filter: ShopFilter
    ): List<Shop> {
        return rawList
            .filter { shop ->
                // Chỉ hiển thị shop APPROVED và còn hạn
                shop.status == AppConstants.SHOP_STATUS_APPROVED && shop.isPlanValid
            }
            .filter { shop ->
                // Lọc theo rating tối thiểu
                shop.rating >= filter.minRating
            }
            .filter { shop ->
                // Lọc chỉ shop VIP nếu được chọn
                if (filter.onlyVip) shop.isVipActive else true
            }
            .filter { shop ->
                // Lọc theo cọc / không cọc
                if (filter.requireDeposit != null) shop.requireDeposit == filter.requireDeposit else true
            }
            .sortedWith(
                // ƯU TIÊN SHOP VIP LÊN ĐẦU
                compareByDescending<Shop> { it.isVipActive }
                    .thenByDescending { it.rating }
            )
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterChanged(newFilter: ShopFilter) {
        _filterState.value = newFilter
    }

    fun resetFilter() {
        _filterState.value = ShopFilter()
    }
}
