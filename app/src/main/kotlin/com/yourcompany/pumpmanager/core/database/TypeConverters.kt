package com.yourcompany.pumpmanager.core.database

import androidx.room.TypeConverter
import java.util.Date

class AppTypeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
