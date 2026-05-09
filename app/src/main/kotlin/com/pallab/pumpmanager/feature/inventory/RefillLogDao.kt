package com.pallab.pumpmanager.feature.inventory

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RefillLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefillLog(log: RefillLogEntity)

    @Query("SELECT * FROM refill_log ORDER BY timestamp DESC")
    fun getAllRefillLogs(): kotlinx.coroutines.flow.Flow<List<RefillLogEntity>>
}
