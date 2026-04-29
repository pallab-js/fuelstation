package com.yourcompany.pumpmanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.yourcompany.pumpmanager.feature.auth.UserEntity
import com.yourcompany.pumpmanager.feature.inventory.FuelTypeEntity
import com.yourcompany.pumpmanager.feature.inventory.TankEntity
import com.yourcompany.pumpmanager.feature.shift.ShiftEntity
import com.yourcompany.pumpmanager.feature.shift.ShiftDao
import com.yourcompany.pumpmanager.feature.sales.SaleEntity
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import com.yourcompany.pumpmanager.feature.inventory.TankDao

@Database(
    entities = [
        UserEntity::class,
        FuelTypeEntity::class,
        TankEntity::class,
        ShiftEntity::class,
        SaleEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun saleDao(): SaleDao
    abstract fun shiftDao(): ShiftDao
    abstract fun tankDao(): TankDao
}
