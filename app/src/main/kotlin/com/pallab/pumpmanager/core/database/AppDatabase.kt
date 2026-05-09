package com.pallab.pumpmanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pallab.pumpmanager.feature.auth.UserDao
import com.pallab.pumpmanager.feature.auth.UserEntity
import com.pallab.pumpmanager.feature.inventory.FuelTypeEntity
import com.pallab.pumpmanager.feature.inventory.TankEntity
import com.pallab.pumpmanager.feature.shift.ShiftEntity
import com.pallab.pumpmanager.feature.shift.ShiftDao
import com.pallab.pumpmanager.feature.sales.SaleEntity
import com.pallab.pumpmanager.feature.sales.SaleDao
import com.pallab.pumpmanager.feature.inventory.FuelTypeDao
import com.pallab.pumpmanager.feature.inventory.RefillLogDao
import com.pallab.pumpmanager.feature.inventory.RefillLogEntity
import com.pallab.pumpmanager.feature.inventory.TankDao

@Database(
    entities = [
        UserEntity::class,
        FuelTypeEntity::class,
        TankEntity::class,
        ShiftEntity::class,
        SaleEntity::class,
        RefillLogEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun saleDao(): SaleDao
    abstract fun shiftDao(): ShiftDao
    abstract fun tankDao(): TankDao
    abstract fun fuelTypeDao(): FuelTypeDao
    abstract fun refillLogDao(): RefillLogDao
}
