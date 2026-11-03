package com.applamdep.merchant.ui.finance

import com.applamdep.merchant.core.base.BaseViewModel
import com.applamdep.merchant.core.base.UiState
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.model.FinancialSummary
import com.applamdep.merchant.domain.repository.TransactionRepository

/**
 * 2. TÍNH CHẤT OOP: KẾ THỪA (Inheritance) - Kế thừa BaseViewModel
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (Abstraction) - Phụ thuộc TransactionRepository
 * ViewModel theo dõi tiền cọc ký quỹ Escrow và thanh khoản tài chính (Merchant Requirement #9)
 */
class MerchantFinanceViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepositoryImpl(),
    private val legacyMerchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository? = null
) : BaseViewModel<FinancialSummary>() {

    // Hỗ trợ secondary constructor cho DI cũ nếu cần
    constructor(merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository) : this(
        transactionRepository = TransactionRepositoryImpl(),
        legacyMerchantRepository = merchantRepository
    )

    fun loadFinance(shopId: String = "shop_01") {
        launchWithLoading {
            transactionRepository.getFinancialSummary(shopId).collect { summary ->
                updateState(UiState.Success(summary))
            }
        }
    }
}
