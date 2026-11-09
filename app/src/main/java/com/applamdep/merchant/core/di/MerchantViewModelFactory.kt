package com.applamdep.merchant.core.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.applamdep.merchant.ui.profile.MerchantProfileViewModel
import com.applamdep.merchant.ui.register.MerchantRegisterViewModel

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * ViewModelProvider.Factory khởi tạo các ViewModel độc lập cho Merchant Portal
 * mà không để UI phụ thuộc trực tiếp vào triển khai cơ sở dữ liệu.
 */
class MerchantViewModelFactory(
    private val container: MerchantContainer = DefaultMerchantContainer()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MerchantRegisterViewModel::class.java) -> {
                MerchantRegisterViewModel(
                    shopRepository = container.shopRepository,
                    planRepository = container.planRepository
                ) as T
            }
            modelClass.isAssignableFrom(MerchantProfileViewModel::class.java) -> {
                MerchantProfileViewModel(
                    shopRepository = container.shopRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.applamdep.merchant.ui.service.MerchantServiceViewModel::class.java) -> {
                com.applamdep.merchant.ui.service.MerchantServiceViewModel(
                    serviceRepository = container.serviceRepository,
                    bookingRepository = container.bookingRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.applamdep.merchant.ui.schedule.MerchantScheduleViewModel::class.java) -> {
                com.applamdep.merchant.ui.schedule.MerchantScheduleViewModel(
                    scheduleRepository = container.scheduleRepository,
                    slotRepository = container.slotRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.applamdep.merchant.ui.booking.MerchantBookingViewModel::class.java) -> {
                com.applamdep.merchant.ui.booking.MerchantBookingViewModel(
                    bookingRepository = container.bookingRepository,
                    transactionRepository = container.transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.applamdep.merchant.ui.review.MerchantReviewViewModel::class.java) -> {
                com.applamdep.merchant.ui.review.MerchantReviewViewModel(
                    reviewRepository = container.reviewRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.applamdep.merchant.ui.finance.MerchantFinanceViewModel::class.java) -> {
                com.applamdep.merchant.ui.finance.MerchantFinanceViewModel(
                    transactionRepository = container.transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.applamdep.merchant.ui.dashboard.MerchantDashboardViewModel::class.java) -> {
                com.applamdep.merchant.ui.dashboard.MerchantDashboardViewModel(
                    shopRepository = container.shopRepository,
                    statsRepository = container.statsRepository,
                    bookingRepository = container.bookingRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        val DefaultFactory = MerchantViewModelFactory(DefaultMerchantContainer())
    }
}
