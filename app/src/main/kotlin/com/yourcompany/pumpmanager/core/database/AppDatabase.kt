package com.yourcompany.pumpmanager.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yourcompany.pumpmanager.feature.auth.UserDao
import com.yourcompany.pumpmanager.feature.auth.UserEntity
import com.yourcompany.pumpmanager.feature.inventory.FuelTypeEntity
import com.yourcompany.pumpmanager.feature.inventory.TankEntity
import com.yourcompany.pumpmanager.feature.shift.ShiftEntity
import com.yourcompany.pumpmanager.feature.shift.ShiftDao
import com.yourcompany.pumpmanager.feature.sales.SaleEntity
import com.yourcompany.pumpmanager.feature.sales.SaleDao
import com.yourcompany.pumpmanager.feature.inventory.TankDao

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_shift_id` ON `sales` (`shift_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_timestamp` ON `sales` (`timestamp`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_shifts_status` ON `shifts` (`status`)")
    }
}

@Database(
    entities = [
        UserEntity::class,
        FuelTypeEntity::class,
        TankEntity::class,
        ShiftEntity::class,
        SaleEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun saleDao(): SaleDao
    abstract fun shiftDao(): ShiftDao
    abstract fun tankDao(): TankDao
}
