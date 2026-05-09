package com.pallab.pumpmanager.core.di

import com.pallab.pumpmanager.feature.auth.AuthRepository
import com.pallab.pumpmanager.feature.auth.AuthRepositoryImpl
import com.pallab.pumpmanager.feature.inventory.InventoryRepository
import com.pallab.pumpmanager.feature.inventory.InventoryRepositoryImpl
import com.pallab.pumpmanager.feature.sales.SalesRepository
import com.pallab.pumpmanager.feature.sales.SalesRepositoryImpl
import com.pallab.pumpmanager.feature.shift.ShiftRepository
import com.pallab.pumpmanager.feature.shift.ShiftRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindSalesRepository(impl: SalesRepositoryImpl): SalesRepository

    @Binds @Singleton
    abstract fun bindShiftRepository(impl: ShiftRepositoryImpl): ShiftRepository

    @Binds @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
