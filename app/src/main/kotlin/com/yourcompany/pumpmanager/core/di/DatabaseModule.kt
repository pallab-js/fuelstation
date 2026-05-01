package com.yourcompany.pumpmanager.core.di

import android.content.Context
import androidx.room.Room
import com.yourcompany.pumpmanager.core.database.AppDatabase
import com.yourcompany.pumpmanager.core.database.MIGRATION_1_2
import com.yourcompany.pumpmanager.feature.auth.UserDao
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
        ).addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
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
