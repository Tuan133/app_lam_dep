package com.applamdep.merchant.core.di

import com.applamdep.merchant.core.network.MerchantApiService
import com.applamdep.merchant.core.network.MerchantRetrofitClient
import com.applamdep.merchant.data.repository.BookingRepositoryImpl
import com.applamdep.merchant.data.repository.PlanRepositoryImpl
import com.applamdep.merchant.data.repository.ReviewRepositoryImpl
import com.applamdep.merchant.data.repository.ScheduleRepositoryImpl
import com.applamdep.merchant.data.repository.ServiceRepositoryImpl
import com.applamdep.merchant.data.repository.ShopRepositoryImpl
import com.applamdep.merchant.data.repository.SlotRepositoryImpl
import com.applamdep.merchant.data.repository.StatsRepositoryImpl
import com.applamdep.merchant.data.repository.TransactionRepositoryImpl
import com.applamdep.merchant.domain.repository.BookingRepository
import com.applamdep.merchant.domain.repository.PlanRepository
import com.applamdep.merchant.domain.repository.ReviewRepository
import com.applamdep.merchant.domain.repository.ScheduleRepository
import com.applamdep.merchant.domain.repository.ServiceRepository
import com.applamdep.merchant.domain.repository.ShopRepository
import com.applamdep.merchant.domain.repository.SlotRepository
import com.applamdep.merchant.domain.repository.StatsRepository
import com.applamdep.merchant.domain.repository.TransactionRepository

/**
 * 4. TÍNH CHẤT OOP: TRỪU TƯỢNG (ABSTRACTION)
 * MerchantContainer quản lý cấp phát Dependency Injection độc lập cho Merchant Portal.
 */
interface MerchantContainer {
    val shopRepository: ShopRepository
    val serviceRepository: ServiceRepository
    val scheduleRepository: ScheduleRepository
    val slotRepository: SlotRepository
    val bookingRepository: BookingRepository
    val reviewRepository: ReviewRepository
    val transactionRepository: TransactionRepository
    val planRepository: PlanRepository
    val statsRepository: StatsRepository
    val apiService: MerchantApiService
}

class DefaultMerchantContainer : MerchantContainer {
    override val shopRepository: ShopRepository by lazy { ShopRepositoryImpl() }
    override val serviceRepository: ServiceRepository by lazy { ServiceRepositoryImpl() }
    override val scheduleRepository: ScheduleRepository by lazy { ScheduleRepositoryImpl() }
    override val slotRepository: SlotRepository by lazy { SlotRepositoryImpl() }
    override val bookingRepository: BookingRepository by lazy { BookingRepositoryImpl() }
    override val reviewRepository: ReviewRepository by lazy { ReviewRepositoryImpl() }
    override val transactionRepository: TransactionRepository by lazy { TransactionRepositoryImpl() }
    override val planRepository: PlanRepository by lazy { PlanRepositoryImpl() }
    override val statsRepository: StatsRepository by lazy {
        StatsRepositoryImpl(bookingRepository, reviewRepository)
    }
    override val apiService: MerchantApiService by lazy { MerchantRetrofitClient.apiService }
}
