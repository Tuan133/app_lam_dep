package com.applamdep.merchant.core.di

import com.applamdep.merchant.core.network.MerchantApiService
import com.applamdep.merchant.core.network.MerchantRetrofitClient
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.PlanRepository
import com.applamdep.merchant.domain.repository.ReviewRepository
import com.applamdep.merchant.domain.repository.ScheduleRepository
import com.applamdep.merchant.domain.repository.ServiceRepository
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.repository.SlotRepository
import com.applamdep.merchant.domain.repository.StatsRepository
import com.applamdep.merchant.domain.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * Hilt DI Module cung cấp các Repository Singleton và Retrofit ApiService cho toàn bộ Merchant Portal.
 */
@Module
@InstallIn(SingletonComponent::class)
object MerchantDiModule {

    private val container = DefaultMerchantContainer()

    @Provides
    @Singleton
    fun provideMerchantContainer(): MerchantContainer = container

    @Provides
    @Singleton
    fun provideShopRepository(container: MerchantContainer): ShopRepository = container.shopRepository

    @Provides
    @Singleton
    fun provideServiceRepository(container: MerchantContainer): ServiceRepository = container.serviceRepository

    @Provides
    @Singleton
    fun provideScheduleRepository(container: MerchantContainer): ScheduleRepository = container.scheduleRepository

    @Provides
    @Singleton
    fun provideSlotRepository(container: MerchantContainer): SlotRepository = container.slotRepository

    @Provides
    @Singleton
    fun provideBookingRepository(container: MerchantContainer): BookingRepository = container.bookingRepository

    @Provides
    @Singleton
    fun provideReviewRepository(container: MerchantContainer): ReviewRepository = container.reviewRepository

    @Provides
    @Singleton
    fun provideTransactionRepository(container: MerchantContainer): TransactionRepository = container.transactionRepository

    @Provides
    @Singleton
    fun providePlanRepository(container: MerchantContainer): PlanRepository = container.planRepository

    @Provides
    @Singleton
    fun provideStatsRepository(container: MerchantContainer): StatsRepository = container.statsRepository

    @Provides
    @Singleton
    fun provideMerchantApiService(): MerchantApiService = MerchantRetrofitClient.apiService
}
