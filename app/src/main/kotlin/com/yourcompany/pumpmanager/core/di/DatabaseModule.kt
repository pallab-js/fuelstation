package com.yourcompany.pumpmanager.core.di

import android.content.Context
import androidx.room.Room
import com.yourcompany.pumpmanager.core.database.AppDatabase
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import com.yourcompany.pumpmanager.feature.shift.ShiftDao
import com.yourcompany.pumpmanager.feature.inventory.TankDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pump_manager_db"
        ).build()
    }

    @Provides
    fun provideSaleDao(database: AppDatabase): SaleDao {
        return database.saleDao()
    }

    @Provides
    fun provideShiftDao(database: AppDatabase): ShiftDao {
        return database.shiftDao()
    }

    @Provides
    fun provideTankDao(database: AppDatabase): TankDao {
        return database.tankDao()
    }
}
