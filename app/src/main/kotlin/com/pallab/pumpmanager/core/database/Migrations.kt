package com.pallab.pumpmanager.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_shift_id` ON `sales` (`shift_id`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_sales_timestamp` ON `sales` (`timestamp`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_shifts_status` ON `shifts` (`status`)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS `refill_log` (
                    `id` TEXT NOT NULL,
                    `tankId` TEXT NOT NULL,
                    `fuelTypeId` TEXT NOT NULL,
                    `litersAdded` REAL NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
            """)
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `sales` ADD COLUMN `is_voided` INTEGER NOT NULL DEFAULT 0")
        }
    }
}
