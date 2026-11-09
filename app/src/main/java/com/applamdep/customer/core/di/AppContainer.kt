package com.applamdep.customer.core.di

import android.content.Context
import com.applamdep.customer.data.repository.FakeBookingRepository
import com.applamdep.customer.data.repository.FakeReviewRepository
import com.applamdep.customer.data.repository.FakeShopRepository
import com.applamdep.customer.data.repository.FakeWalletRepository
import com.applamdep.customer.domain.policy.CancellationPolicy
import com.applamdep.customer.domain.policy.SlotConflictResolver
import com.applamdep.customer.domain.policy.StandardCancellationPolicy
import com.applamdep.customer.domain.repository.BookingRepository
import com.applamdep.customer.domain.repository.ReviewRepository
import com.applamdep.customer.domain.repository.ShopRepository
import com.applamdep.customer.domain.repository.WalletRepository

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * AppContainer đóng vai trò Service Locator / Dependency Container,
 * cung cấp các Repository Interface cho ViewModel mà không để ViewModel phụ thuộc vào lớp cụ thể.
 * Tích hợp Context để lưu trữ dữ liệu trực tiếp vào bộ nhớ máy (Device Local Storage).
 */
interface AppContainer {
    val shopRepository: ShopRepository
    val bookingRepository: BookingRepository
    val walletRepository: WalletRepository
    val reviewRepository: ReviewRepository
    val cancellationPolicy: CancellationPolicy
    val slotConflictResolver: SlotConflictResolver
    val autoCompletionPolicy: com.applamdep.merchant.domain.policy.AutoCompletionPolicy
    val merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository
}

class DefaultAppContainer(
    private val context: Context? = null
) : AppContainer {
    override val shopRepository: ShopRepository by lazy {
        FakeShopRepository(context = context)
    }

    override val bookingRepository: BookingRepository by lazy {
        FakeBookingRepository()
    }

    override val walletRepository: WalletRepository by lazy {
        FakeWalletRepository()
    }

    override val reviewRepository: ReviewRepository by lazy {
        FakeReviewRepository()
    }

    override val cancellationPolicy: CancellationPolicy by lazy {
        StandardCancellationPolicy()
    }

    override val slotConflictResolver: SlotConflictResolver by lazy {
        SlotConflictResolver()
    }

    override val autoCompletionPolicy: com.applamdep.merchant.domain.policy.AutoCompletionPolicy by lazy {
        com.applamdep.merchant.domain.policy.StandardAutoCompletionPolicy()
    }

    override val merchantRepository: com.applamdep.merchant.domain.repository.MerchantRepository by lazy {
        com.applamdep.merchant.data.repository.FakeMerchantRepository(
            bookingRepository = bookingRepository,
            reviewRepository = reviewRepository,
            autoCompletionPolicy = autoCompletionPolicy
        )
    }
}
