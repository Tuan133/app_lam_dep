package com.applamdep.customer.core.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.applamdep.customer.CustomerApplication
import com.applamdep.customer.ui.booking.BookingHistoryViewModel
import com.applamdep.customer.ui.booking.BookingViewModel
import com.applamdep.customer.ui.shop.ShopDetailViewModel
import com.applamdep.customer.ui.shop.ShopListViewModel

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * ViewModelFactory tiêm các Repository Interface từ AppContainer vào ViewModel,
 * ViewModel hoàn toàn độc lập và không biết nguồn dữ liệu thật hay giả.
 */
object ViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = customerApplication()
            ShopListViewModel(
                shopRepository = app.container.shopRepository
            )
        }
        initializer {
            val app = customerApplication()
            ShopDetailViewModel(
                shopRepository = app.container.shopRepository,
                reviewRepository = app.container.reviewRepository
            )
        }
        initializer {
            val app = customerApplication()
            BookingViewModel(
                shopRepository = app.container.shopRepository,
                bookingRepository = app.container.bookingRepository,
                walletRepository = app.container.walletRepository,
                slotConflictResolver = app.container.slotConflictResolver
            )
        }
        initializer {
            val app = customerApplication()
            BookingHistoryViewModel(
                bookingRepository = app.container.bookingRepository,
                walletRepository = app.container.walletRepository,
                cancellationPolicy = app.container.cancellationPolicy
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.customer.ui.wallet.WalletViewModel(
                walletRepository = app.container.walletRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.customer.ui.review.WriteReviewViewModel(
                reviewRepository = app.container.reviewRepository,
                bookingRepository = app.container.bookingRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.customer.ui.review.CustomerReviewsViewModel(
                reviewRepository = app.container.reviewRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.dashboard.MerchantDashboardViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.booking.MerchantBookingViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.service.MerchantServiceViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.schedule.MerchantScheduleViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.review.MerchantReviewViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.finance.MerchantFinanceViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.profile.MerchantProfileViewModel(
                merchantRepository = app.container.merchantRepository
            )
        }
        initializer {
            val app = customerApplication()
            com.applamdep.merchant.ui.register.MerchantRegisterViewModel(
                shopRepository = com.applamdep.merchant.data.repository.ShopRepositoryImpl(),
                planRepository = com.applamdep.merchant.data.repository.PlanRepositoryImpl()
            )
        }
    }
}

fun CreationExtras.customerApplication(): CustomerApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as CustomerApplication)
